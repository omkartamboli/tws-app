package com.trading.app.tradingapp.service.impl;

import com.ib.client.*;
import com.ib.contracts.FutContract;
import com.trading.app.tradingapp.TradingAppApplication;
import com.trading.app.tradingapp.dto.OptionType;
import com.trading.app.tradingapp.dto.response.MarketDataDto;
import com.trading.app.tradingapp.persistance.entity.ContractEntity;
import com.trading.app.tradingapp.persistance.entity.OrderEntity;
import com.trading.app.tradingapp.persistance.entity.TriggerOrderEntity;
import com.trading.app.tradingapp.persistance.repository.ContractRepository;
import com.trading.app.tradingapp.persistance.repository.OrderRepository;
import com.trading.app.tradingapp.persistance.repository.TriggerOrderRepository;
import com.trading.app.tradingapp.service.BaseService;
import com.trading.app.tradingapp.service.ContractService;
import com.trading.app.tradingapp.service.OrderService;
import com.trading.app.tradingapp.service.SystemConfigService;
import com.trading.app.tradingapp.util.ProcessTriggerOrderThread;
import com.trading.app.tradingapp.util.RequestMarketDataThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.hibernate.Session;

import javax.annotation.Resource;
import javax.persistence.*;
import javax.transaction.Transactional;
import java.util.*;
import java.util.concurrent.TimeoutException;

@Service
public class BaseServiceImpl implements BaseService, EWrapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseServiceImpl.class);

    private final EClientSocket eClientSocket;

    private final EReaderSignal eReaderSignal;

    private EReader eReader;

    private static final String CURRENCY = "USD";

    private static final String SECURITY_TYPE = "STK";

    public static final String OPTIONS_TYPE = "OPT";

    public static final String FUTURE_OPTIONS_TYPE = "FOP";

    public static final String STRADDLE_TYPE = "BAG";

    private static final String EXCHANGE = "SMART";

    private static final String ISLAND_EXCHANGE = "ISLAND";

    private static final String SMART_EXCHANGE = "SMART";

    private static final String MULTIPLIER_100 = "100";

    private static final String DEFAULT_API_HOST = "127.0.0.1";

    private static final int DEFAULT_API_PORT = 9902;

    private static final int DEFAULT_API_CLIENT_ID = 1;

    private static final int LTP_FIELD = 4;

    private static final int ASK_FIELD = 2;

    private static final int BID_FIELD = 1;

    private static final String GENERIC_TICKS = "104, 106"; // Hist Vol, Imp Vol

    private static final int MILLIS_IN_DAY = 24 * 60 * 60 * 1000;

    private int nextTickerId = 101;

    private int nextTWSOrderId;

    private static final String FILLED_STATUS = "Filled";

    private static final String SUBMITTED_STATUS = "Submitted";

    private static final String CANCELLED_STATUS = "Cancelled";

    private Map<Integer, ContractDetails> contractDetailsMap = new HashMap<>();

    protected static final int MAX_WAIT_COUNT = 50; // 0.5 sec

    protected static final int WAIT_TIME = 10; // 10 millis

    protected static final int HALF_MINUTE_OFFSET = 30000; // 1 sec

    private static final String TRIGGER_ORDER_QUERY_STRING = "select to                                                  " +
            "from TriggerOrderEntity as to                                                                                " +
            "where to.symbol = :symbol                                                                                    " +
            "and to.orderId = -1                                                                                          " +
            "and    (                                                                                                     " +
            "           (to.orderTriggerPrice < :ltp and to.orderType = 'STP LMT' and to.orderAction = 'BUY')         or  " +
            "           (to.orderTriggerPrice > :ltp and to.orderType = 'STP LMT' and to.orderAction = 'SELL')        or  " +
            "           (to.orderTriggerPrice < :ltp and to.orderType = 'TRAIL LIMIT' and to.orderAction = 'SELL')    or  " +
            "           (to.orderTriggerPrice > :ltp and to.orderType = 'TRAIL LIMIT' and to.orderAction = 'BUY')         " +
            "       )                                                                                                     ";

    @Value("${tws.api.port}")
    private String apiPort;

    @Value("${tws.api.host}")
    private String apiHost;

    @Value("${tws.api.clientid}")
    private String apiClientId;

    @Resource
    private ContractRepository contractRepository;

    @Resource
    private SystemConfigService systemConfigService;

    @Resource
    private OrderRepository orderRepository;

    @Resource
    private EntityManager entityManager;

    @Resource
    private ContractService contractService;

    @Resource
    private OrderService orderService;

    @Resource
    private TriggerOrderRepository triggerOrderRepository;

    @PersistenceUnit
    private EntityManagerFactory entityManagerFactory;

    private final HashSet<Long> previouslyPlacedTriggeredOrdersInTWS = new HashSet<>();

    private boolean canCreateTWSOrderForThisTriggerOrder(Long triggerOrderPk) {
        if (previouslyPlacedTriggeredOrdersInTWS.contains(triggerOrderPk)) {
            return false;
        } else {
            previouslyPlacedTriggeredOrdersInTWS.add(triggerOrderPk);
            return true;
        }
    }

    public BaseServiceImpl() {
        eReaderSignal = new EJavaSignal();
        eClientSocket = new EClientSocket(this, eReaderSignal);
    }

    private void logLine() {
        LOGGER.info("____________________________________________________________________________________________________\n");
    }

    @Override
    public void setupNewConnection() throws Exception {
        LOGGER.info("Creating TWS connection .....");
        logLine();

        LOGGER.info("Params:");
        LOGGER.info("[tws.api.host]=[{}]", getApiHost());
        LOGGER.info("[tws.api.port]=[{}]", getApiPort());
        LOGGER.info("[tws.api.clientId]=[{}]", getApiClientId());
        logLine();

        eClientSocket.eConnect(DEFAULT_API_HOST, DEFAULT_API_PORT, DEFAULT_API_CLIENT_ID);

        try {
            Thread.sleep(10);
            int waitingCounter = 0;
            while (!eClientSocket.isConnected() && waitingCounter < 100) {
                Thread.sleep(10);
                ++waitingCounter;
            }

            if (!eClientSocket.isConnected()) {
                LOGGER.error("Could not establish TWS connection within 1 second. Timed out");
                throw new TimeoutException("Could not establish TWS connection within 1 second. Timed out");
            } else {
                eReader = new EReader(eClientSocket, eReaderSignal);
                eReader.start();
                new Thread(() -> processMessages(eClientSocket)).start();

                // start market data feed
                getContractService().startMarketDataFeed(eClientSocket);
            }
        } catch (Exception ex) {
            LOGGER.error("Could not establish TWS connection .....", ex);
            throw ex;
        }
        LOGGER.info("TWS connection established .....");
    }

    @Override
    public EClientSocket getConnection() throws Exception {
        if (eClientSocket.isConnected()) {
            return eClientSocket;
        } else {
            setupNewConnection();
            return eClientSocket;
        }
    }

    private void processMessages(EClientSocket eClientSocket) {
        while (eClientSocket.isConnected()) {
            try {
                eReader.processMsgs();
            } catch (Exception e) {
                error(e);
            }
        }
    }

    @Override
    public synchronized int getNextOrderId() throws Exception {
        //LOGGER.info("getting next order ID .....");
        EClientSocket eClientSocket = getConnection();

        setNextTWSOrderId(-1);
        eClientSocket.reqIds(-1);
        while (getNextTWSOrderId() < 0) {
            Thread.sleep(10);
        }
        return getNextTWSOrderId();
    }


    @Override
    public RequestMarketDataThread startMarketDataStreamForContract(Contract contract) throws Exception {
        RequestMarketDataThread thread = new RequestMarketDataThread(contract, getContractRepository(), getConnection());
        thread.start();
        return thread;
    }

    @Transactional
    @Override
    public MarketDataDto getMarketDataForContract(Contract contract, boolean acceptStaleData) throws Exception {

        long currentTime = new java.sql.Timestamp(new Date().getTime()).getTime();

        RequestMarketDataThread thread = startMarketDataStreamForContract(contract);

        // Wait for market data request call to finish
        thread.join();

        // Wait for market data to be updated in database
        Thread.sleep(500);


        MarketDataDto marketDataDto = new MarketDataDto();

        Optional<ContractEntity> contractEntityOptional = getContractRepository().findById(contract.symbol());


        if (contractEntityOptional.isEmpty()) {
            LOGGER.error("Contract Entity is not present in Database, Use Create contract entity API first.");
            throw new IllegalArgumentException("Contract Entity is not present in Database, Use Create contract entity API first.");
        } else {

            ContractEntity contractEntity = contractEntityOptional.get();

            if (contractEntity.getTickerAskBidLtpValuesUpdateTimestamp() != null && contractEntity.getTickerAskBidLtpValuesUpdateTimestamp().getTime() > currentTime) {
                if (contractEntity.getLtp() > 0 || contractEntity.getLastAsk() > 0 || contractEntity.getLastBid() > 0) {
                    marketDataDto.setLtp(contractEntity.getLtp());
                    marketDataDto.setLastAsk(contractEntity.getLastAsk());
                    marketDataDto.setLastBid(contractEntity.getLastBid());
                    return marketDataDto;
                }
            }

            if (acceptStaleData) {
                if (contractEntity.getLtp() > 0 || contractEntity.getLastAsk() > 0 || contractEntity.getLastBid() > 0) {
                    marketDataDto.setLtp(contractEntity.getLtp());
                    marketDataDto.setLastAsk(contractEntity.getLastAsk());
                    marketDataDto.setLastBid(contractEntity.getLastBid());
                    return marketDataDto;
                }
            }

            LOGGER.error("Could not get Market Data for given Ticker. Timed out");
            throw new TimeoutException("Could not get Market Data for given Ticker. Timed out");
        }
    }

    //@Transactional
    public void updateContractEntity(ContractEntity contractEntity) {
        getContractRepository().saveAndFlush(contractEntity);
    }

    @Override
    public Contract createContract(String ticker) {

        Optional<ContractEntity> contractEntity = getContractRepository().findById(ticker);
        if (contractEntity.isPresent() && Types.SecType.FUT.name().equalsIgnoreCase(contractEntity.get().getSecType())) {
            return createFuturesContract(ticker, contractEntity.get().getNextFutDate());
        } else {
            Contract contract = new Contract();
            contract.symbol(ticker);
            contract.currency(CURRENCY);
            // ABNB does not work with SMART exchange
            contract.exchange("ABNB".equalsIgnoreCase(ticker) || "META".equalsIgnoreCase(ticker) ? ISLAND_EXCHANGE : EXCHANGE);
            contract.secType(SECURITY_TYPE);
            return contract;
        }
    }

    @Override
    public Contract createFuturesContract(String ticker, String lastTradeDateOrContractMonth) {
        Contract contract = new FutContract(ticker, (lastTradeDateOrContractMonth == null || lastTradeDateOrContractMonth.isEmpty()) ? "202412" : lastTradeDateOrContractMonth);
        contract.exchange("CME");
        return contract;
    }

    @Override
    public Contract createOptionsContract(String ticker, Double strike, String dateYYYYMMDD, String callOrPut) {
        Contract contract = new Contract();
        contract.symbol(ticker);
        contract.secType(OPTIONS_TYPE);
        contract.currency(CURRENCY);
        contract.exchange(SMART_EXCHANGE);
        contract.multiplier(MULTIPLIER_100);
        contract.strike(strike);
        contract.lastTradeDateOrContractMonth(dateYYYYMMDD);
        contract.right(callOrPut);
        return contract;
    }

    @Override
    public Contract createFutureOptionsContract(String ticker, Double strike, String dateYYYYMMDD, String callOrPut, ContractEntity contractEntity) {
        Contract contract = new Contract();
        contract.symbol(ticker);
        contract.secType(FUTURE_OPTIONS_TYPE);
        contract.currency(CURRENCY);
        contract.exchange(SMART_EXCHANGE);
        contract.strike(strike);
        contract.lastTradeDateOrContractMonth(dateYYYYMMDD);
        contract.right(callOrPut);
        contract.multiplier(getFutureOptionContractMultiplier(ticker));
        contract.tradingClass("NQ");
        return contract;
    }

    private String getFutureOptionContractMultiplier(String ticker) {
        if (null == ticker || ticker.isEmpty()) {
            return MULTIPLIER_100;
        } else if (ticker.startsWith("NQ")) {
            return "20";
        } else if (ticker.startsWith("MNQ")) {
            return "2";
        } else if (ticker.startsWith("ES")) {
            return "50";
        } else if (ticker.startsWith("MES")) {
            return "5";
        } else {
            return MULTIPLIER_100;
        }
    }

    @Override
    public ContractDetails getOptionsChainContract(String ticker, String dateYYYYMMDD) {
        Contract contract = new Contract();
        contract.symbol(ticker);
        contract.secType(OPTIONS_TYPE);
        contract.currency(CURRENCY);
        contract.exchange(SMART_EXCHANGE);
        contract.multiplier(MULTIPLIER_100);
        contract.lastTradeDateOrContractMonth(dateYYYYMMDD);

        int timeAsInt = Math.toIntExact(System.currentTimeMillis() % MILLIS_IN_DAY);

        eClientSocket.reqContractDetails(timeAsInt, contract);

        ContractDetails contractDetails = new ContractDetails();


        return getOptionsContractDetails(timeAsInt);
    }

    @Override
    public Contract createOptionsStraddleContract(String ticker, Double strike, String dateYYYYMMDD, com.trading.app.tradingapp.dto.OrderType orderType, boolean isFutOpt, ContractEntity contractEntity) {

        Contract straddleContract = new Contract();
        straddleContract.symbol(ticker);
        straddleContract.secType(STRADDLE_TYPE);
        straddleContract.currency(CURRENCY);
        straddleContract.exchange(SMART_EXCHANGE);
        straddleContract.multiplier(MULTIPLIER_100);
        straddleContract.strike(strike);
        straddleContract.lastTradeDateOrContractMonth(dateYYYYMMDD);


        int timeAsInt = Math.toIntExact(System.currentTimeMillis() % MILLIS_IN_DAY);

        Contract callContract = isFutOpt ? createFutureOptionsContract(ticker, strike, dateYYYYMMDD, OptionType.CALL.toString(), contractEntity) : createOptionsContract(ticker, strike, dateYYYYMMDD, OptionType.CALL.toString());
        eClientSocket.reqContractDetails(timeAsInt, callContract);
        callContract.conid(getOptionsContractDetails(timeAsInt).conid());

        Contract putContract = isFutOpt ? createFutureOptionsContract(ticker, strike, dateYYYYMMDD, OptionType.PUT.toString(), contractEntity) : createOptionsContract(ticker, strike, dateYYYYMMDD, OptionType.PUT.toString());
        eClientSocket.reqContractDetails(timeAsInt + 1, putContract);

        putContract.conid(getOptionsContractDetails(timeAsInt + 1).conid());

        ComboLeg callLeg = new ComboLeg();
        callLeg.conid(callContract.conid());
        callLeg.ratio(1);
        callLeg.action(orderType.toString());
        callLeg.exchange(SMART_EXCHANGE);


        ComboLeg putLeg = new ComboLeg();
        putLeg.conid(putContract.conid());
        putLeg.ratio(1);
        putLeg.action(orderType.toString());
        putLeg.exchange(SMART_EXCHANGE);

        straddleContract.comboLegs(Arrays.asList(callLeg, putLeg));

        return straddleContract;
    }

    private ContractDetails getOptionsContractDetails(int requestId) {
        try {
            while (getContractDetailsMap().get(requestId) == null) {
                Thread.sleep(10);
            }

            ContractDetails contractDetails = getContractDetailsMap().get(requestId);
            getContractDetailsMap().remove(requestId);
            return contractDetails;

        } catch (InterruptedException ie) {
            return null;
        }
    }

    public ContractEntity getContractByTickerId(Integer tickerId) {
        List<ContractEntity> contracts = getContractRepository().findByTickerId(tickerId);
        return (null == contracts || contracts.isEmpty()) ? null : contracts.get(0);
    }

    @Override
    public ContractEntity getContractByTickerSymbol(String tickerSymbol) {
        List<ContractEntity> contracts = getContractRepository().findBySymbol(tickerSymbol);
        return (null == contracts || contracts.isEmpty()) ? null : contracts.get(0);
    }

    @Override
    public ContractEntity createContractEntity(Contract contract) {
        Optional<ContractEntity> contractEntityOptional = getContractRepository().findById(contract.symbol());
        if (contractEntityOptional.isEmpty()) {
            ContractEntity contractEntity = new ContractEntity();
            contractEntity.setSymbol(contract.symbol());
            contractEntity.setCurrency(contract.currency());
            contractEntity.setExchange(contract.exchange());
            contractEntity.setSecType(contract.secType().getApiString());
            contractEntity.setLtp(0.0d);
            contractEntity.setLastBid(0.0d);
            contractEntity.setLastAsk(0.0d);
            contractEntity.setDefaultQuantity(100);
            contractEntity.setStep1(0.1d);
            contractEntity.setStep2(0.2d);
            contractEntity.setStep3(0.5d);
            contractEntity.setStep4(1d);
            contractEntity.setStep5(2d);
            contractEntity.setTickerId((int) getContractRepository().count() + 1);
            contractEntity.setTickerAskBidLtpValuesUpdateTimestamp(new java.sql.Timestamp(new Date().getTime()));
            updateContractEntity(contractEntity);
            return contractEntity;
        } else {
            return contractEntityOptional.get();
        }
    }

    @Override
    public void setOrderStatusAsCancelled(int orderId) {
        Optional<OrderEntity> orderEntityOptional = getOrderRepository().findById(orderId);
        if (orderEntityOptional.isPresent()) {
            OrderEntity orderEntity = orderEntityOptional.get();
            orderEntity.setOrderStatus(CANCELLED_STATUS);
            orderEntity.setStatusUpdateTimestamp(new java.sql.Timestamp(new Date().getTime()));
            getOrderRepository().saveAndFlush(orderEntity);
        }
    }

    @Override
    public OrderEntity updateOrderStatus(int orderId, String status, double filled, double remaining, double avgFillPrice, String whyHeld, double mktCapPrice) {
        Optional<OrderEntity> orderEntityOptional = getOrderRepository().findById(orderId);
        if (orderEntityOptional.isPresent()) {
            OrderEntity orderEntity = orderEntityOptional.get();
            orderEntity.setOrderStatus(status);
            orderEntity.setFilled(filled);
            Double previousRemaining = orderEntity.getRemaining();
            orderEntity.setRemaining(remaining);
            orderEntity.setAvgFillPrice(avgFillPrice);
            orderEntity.setMktCapPrice(mktCapPrice);
            orderEntity.setWhyHeld(whyHeld);

            if (CANCELLED_STATUS.equalsIgnoreCase(status)) {
                LOGGER.info("Order status is CANCELLED");
            }
            if (FILLED_STATUS.equalsIgnoreCase(status)) {
                // Set Realized PNL, if any of the child orders are filled completely
                if (null != orderEntity.getParentOrder()) {
                    OrderEntity parentOrderEntity = orderEntity.getParentOrder();
                    double fillPriceDiff = ((orderEntity.getAvgFillPrice() - parentOrderEntity.getAvgFillPrice()) * (com.trading.app.tradingapp.dto.OrderType.BUY.toString().equalsIgnoreCase(parentOrderEntity.getOrderAction()) ? 1 : -1));
                    parentOrderEntity.setRealizedPNL(roundOffDoubleForPriceDecimalFormat(fillPriceDiff * orderEntity.getFilled()));
                    getOrderRepository().saveAndFlush(parentOrderEntity);
                }

                if (previousRemaining != null && previousRemaining > remaining) {
                    List<OrderEntity> ocaOrders = new ArrayList<>();
                    getOrderRepository().findAll().forEach(order -> {
                        if (order.getParentOcaOrder() != null && orderId == order.getParentOcaOrder().getOrderId()) {
                            ocaOrders.add(order);
                        }
                    });

                    if (!CollectionUtils.isEmpty(ocaOrders)) {
                        LOGGER.warn("Transmitting OCA orders >> " + ocaOrders.size());
                        ocaOrders.forEach(ocaOrderEntity -> transmitOrder(prepareOrderForTransmit(ocaOrderEntity, filled), ocaOrderEntity.getSymbol(), true));
                    }
                }
            }

            orderEntity.setStatusUpdateTimestamp(new java.sql.Timestamp(new Date().getTime()));
            getOrderRepository().saveAndFlush(orderEntity);
            return orderEntity;
        }
        return null;
    }


    private Order prepareOrderForTransmit(OrderEntity orderEntity, double filledQty) {
        Order transmitOrder = new Order();
        transmitOrder.orderId(orderEntity.getOrderId());
        transmitOrder.action(orderEntity.getOrderAction());
        transmitOrder.orderType(orderEntity.getOrderType());
        transmitOrder.hidden(true);
        transmitOrder.totalQuantity(filledQty * (orderEntity.getOcaHedgeMultiplier() == null ? 1 : orderEntity.getOcaHedgeMultiplier()));
        if ("STP".equalsIgnoreCase(orderEntity.getOrderType())) {
            transmitOrder.auxPrice(roundOffDoubleForPriceDecimalFormat(orderEntity.getStopLossTriggerPrice()));
        } else {
            transmitOrder.lmtPrice(roundOffDoubleForPriceDecimalFormat(orderEntity.getTransactionPrice()));
        }
        transmitOrder.tif(Types.TimeInForce.GTC);
        transmitOrder.outsideRth(true);
        transmitOrder.account(getTradingAccount());
        transmitOrder.ocaGroup("OCA_" + orderEntity.getParentOcaOrder().getOrderId());
        transmitOrder.ocaType(1);

        return transmitOrder;
    }

    public void transmitOrder(Order order, String ticker, boolean transmitFlag) {
        order.transmit(transmitFlag);
        LOGGER.warn("Transmitting order with ID >>" + order.orderId());
        eClientSocket.placeOrder(order.orderId(), createContract(ticker), order);
    }


    public String getApiHost() {
        return apiHost;
    }

    public void setApiHost(String apiHost) {
        this.apiHost = apiHost;
    }

    public String getApiPort() {
        return apiPort;
    }

    public void setApiPort(String apiPort) {
        this.apiPort = apiPort;
    }

    public String getApiClientId() {
        return apiClientId;
    }

    public void setApiClientId(String apiClientId) {
        this.apiClientId = apiClientId;
    }

    public int getNextTWSOrderId() {
        return nextTWSOrderId;
    }

    public void setNextTWSOrderId(int nextTWSOrderId) {
        this.nextTWSOrderId = nextTWSOrderId;
    }

    public int getNextTickerId() {
        return nextTickerId;
    }

    public void setNextTickerId(int nextTickerId) {
        this.nextTickerId = nextTickerId;
    }

    public ContractRepository getContractRepository() {
        return contractRepository;
    }

    public void setContractRepository(ContractRepository contractRepository) {
        this.contractRepository = contractRepository;
    }

    public OrderRepository getOrderRepository() {
        return orderRepository;
    }

    public void setOrderRepository(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public ContractService getContractService() {
        return contractService;
    }

    public void setContractService(ContractService contractService) {
        this.contractService = contractService;
    }

    @Override
    public void tickPrice(int tickerId, int field, double price, TickAttrib attribs) {
        //String fieldName = LTP_FIELD == field ? "LTP" : (ASK_FIELD == field ? "ASK" : (BID_FIELD == field ? "BID" : ""+field));
        //LOGGER.info("Tick Price data: Ticker Id:{}, Field: {}, Price: {}, CanAutoExecute: {}, pastLimit: {}, pre-open: {}", tickerId, fieldName, price, attribs.canAutoExecute(), attribs.pastLimit(), attribs.preOpen());

        if (field == LTP_FIELD || field == ASK_FIELD || field == BID_FIELD) {
            // LOGGER.info("Updated LTP received for tracker: Ticker Id:{}, Price: {}", tickerId, price);
            ContractEntity contractEntity = getContractByTickerId(tickerId);
            if (null != contractEntity) {
                if (field == LTP_FIELD) {
                    contractEntity.setLtp(price);
                    processTriggerOrders(contractEntity.getSymbol(), price);
                } else if (field == ASK_FIELD) {
                    contractEntity.setLastAsk(price);
                } else {
                    contractEntity.setLastBid(price);
                }
                contractEntity.setTickerAskBidLtpValuesUpdateTimestamp(new java.sql.Timestamp(new Date().getTime()));
                // persist new LTP and timestamp
                updateContractEntity(contractEntity);


                // update LTP in the runner
//                if (getContractService().getTickerSequenceTrackerMap() != null) {
//                    SequenceTracker sequenceTracker = getContractService().getTickerSequenceTrackerMap().get(contractEntity.getSymbol());
//                    if (sequenceTracker != null) {
//                        sequenceTracker.setLtp(price);
//                        LOGGER.info("Updated LTP in sequence tracker: Ticker Id:{}, Price: {}", tickerId, price);
//                    }
//                }
            } else {
                LOGGER.warn("Received ticker data for invalid tickerId {}", tickerId);
            }
        }

        if (field == ASK_FIELD) {
            ContractEntity contractEntity = getContractByTickerId(tickerId);
            if (null != contractEntity) {
                contractEntity.setLastAsk(price);
                contractEntity.setTickerAskBidLtpValuesUpdateTimestamp(new java.sql.Timestamp(new Date().getTime()));
                // persist new last ask price and timestamp
                updateContractEntity(contractEntity);
            } else {
                LOGGER.warn("Received Last ask price for invalid tickerId {}", tickerId);
            }
        }

        if (field == BID_FIELD) {
            ContractEntity contractEntity = getContractByTickerId(tickerId);
            if (null != contractEntity) {
                contractEntity.setLastBid(price);
                contractEntity.setTickerAskBidLtpValuesUpdateTimestamp(new java.sql.Timestamp(new Date().getTime()));
                // persist new last bid price and timestamp
                updateContractEntity(contractEntity);
            } else {
                LOGGER.warn("Received Last bid price for invalid tickerId {}", tickerId);
            }
        }
    }

    public void processTriggerOrders(String symbol, double price) {
        List<TriggerOrderEntity> triggerOrderList = getTriggerOrderRepository().findTriggeredOrdersForTicker(symbol, price);
        if (!triggerOrderList.isEmpty()) {
            LOGGER.info("Found [{}] triggered orders for symbol [{}] at price [{}]", triggerOrderList.size(), symbol, price);
            Contract contract = createContract(symbol);
            for (TriggerOrderEntity triggerOrder : triggerOrderList) {
                if (canCreateTWSOrderForThisTriggerOrder(triggerOrder.getPk())) {
                    ProcessTriggerOrderThread processTriggerOrderThread = new ProcessTriggerOrderThread("ProcessTriggerOrderThread - " + triggerOrder.getSequenceId(), triggerOrder.getPk(), getOrderService(), contract, triggerOrder);
                    processTriggerOrderThread.start();
                } else {
                    LOGGER.info("Actual TWS order already created for trigger order with pk [{}]", triggerOrder.getPk());
                }
            }
            LOGGER.info("List of triggered orders processed successfully so far [{}]", previouslyPlacedTriggeredOrdersInTWS);
        }
//        else {
//            LOGGER.info("No triggered orders found for symbol [{}] at price [{}]", symbol, price);
//        }
    }

//    @Transactional
//    public List<TriggerOrderEntity> findTriggeredOrdersForTicker(String symbol, Double price){
//        EntityTransaction tx = null;
//        boolean txSuccess = false;
//        List<TriggerOrderEntity> result = new ArrayList<>();
//
//        try {
//           // session = em.unwrap(Session.class);
//            EntityManager em = entityManagerFactory.createEntityManager();
//            tx = em.getTransaction();
//            tx.begin();
//
//            Query query = em.createQuery(TRIGGER_ORDER_QUERY_STRING);
//            query.setParameter("symbol", symbol);
//            query.setParameter("ltp", price);
//            //query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
//            result.addAll(query.getResultList());
//            txSuccess = true;
//        } catch (Exception ex){
//            LOGGER.error("Error while getting transaction", ex);
//        }
//        finally {
//            if(null != tx) {
//                if (!txSuccess) {
//                    tx.rollback();
//                } else {
//                    tx.commit();
//                }
//            }
//        }
//        return result;
//    }


    @Override
    public void tickSize(int tickerId, int field, int size) {

    }

    @Override
    public void tickOptionComputation(int tickerId, int field, double impliedVol, double delta, double optPrice, double pvDividend, double gamma, double vega, double theta, double undPrice) {

    }

    @Override
    public void tickGeneric(int tickerId, int tickType, double value) {

    }

    @Override
    public void tickString(int tickerId, int tickType, String value) {

    }

    @Override
    public void tickEFP(int tickerId, int tickType, double basisPoints, String formattedBasisPoints, double impliedFuture, int holdDays, String futureLastTradeDate, double dividendImpact, double dividendsToLastTradeDate) {

    }

    @Override
    public void orderStatus(int orderId, String status, double filled, double remaining, double avgFillPrice, int permId, int parentId, double lastFillPrice, int clientId, String whyHeld, double mktCapPrice) {
        //LOGGER.info("Got order status for order: [{}]", orderId);
        //LOGGER.info("Data: status:[{}], filled:[{}], remaining:[{}], avgFillPrice:[{}], parent order id:[{}], clientId:[{}], mktCapPrice:[{}], whyHeld:[{}]", status, filled, remaining, avgFillPrice, parentId, clientId, mktCapPrice, whyHeld);
        OrderEntity orderToUpdate = updateOrderStatus(orderId, status, filled, remaining, avgFillPrice, whyHeld, mktCapPrice);

        if(!SUBMITTED_STATUS.equals(status)) {
            LOGGER.info("Order status is updated in the database for order: [{}]. Status: [{}]", orderId, status);
        }

        // If order is Filled
        if (FILLED_STATUS.equalsIgnoreCase(status)) {

//            getContractService().getTickerSequenceTrackerMap().values().forEach(sequenceTracker -> {
//                if (sequenceTracker.getBuyTpOrderId().equals(orderId)) {
//                    sequenceTracker.setBuyTpOrderFilled(true);
//                    return;
//                }
//                if (sequenceTracker.getSellTpOrderId().equals(orderId)) {
//                    sequenceTracker.setSellTpOrderFilled(true);
//                    return;
//                }
//            });

            // Update SL order quantity based on original order Fill status
            if (null != orderToUpdate && null != orderToUpdate.getTradeStartSequenceId()) {
                OrderEntity slOrder = getOrderService().getSingleOrderBySlCheckSequenceIdAndTrigger(orderToUpdate.getTradeStartSequenceId(), orderToUpdate.getOrderTrigger());
                if (null != slOrder && slOrder.getOrderId() != null && slOrder.getOrderId() != orderId) {
                    double currentOutstandingQty = Math.abs(getOrderService().findOutstandingQtyForTickerWithSpecificOrderTrigger(orderToUpdate.getSymbol(), orderToUpdate.getOrderTrigger(), orderToUpdate.getOrderTriggerInterval()));

                    // If new quantity for SL order  is zero then cancel SL order, else change quantity to new quantity
                    if (currentOutstandingQty == 0) {
                        LOGGER.info("Cancelling the SL order with OrderId=[{}], as the outstanding qty of original trade is 0, so SL order is not needed.", slOrder.getOrderId());
                        getOrderService().cancelOrder(slOrder.getOrderId());
                    } else {
                        LOGGER.info("Received FILLED status update for order [{}]. Updating quantity of the associated SL order with OrderId=[{}].", orderId, slOrder.getOrderId());
                        getOrderService().updateOrderQuantity(slOrder.getOrderId(), slOrder, currentOutstandingQty, eClientSocket);
                    }
                } else if (null != slOrder && slOrder.getOrderId() != null && slOrder.getOrderId() == orderId) {
                    double currentOutstandingQtyForSlOrder = slOrder.getQuantity() - slOrder.getFilled();
                    if (0 == currentOutstandingQtyForSlOrder && slOrder.getFilled() > 0) {
                        LOGGER.info("Received FULLY FILLED status update for SL order [{}]. Cancelling all other non filled non SL orders", slOrder.getOrderId());
                        getOrderService().cancelAllUnfilledNonSLOrdersForCurrentTrade(slOrder.getOrderTriggerInterval(), slOrder.getTradeStartSequenceId(), slOrder.getOrderTrigger(), slOrder.getSymbol());
                    }
                }
            }

            if (null != orderToUpdate) {
                List<TriggerOrderEntity> triggerOrderEntityList = getTriggerOrderRepository().findByParentOrderAndActive(orderToUpdate, Boolean.FALSE);
                if (null != triggerOrderEntityList && !triggerOrderEntityList.isEmpty()) {
                    for (TriggerOrderEntity triggerOrderEntity : triggerOrderEntityList) {
                        triggerOrderEntity.setActive(Boolean.TRUE);
                        getTriggerOrderRepository().saveAndFlush(triggerOrderEntity);
                    }
                }
            }
        }
    }

    @Override
    public void openOrder(int orderId, Contract contract, Order order, OrderState orderState) {


    }

    @Override
    public void openOrderEnd() {

    }

    @Override
    public void updateAccountValue(String key, String value, String currency, String accountName) {

    }

    @Override
    public void updatePortfolio(Contract contract, double position, double marketPrice, double marketValue, double averageCost, double unrealizedPNL, double realizedPNL, String accountName) {

    }

    @Override
    public void updateAccountTime(String timeStamp) {

    }

    @Override
    public void accountDownloadEnd(String accountName) {

    }

    @Override
    public void nextValidId(int orderId) {
        // LOGGER.info("Received next order id from TWS [{}] ***", orderId);
        setNextTWSOrderId(orderId);
    }

    @Override
    public void contractDetails(int reqId, ContractDetails contractDetails) {
        LOGGER.info("Fetched contract details for request id {}", reqId);
        getContractDetailsMap().put(reqId, contractDetails);
    }

    @Override
    public void bondContractDetails(int reqId, ContractDetails contractDetails) {

    }

    @Override
    public void contractDetailsEnd(int reqId) {

    }

    @Override
    public void execDetails(int reqId, Contract contract, Execution execution) {

    }

    @Override
    public void execDetailsEnd(int reqId) {

    }

    @Override
    public void updateMktDepth(int tickerId, int position, int operation, int side, double price, int size) {

    }

    @Override
    public void updateMktDepthL2(int tickerId, int position, String marketMaker, int operation, int side, double price, int size, boolean isSmartDepth) {

    }

    @Override
    public void updateNewsBulletin(int msgId, int msgType, String message, String origExchange) {

    }

    @Override
    public void managedAccounts(String accountsList) {

    }

    @Override
    public void receiveFA(int faDataType, String xml) {

    }

    @Override
    public void historicalData(int reqId, Bar bar) {

    }

    @Override
    public void scannerParameters(String xml) {

    }

    @Override
    public void scannerData(int reqId, int rank, ContractDetails contractDetails, String distance, String benchmark, String projection, String legsStr) {

    }

    @Override
    public void scannerDataEnd(int reqId) {

    }

    @Override
    public void realtimeBar(int reqId, long time, double open, double high, double low, double close, long volume, double wap, int count) {

    }

    @Override
    public void currentTime(long time) {

    }

    @Override
    public void fundamentalData(int reqId, String data) {

    }

    @Override
    public void deltaNeutralValidation(int reqId, DeltaNeutralContract deltaNeutralContract) {

    }

    @Override
    public void tickSnapshotEnd(int reqId) {

    }

    @Override
    public void marketDataType(int reqId, int marketDataType) {

    }

    @Override
    public void commissionReport(CommissionReport commissionReport) {

    }

    @Override
    public void position(String account, Contract contract, double pos, double avgCost) {

    }

    @Override
    public void positionEnd() {

    }

    @Override
    public void accountSummary(int reqId, String account, String tag, String value, String currency) {

    }

    @Override
    public void accountSummaryEnd(int reqId) {

    }

    @Override
    public void verifyMessageAPI(String apiData) {

    }

    @Override
    public void verifyCompleted(boolean isSuccessful, String errorText) {

    }

    @Override
    public void verifyAndAuthMessageAPI(String apiData, String xyzChallenge) {

    }

    @Override
    public void verifyAndAuthCompleted(boolean isSuccessful, String errorText) {

    }

    @Override
    public void displayGroupList(int reqId, String groups) {

    }

    @Override
    public void displayGroupUpdated(int reqId, String contractInfo) {

    }

    @Override
    public void error(Exception e) {

        LOGGER.error("Error : ", e);
    }

    @Override
    public void error(String str) {
        LOGGER.error("Error : " + str);
    }

    @Override
    public void error(int id, int errorCode, String errorMsg) {

        if (errorCode == 2104 || errorCode == 2106 || errorCode == 2158) {
            // ignore these error codes as they are not real errors.
            return;
        }
        if (errorCode != 2109 || !errorMsg.startsWith("Order Event Warning:Attribute 'Outside Regular Trading Hours' is ignored")) {
            LOGGER.error("Error : id=" + id + " code=" + errorCode + " msg=" + errorMsg);
        }

        if (errorCode == 10148 && !errorMsg.contains("state: Cancelled")) {
            LOGGER.error("*** >>> *** >>> Cancelling order with id [{}] failed. Attempting to cancel order again.", id);
            getOrderService().cancelOrder(id);
        }

        if (errorCode == 507 || errorCode == 2110 || errorCode == 1100 || errorCode == 504) {
            LOGGER.error("The connection with TWS client is interrupted!!");
            restartSpringBootApplication();
        }
    }

    private void restartSpringBootApplication() {
        try {
            LOGGER.error("Terminating current connection with TWS client .....");
            LOGGER.info("Initiating interrupt !!");
            eReader.interrupt();
            LOGGER.info("Initiating Disconnect !!");
            eClientSocket.eDisconnect();
            TradingAppApplication.restart();
        } catch (Exception ex) {
            LOGGER.error("Exception while waiting to reconnect with TWS client", ex);
        }
    }

    @Override
    public void connectionClosed() {

    }

    @Override
    public void connectAck() {

    }

    @Override
    public void positionMulti(int reqId, String account, String modelCode, Contract contract, double pos, double avgCost) {

    }

    @Override
    public void positionMultiEnd(int reqId) {

    }

    @Override
    public void accountUpdateMulti(int reqId, String account, String modelCode, String key, String value, String currency) {

    }

    @Override
    public void accountUpdateMultiEnd(int reqId) {

    }

    @Override
    public void securityDefinitionOptionalParameter(int reqId, String exchange, int underlyingConId, String tradingClass, String multiplier, Set<String> expirations, Set<Double> strikes) {

    }

    @Override
    public void securityDefinitionOptionalParameterEnd(int reqId) {

    }

    @Override
    public void softDollarTiers(int reqId, SoftDollarTier[] tiers) {

    }

    @Override
    public void familyCodes(FamilyCode[] familyCodes) {

    }

    @Override
    public void symbolSamples(int reqId, ContractDescription[] contractDescriptions) {

    }

    @Override
    public void historicalDataEnd(int reqId, String startDateStr, String endDateStr) {

    }

    @Override
    public void mktDepthExchanges(DepthMktDataDescription[] depthMktDataDescriptions) {

    }

    @Override
    public void tickNews(int tickerId, long timeStamp, String providerCode, String articleId, String headline, String extraData) {

    }

    @Override
    public void smartComponents(int reqId, Map<Integer, Map.Entry<String, Character>> theMap) {

    }

    @Override
    public void tickReqParams(int tickerId, double minTick, String bboExchange, int snapshotPermissions) {

    }

    @Override
    public void newsProviders(NewsProvider[] newsProviders) {

    }

    @Override
    public void newsArticle(int requestId, int articleType, String articleText) {

    }

    @Override
    public void historicalNews(int requestId, String time, String providerCode, String articleId, String headline) {

    }

    @Override
    public void historicalNewsEnd(int requestId, boolean hasMore) {

    }

    @Override
    public void headTimestamp(int reqId, String headTimestamp) {

    }

    @Override
    public void histogramData(int reqId, List<HistogramEntry> items) {

    }

    @Override
    public void historicalDataUpdate(int reqId, Bar bar) {

    }

    @Override
    public void rerouteMktDataReq(int reqId, int conId, String exchange) {

    }

    @Override
    public void rerouteMktDepthReq(int reqId, int conId, String exchange) {

    }

    @Override
    public void marketRule(int marketRuleId, PriceIncrement[] priceIncrements) {

    }

    @Override
    public void pnl(int reqId, double dailyPnL, double unrealizedPnL, double realizedPnL) {

    }

    @Override
    public void pnlSingle(int reqId, int pos, double dailyPnL, double unrealizedPnL, double realizedPnL, double value) {

    }

    @Override
    public void historicalTicks(int reqId, List<HistoricalTick> ticks, boolean done) {

    }

    @Override
    public void historicalTicksBidAsk(int reqId, List<HistoricalTickBidAsk> ticks, boolean done) {

    }

    @Override
    public void historicalTicksLast(int reqId, List<HistoricalTickLast> ticks, boolean done) {

    }

    @Override
    public void tickByTickAllLast(int reqId, int tickType, long time, double price, int size, TickAttribLast tickAttribLast, String exchange, String specialConditions) {

    }

    @Override
    public void tickByTickBidAsk(int reqId, long time, double bidPrice, double askPrice, int bidSize, int askSize, TickAttribBidAsk tickAttribBidAsk) {

    }

    @Override
    public void tickByTickMidPoint(int reqId, long time, double midPoint) {

    }

    @Override
    public void orderBound(long orderId, int apiClientId, int apiOrderId) {

    }

    @Override
    public void completedOrder(Contract contract, Order order, OrderState orderState) {

    }

    @Override
    public void completedOrdersEnd() {

    }


    private double roundOffDoubleForPriceDecimalFormat(double price) {
        return Math.round(price * 100.0d) / 100.0d;
    }

    public Map<Integer, ContractDetails> getContractDetailsMap() {
        return contractDetailsMap;
    }

    public void setContractDetailsMap(Map<Integer, ContractDetails> contractDetailsMap) {
        this.contractDetailsMap = contractDetailsMap;
    }

    public SystemConfigService getSystemConfigService() {
        return systemConfigService;
    }

    public void setSystemConfigService(SystemConfigService systemConfigService) {
        this.systemConfigService = systemConfigService;
    }

    private String getTradingAccount() {
        return getSystemConfigService().getString("tws.trading.account");
    }

    public OrderService getOrderService() {
        return orderService;
    }

    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }

    public TriggerOrderRepository getTriggerOrderRepository() {
        return triggerOrderRepository;
    }

    public void setTriggerOrderRepository(TriggerOrderRepository triggerOrderRepository) {
        this.triggerOrderRepository = triggerOrderRepository;
    }

    public EntityManagerFactory getEntityManagerFactory() {
        return entityManagerFactory;
    }

    public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }
}
