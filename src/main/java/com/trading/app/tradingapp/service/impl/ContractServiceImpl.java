package com.trading.app.tradingapp.service.impl;

import com.ib.client.Contract;
import com.ib.client.EClientSocket;
import com.trading.app.tradingapp.dto.OrderType;
import com.trading.app.tradingapp.dto.SequenceTracker;
import com.trading.app.tradingapp.dto.request.CreateSetOrderRequestDto;
import com.trading.app.tradingapp.dto.response.CreateSetOrderResponseDto;
import com.trading.app.tradingapp.dto.response.GetMarketDataResponseDto;
import com.trading.app.tradingapp.dto.response.MarketDataDto;
import com.trading.app.tradingapp.persistance.entity.ContractEntity;
import com.trading.app.tradingapp.persistance.repository.ContractRepository;
import com.trading.app.tradingapp.service.BaseService;
import com.trading.app.tradingapp.service.ContractService;
import com.trading.app.tradingapp.service.OrderService;
import com.trading.app.tradingapp.util.RequestMarketDataThread;
import com.trading.app.tradingapp.util.UpdateSequenceTrackerThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ContractServiceImpl implements ContractService {

    @Resource
    private BaseService baseService;

    @Resource
    private OrderService orderService;

    @Resource
    private ContractRepository contractRepository;

    private Map<String, SequenceTracker> tickerSequenceTrackerMap = new HashMap<>();

    private static final Logger LOGGER = LoggerFactory.getLogger(ContractServiceImpl.class);

    private static final String CONTINUOUS_SEQUENCE_TRIGGER_ORDER = "Automate Order - CONTINUOUS_SEQUENCE_TRIGGER_ORDER";

    @Override
    public GetMarketDataResponseDto getMarketData(String ticker) {
        LOGGER.info("Fetching Market Data for ticker [{}]", ticker);
        try {
            Contract contract = getBaseService().createContract(ticker);
            return getSuccessGetMarketDataResult(ticker, getBaseService().getMarketDataForContract(contract, true));
        } catch (Exception ex) {
            LOGGER.error("Could not fetch Market Data for ticker [{}]. Exception: [{}]", ticker, ex.getMessage());
            return getFailedGetMarketDataResult(ticker, ex);
        }
    }

    @Override
    public void createContractEntity(String ticker) {
        LOGGER.info("Creating contract entity for ticker [{}]", ticker);
        try {
            Contract contract = getBaseService().createContract(ticker);
            getBaseService().createContractEntity(contract);
        } catch (Exception ex) {
            LOGGER.error("Could not create contract entity for ticker [{}]. Exception: [{}]", ticker, ex.getMessage());
        }
    }

    @Override
    public void startBuySequence(String ticker, Double tpMargin, Integer quantity) {

        SequenceTracker sequenceTracker = getTickerSequenceTrackerMap().get(ticker);

        if(sequenceTracker != null && sequenceTracker.isBuyRunEnable()){
            LOGGER.info("Buy sequence already running for ticker [{}]", ticker);
            return;
        }

        try {
            // initiate ticker sequence tracker for BUY
            addUpdateTickerSequenceTracker(ticker, tpMargin, OrderType.BUY);

            Contract contract = getBaseService().createContract(ticker);

            // start market data stream for the ticker
            getBaseService().startMarketDataStreamForContract(contract);

            // Wait for half second for data stream to initiate
            Thread.sleep(500);

            sequenceTracker = getTickerSequenceTrackerMap().get(ticker);

            LOGGER.info("Started buy sequence for ticker [{}]", ticker);

            while (sequenceTracker.isBuyRunEnable()) {

                // Existing buy order is not filled yet, so don't place next order
                if (!sequenceTracker.isBuyTpOrderFilled()) {
                    Thread.sleep(500);
                    continue;
                }

                // If latest LTP is updated by Base service in this map use it, else get latest updated LTP using thread
                Double tickerLtp = getLatestTickerLTP(ticker);

                // If ticker just initiated
                if (sequenceTracker.getNextBuyValue() == 0.0d && sequenceTracker.getLastBuyValue() == 0.0d) {
                    sequenceTracker.setNextBuyValue(tickerLtp);
                    sequenceTracker.setLastBuyValue(tickerLtp);
                }
                // Else if LTP is lower than last buy value so trend could be reversing. no trade.
                else if (sequenceTracker.getLastBuyValue() > tickerLtp) {
                    Thread.sleep(500);
                    continue;
                }

                // Else if LTP is higher than LBV but lower than NBV, then use same trade values as last trade
                else if (tickerLtp >= sequenceTracker.getLastBuyValue() && tickerLtp < sequenceTracker.getNextBuyValue()) {
                    sequenceTracker.setNextBuyValue(sequenceTracker.getLastBuyValue());
                }

                // if LTP is higher than NBV and less than NBV + Margin 1, then keep NBV as is
                else if (tickerLtp >= sequenceTracker.getNextBuyValue() && tickerLtp < sequenceTracker.getNextBuyValue() + sequenceTracker.getBuyMargin1()) {
                    // do nothing as NBV is already set
                }

                // if LTP is higher than NBV + Margin 1 and less than NBV + Margin 2, then keep NBV as NBV + Margin 1
                else if (tickerLtp >= sequenceTracker.getNextBuyValue() + sequenceTracker.getBuyMargin1() && tickerLtp < sequenceTracker.getNextBuyValue() + sequenceTracker.getBuyMargin2()) {
                    sequenceTracker.setNextBuyValue(sequenceTracker.getNextBuyValue() + sequenceTracker.getBuyMargin1());
                }

                // If LTP had moved a lot ahead, set LTP as next buy value
                else if (tickerLtp >= sequenceTracker.getNextBuyValue() + sequenceTracker.getBuyMargin2()) {
                    sequenceTracker.setNextBuyValue(tickerLtp);
                }

                // else do noting, no trade
                else {
                    continue;
                }

                // Place the order with derived values
                createOrderWithNextValue(sequenceTracker, OrderType.BUY, ticker, sequenceTracker.getNextBuyValue(), quantity);
            }

        } catch (Exception ex) {
            LOGGER.error("Could not start buy sequence for ticker [{}]. Exception: [{}]", ticker, ex);
        }

    }

    @Override
    public void stopBuySequence(String tickerSymbol) {
        if (getTickerSequenceTrackerMap() != null && getTickerSequenceTrackerMap().get(tickerSymbol) != null) {
            getTickerSequenceTrackerMap().get(tickerSymbol).setBuyRunEnable(false);
            LOGGER.info("Stopped buy sequence for ticker [{}]", tickerSymbol);
        } else {
            LOGGER.error("Could not stop buy sequence for ticker [{}]. Ticker sequence does not exist.", tickerSymbol);
        }

    }

    @Override
    public void startSellSequence(String ticker, Double tpMargin, Integer quantity) {
        SequenceTracker sequenceTracker = getTickerSequenceTrackerMap().get(ticker);

        if(sequenceTracker != null && sequenceTracker.isSellRunEnable()){
            LOGGER.info("Sell sequence already running for ticker [{}]", ticker);
            return;
        }

        try {
            // initiate ticker sequence tracker for Sell
            addUpdateTickerSequenceTracker(ticker, tpMargin, OrderType.SELL);

            Contract contract = getBaseService().createContract(ticker);

            // start market data stream for the ticker
            getBaseService().startMarketDataStreamForContract(contract);

            // Wait for half second for data stream to initiate
            Thread.sleep(500);

            sequenceTracker = getTickerSequenceTrackerMap().get(ticker);

            LOGGER.info("Started sell sequence for ticker [{}]", ticker);

            while (sequenceTracker.isSellRunEnable()) {

                // Existing sell order is not filled yet, so don't place next order
                if (!sequenceTracker.isSellTpOrderFilled()) {
                    Thread.sleep(500);
                    continue;
                }

                // If latest LTP is updated by Base service in this map use it, else get latest updated LTP using thread
                Double tickerLtp = getLatestTickerLTP(ticker);

                // If ticker just initiated
                if (sequenceTracker.getNextSellValue() == 0.0d && sequenceTracker.getLastSellValue() == 0.0d) {
                    sequenceTracker.setNextSellValue(tickerLtp);
                    sequenceTracker.setLastSellValue(tickerLtp);
                }
                // Else if LTP is higher than last sell value so trend could be reversing. no trade.
                else if (sequenceTracker.getLastSellValue() < tickerLtp) {
                    Thread.sleep(500);
                    continue;
                }

                // Else if LTP is lower than LSV but higher than NSV, then use same trade values as last trade
                else if (tickerLtp <= sequenceTracker.getLastSellValue() && tickerLtp > sequenceTracker.getNextSellValue()) {
                    sequenceTracker.setNextSellValue(sequenceTracker.getLastSellValue());
                }

                // if LTP is lower than NSV and higher than (NSV + Margin 1), then keep NSV as is
                else if (tickerLtp <= sequenceTracker.getNextSellValue() && tickerLtp > (sequenceTracker.getNextSellValue() - sequenceTracker.getSellMargin1())) {
                    // do nothing as NSV is already set
                }

                // if LTP is lower than (NSV + Margin 1) and higher than (NSV + Margin 2), then keep NSV as (NSV + Margin 1)
                else if (tickerLtp <= (sequenceTracker.getNextSellValue() - sequenceTracker.getSellMargin1()) && tickerLtp > (sequenceTracker.getNextSellValue() - sequenceTracker.getSellMargin2())) {
                    sequenceTracker.setNextSellValue(sequenceTracker.getNextSellValue() - sequenceTracker.getSellMargin1());
                }

                // If LTP had moved a lot below, set LTP as next sell value
                else if (tickerLtp <= sequenceTracker.getNextSellValue() - sequenceTracker.getSellMargin2()) {
                    sequenceTracker.setNextSellValue(tickerLtp);
                }

                // else do noting, no trade
                else {
                    continue;
                }

                // Place the order with derived values
                createOrderWithNextValue(sequenceTracker, OrderType.SELL, ticker, sequenceTracker.getNextSellValue(), quantity);
            }

        } catch (Exception ex) {
            LOGGER.error("Could not start sell sequence for ticker [{}]. Exception: [{}]", ticker, ex);
        }

    }

    @Override
    public void stopSellSequence(String tickerSymbol) {

        if (getTickerSequenceTrackerMap() != null && getTickerSequenceTrackerMap().get(tickerSymbol) != null) {
            getTickerSequenceTrackerMap().get(tickerSymbol).setSellRunEnable(false);
            LOGGER.info("Stopped buy sequence for ticker [{}]", tickerSymbol);
        } else {
            LOGGER.error("Could not stop buy sequence for ticker [{}]. Ticker sequence does not exist.", tickerSymbol);
        }
    }

    @Override
    public void startMarketDataFeed(final EClientSocket connection) {

        if (connection == null || ! connection.isConnected()) {
            LOGGER.error("Could not initiate Market Data stream as EClientSocket is not connected");
        }

        List<ContractEntity> contractEntityList = contractRepository.findAllByOrderBySymbolAsc();

        if (contractEntityList == null || contractEntityList.isEmpty()) {
            return;
        }

        try {
            contractEntityList.forEach(contractEntity -> {
                try {
                    if (Boolean.TRUE.equals(contractEntity.isActive())) {
                        RequestMarketDataThread requestMarketDataThread = new RequestMarketDataThread(getBaseService().createContract(contractEntity.getSymbol()), getContractRepository(), connection);
                        requestMarketDataThread.start();
                        LOGGER.debug("Initiate Market Data stream for ticker [{}] after application startup", contractEntity.getSymbol());
                    }
                } catch (Exception ex) {
                    LOGGER.error("Could not initiate Market Data stream for ticker [{}]. Exception: [{}]", contractEntity.getSymbol(), ex.getMessage());
                }
            });

        } catch (Exception ex) {
            LOGGER.error("Could not connect with TWS app.");
        }
    }

    private void addUpdateTickerSequenceTracker(String ticker, Double tpMargin, OrderType orderType) {

        SequenceTracker sequenceTracker = getTickerSequenceTrackerMap().get(ticker);
        if (sequenceTracker == null) {
            sequenceTracker = new SequenceTracker();
        }

        if (OrderType.BUY.equals(orderType)) {
            sequenceTracker.setBuyRunEnable(true);
            sequenceTracker.setLastBuyValue(0.0d);
            sequenceTracker.setNextBuyValue(0.0d);
            sequenceTracker.setTpBuyMargin(tpMargin);
            sequenceTracker.setBuyMargin1(tpMargin / 4.0d);
            sequenceTracker.setBuyMargin2(tpMargin / 2.0d);
            sequenceTracker.setBuyTpOrderFilled(true);
        } else if (OrderType.SELL.equals(orderType)) {
            sequenceTracker.setSellRunEnable(false);
            sequenceTracker.setLastSellValue(0.0d);
            sequenceTracker.setNextSellValue(0.0d);
            sequenceTracker.setTpSellMargin(tpMargin);
            sequenceTracker.setSellMargin1(tpMargin / 4.0d);
            sequenceTracker.setSellMargin2(tpMargin / 2.0d);
            sequenceTracker.setSellTpOrderFilled(true);
        }

        getTickerSequenceTrackerMap().put(ticker, sequenceTracker);

        LOGGER.info("Added sequence tracker for ticker [{}]", ticker);
    }

    private void createOrderWithNextValue(SequenceTracker sequenceTracker, OrderType orderType, String ticker, Double transactionPrice, Integer quantity) {

        LOGGER.info("CreateOrderWithNextValue params [{}], [{}], [{}], [{}], [{}], ", sequenceTracker, orderType, ticker, transactionPrice, quantity);

        CreateSetOrderRequestDto createSetOrderRequestDto = new CreateSetOrderRequestDto();

        createSetOrderRequestDto.setTicker(ticker);
        createSetOrderRequestDto.setQuantity(quantity);
        createSetOrderRequestDto.setOrderType(orderType);
        createSetOrderRequestDto.setTransactionPrice(transactionPrice);

        if (OrderType.BUY.equals(orderType)) {
            createSetOrderRequestDto.setTargetPrice(createSetOrderRequestDto.getTransactionPrice() + sequenceTracker.getTpBuyMargin());
        } else {
            createSetOrderRequestDto.setTargetPrice(createSetOrderRequestDto.getTransactionPrice() - sequenceTracker.getTpSellMargin());
        }


        // Set TP order filled to false first before placing order.
        sequenceTracker.setBuyTpOrderFilled(false);

        // place the order
        CreateSetOrderResponseDto responseDto = getOrderService().createOrder(createSetOrderRequestDto, CONTINUOUS_SEQUENCE_TRIGGER_ORDER, null);

        // if order placed successfully, update values for next run
        if (Boolean.TRUE.equals(responseDto.getStatus())) {
            sequenceTracker.setLastBuyValue(transactionPrice);
            if (OrderType.BUY.equals(orderType)) {
                sequenceTracker.setBuyTpOrderId(responseDto.getTpOrderId());
                sequenceTracker.setNextBuyValue(sequenceTracker.getLastBuyValue() + sequenceTracker.getBuyMargin1());
            } else {
                sequenceTracker.setSellTpOrderId(responseDto.getTpOrderId());
                sequenceTracker.setNextBuyValue(sequenceTracker.getLastBuyValue() - sequenceTracker.getSellMargin1());
            }
        } else {
            // else stop the run if order placement failed
            if (OrderType.BUY.equals(orderType)) {
                sequenceTracker.setBuyRunEnable(false);
            } else {
                sequenceTracker.setSellRunEnable(false);
            }
        }
    }

    private GetMarketDataResponseDto getSuccessGetMarketDataResult(String ticker, MarketDataDto marketDataDto) {
        GetMarketDataResponseDto getMarketDataResponseDto = new GetMarketDataResponseDto();
        getMarketDataResponseDto.setStatus(true);
        getMarketDataResponseDto.setLtp(marketDataDto.getLtp());
        getMarketDataResponseDto.setBidPrice(marketDataDto.getLastBid());
        getMarketDataResponseDto.setAskPrice(marketDataDto.getLastAsk());
        getMarketDataResponseDto.setTicker(ticker);
        LOGGER.info("Fetched Market Data for ticker [{}] Successfully !!!", ticker);
        return getMarketDataResponseDto;
    }

    private GetMarketDataResponseDto getFailedGetMarketDataResult(String ticker, Exception ex) {
        GetMarketDataResponseDto getMarketDataResponseDto = new GetMarketDataResponseDto();
        getMarketDataResponseDto.setStatus(false);
        getMarketDataResponseDto.setError(ex.getMessage());
        getMarketDataResponseDto.setTicker(ticker);
        return getMarketDataResponseDto;
    }

    @Override
    public Double getLatestTickerLTP(String ticker) throws Exception {
        if(tickerSequenceTrackerMap.get(ticker).getLtp() != null){
            return tickerSequenceTrackerMap.get(ticker).getLtp();
        }
        else {
            UpdateSequenceTrackerThread thread = new UpdateSequenceTrackerThread(getTickerSequenceTrackerMap().get(ticker), getContractRepository(), ticker);
            thread.start();
            thread.join();
            return getTickerSequenceTrackerMap().get(ticker).getLtp();
        }
    }

    public BaseService getBaseService() {
        return baseService;
    }

    public void setBaseService(BaseService baseService) {
        this.baseService = baseService;
    }

    @Override
    public Map<String, SequenceTracker> getTickerSequenceTrackerMap() {
        return tickerSequenceTrackerMap;
    }

    public void setTickerSequenceTrackerMap(Map<String, SequenceTracker> tickerSequenceTrackerMap) {
        this.tickerSequenceTrackerMap = tickerSequenceTrackerMap;
    }

    public ContractRepository getContractRepository() {
        return contractRepository;
    }

    public void setContractRepository(ContractRepository contractRepository) {
        this.contractRepository = contractRepository;
    }

    public OrderService getOrderService() {
        return orderService;
    }

    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }
}
