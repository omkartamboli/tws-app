package com.trading.app.tradingapp.service.impl;

import com.ib.client.*;
import com.trading.app.tradingapp.dto.request.*;
import com.trading.app.tradingapp.dto.response.CreateOptionsOrderResponseDto;
import com.trading.app.tradingapp.dto.response.CreateOrderResponseDto;
import com.trading.app.tradingapp.dto.response.CreateSetOrderResponseDto;
import com.trading.app.tradingapp.dto.response.UpdateSetOrderResponseDto;
import com.trading.app.tradingapp.persistance.entity.ContractEntity;
import com.trading.app.tradingapp.persistance.entity.OrderEntity;
import com.trading.app.tradingapp.persistance.repository.ContractRepository;
import com.trading.app.tradingapp.persistance.repository.OrderRepository;
import com.trading.app.tradingapp.service.BaseService;
import com.trading.app.tradingapp.service.ContractService;
import com.trading.app.tradingapp.service.OrderService;
import com.trading.app.tradingapp.service.SystemConfigService;
import com.trading.app.tradingapp.util.JsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    public static final String OPTIONS_TYPE = "OPT";
    public static final String STRADDLE_TYPE = "BAG";
    public static final String DEFAULT_STATUS = "DefaultStatus";
    public static final String EMPTY_STRING = "";
    public static final String MID_SL_FAILED_MESSAGE = "Could not place an order because midSL filter has failed.";
    public static final String ORDER_TYPE_INVALID_MESSAGE = "Could not place an order because invalid order type is sent";
    public static final String PRIMARY_ORDER_DOES_NOT_EXIST_MESSAGE = "Could not find a primary order for an exit order, so skipping exit order creation";
    public static final String INVALID_OUTSTANDING_QUANTITY_FOR_EXIT_ORDER = "Invalid outstanding quantity for an exit order, so skipping exit order creation";
    public static final String PRIMARY_ORDER_NOT_FILLED_MESSAGE = "Primary order is not filled for a given exit order, so skipping exit order creation. Also deleting primary order = \"[{}]\"";
    public static final String ORDER_DOES_NOT_EXIST = "Order with order sequence id = \"[{}]\" and ots order type not found = \"[{}]\"";
    public static final String EXIT_ORDER_QUANTITY_CHANGED_TO_MATCH_PRIMARY_ORDER_MESSAGE = "Exit order quantity is changed to match the filled quantity on the primary order. Primary order=[{}], exit qty=[{}], changed qty=[{}]";
    public static final String CME_FUTURES_SUFFIX = "1!";
    private static final String SECURITY_TYPE = "STK";
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderServiceImpl.class);
    private static final double DEFAULT_ORDER_VALUE = 10000.00d;
    private static final String DEFAULT_TRADING_ACCOUNT_NUMBER = "DU3702853";
    @Resource
    private BaseService baseService;
    @Resource
    private OrderRepository orderRepository;
    @Resource
    private ContractRepository contractRepository;
    @Resource
    private ContractService contractService;
    @Resource
    private SystemConfigService systemConfigService;
    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public CreateSetOrderResponseDto createOrder(CreateSetOrderRequestDto createSetOrderRequestDto, String orderTrigger, String orderTriggerInterval) {
        //TODO: Add request object validation
        try {
            EClientSocket eClientSocket = getBaseService().getConnection();
            Contract contract = getBaseService().createContract(createSetOrderRequestDto.getTicker());
            List<ContractEntity> contractEntityList = getContractRepository().findBySymbol(createSetOrderRequestDto.getTicker());
            boolean useOcaHedgeOrder = !CollectionUtils.isEmpty(contractEntityList) && Boolean.TRUE.equals(contractEntityList.get(0).isUseOcaHedgeOrder());
            List<Order> bracketOrders;

            if (null != createSetOrderRequestDto.getStopLossPrice()) {
                if (useOcaHedgeOrder && ("NQ".equalsIgnoreCase(contract.symbol()) || "ES".equalsIgnoreCase(contract.symbol()))) {
                    bracketOrders = createBracketOrderWithTPWithOCAHedge(getBaseService().getNextOrderId(), createSetOrderRequestDto.getOrderType().toString(), createSetOrderRequestDto.getQuantity(), createSetOrderRequestDto.getTransactionPrice(), createSetOrderRequestDto.getTargetPrice(), createSetOrderRequestDto.getStopLossPrice(), contract, orderTrigger, orderTriggerInterval, 10);
                } else {
                    bracketOrders = createBracketOrderWithTPSLWithSLCover(getBaseService().getNextOrderId(), createSetOrderRequestDto.getOrderType().toString(), createSetOrderRequestDto.getQuantity(), createSetOrderRequestDto.getTransactionPrice(), createSetOrderRequestDto.getTargetPrice(), createSetOrderRequestDto.getStopLossPrice(), contract, orderTrigger, orderTriggerInterval);
                }
            } else if (null != createSetOrderRequestDto.getTrailingStopLossAmount()) {
                bracketOrders = createBracketOrderWithTrailingSL(getBaseService().getNextOrderId(), createSetOrderRequestDto.getOrderType().toString(), createSetOrderRequestDto.getQuantity(), createSetOrderRequestDto.getTransactionPrice(), createSetOrderRequestDto.getTrailingStopLossAmount(), contract, orderTrigger, orderTriggerInterval);
            } else {
                bracketOrders = createBracketOrderWithTP(getBaseService().getNextOrderId(), createSetOrderRequestDto.getOrderType().toString(), createSetOrderRequestDto.getQuantity(), createSetOrderRequestDto.getTransactionPrice(), createSetOrderRequestDto.getTargetPrice(), contract, orderTrigger, orderTriggerInterval);
                //bracketOrders =createBracketOrderAtMarketOpen(getBaseService().getNextOrderId(), createSetOrderRequestDto.getOrderType().toString(), createSetOrderRequestDto.getQuantity(), createSetOrderRequestDto.getTransactionPrice(), createSetOrderRequestDto.getTargetPrice(), contract, orderTrigger, orderTriggerInterval);
            }


            if (useOcaHedgeOrder && ("NQ".equalsIgnoreCase(contract.symbol()) || "ES".equalsIgnoreCase(contract.symbol()))) {
                for (Order bracketOrder : bracketOrders) {
                    if (OrderType.STP.equals(bracketOrder.orderType())) {
                        eClientSocket.placeOrder(bracketOrder.orderId(), getBaseService().createContract("M" + contract.symbol()), bracketOrder);
                    } else {
                        eClientSocket.placeOrder(bracketOrder.orderId(), contract, bracketOrder);
                    }
                }
            } else {
                for (Order bracketOrder : bracketOrders) {
                    eClientSocket.placeOrder(bracketOrder.orderId(), contract, bracketOrder);
                }
            }

            return getSuccessCreateSetOrderResult(bracketOrders.stream().map(Order::orderId).collect(Collectors.toList()));
        } catch (Exception ex) {
            LOGGER.error("Could not place an order.", ex);
            return getFailedCreateSetOrderResult(ex);
        }
    }

    @Override
    public CreateSetOrderResponseDto createSLOrder(CreateSetOrderRequestDto createSetOrderRequestDto, String orderTrigger, String orderTriggerInterval) {
        try {
            EClientSocket eClientSocket = getBaseService().getConnection();
            Contract contract = getBaseService().createContract(createSetOrderRequestDto.getTicker());
            List<Order> bracketOrders;

            if (null == createSetOrderRequestDto.getTrailingStopLossAmount()) {
                bracketOrders = createSLOrder(getBaseService().getNextOrderId(), createSetOrderRequestDto.getOrderType().toString(), createSetOrderRequestDto.getQuantity(), createSetOrderRequestDto.getTransactionPrice(), contract, orderTrigger, orderTriggerInterval);
            } else {
                bracketOrders = createTrailingSLOrder(getBaseService().getNextOrderId(), createSetOrderRequestDto.getOrderType().toString(), createSetOrderRequestDto.getQuantity(), createSetOrderRequestDto.getTransactionPrice(), createSetOrderRequestDto.getTrailingStopLossAmount(), contract, orderTrigger, orderTriggerInterval);
            }

            for (Order bracketOrder : bracketOrders) {
                eClientSocket.placeOrder(bracketOrder.orderId(), contract, bracketOrder);
            }
            return getSuccessCreateSetOrderResult(bracketOrders.stream().map(Order::orderId).collect(Collectors.toList()));
        } catch (Exception ex) {
            LOGGER.error("Could not place an order.", ex);
            return getFailedCreateSetOrderResult(ex);
        }
    }

    @Override
    public CreateOptionsOrderResponseDto createOptionsOrder(CreateOptionsOrderRequestDto createOptionsOrderRequestDto, String orderTrigger, String orderTriggerInterval, boolean isStraddle) {
        try {
            EClientSocket eClientSocket = getBaseService().getConnection();
            Contract contract;
            if (isStraddle) {
                contract = getBaseService().createOptionsStraddleContract(createOptionsOrderRequestDto.getTicker(), createOptionsOrderRequestDto.getStrike(), createOptionsOrderRequestDto.getDateYYYYMMDD(), createOptionsOrderRequestDto.getOrderType());
            } else {
                contract = getBaseService().createOptionsContract(createOptionsOrderRequestDto.getTicker(), createOptionsOrderRequestDto.getStrike(), createOptionsOrderRequestDto.getDateYYYYMMDD(), createOptionsOrderRequestDto.getOptionType().toString());
            }

            List<Order> bracketOrders = new ArrayList<>();
            if (createOptionsOrderRequestDto.getStopLossPrice() == null) {
                bracketOrders.addAll(createBracketOrderWithTP(getBaseService().getNextOrderId(), createOptionsOrderRequestDto.getOrderType().toString(), createOptionsOrderRequestDto.getQuantity(), createOptionsOrderRequestDto.getTransactionPrice(), createOptionsOrderRequestDto.getTargetPrice(), contract, orderTrigger, orderTriggerInterval));
            } else {
                bracketOrders.addAll(createBracketOrderWithTPSL(getBaseService().getNextOrderId(), createOptionsOrderRequestDto.getOrderType().toString(), createOptionsOrderRequestDto.getQuantity(), createOptionsOrderRequestDto.getTransactionPrice(), createOptionsOrderRequestDto.getTargetPrice(), createOptionsOrderRequestDto.getStopLossPrice(), contract, orderTrigger, orderTriggerInterval));
            }

            for (Order bracketOrder : bracketOrders) {
                eClientSocket.placeOrder(bracketOrder.orderId(), contract, bracketOrder);
            }
            return getSuccessCreateOptionsOrderResult(bracketOrders.stream().map(Order::orderId).collect(Collectors.toList()));
        } catch (Exception ex) {
            LOGGER.error("Could not place an options order.", ex);
            return getFailedCreateOptionsOrderResult(ex);
        }
    }

    @Override
    public UpdateSetOrderResponseDto updateOrder(UpdateSetOrderRequestDto updateSetOrderRequestDto) {
        //TODO: Add request object validation
        try {

            List<OrderEntity> orders = getOrderRepository().findByOrderId(updateSetOrderRequestDto.getOrderId());

            if (null == orders || orders.isEmpty()) {
                LOGGER.error("Could not find order with OrderId=[{}] for update. Could not proceed update operation.", updateSetOrderRequestDto.getOrderId());
                return getFailedUpdateSetOrderResult(updateSetOrderRequestDto.getOrderId(), "Could not find order with OrderId=[" + updateSetOrderRequestDto.getOrderId() + "] for update. Could not proceed update operation.");
            } else if (orders.size() > 1) {
                LOGGER.error("Multiple orders found with OrderId=[{}] for update. Could not proceed update operation.", updateSetOrderRequestDto.getOrderId());
                return getFailedUpdateSetOrderResult(updateSetOrderRequestDto.getOrderId(), "Multiple orders found with OrderId=[" + updateSetOrderRequestDto.getOrderId() + "] for update. Could not proceed update operation.");
            } else {

                OrderEntity orderToBeUpdated = orders.get(0);

                EClientSocket eClientSocket = getBaseService().getConnection();

                Contract contract = Boolean.TRUE.equals(orderToBeUpdated.getOptionsOrder()) ? getBaseService().createOptionsContract(orderToBeUpdated.getSymbol(), orderToBeUpdated.getOptionStrikePrice(), orderToBeUpdated.getOptionExpiryDate(), orderToBeUpdated.getOptionType()) : getBaseService().createContract(orderToBeUpdated.getSymbol());

                Order updateOrder = updateOrder(orderToBeUpdated.getOrderId(), updateSetOrderRequestDto.getParentOrderId(), orderToBeUpdated.getOrderAction(), updateSetOrderRequestDto.getQuantity(), updateSetOrderRequestDto.getTargetPrice(), updateSetOrderRequestDto.getTriggerPrice(), contract, updateSetOrderRequestDto.getOrderType(), orderToBeUpdated.getOrderTrigger(), orderToBeUpdated.getOrderTriggerInterval());

                eClientSocket.placeOrder(updateOrder.orderId(), contract, updateOrder);

                return getSuccessUpdateSetOrderResult(updateSetOrderRequestDto.getOrderId());
            }
        } catch (Exception ex) {
            LOGGER.error("Could not place a update order.", ex);
            return getFailedUpdateSetOrderResult(updateSetOrderRequestDto.getOrderId(), ex.getMessage());
        }
    }

    @Override
    public UpdateSetOrderResponseDto cancelOrder(UpdateSetOrderRequestDto updateSetOrderRequestDto) {
        //TODO: Add request object validation
        try {
            LOGGER.info("Cancelling order with OrderId=[{}]", updateSetOrderRequestDto.getOrderId());
            getBaseService().getConnection().cancelOrder(updateSetOrderRequestDto.getOrderId());
            return getSuccessUpdateSetOrderResult(updateSetOrderRequestDto.getOrderId());

        } catch (Exception ex) {
            LOGGER.error("Could not place a cancel order.", ex);
            return getFailedUpdateSetOrderResult(updateSetOrderRequestDto.getOrderId(), ex.getMessage());
        }
    }

    @Override
    public void cancelOrder(int orderId) {
        try {
            LOGGER.info("Cancelling order with OrderId=[{}], as the filled qty is less than the original qty", orderId);
            getBaseService().getConnection().cancelOrder(orderId);
        } catch (Exception ex) {
            LOGGER.error("Could not place a cancel order.", ex);
        }
    }

    @Override
    public CreateOrderResponseDto createOrder(CreateLtpOrderRequestDto createLtpOrderRequestDto, String orderTrigger, String orderTriggerInterval) {
        //TODO: Add request object validation
        try {
            EClientSocket eClientSocket = getBaseService().getConnection();
            Contract contract = getBaseService().createContract(createLtpOrderRequestDto.getTicker());
            double contractLtp = getBaseService().getMarketDataForContract(contract, false).getLtp();
            double targetPrice = com.trading.app.tradingapp.dto.OrderType.BUY.equals(createLtpOrderRequestDto.getOrderType()) ? contractLtp + createLtpOrderRequestDto.getTargetPriceOffset() : contractLtp - createLtpOrderRequestDto.getTargetPriceOffset();

            if (createLtpOrderRequestDto.getStopPriceOffset() != null) {
                // create stop loss order as well
            }
            List<Order> bracketOrders = createBracketOrderWithTP(getBaseService().getNextOrderId(), createLtpOrderRequestDto.getOrderType().toString(), createLtpOrderRequestDto.getQuantity(), contractLtp, targetPrice, contract, orderTrigger, orderTriggerInterval);

            for (Order bracketOrder : bracketOrders) {
                eClientSocket.placeOrder(bracketOrder.orderId(), contract, bracketOrder);
            }
            return getSuccessCreateLtpOrderResult(bracketOrders.stream().map(Order::orderId).collect(Collectors.toList()));
        } catch (Exception ex) {
            LOGGER.error("Could not place an order.", ex);
            return getFailedCreateLtpOrderResult(ex);
        }
    }

    @Override
    public CreateSetOrderResponseDto createOrder(CreateDivergenceTriggerOrderRequestDto createDivergenceTriggerOrderRequestDto, String orderTrigger) {
        LOGGER.info(JsonSerializer.serialize(createDivergenceTriggerOrderRequestDto));
        LOGGER.info("RSI = {}", createDivergenceTriggerOrderRequestDto.getRsi());
        if (Boolean.TRUE.equals(isDivergenceOrderEnabled())) {
            if (qualifyRsiFilter(createDivergenceTriggerOrderRequestDto.getRsi(), createDivergenceTriggerOrderRequestDto.getDivergenceDiff())) {
                if (qualifyOutOfHoursOrderFilter()) {
                    try {
                        EClientSocket eClientSocket = getBaseService().getConnection();
                        Contract contract = getBaseService().createContract(createDivergenceTriggerOrderRequestDto.getTicker());

                        List<Order> bracketOrders;


                        String divergenceOrderType = getDivergenceOrderType();
                        double tradePrice = createDivergenceTriggerOrderRequestDto.getClose();
                        com.trading.app.tradingapp.dto.OrderType orderType = createDivergenceTriggerOrderRequestDto.getDivergenceDiff() > 0 ? com.trading.app.tradingapp.dto.OrderType.BUY : com.trading.app.tradingapp.dto.OrderType.SELL;

                        if ("TPSL".equalsIgnoreCase(divergenceOrderType)) {

                            double targetPricePercentage = (null == createDivergenceTriggerOrderRequestDto.getTpOffsetPrice() ? 0.25d : createDivergenceTriggerOrderRequestDto.getTpOffsetPrice());
                            double targetPriceOffset = tradePrice * targetPricePercentage / 100.0d * (createDivergenceTriggerOrderRequestDto.getDivergenceDiff() > 0 ? 1 : -1);
                            double targetPrice = tradePrice + targetPriceOffset;
                            double stopLossPrice = tradePrice - (targetPriceOffset);
                            bracketOrders = createBracketOrderWithTPSL(getBaseService().getNextOrderId(), orderType.toString(), getQuantity(tradePrice), tradePrice, targetPrice, stopLossPrice, contract, orderTrigger, createDivergenceTriggerOrderRequestDto.getInterval());

                        } else if ("TRSL".equalsIgnoreCase(divergenceOrderType)) {

                            double trailingSLAmount = tradePrice * 0.01;
                            bracketOrders = createBracketOrderWithTrailingSL(getBaseService().getNextOrderId(), orderType.toString(), getQuantity(tradePrice), tradePrice, trailingSLAmount, contract, orderTrigger, createDivergenceTriggerOrderRequestDto.getInterval());

                        } else {
                            LOGGER.info("Invalid \"divergence.order.type\" configuration in properties file.");
                            return getFailedCreateSetOrderResult(new Exception("Invalid \"divergence.order.type\" configuration in properties file."));
                        }

                        for (Order bracketOrder : bracketOrders) {
                            eClientSocket.placeOrder(bracketOrder.orderId(), contract, bracketOrder);
                        }
                        return getSuccessCreateSetOrderResult(bracketOrders.stream().map(Order::orderId).collect(Collectors.toList()));
                    } catch (Exception ex) {
                        LOGGER.error("Could not place an order.", ex);
                        return getFailedCreateSetOrderResult(ex);
                    }
                } else {
                    LOGGER.info("Divergence order did not qualify out of hours order filter");
                    return getFailedCreateSetOrderResult(new Exception("Divergence order did not qualify out of hours order filter"));
                }
            } else {
                LOGGER.info("Divergence order did not qualify RSI filter");
                return getFailedCreateSetOrderResult(new Exception("Divergence order did not qualify RSI filter"));
            }
        } else {
            LOGGER.info("Divergence order is not enabled.");
            return getFailedCreateSetOrderResult(new Exception("Divergence order is not enabled."));
        }
    }

    private boolean qualifyRsiFilter(Double rsiValue, Integer divergenceDiff) {
        if (Boolean.TRUE.equals(isDivergenceOrderRsiFilterEnabled())) {
            if (rsiValue == null || divergenceDiff == null || (divergenceDiff > 0 && rsiValue > 30) || (divergenceDiff < 0 && rsiValue < 70)) {
                return false;
            }
        }
        return true;
    }

    private boolean qualifyOutOfHoursOrderFilter() {
        if (Boolean.FALSE.equals(isOutOfHoursOrderEnabled())) {
            Date date = new Date();   // given date
            Calendar calendar = GregorianCalendar.getInstance(); // creates a new calendar instance
            calendar.setTime(date);   // assigns calendar to given date
            int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
            int currentMinute = calendar.get(Calendar.MINUTE);

            int currentMinuteOfTheDay = currentHour * 60 + currentMinute;
            int startMinuteOfTheDay = getTradingStartHour() * 60 + getTradingStartMinute();
            int endMinuteOfTheDay = getTradingEndHour() * 60 + getTradingEndMinute();

            return currentMinuteOfTheDay >= startMinuteOfTheDay && currentMinuteOfTheDay <= endMinuteOfTheDay;
        }
        return true;
    }

    @Override
    public CreateSetOrderResponseDto createOrder(CreatePivotBreakOrderRequestDto createPivotBreakOrderRequestDto, String orderTrigger) {
        LOGGER.info(JsonSerializer.serialize(createPivotBreakOrderRequestDto));

        try {
            EClientSocket eClientSocket = getBaseService().getConnection();
            Contract contract = getBaseService().createContract(createPivotBreakOrderRequestDto.getTicker());
            double tradePrice = createPivotBreakOrderRequestDto.getClose();
            double targetPricePercentage = (null == createPivotBreakOrderRequestDto.getTpOffsetPrice() ? 0.5d : createPivotBreakOrderRequestDto.getTpOffsetPrice());
            double targetPriceOffset = tradePrice * targetPricePercentage / 100.0d * (createPivotBreakOrderRequestDto.getPivotbreakval() > 0 ? 1 : -1);
            double targetPrice = tradePrice + targetPriceOffset;
            double stoplossPrice = tradePrice - (targetPriceOffset / 2.0d);

            com.trading.app.tradingapp.dto.OrderType orderType = createPivotBreakOrderRequestDto.getPivotbreakval() > 0 ? com.trading.app.tradingapp.dto.OrderType.BUY : com.trading.app.tradingapp.dto.OrderType.SELL;

            List<Order> bracketOrders = createBracketOrderWithTPSL(getBaseService().getNextOrderId(), orderType.toString(), getQuantity(tradePrice), tradePrice, targetPrice, stoplossPrice, contract, orderTrigger, createPivotBreakOrderRequestDto.getInterval());

            for (Order bracketOrder : bracketOrders) {
                eClientSocket.placeOrder(bracketOrder.orderId(), contract, bracketOrder);
            }
            return getSuccessCreateSetOrderResult(bracketOrders.stream().map(Order::orderId).collect(Collectors.toList()));
        } catch (Exception ex) {
            LOGGER.error("Could not place an order.", ex);
            return getFailedCreateSetOrderResult(ex);
        }
    }

    @Override
    public CreateSetOrderResponseDto createOrder(RKLTradeOrderRequestDto rklTradeOrderRequestDto, String orderTrigger) {
        LOGGER.info(JsonSerializer.serialize(rklTradeOrderRequestDto));

        try {

            String ticker = rklTradeOrderRequestDto.getTicker();
            if ("ES1!".equalsIgnoreCase(ticker) || "MES1!".equalsIgnoreCase(ticker)) {
                ticker = "MES";
            }
            if ("NQ1!".equalsIgnoreCase(ticker) || "MNQ1!".equalsIgnoreCase(ticker)) {
                ticker = "MNQ";
            }
            Contract contract = getBaseService().createContract(ticker);
            double tradePrice = roundOff(rklTradeOrderRequestDto.getEntry(), rklTradeOrderRequestDto.getTicker());
            double targetPrice1 = roundOff(rklTradeOrderRequestDto.getTp1(), rklTradeOrderRequestDto.getTicker());
            double targetPrice2 = roundOff(rklTradeOrderRequestDto.getTp2(), rklTradeOrderRequestDto.getTicker());
            double stopLossPrice = roundOff(rklTradeOrderRequestDto.getSl(), rklTradeOrderRequestDto.getTicker());
            int quantity = rklTradeOrderRequestDto.getQty();

            if (!checkIfTradeHasCrossedTheSLLimit(tradePrice, stopLossPrice, ticker)) {
                LOGGER.error(MID_SL_FAILED_MESSAGE);
                return getFailedCreateSetOrderResult(MID_SL_FAILED_MESSAGE);
            }

            EClientSocket eClientSocket = getBaseService().getConnection();

            com.trading.app.tradingapp.dto.OrderType orderType = targetPrice1 > tradePrice ? com.trading.app.tradingapp.dto.OrderType.BUY : com.trading.app.tradingapp.dto.OrderType.SELL;

            List<Order> bracketOrders = createBracketOrderWith2TPSL(getBaseService().getNextOrderId(), orderType.toString(), quantity, tradePrice, targetPrice1, targetPrice2, stopLossPrice, contract, orderTrigger, rklTradeOrderRequestDto.getInterval());

            for (Order bracketOrder : bracketOrders) {
                eClientSocket.placeOrder(bracketOrder.orderId(), contract, bracketOrder);
            }
            return getSuccessCreateSetOrderResult(bracketOrders.stream().map(Order::orderId).collect(Collectors.toList()));
        } catch (Exception ex) {
            LOGGER.error("Could not place an order.", ex);
            return getFailedCreateSetOrderResult(ex);
        }
    }

    @Override
    public int findOutstandingQtyForTickerWithSpecificOrderTrigger(String ticker, String orderTrigger, String interval){
        Integer buyQty = orderRepository.getTotalBuyQtyForTickerWithSpecificOrderTrigger(ticker, orderTrigger, interval);
        Integer sellQty = orderRepository.getTotalSellQtyForTickerWithSpecificOrderTrigger(ticker, orderTrigger, interval);

        int buyQtyInt = null == buyQty ? 0 : buyQty;
        int sellQtyInt = null == sellQty ? 0 : sellQty;
        return (buyQtyInt - sellQtyInt);
    }

    @Override
    public CreateSetOrderResponseDto createOTMacdOrder(OTMacdOrderRequestDto otMacdOrderRequestDto, String orderTrigger) throws Exception{
        // Date start = new Date();

        if (!"BUY".equalsIgnoreCase(otMacdOrderRequestDto.getOrderType()) && !"SELL".equalsIgnoreCase(otMacdOrderRequestDto.getOrderType())) {
            LOGGER.error(ORDER_TYPE_INVALID_MESSAGE);
            return getFailedCreateSetOrderResult(ORDER_TYPE_INVALID_MESSAGE);
        }

        String ticker = otMacdOrderRequestDto.getTicker().replace(CME_FUTURES_SUFFIX, EMPTY_STRING);

        boolean isExitOrder = null != otMacdOrderRequestDto.getCurrentSequenceId() && otMacdOrderRequestDto.getCurrentSequenceId() == -1;
//        boolean isLongExitOrder = isExitOrder && "SELL".equalsIgnoreCase(otMacdOrderRequestDto.getOtsOrderType());
//        boolean isShortExitOrder = isExitOrder && "BUY".equalsIgnoreCase(otMacdOrderRequestDto.getOtsOrderType());

        Double outstandingQty = (double) Math.abs(findOutstandingQtyForTickerWithSpecificOrderTrigger(ticker, orderTrigger, otMacdOrderRequestDto.getInterval()));

        if (isExitOrder) {
            if (outstandingQty > 0) {
                // We want to cancel all unfilled orders when exit order is received, to avoid quantity ambiguity
                List<OrderEntity> unFilledOrderList = getOrderRepository().findUnFilledOrdersWithoutSLOrder(ticker, orderTrigger, otMacdOrderRequestDto.getInterval());
                if (!CollectionUtils.isEmpty(unFilledOrderList)) {
                    unFilledOrderList.forEach(orderEntity -> cancelOrder(orderEntity.getOrderId()));
                }

                // The exit order should make outstanding quantity zero
                otMacdOrderRequestDto.setQuantity(Math.abs(outstandingQty.intValue()));
            } else {
                LOGGER.error(INVALID_OUTSTANDING_QUANTITY_FOR_EXIT_ORDER + ". \n Outstanding qty = [{}] \n Request json = [{}]",outstandingQty, JsonSerializer.serialize(otMacdOrderRequestDto));
                return getFailedCreateSetOrderResult(INVALID_OUTSTANDING_QUANTITY_FOR_EXIT_ORDER);
            }
        }

        try {
            boolean enableOrth = !otMacdOrderRequestDto.getTicker().contains(CME_FUTURES_SUFFIX);
            double tradePrice = roundOff(otMacdOrderRequestDto.getEntry(), otMacdOrderRequestDto.getTicker());

            try {
                // For Exit orders, set tradePrice as LTP if latest LTP is available for ticker in DB, for better execution success.
                if(isExitOrder) {
                    double latestLTP = getLtpForTicker(ticker);
                    // Set tradePrice as LTP if latest LTP is available for ticker in DB.
                    if (("BUY".equalsIgnoreCase(otMacdOrderRequestDto.getOrderType()) && latestLTP > tradePrice) || ("SELL".equalsIgnoreCase(otMacdOrderRequestDto.getOrderType()) && latestLTP < tradePrice)) {
                        tradePrice = latestLTP;
                        LOGGER.info("Updating transaction price for order [{}] with latest LTP [{}]", otMacdOrderRequestDto.getSequenceId(), latestLTP);
                    }
                }
            } catch (Exception ex) {
                //LOGGER.error(ex.getMessage(), ex);
                LOGGER.error("LTP could not be used as LTP is not available, using trade price on order to set limit order. Exception = [{}]", ex.getMessage());
            }

            Contract contract = getBaseService().createContract(ticker);
            int quantity = otMacdOrderRequestDto.getQuantity();
            EClientSocket eClientSocket = getBaseService().getConnection();

            List<Order> orderList;
            int orderId = getBaseService().getNextOrderId();

            // Synchronizing the order creation block to avoid creating orders with duplicate order ID
            synchronized (this) {
                try {
                    orderList = createMacdOrders(eClientSocket, contract, getBaseService().getNextOrderId(), otMacdOrderRequestDto.getOrderType(), quantity, otMacdOrderRequestDto.getSlQuantity(), outstandingQty, tradePrice, otMacdOrderRequestDto.getSlForAvg(), enableOrth, otMacdOrderRequestDto.getTradeStartSequenceId(), otMacdOrderRequestDto.getCurrentSequenceId(), ticker, orderTrigger, otMacdOrderRequestDto.getInterval());
                } catch (Exception ex) {
                    LOGGER.error("Could not place a OT-MACD order with order id = [{}].", orderId, ex);
                    return getFailedCreateSetOrderResult(ex);
                }
            }

            // Date end = new Date();

            // persisting the order after order is sent to TWS, to avoid delay in sending orders to exchange
            if(!orderList.isEmpty()) {
                persistMacdOrder(orderList.get(0), contract, orderTrigger, otMacdOrderRequestDto.getInterval(), false, null, null, null, otMacdOrderRequestDto.getSequenceId(), otMacdOrderRequestDto.getOtsOrderType(), otMacdOrderRequestDto.getTradeStartSequenceId(), null);
                if(orderList.size() > 1) {
                    persistMacdOrder(orderList.get(1), contract, orderTrigger, otMacdOrderRequestDto.getInterval(), false, null, null, null, otMacdOrderRequestDto.getTradeStartSequenceId().toString(), otMacdOrderRequestDto.getOtsOrderType(), otMacdOrderRequestDto.getTradeStartSequenceId(), otMacdOrderRequestDto.getTradeStartSequenceId());
                }
            }

            // LOGGER.info("OTS-Create Order API execution time is [{}] ms", end.getTime() - start.getTime());
//            int counter = 0;
//            int retryCount = isExitOrder ? 30 : 5;
//            while (counter < retryCount) {
//                try {
//
//                    // Clear entities fetched previously
//                    entityManager.clear();
//
//                    OrderEntity orderEntity = getSingleOrderByOrderId(orderId);
//                    int filledQty = orderEntity.getFilled() == null ? 0 : orderEntity.getFilled().intValue();
//                    if (filledQty == otMacdOrderRequestDto.getQuantity()) {
//                        if (counter > 0) {
//                            LOGGER.info("Filled order in LTP reset retry no [{}]", counter);
//                        }
//                        return getSuccessCreateSetOrderResult(orderList.stream().map(Order::orderId).collect(Collectors.toList()));
//                    } else {
//                        double latestLTP = getLtpForTicker(ticker);
//
//                        // Update transaction price only if LTP have moved away from order transaction price unfavourably
//                        if( ("BUY".equalsIgnoreCase(orderEntity.getOrderAction()) && latestLTP > orderEntity.getTransactionPrice()) || ("SELL".equalsIgnoreCase(orderEntity.getOrderAction()) && latestLTP < orderEntity.getTransactionPrice())) {
//                            Order updateOrder = updateOrderTransactionPrice(orderEntity, latestLTP);
//                            eClientSocket.placeOrder(updateOrder.orderId(), contract, updateOrder);
//                            LOGGER.info("Loop: Updating transaction price for order [{}] with latest LTP [{}]", updateOrder.orderId(), latestLTP);
//                        }
//                        counter++;
//                        Thread.sleep(5000);
//                    }
//                } catch (Exception ex) {
//                    LOGGER.warn("Exception while updating order transaction price for order [{}] in counter loop. Exception : [{}]", orderId, ex.getMessage());
//                    Thread.sleep(5000);
//                    counter++;
//                }
//            }
            return getSuccessCreateSetOrderResult(orderList.stream().map(Order::orderId).collect(Collectors.toList()));
        } catch (Exception ex) {
            LOGGER.error("Could not place a OT-MACD order.", ex);
            return getFailedCreateSetOrderResult(ex);
        }
    }

    @Override
    public CreateSetOrderResponseDto createOTSOrder(OtStarTradeOrderRequestDto otStarTradeOrderRequestDto, String orderTrigger) {
        // LOGGER.info(JsonSerializer.serialize(otStarTradeOrderRequestDto));
        // Date start = new Date();

        if (!"BUY".equalsIgnoreCase(otStarTradeOrderRequestDto.getOrderType()) && !"SELL".equalsIgnoreCase(otStarTradeOrderRequestDto.getOrderType())) {
            LOGGER.error(ORDER_TYPE_INVALID_MESSAGE);
            return getFailedCreateSetOrderResult(ORDER_TYPE_INVALID_MESSAGE);
        }

        // remove continuous futures suffix (1!) from ticker
        String ticker = otStarTradeOrderRequestDto.getTicker().replace(CME_FUTURES_SUFFIX, EMPTY_STRING);

        boolean isLongExitOrder = "LX".equals(otStarTradeOrderRequestDto.getOtsOrderType());
        boolean isShortExitOrder = "SX".equals(otStarTradeOrderRequestDto.getOtsOrderType());
        boolean isExitOrder = isLongExitOrder || isShortExitOrder;

        if (isExitOrder) {

            int outstandingQty = findOutstandingQtyForTickerWithSpecificOrderTrigger(ticker, orderTrigger, otStarTradeOrderRequestDto.getInterval());

            if (isLongExitOrder && outstandingQty > 0 || isShortExitOrder && outstandingQty < 0) {
                // The exit order should make outstanding quantity zero
                otStarTradeOrderRequestDto.setQuantity(Math.abs(outstandingQty));

                // Once we have set the exit order quantity to outstanding quantity, we want to cancel all unfilled orders to avoid quantity ambiguity
                List<OrderEntity> unFilledOrderList = getOrderRepository().findUnFilledOrders(ticker, orderTrigger, otStarTradeOrderRequestDto.getInterval());
                if (!CollectionUtils.isEmpty(unFilledOrderList)) {
                    unFilledOrderList.forEach(orderEntity -> cancelOrder(orderEntity.getOrderId()));
                }
                if (isPessimisticOrderExitPriceEnabled()) {
                    otStarTradeOrderRequestDto.setEntry(isLongExitOrder ? otStarTradeOrderRequestDto.getLow() : otStarTradeOrderRequestDto.getHigh());
                }
            } else {

                LOGGER.error(INVALID_OUTSTANDING_QUANTITY_FOR_EXIT_ORDER + ". \n Outstanding qty = [{}] \n Request json = [{}]",outstandingQty, JsonSerializer.serialize(otStarTradeOrderRequestDto));
                return getFailedCreateSetOrderResult(INVALID_OUTSTANDING_QUANTITY_FOR_EXIT_ORDER);
            }
        }

        try {
            boolean enableOrth = !otStarTradeOrderRequestDto.getTicker().contains(CME_FUTURES_SUFFIX);
            double tradePrice = roundOff(otStarTradeOrderRequestDto.getEntry(), otStarTradeOrderRequestDto.getTicker());

            try {
                double latestLTP = getLtpForTicker(ticker);
                // Set tradePrice as LTP if latest LTP is available for ticker in DB.
                if( ("BUY".equalsIgnoreCase(otStarTradeOrderRequestDto.getOrderType()) && latestLTP > tradePrice) || ("SELL".equalsIgnoreCase(otStarTradeOrderRequestDto.getOrderType()) && latestLTP < tradePrice)) {
                    tradePrice = latestLTP;
                    LOGGER.info("Updating transaction price for order [{}] with latest LTP [{}]", otStarTradeOrderRequestDto.getSequenceId(), latestLTP);
                }
            } catch (Exception ex) {
                //LOGGER.error(ex.getMessage(), ex);
                LOGGER.error("LTP could not be used as LTP is not available, using trade price on order to set limit order. Exception = [{}]", ex.getMessage());
            }

            Contract contract = getBaseService().createContract(ticker);
            int quantity = otStarTradeOrderRequestDto.getQuantity();
            EClientSocket eClientSocket = getBaseService().getConnection();

            Order order = null;
            int orderId = -1;

            // Synchronizing the order creation block to avoid creating orders with duplicate order ID
            synchronized (this) {
                try {
                    order = createSingleOrder(getBaseService().getNextOrderId(), otStarTradeOrderRequestDto.getOrderType(), quantity, tradePrice, enableOrth);
                    orderId = order.orderId();
                    eClientSocket.placeOrder(order.orderId(), contract, order);
                } catch (Exception ex) {
                    LOGGER.error("Could not place a OT-Star order with order id = [{}].", orderId, ex);
                    return getFailedCreateSetOrderResult(ex);
                }
            }

            // Date end = new Date();

            // persisting the order after order is sent to TWS, to avoid delay in sending orders to exchange
            persistOrder(order, contract, orderTrigger, otStarTradeOrderRequestDto.getInterval(), true, otStarTradeOrderRequestDto.getSequenceId(), otStarTradeOrderRequestDto.getOtsOrderType());

            // LOGGER.info("OTS-Create Order API execution time is [{}] ms", end.getTime() - start.getTime());

            int counter = 0;
            int retryCount = isExitOrder ? 30 : 5;
            while (counter < retryCount) {
                try {

                    // Clear entities fetched previously
                    entityManager.clear();

                    OrderEntity orderEntity = getSingleOrderByOrderId(orderId);
                    int filledQty = orderEntity.getFilled() == null ? 0 : orderEntity.getFilled().intValue();
                    if (filledQty == otStarTradeOrderRequestDto.getQuantity()) {
                        if (counter > 0) {
                            LOGGER.info("Filled order in LTP reset retry no [{}]", counter);
                        }
                        return getSuccessCreateSetOrderResult(List.of(order.orderId()));
                    } else {
                        double latestLTP = getLtpForTicker(ticker);

                        // Update transaction price only if LTP have moved away from order transaction price unfavourably
                        if( ("BUY".equalsIgnoreCase(orderEntity.getOrderAction()) && latestLTP > orderEntity.getTransactionPrice()) || ("SELL".equalsIgnoreCase(orderEntity.getOrderAction()) && latestLTP < orderEntity.getTransactionPrice())) {
                            Order updateOrder = updateOrderTransactionPrice(orderEntity, latestLTP);
                            eClientSocket.placeOrder(updateOrder.orderId(), contract, updateOrder);
                            LOGGER.info("Loop: Updating transaction price for order [{}] with latest LTP [{}]", updateOrder.orderId(), latestLTP);
                        }
                        counter++;
                        Thread.sleep(5000);
                    }
                } catch (Exception ex) {
                    LOGGER.warn("Exception while updating order transaction price for order [{}] in counter loop. Exception : [{}]", orderId, ex.getMessage());
                    Thread.sleep(5000);
                    counter++;
                }
            }
            return getSuccessCreateSetOrderResult(List.of(order.orderId()));
        } catch (Exception ex) {
            LOGGER.error("Could not place a OT-Star order.", ex);
            return getFailedCreateSetOrderResult(ex);
        }
    }

    private OrderEntity getSingleOrderByOrderId(int orderId) throws Exception {
        List<OrderEntity> orderEntityList = getOrderRepository().findByOrderId(orderId);
        if (CollectionUtils.isEmpty(orderEntityList)) {
            throw new Exception();
        }
        return orderEntityList.get(0);
    }

    @Override
    public OrderEntity getSingleOrderBySlCheckSequenceIdAndTrigger(Long slCheckSequenceId, String trigger) {
        List<OrderEntity> orderEntityList = getOrderRepository().findBySlCheckSequenceIdAndTrigger(slCheckSequenceId, trigger);
        if (CollectionUtils.isEmpty(orderEntityList)) {
            return null;
        }
        return orderEntityList.get(0);
    }

    private int getFilledQtyForOrderBySequenceIdAndOtsOrderType(String sequenceId, String otsOrderType) throws Exception {
        List<OrderEntity> orderEntityList = getOrderRepository().findBySequenceIdAndOtsOrderType(sequenceId, otsOrderType);
        if (CollectionUtils.isEmpty(orderEntityList)) {
            throw new Exception(ORDER_DOES_NOT_EXIST.replace("[{}]", sequenceId).replace("[{}]", otsOrderType));
        } else {
            //int twsOrderId = orderEntityList.get(0).getOrderId();
            return Objects.isNull(orderEntityList.get(0).getFilled()) ? 0 : orderEntityList.get(0).getFilled().intValue();
        }
    }

    private OrderEntity getOrderQtyForOrderBySequenceIdAndOtsOrderType(String sequenceId, String otsOrderType) throws Exception {
        List<OrderEntity> orderEntityList = getOrderRepository().findBySequenceIdAndOtsOrderType(sequenceId, otsOrderType);
        if (CollectionUtils.isEmpty(orderEntityList)) {
            throw new Exception(ORDER_DOES_NOT_EXIST.replace("[{}]", sequenceId).replace("[{}]", otsOrderType));
        } else {
            //int twsOrderId = orderEntityList.get(0).getOrderId();
            return orderEntityList.get(0);
        }
    }

//    private synchronized int getNextOrderIdForOtStarTrade() throws Exception {
//        synchronized (this) {
//            return getBaseService().getNextOrderId();
//        }
//    }


    private boolean checkIfTradeHasCrossedTheSLLimit(double entry, double sl, String ticker) {
        if (ticker == null || EMPTY_STRING.equals(ticker)) {
            return false;
        }
        List<ContractEntity> contractEntityList = getContractRepository().findBySymbol(ticker.replace(CME_FUTURES_SUFFIX, EMPTY_STRING));
        if (!CollectionUtils.isEmpty(contractEntityList)) {
            ContractEntity contractEntity = contractEntityList.get(0);

            Calendar oneMinuteBefore = Calendar.getInstance();
            oneMinuteBefore.add(Calendar.MINUTE, -1);

            // Check if LTP is modified recently in last 1 minute
            if (contractEntity.getTickerAskBidLtpValuesUpdateTimestamp() != null && contractEntity.getTickerAskBidLtpValuesUpdateTimestamp().getTime() > oneMinuteBefore.getTime().getTime()) {
                double midSL = (entry + sl) / 2;
                boolean midSLFilter = entry > sl ? contractEntity.getLtp() > midSL : contractEntity.getLtp() < midSL;

                if (!midSLFilter) {
                    LOGGER.info("Could not take trade because LTP has moved beyond mid SL");
                }

                return midSLFilter;
            } else {
                LOGGER.info("Could not take trade because security LTP is not updated in last 1 minute");
                return false;
            }
        } else {
            LOGGER.info("Could not take trade because security is not defined in local DB");
            return false;
        }
    }

    private double roundOff(double doubleVal, String ticker) {
        double roundedValue = ((double) Math.round(doubleVal * 100.0)) / 100.0;
        if ("ES1!".equalsIgnoreCase(ticker) || "MES1!".equalsIgnoreCase(ticker) || "NQ1!".equalsIgnoreCase(ticker) || "MNQ1!".equalsIgnoreCase(ticker) || "ES".equalsIgnoreCase(ticker) || "MES".equalsIgnoreCase(ticker) || "NQ".equalsIgnoreCase(ticker) || "MNQ".equalsIgnoreCase(ticker)) {
            roundedValue = ((double) Math.round(doubleVal * 4.0)) / 4.0;
        }
        return roundedValue;
    }

    private CreateSetOrderResponseDto getSuccessCreateSetOrderResult(List<Integer> orderIds) {
        CreateSetOrderResponseDto createSetOrderResponseDto = new CreateSetOrderResponseDto();
        createSetOrderResponseDto.setStatus(true);
        createSetOrderResponseDto.setParentOrderId(orderIds.get(0));
        createSetOrderResponseDto.setTpOrderId(orderIds.size() >= 2 ? orderIds.get(1) : null);
        createSetOrderResponseDto.setSlOrderId(orderIds.size() >= 3 ? orderIds.get(2) : null);
        return createSetOrderResponseDto;
    }

    private CreateOptionsOrderResponseDto getSuccessCreateOptionsOrderResult(List<Integer> orderIds) {
        CreateOptionsOrderResponseDto createOptionsOrderResponseDto = new CreateOptionsOrderResponseDto();
        createOptionsOrderResponseDto.setStatus(true);
        createOptionsOrderResponseDto.setParentOrderId(orderIds.get(0));
        createOptionsOrderResponseDto.setTpOrderId(orderIds.size() >= 2 ? orderIds.get(1) : null);
        createOptionsOrderResponseDto.setSlOrderId(orderIds.size() >= 3 ? orderIds.get(2) : null);
        return createOptionsOrderResponseDto;
    }

    private UpdateSetOrderResponseDto getSuccessUpdateSetOrderResult(Integer orderId) {
        UpdateSetOrderResponseDto updateSetOrderResponseDto = new UpdateSetOrderResponseDto();
        updateSetOrderResponseDto.setStatus(true);
        updateSetOrderResponseDto.setOrderId(orderId);
        return updateSetOrderResponseDto;
    }

    private UpdateSetOrderResponseDto getFailedUpdateSetOrderResult(Integer orderId, String exceptionMessage) {
        UpdateSetOrderResponseDto updateSetOrderResponseDto = new UpdateSetOrderResponseDto();
        updateSetOrderResponseDto.setStatus(false);
        updateSetOrderResponseDto.setOrderId(orderId);
        updateSetOrderResponseDto.setError(exceptionMessage);
        return updateSetOrderResponseDto;
    }

    private CreateSetOrderResponseDto getFailedCreateSetOrderResult(Exception ex) {
        CreateSetOrderResponseDto createSetOrderResponseDto = new CreateSetOrderResponseDto();
        createSetOrderResponseDto.setStatus(false);
        createSetOrderResponseDto.setError(ex.getMessage());
        return createSetOrderResponseDto;
    }

    private CreateSetOrderResponseDto getFailedCreateSetOrderResult(String message) {
        CreateSetOrderResponseDto createSetOrderResponseDto = new CreateSetOrderResponseDto();
        createSetOrderResponseDto.setStatus(false);
        createSetOrderResponseDto.setError(message);
        return createSetOrderResponseDto;
    }

    private CreateOptionsOrderResponseDto getFailedCreateOptionsOrderResult(Exception ex) {
        CreateOptionsOrderResponseDto createOptionsOrderResponseDto = new CreateOptionsOrderResponseDto();
        createOptionsOrderResponseDto.setStatus(false);
        createOptionsOrderResponseDto.setError(ex.getMessage());
        return createOptionsOrderResponseDto;
    }

    private CreateOrderResponseDto getSuccessCreateLtpOrderResult(List<Integer> orderIds) {
        CreateOrderResponseDto createOrderResponseDto = new CreateOrderResponseDto();
        createOrderResponseDto.setStatus(true);
        createOrderResponseDto.setOrderIds(orderIds);
        return createOrderResponseDto;
    }

    private CreateOrderResponseDto getFailedCreateLtpOrderResult(Exception ex) {
        CreateOrderResponseDto createOrderResponseDto = new CreateOrderResponseDto();
        createOrderResponseDto.setStatus(false);
        createOrderResponseDto.setError(ex.getMessage());
        return createOrderResponseDto;
    }


    private double getQuantity(double tradePrice) {
        double orderSize = (getDefaultOrderValue() == null) ? DEFAULT_ORDER_VALUE : getDefaultOrderValue();
        return Math.floor(orderSize / tradePrice);
    }


    private List<Order> createBracketOrderWithTrailingSL(int parentOrderId, String action, double quantity, double limitPrice, double stopLossTrailingAmount, Contract contract, String orderTrigger, String orderTriggerInterval) {

        LOGGER.info("Creating bracket order with orderTrigger [{}], parentOrderId=[{}], type=[{}], quantity=[{}], limitPrice=[{}], stopLossTrailingAmount=[{}], interval=[{}]", orderTrigger, parentOrderId, action, quantity, limitPrice, stopLossTrailingAmount, orderTriggerInterval);


        List<Order> bracketOrder = new ArrayList<>();

        //This will be our main or "parent" order
        Order parent = new Order();
        parent.orderId(parentOrderId);
        parent.action(action);
        parent.orderType(com.ib.client.OrderType.LMT);
        parent.hidden(true);
        parent.totalQuantity(quantity);
        parent.lmtPrice(roundOffDoubleForPriceDecimalFormat(limitPrice));
        parent.tif(Types.TimeInForce.GTC);
        parent.outsideRth(true);
        parent.account(getTradingAccount());
        parent.transmit(false);

        bracketOrder.add(parent);
        persistOrder(parent, contract, orderTrigger, orderTriggerInterval, false, null, null);

        Order stopLoss = new Order();
        stopLoss.orderId(parent.orderId() + 1);
        stopLoss.action(action.equalsIgnoreCase("BUY") ? "SELL" : "BUY");
        stopLoss.orderType(OrderType.TRAIL_LIMIT);

        stopLoss.auxPrice(roundOffDoubleForPriceDecimalFormat(stopLossTrailingAmount));
        stopLoss.lmtPriceOffset(0.0d);
        stopLoss.trailStopPrice(roundOffDoubleForPriceDecimalFormat(action.equalsIgnoreCase("BUY") ? limitPrice - stopLossTrailingAmount : limitPrice + stopLossTrailingAmount));

        stopLoss.tif(Types.TimeInForce.GTC);
        stopLoss.outsideRth(true);
        stopLoss.hidden(true);
        stopLoss.totalQuantity(quantity);
        stopLoss.account(getTradingAccount());
        stopLoss.parentId(parentOrderId);
        stopLoss.transmit(true);

        bracketOrder.add(stopLoss);
        persistOrder(stopLoss, contract, orderTrigger, orderTriggerInterval, true, null, null);

        return bracketOrder;
    }


    private List<Order> createSLOrder(int orderId, String action, double quantity, double limitPrice, Contract contract, String orderTrigger, String orderTriggerInterval) {

        LOGGER.info("Creating SL order");

        List<Order> bracketOrder = new ArrayList<>();

        Order stopLoss = new Order();
        stopLoss.orderId(orderId);
        stopLoss.action(action.equalsIgnoreCase("BUY") ? "BUY" : "SELL");
        stopLoss.orderType(OrderType.STP_LMT);
        //Stop trigger price
        stopLoss.auxPrice(roundOffDoubleForPriceDecimalFormat(limitPrice));
        stopLoss.lmtPrice(roundOffDoubleForPriceDecimalFormat(limitPrice));

        stopLoss.tif(Types.TimeInForce.GTC);
        stopLoss.outsideRth(true);
        stopLoss.hidden(true);
        stopLoss.totalQuantity(quantity);
        stopLoss.account(getTradingAccount());
        stopLoss.transmit(true);

        bracketOrder.add(stopLoss);
        persistOrder(stopLoss, contract, orderTrigger, orderTriggerInterval, true, null, null);

        return bracketOrder;
    }

    private List<Order> createTrailingSLOrder(int orderId, String action, double quantity, double limitPrice, double stopLossTrailingAmount, Contract contract, String orderTrigger, String orderTriggerInterval) {

        LOGGER.info("Creating SL order");

        List<Order> bracketOrder = new ArrayList<>();

        Order stopLoss = new Order();
        stopLoss.orderId(orderId);
        stopLoss.action(action.equalsIgnoreCase("BUY") ? "BUY" : "SELL");
        stopLoss.orderType(OrderType.TRAIL_LIMIT);

        //Stop trigger price
        stopLoss.auxPrice(roundOffDoubleForPriceDecimalFormat(stopLossTrailingAmount));
        stopLoss.lmtPriceOffset(0.0d);
        stopLoss.trailStopPrice(roundOffDoubleForPriceDecimalFormat(action.equalsIgnoreCase("SELL") ? limitPrice - stopLossTrailingAmount : limitPrice + stopLossTrailingAmount));

        stopLoss.tif(Types.TimeInForce.GTC);
        stopLoss.outsideRth(true);
        stopLoss.hidden(true);
        stopLoss.totalQuantity(quantity);
        stopLoss.account(getTradingAccount());
        stopLoss.transmit(true);

        bracketOrder.add(stopLoss);
        persistOrder(stopLoss, contract, orderTrigger, orderTriggerInterval, true, null, null);

        return bracketOrder;
    }

    private List<Order> createBracketOrderWithTPSL(int parentOrderId, String action, double quantity, double limitPrice, double takeProfitLimitPrice, double stopLossPrice, Contract contract, String orderTrigger, String orderTriggerInterval) {
        return createBracketOrderWithTPSL(parentOrderId, action, quantity, limitPrice, takeProfitLimitPrice, stopLossPrice, contract, orderTrigger, orderTriggerInterval, false, true);
    }

    private List<Order> createBracketOrderWithTPSLWithSLCover(int parentOrderId, String action, double quantity, double limitPrice, double takeProfitLimitPrice, double stopLossPrice, Contract contract, String orderTrigger, String orderTriggerInterval) {
        return createBracketOrderWithTPSLWithSLCover(parentOrderId, action, quantity, limitPrice, takeProfitLimitPrice, stopLossPrice, contract, orderTrigger, orderTriggerInterval, false, true);
    }

    private List<Order> createBracketOrderWith2TPSL(int parentOrderId, String action, double quantity, double limitPrice, double takeProfitLimitPrice1, double takeProfitLimitPrice2, double stopLossPrice, Contract contract, String orderTrigger, String orderTriggerInterval) {

        LOGGER.info("Creating bracket order with orderTrigger [{}], parentOrderId=[{}], type=[{}], quantity=[{}], limitPrice=[{}], tp1=[{}], tp2=[{}], sl=[{}], interval=[{}]", orderTrigger, parentOrderId, action, quantity, limitPrice, takeProfitLimitPrice1, takeProfitLimitPrice2, stopLossPrice, orderTriggerInterval);

        List<Order> bracketOrder = new ArrayList<>();

        if (quantity > 1) {
            int secondOrderQty = (int) (Math.floor(quantity / 2));
            int firstOrderQty = (int) quantity - secondOrderQty;
            bracketOrder.addAll(createBracketOrderWithTPSL(parentOrderId, action, firstOrderQty, limitPrice, takeProfitLimitPrice1, stopLossPrice, contract, orderTrigger, orderTriggerInterval, false, false));
            bracketOrder.addAll(createBracketOrderWithTPSL(parentOrderId + 3, action, secondOrderQty, limitPrice, takeProfitLimitPrice2, stopLossPrice, contract, orderTrigger, orderTriggerInterval, false, false));
        } else {
            bracketOrder.addAll(createBracketOrderWithTPSL(parentOrderId, action, quantity, limitPrice, takeProfitLimitPrice1, stopLossPrice, contract, orderTrigger, orderTriggerInterval));
        }
        return bracketOrder;
    }


    private List<Order> createBracketOrderWithTPSL(int parentOrderId, String action, double quantity, double limitPrice, double takeProfitLimitPrice, double stopLossPrice, Contract contract, String orderTrigger, String orderTriggerInterval, boolean psudoSL, boolean orth) {

        LOGGER.info("Creating bracket order with orderTrigger [{}], parentOrderId=[{}], type=[{}], quantity=[{}], limitPrice=[{}], tp=[{}], sl=[{}], interval=[{}]", orderTrigger, parentOrderId, action, quantity, limitPrice, takeProfitLimitPrice, stopLossPrice, orderTriggerInterval);


        List<Order> bracketOrder = new ArrayList<>();

        //This will be our main or "parent" order
        Order parent = new Order();
        parent.orderId(parentOrderId);
        parent.action(action);
        parent.orderType(com.ib.client.OrderType.LMT);
        parent.hidden(true);
        parent.totalQuantity(quantity);
        parent.lmtPrice(roundOffDoubleForPriceDecimalFormat(limitPrice));
        parent.tif(Types.TimeInForce.GTC);
        parent.outsideRth(orth);
        parent.account(getTradingAccount());
        parent.transmit(false);

        bracketOrder.add(parent);
        persistOrder(parent, contract, orderTrigger, orderTriggerInterval, false, null, null);

        Order takeProfit = new Order();
        takeProfit.orderId(parent.orderId() + 1);
        takeProfit.action(action.equalsIgnoreCase("BUY") ? "SELL" : "BUY");
        takeProfit.orderType(com.ib.client.OrderType.LMT);
        takeProfit.hidden(true);
        takeProfit.totalQuantity(quantity);
        takeProfit.lmtPrice(roundOffDoubleForPriceDecimalFormat(takeProfitLimitPrice));
        takeProfit.tif(Types.TimeInForce.GTC);
        takeProfit.outsideRth(orth);
        takeProfit.parentId(parentOrderId);
        takeProfit.account(getTradingAccount());
        takeProfit.transmit(false);

        bracketOrder.add(takeProfit);
        persistOrder(takeProfit, contract, orderTrigger, orderTriggerInterval, false, null, null);

        Order stopLoss = new Order();
        stopLoss.orderId(parent.orderId() + 2);
        stopLoss.action(action.equalsIgnoreCase("BUY") ? "SELL" : "BUY");

        //Stop trigger price
        stopLoss.auxPrice(roundOffDoubleForPriceDecimalFormat(stopLossPrice));
        stopLoss.orderType(OrderType.STP);

//        stopLoss.orderType(OrderType.STP_LMT);
//        if(psudoSL){
//            // Setting stop loss limit price as purchase price, to clear position at purchase price, When stop loss price is hit.
//            double offsetQty = OPTIONS_TYPE.equalsIgnoreCase(contract.getSecType()) || STRADDLE_TYPE.equalsIgnoreCase(contract.getSecType()) ? quantity * 100 : quantity;
//            double commissionOffset = 10 / offsetQty;
//            stopLoss.lmtPrice(roundOffDoubleForPriceDecimalFormat(action.equalsIgnoreCase("BUY") ? limitPrice + commissionOffset : limitPrice - commissionOffset));
//        } else {
//            stopLoss.lmtPrice(roundOffDoubleForPriceDecimalFormat(stopLossPrice));
//        }

        stopLoss.tif(Types.TimeInForce.GTC);
        stopLoss.outsideRth(orth);
        stopLoss.hidden(true);
        stopLoss.totalQuantity(quantity);
        stopLoss.account(getTradingAccount());
        stopLoss.parentId(parentOrderId);
        stopLoss.transmit(true);

        bracketOrder.add(stopLoss);
        persistOrder(stopLoss, contract, orderTrigger, orderTriggerInterval, true, null, null);

        return bracketOrder;
    }


    private List<Order> createBracketOrderAtMarketOpen(int parentOrderId, String action, double quantity, double limitPrice, double takeProfitLimitPrice, Contract contract, String orderTrigger, String orderTriggerInterval) {

        LOGGER.info("Creating bracket order with orderTrigger [{}], parentOrderId=[{}], type=[{}], quantity=[{}], limitPrice=[{}], tp=[{}], interval=[{}]", orderTrigger, parentOrderId, action, quantity, limitPrice, takeProfitLimitPrice, orderTriggerInterval);

        List<Order> bracketOrder = new ArrayList<>();

        Order parent = new Order();
        parent.orderId(parentOrderId);
        parent.action(action);
        parent.orderType(com.ib.client.OrderType.LMT);
        parent.hidden(true);
        parent.totalQuantity(quantity);
        parent.lmtPrice(roundOffDoubleForPriceDecimalFormat((2 * limitPrice) - takeProfitLimitPrice));
        parent.tif(Types.TimeInForce.GTC);
        parent.outsideRth(true);
        parent.account(getTradingAccount());
        parent.transmit(true);

        bracketOrder.add(parent);
        persistOrder(parent, contract, orderTrigger, orderTriggerInterval, false, null, null);

        Order takeProfit = new Order();
        takeProfit.orderId(parent.orderId() + 1);
        takeProfit.action(action.equalsIgnoreCase("BUY") ? "SELL" : "BUY");
        takeProfit.orderType(com.ib.client.OrderType.LMT);
        takeProfit.hidden(true);
        takeProfit.totalQuantity(quantity);
        takeProfit.lmtPrice(roundOffDoubleForPriceDecimalFormat(takeProfitLimitPrice));
        takeProfit.tif(Types.TimeInForce.GTC);
        takeProfit.outsideRth(true);
        takeProfit.account(getTradingAccount());
        takeProfit.transmit(true);

        bracketOrder.add(takeProfit);
        persistOrder(takeProfit, contract, orderTrigger, orderTriggerInterval, false, null, null);

        return bracketOrder;
    }

    private List<Order> createBracketOrderWithTPSLWithSLCover(int parentOrderId, String action, double quantity, double limitPrice, double takeProfitLimitPrice, double stopLossPrice, Contract contract, String orderTrigger, String orderTriggerInterval, boolean psudoSL, boolean orth) {

        LOGGER.info("Creating bracket order with orderTrigger [{}], parentOrderId=[{}], type=[{}], quantity=[{}], limitPrice=[{}], tp=[{}], sl=[{}], interval=[{}]", orderTrigger, parentOrderId, action, quantity, limitPrice, takeProfitLimitPrice, stopLossPrice, orderTriggerInterval);


        List<Order> bracketOrder = new ArrayList<>();

        //This will be our main or "parent" order
        Order parent = new Order();
        parent.orderId(parentOrderId);
        parent.action(action);
        parent.orderType(com.ib.client.OrderType.LMT);
        parent.hidden(true);
        parent.totalQuantity(quantity);
        parent.lmtPrice(roundOffDoubleForPriceDecimalFormat(limitPrice));
        parent.tif(Types.TimeInForce.GTC);
        parent.outsideRth(orth);
        parent.account(getTradingAccount());
        parent.transmit(false);

        bracketOrder.add(parent);
        persistOrder(parent, contract, orderTrigger, orderTriggerInterval, false, null, null);

        Order takeProfit = new Order();
        takeProfit.orderId(parent.orderId() + 1);
        takeProfit.action(action.equalsIgnoreCase("BUY") ? "SELL" : "BUY");
        takeProfit.orderType(com.ib.client.OrderType.LMT);
        takeProfit.hidden(true);
        takeProfit.totalQuantity(quantity);
        takeProfit.lmtPrice(roundOffDoubleForPriceDecimalFormat(takeProfitLimitPrice));
        takeProfit.tif(Types.TimeInForce.GTC);
        takeProfit.outsideRth(orth);
        takeProfit.parentId(parentOrderId);
        takeProfit.account(getTradingAccount());
        takeProfit.transmit(false);

        bracketOrder.add(takeProfit);
        persistOrder(takeProfit, contract, orderTrigger, orderTriggerInterval, false, null, null);

        Order stopLoss = new Order();
        stopLoss.orderId(parent.orderId() + 2);
        stopLoss.action(action.equalsIgnoreCase("BUY") ? "SELL" : "BUY");

        //Stop trigger price
        stopLoss.auxPrice(roundOffDoubleForPriceDecimalFormat(stopLossPrice));
        stopLoss.orderType(OrderType.STP);

        stopLoss.tif(Types.TimeInForce.GTC);
        stopLoss.outsideRth(orth);
        stopLoss.hidden(true);
        stopLoss.totalQuantity(quantity);
        stopLoss.account(getTradingAccount());
        stopLoss.parentId(parentOrderId);
        stopLoss.transmit(true);

        bracketOrder.add(stopLoss);
        persistOrder(stopLoss, contract, orderTrigger, orderTriggerInterval, true, null, null);


//        Order slTakeProfit = new Order();
//        slTakeProfit.orderId(parent.orderId() + 3);
//        slTakeProfit.action(action);
//        slTakeProfit.orderType(com.ib.client.OrderType.LMT);
//        slTakeProfit.hidden(true);
//        slTakeProfit.totalQuantity(quantity);
//        slTakeProfit.lmtPrice(roundOffDoubleForPriceDecimalFormat((2*stopLossPrice)-limitPrice));
//        slTakeProfit.tif(Types.TimeInForce.GTC);
//        slTakeProfit.outsideRth(orth);
//        slTakeProfit.parentId(stopLoss.orderId());
//        slTakeProfit.account(getTradingAccount());
//        slTakeProfit.transmit(false);
//
//
//        bracketOrder.add(slTakeProfit);
//        persistOrder(slTakeProfit, contract, orderTrigger, orderTriggerInterval, true);
//
//
//        Order slStopLoss = new Order();
//        slStopLoss.orderId(parent.orderId() + 4);
//        slStopLoss.action(action);
//
//        //Stop trigger price
//        slStopLoss.auxPrice(roundOffDoubleForPriceDecimalFormat(limitPrice));
//        slStopLoss.orderType(OrderType.STP);
//
//        slStopLoss.tif(Types.TimeInForce.GTC);
//        slStopLoss.outsideRth(orth);
//        slStopLoss.hidden(true);
//        slStopLoss.totalQuantity(quantity );
//        slStopLoss.account(getTradingAccount());
//        slStopLoss.parentId(stopLoss.orderId());
//        slStopLoss.transmit(true);
//
//        bracketOrder.add(slStopLoss);
//        persistOrder(slStopLoss, contract, orderTrigger, orderTriggerInterval, true);

        return bracketOrder;
    }


    private List<Order> createBracketOrderWithTPWithOCAHedge(int parentOrderId, String action, double quantity, double limitPrice, double takeProfitLimitPrice, double stopLossPrice, Contract contract, String orderTrigger, String orderTriggerInterval, int hedgeQtyMultiplier) {

        LOGGER.info("Creating Hedge OCA bracket order with orderTrigger [{}], parentOrderId=[{}], type=[{}], quantity=[{}], limitPrice=[{}], tp=[{}], sl=[{}], interval=[{}]", orderTrigger, parentOrderId, action, quantity, limitPrice, takeProfitLimitPrice, stopLossPrice, orderTriggerInterval);


        List<Order> bracketOrder = new ArrayList<>();

        //This will be our main or "parent" order
        Order parent = new Order();
        parent.orderId(parentOrderId);
        parent.action(action);
        parent.orderType(OrderType.LMT);
        parent.hidden(true);
        parent.totalQuantity(quantity);
        parent.lmtPrice(roundOffDoubleForPriceDecimalFormat(limitPrice));
        parent.tif(Types.TimeInForce.GTC);
        parent.outsideRth(true);
        parent.account(getTradingAccount());
        parent.transmit(true);

        bracketOrder.add(parent);

        OrderEntity parentOrderEntity = persistOrder(parent, contract, orderTrigger, orderTriggerInterval, false, null, null, null, null, null);


        Order takeProfit = new Order();
        takeProfit.orderId(parent.orderId() + 1);
        takeProfit.action(action.equalsIgnoreCase("BUY") ? "SELL" : "BUY");
        takeProfit.orderType(OrderType.LMT);
        takeProfit.hidden(true);
        takeProfit.totalQuantity(quantity);
        takeProfit.lmtPrice(roundOffDoubleForPriceDecimalFormat(takeProfitLimitPrice));
        takeProfit.tif(Types.TimeInForce.GTC);
        takeProfit.outsideRth(true);
        //takeProfit.parentId(parentOrderId);
        takeProfit.account(getTradingAccount());
        takeProfit.transmit(false);
        takeProfit.ocaGroup("OCA_" + parentOrderId);
        takeProfit.ocaType(1);

        bracketOrder.add(takeProfit);
        OrderEntity takeProfitOrderEntity = persistOrder(takeProfit, contract, orderTrigger, orderTriggerInterval, false, null, parentOrderEntity, null, null, null);

        Order ocaHedge = new Order();
        ocaHedge.orderId(parent.orderId() + 2);
        ocaHedge.action(action.equalsIgnoreCase("BUY") ? "SELL" : "BUY");
        ocaHedge.orderType(OrderType.STP);
        //Stop trigger price
        ocaHedge.auxPrice(roundOffDoubleForPriceDecimalFormat(stopLossPrice));
        ocaHedge.tif(Types.TimeInForce.GTC);
        ocaHedge.outsideRth(true);
        ocaHedge.hidden(true);
        ocaHedge.totalQuantity(quantity * hedgeQtyMultiplier);
        ocaHedge.account(getTradingAccount());
        ocaHedge.transmit(false);
        ocaHedge.ocaGroup("OCA_" + parentOrderId);
        ocaHedge.ocaType(1);


        bracketOrder.add(ocaHedge);

        Contract ocaHedgeContract = ("NQ".equalsIgnoreCase(contract.symbol()) || "ES".equalsIgnoreCase(contract.symbol())) ? getBaseService().createContract("M" + contract.symbol()) : contract;
        OrderEntity ocaHedgeOrderEntity = persistOrder(ocaHedge, ocaHedgeContract, orderTrigger, orderTriggerInterval, false, null, parentOrderEntity, hedgeQtyMultiplier, null, null);


        return bracketOrder;
    }

    private List<Order> createBracketOrderWithTP(int parentOrderId, String action, double quantity, double limitPrice, double takeProfitLimitPrice, Contract contract, String orderTrigger, String orderTriggerInterval) {

        LOGGER.info("Creating bracket order with orderTrigger [{}], parentOrderId=[{}], type=[{}], quantity=[{}], limitPrice=[{}], tp=[{}], sl=[{}], interval=[{}]", orderTrigger, parentOrderId, action, quantity, limitPrice, takeProfitLimitPrice, "NOT_APPLICABLE", orderTriggerInterval);
        //This will be our main or "parent" order
        Order parent = new Order();
        parent.orderId(parentOrderId);
        parent.action(action.toUpperCase());
        parent.orderType(com.ib.client.OrderType.LMT);
        parent.hidden(true);
        parent.totalQuantity(quantity);
        parent.lmtPrice(roundOffDoubleForPriceDecimalFormat(limitPrice));
        parent.tif(Types.TimeInForce.GTC);
        parent.outsideRth(true);
        parent.account(getTradingAccount());
        //The parent and children orders will need this attribute set to false to prevent accidental executions.
        //The LAST CHILD will have it set to true.
        parent.transmit(false);

        persistOrder(parent, contract, orderTrigger, orderTriggerInterval, true, null, null);

        List<Order> bracketOrder = new ArrayList<>();
        bracketOrder.add(parent);

        if (takeProfitLimitPrice > 0) {
            Order takeProfit = new Order();
            takeProfit.orderId(parent.orderId() + 1);
            takeProfit.action(action.equalsIgnoreCase("BUY") ? "SELL" : "BUY");
            takeProfit.orderType(com.ib.client.OrderType.LMT);
            takeProfit.hidden(true);
            takeProfit.totalQuantity(quantity);
            takeProfit.lmtPrice(roundOffDoubleForPriceDecimalFormat(takeProfitLimitPrice));
            takeProfit.tif(Types.TimeInForce.GTC);
            takeProfit.outsideRth(true);
            takeProfit.account(getTradingAccount());
            takeProfit.parentId(parentOrderId);
            takeProfit.transmit(true);

            persistOrder(takeProfit, contract, orderTrigger, orderTriggerInterval, true, null, null);

            bracketOrder.add(takeProfit);
        } else {
            parent.transmit(true);
        }

        return bracketOrder;
    }

    private List<Order> createMacdOrders(EClientSocket eClientSocket, Contract contract , int orderId,  String action, double quantity, Double slQuantity, Double outstandingQty, double tradePrice, double slPrice, boolean enableOrth, Long tradeStartSequenceId, Long currentSequenceId, String ticker, String orderTrigger, String interval) throws Exception{
        List<Order> bracketOrderList = new ArrayList<>();

        // LOGGER.info("Creating single order with orderTrigger [{}], parentOrderId=[{}], type=[{}], quantity=[{}], limitPrice=[{}], interval=[{}]", orderTrigger, orderId, action, quantity, limitPrice, orderTriggerInterval);
        //This will be our main or "parent" order
        Order order = new Order();
        order.orderId(orderId);
        order.action(action.toUpperCase());
        order.orderType(com.ib.client.OrderType.LMT);
        order.hidden(true);
        order.totalQuantity(quantity);
        order.lmtPrice(tradePrice);
        order.tif(Types.TimeInForce.GTC);
        if (enableOrth) {
            order.outsideRth(true);
        }
        order.account(DEFAULT_TRADING_ACCOUNT_NUMBER);
        order.transmit(true);
        bracketOrderList.add(order);

        LOGGER.info("Placing order with id [{}]", order.orderId());
        try {
            eClientSocket.placeOrder(order.orderId(), contract, order);
        } catch (Exception ex) {
            LOGGER.error("Placing order with id [{}] failed. Failed to get next Order Id", order.orderId());
        }

        Order slOrder;
        OrderEntity slOrderEntity = getSingleOrderBySlCheckSequenceIdAndTrigger(tradeStartSequenceId, orderTrigger);
        Double currentOutstandingQty = (double) Math.abs(findOutstandingQtyForTickerWithSpecificOrderTrigger(ticker, orderTrigger, interval));
        if(currentSequenceId != null) {
            if(currentSequenceId == -1) {
                // commented cancelling SL order here, as SL order quantity will be updated as and when the exit order will get filled.
                // So that we will still have an SL order if target order do not get filled completely.
//                if(null != slOrderEntity){
//                    cancelOrder(slOrderEntity.getOrderId());
//                }
            } else {
                slOrder = new Order();
                Double absoluteSlQuantity = Math.abs(slQuantity);
                double roundedSlPrice = roundOff(Math.abs(slPrice), ticker);
                slOrder.orderId(tradeStartSequenceId.equals(currentSequenceId) || slOrderEntity == null ? getBaseService().getNextOrderId() : slOrderEntity.getOrderId());
                slOrder.action("BUY".equalsIgnoreCase(action) ? "SELL" : "BUY");
                slOrder.orderType(OrderType.STP_LMT); //////// *******
                slOrder.auxPrice(roundedSlPrice);
                slOrder.lmtPrice(roundedSlPrice);
                slOrder.tif(Types.TimeInForce.GTC);
                slOrder.outsideRth(enableOrth);
                slOrder.hidden(true);
                slOrder.totalQuantity(Double.valueOf(0).equals(currentOutstandingQty) ? absoluteSlQuantity : currentOutstandingQty);
                slOrder.account(DEFAULT_TRADING_ACCOUNT_NUMBER);

                // Do not transmit SL order here, just create order in exchange with all possible fields.
                // When original order gets filled, then SL order will be updated with the filled qty and order will be transmitted then.
                slOrder.transmit(false);
                bracketOrderList.add(slOrder);
                if(!slQuantity.equals(currentOutstandingQty)){
                    LOGGER.error("Outstanding Qty [{}] does not match with SL Quantity [{}]. Using quantity [{}] for SL order", currentOutstandingQty, absoluteSlQuantity, slOrder.totalQuantity());
                }

                LOGGER.info("Placing STOP-LOSS order with id [{}], stop loss [{}], and quantity [{}] for ticker [{}]", slOrder.orderId(), roundedSlPrice, slOrder.totalQuantity(), ticker);
                try {
                    eClientSocket.placeOrder(slOrder.orderId(), contract, slOrder);
                } catch (Exception ex) {
                    LOGGER.error("Placing STOP-LOSS order with id [{}], stop loss [{}], and quantity [{}] for ticker [{}]", slOrder.orderId(), roundedSlPrice, slOrder.totalQuantity(), ticker);
                }
            }
        }
        return bracketOrderList;
    }

    @Override
    public void updateOrderQuantity(int orderId, OrderEntity orderEntity, double newQuantity, EClientSocket eClientSocket){
        OrderEntity orderToUpdate = orderEntity;
        if(null == orderToUpdate){
            try {
                orderToUpdate = getSingleOrderByOrderId(orderId);
            } catch (Exception ex){
                LOGGER.error("Could not find the order with order id [{}] to update quantity", orderId);
                return;
            }
        }

        Order newQtyOrder = new Order();
        Double absoluteSlQuantity = Math.abs(newQuantity);
        newQtyOrder.orderId(orderToUpdate.getOrderId());
        newQtyOrder.action(orderToUpdate.getOrderAction());
        newQtyOrder.orderType(orderToUpdate.getOrderType());
        if("STP LMT".equals(orderToUpdate.getOrderType())) {
            newQtyOrder.auxPrice(orderToUpdate.getStopLossTriggerPrice());

        }

        newQtyOrder.lmtPrice(orderToUpdate.getTransactionPrice());
        newQtyOrder.tif(Types.TimeInForce.GTC);
        newQtyOrder.outsideRth(orderToUpdate.getOutsideRth());
        newQtyOrder.hidden(true);
        newQtyOrder.totalQuantity(absoluteSlQuantity);
        newQtyOrder.account(DEFAULT_TRADING_ACCOUNT_NUMBER);
        newQtyOrder.transmit(true);


        LOGGER.info("Updating quantity for order with id [{}]", newQtyOrder.orderId());
        try {
            Contract contract = getBaseService().createContract(orderToUpdate.getSymbol());
            eClientSocket.placeOrder(newQtyOrder.orderId(), contract, newQtyOrder);
        } catch (Exception ex) {
            LOGGER.error("Placing order with id [{}] failed. Failed to get next Order Id", newQtyOrder.orderId());
        }
    }

    private Order createSingleOrder(int orderId, String action, double quantity, double tradePrice, boolean enableOrth) {

        // LOGGER.info("Creating single order with orderTrigger [{}], parentOrderId=[{}], type=[{}], quantity=[{}], limitPrice=[{}], interval=[{}]", orderTrigger, orderId, action, quantity, limitPrice, orderTriggerInterval);
        //This will be our main or "parent" order
        Order order = new Order();
        order.orderId(orderId);
        order.action(action.toUpperCase());
        order.orderType(com.ib.client.OrderType.LMT);
        order.hidden(true);
        order.totalQuantity(quantity);
        order.lmtPrice(tradePrice);
        order.tif(Types.TimeInForce.GTC);
        if (enableOrth) {
            order.outsideRth(true);
        }
        //order.account(getTradingAccount());
        order.account(DEFAULT_TRADING_ACCOUNT_NUMBER);
        order.transmit(true);

        // This is delaying the order creation hence commented and added later after orders are sent to TWS
        // persistOrder(order, contract, orderTrigger, orderTriggerInterval, true);

        return order;
    }

    private Order updateOrder(Integer orderId, Integer parentOrderId, String action, Integer quantity, Double limitPrice, Double triggerPrice, Contract contract, String orderType, String orderTrigger, String orderTriggerInterval) {

        LOGGER.error("Updating order with OrderId=[{}], target price=[{}], quantity=[{}], ParentOrderId=[{}]", orderId, limitPrice, quantity, parentOrderId);
        Order updateOrder = new Order();
        updateOrder.orderId(orderId);
        updateOrder.action(action.toUpperCase());
        updateOrder.orderType(orderType);
        updateOrder.totalQuantity(quantity);
        updateOrder.hidden(true);

        if (OrderType.STP_LMT.getApiString().equalsIgnoreCase(orderType)) {
            updateOrder.auxPrice(roundOffDoubleForPriceDecimalFormat(triggerPrice));
            updateOrder.lmtPrice(roundOffDoubleForPriceDecimalFormat(limitPrice));
        } else if (OrderType.STP.getApiString().equalsIgnoreCase(orderType)) {
            updateOrder.auxPrice(roundOffDoubleForPriceDecimalFormat(limitPrice));
        } else {
            updateOrder.lmtPrice(roundOffDoubleForPriceDecimalFormat(limitPrice));
        }

        updateOrder.tif(Types.TimeInForce.GTC);
        updateOrder.outsideRth(true);
        updateOrder.account(getTradingAccount());
        updateOrder.transmit(true);

        persistOrder(updateOrder, contract, orderTrigger, orderTriggerInterval, true, null, null);


        return updateOrder;
    }


    private OrderEntity persistOrder(Order order, Contract contract, String orderTrigger, String orderTriggerInterval, boolean isOptionsOrder, Double optionStrikePrice, String optionExpiryDate, String optionType, boolean waitForOrdersToBeCreated, List<OrderEntity> ocaOrders, OrderEntity parentOcaOrder, Integer hedgeQtyMultiplier, String sequenceId, String otsOrderType, Long tradeStartSequenceId, Long slCheckSequenceId) {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderId(order.orderId());
        orderEntity.setSymbol(contract.symbol());
        orderEntity.setOrderType(order.orderType().getApiString());
        orderEntity.setOrderAction(order.getAction());
        orderEntity.setQuantity(order.totalQuantity());
        orderEntity.setTransactionPrice(OrderType.STP.getApiString().equalsIgnoreCase(order.orderType().getApiString()) ? order.auxPrice() : order.lmtPrice());
        if (OrderType.STP.getApiString().equalsIgnoreCase(order.orderType().getApiString()) || OrderType.STP_LMT.getApiString().equalsIgnoreCase(order.orderType().getApiString())) {
            orderEntity.setStopLossTriggerPrice(order.auxPrice());
        }
        orderEntity.setTimeInForce(order.tif().getApiString());
        orderEntity.setOutsideRth(order.outsideRth());
        orderEntity.setTransmit(order.transmit());
        orderEntity.setCurrency(contract.currency());
        orderEntity.setOrderTrigger(orderTrigger);
        orderEntity.setOrderTriggerInterval(orderTriggerInterval);
        orderEntity.setOptionsOrder(isOptionsOrder);
        orderEntity.setSequenceId(sequenceId);
        orderEntity.setOtsOrderType(otsOrderType);
        orderEntity.setFilled(0d);
        orderEntity.setTradeStartSequenceId(tradeStartSequenceId);
        orderEntity.setSlCheckSequenceId(slCheckSequenceId);

        // Set fields for Options order
        if (isOptionsOrder) {
            orderEntity.setOptionStrikePrice(optionStrikePrice);
            orderEntity.setOptionExpiryDate(optionExpiryDate);
            orderEntity.setOptionType(optionType);
        }

        // Set fields for hedge order
        orderEntity.setOcaHedgeMultiplier(hedgeQtyMultiplier == null ? 1 : hedgeQtyMultiplier);


        // Set default status if Order is new

        Optional<OrderEntity> optionalOrderEntity = getOrderRepository().findById(order.orderId());
        if (!optionalOrderEntity.isPresent()) {
            orderEntity.setOrderStatus(DEFAULT_STATUS);
        }

        // set parent order if available
        if (order.parentId() != 0) {
            getOrderRepository().findById(order.parentId()).ifPresent(orderEntity::setParentOrder);
        }
        orderEntity.setCreatedTimestamp(new java.sql.Timestamp(new Date().getTime()));
        if (!CollectionUtils.isEmpty(ocaOrders)) {
            orderEntity.setOcaOrders(ocaOrders);
        }
        if (parentOcaOrder != null) {
            orderEntity.setParentOcaOrder(parentOcaOrder);
        }
        getOrderRepository().saveAndFlush(orderEntity);

        if (waitForOrdersToBeCreated) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ie) {
                // do nothing
            }
        }

        LOGGER.info("##### ***** >>> TWS order id [{}], Sequence id [{}], OTS Order Type [{}] ***** #####", order.orderId(), sequenceId, otsOrderType);

        Optional<OrderEntity> orderEntity1 = getOrderRepository().findById(order.orderId());
        return orderEntity1.orElse(orderEntity);
    }


    private Order updateOrderTransactionPrice(OrderEntity orderEntity, double newTransactionPrice) {
        LOGGER.info("Update Order: Updating order with OrderId=[{}], to latest LTP as transaction price=[{}]", orderEntity.getOrderId(), newTransactionPrice);
        Order updateOrder = new Order();
        updateOrder.orderId(orderEntity.getOrderId());

        updateOrder.action(orderEntity.getOrderAction());
        updateOrder.orderType(orderEntity.getOrderType());
        updateOrder.totalQuantity(orderEntity.getQuantity());

        updateOrder.hidden(true);

        if (OrderType.STP_LMT.getApiString().equalsIgnoreCase(orderEntity.getOrderType())) {
            updateOrder.auxPrice(roundOffDoubleForPriceDecimalFormat(newTransactionPrice));
            updateOrder.lmtPrice(roundOffDoubleForPriceDecimalFormat(newTransactionPrice));
        } else if (OrderType.STP.getApiString().equalsIgnoreCase(orderEntity.getOrderType())) {
            updateOrder.auxPrice(roundOffDoubleForPriceDecimalFormat(newTransactionPrice));
        } else {
            updateOrder.lmtPrice(roundOffDoubleForPriceDecimalFormat(newTransactionPrice));
        }

        updateOrder.tif(Types.TimeInForce.GTC);
        updateOrder.outsideRth(true);
        updateOrder.account(getTradingAccount());
        updateOrder.transmit(true);

        orderEntity.setTransactionPrice(newTransactionPrice);
        //orderEntity.setStatusUpdateTimestamp(new java.sql.Timestamp(new Date().getTime()));
        getOrderRepository().saveAndFlush(orderEntity);
        return updateOrder;
    }


    private OrderEntity persistOrder(Order order, Contract contract, String orderTrigger, String orderTriggerInterval, boolean waitForOrdersToBeCreated, String sequenceId, String otsOrderType) {
        return persistOrder(order, contract, orderTrigger, orderTriggerInterval, waitForOrdersToBeCreated, null, null, null, sequenceId, otsOrderType);
    }

    private OrderEntity persistOrder(Order order, Contract contract, String orderTrigger, String orderTriggerInterval, boolean waitForOrdersToBeCreated, Integer hedgeQtyMultiplier, String sequenceId, String otsOrderType) {
        return persistOrder(order, contract, orderTrigger, orderTriggerInterval, waitForOrdersToBeCreated, null, null, hedgeQtyMultiplier, sequenceId, otsOrderType);
    }


    private OrderEntity persistOrder(Order order, Contract contract, String orderTrigger, String orderTriggerInterval, boolean waitForOrdersToBeCreated, List<OrderEntity> ocaOrders, OrderEntity parentOcaOrder, Integer hedgeQtyMultiplier, String sequenceId, String otsOrderType) {
        if (BaseServiceImpl.OPTIONS_TYPE.equalsIgnoreCase(contract.getSecType())) {
            return persistOrder(order, contract, orderTrigger, orderTriggerInterval, true, contract.strike(), contract.lastTradeDateOrContractMonth(), contract.getRight(), waitForOrdersToBeCreated, null, null, null, sequenceId, otsOrderType, null, null);
        } else {
            return persistOrder(order, contract, orderTrigger, orderTriggerInterval, false, null, null, null, waitForOrdersToBeCreated, ocaOrders, parentOcaOrder, hedgeQtyMultiplier, sequenceId, otsOrderType, null, null);
        }
    }

    private OrderEntity persistMacdOrder(Order order, Contract contract, String orderTrigger, String orderTriggerInterval, boolean waitForOrdersToBeCreated, List<OrderEntity> ocaOrders, OrderEntity parentOcaOrder, Integer hedgeQtyMultiplier, String sequenceId, String otsOrderType, Long tradeStartSequenceId, Long slCheckSequenceId) {
        if (BaseServiceImpl.OPTIONS_TYPE.equalsIgnoreCase(contract.getSecType())) {
            return persistOrder(order, contract, orderTrigger, orderTriggerInterval, true, contract.strike(), contract.lastTradeDateOrContractMonth(), contract.getRight(), waitForOrdersToBeCreated, null, null, null, sequenceId, otsOrderType, tradeStartSequenceId, slCheckSequenceId);
        } else {
            return persistOrder(order, contract, orderTrigger, orderTriggerInterval, false, null, null, null, waitForOrdersToBeCreated, ocaOrders, parentOcaOrder, hedgeQtyMultiplier, sequenceId, otsOrderType, tradeStartSequenceId, slCheckSequenceId);
        }
    }

    @Override
    public void deleteAllInactiveOrders() {
        Iterable<OrderEntity> ordersIterable = getOrderRepository().findAll();
        List<OrderEntity> orders = new ArrayList<>();

        if (ordersIterable != null) {
            ordersIterable.forEach(orders::add);
        }

        if (!orders.isEmpty()) {
            //List<OrderEntity> inactiveOrders = orders.stream().filter(order -> "PreSubmitted".equalsIgnoreCase(order.getOrderStatus()) || "Submitted".equalsIgnoreCase(order.getOrderStatus()) || "Inactive".equalsIgnoreCase(order.getOrderStatus()) || "Cancelled".equalsIgnoreCase(order.getOrderStatus())).collect(Collectors.toList());

            List<OrderEntity> inactiveOrders = orders.stream().filter(order -> "Cancelled".equalsIgnoreCase(order.getOrderStatus())).collect(Collectors.toList());

            LOGGER.info("Deleting {} inactive orders", inactiveOrders.size());

            List<OrderEntity> childOrders = inactiveOrders.stream().filter(order -> order.getParentOrder() != null || order.getParentOcaOrder() != null).collect(Collectors.toList());

            List<OrderEntity> parentOrders = inactiveOrders.stream().filter(order -> order.getParentOrder() == null && order.getParentOcaOrder() == null).collect(Collectors.toList());

            childOrders.forEach(this::deleteOrderIfExists);
            parentOrders.forEach(this::deleteOrderIfExists);
        }
    }

    private void deleteOrderIfExists(OrderEntity orderEntity) {
        if (orderEntity != null && orderEntity.getOrderId() != null) {
            if (null == orderEntity.getFilled() || orderEntity.getFilled() == 0d) {
                try {
                    getOrderRepository().delete(orderEntity);
                } catch (Exception ex) {
                    LOGGER.warn("Could not delete order with id " + orderEntity.getOrderId());
                }
            }
        }
    }

    private Double getLtpForTicker(String ticker) throws Exception {
        // Clear entities fetched previously
        entityManager.clear();
        List<ContractEntity> contractEntityList = getContractRepository().findBySymbol(ticker.replace(CME_FUTURES_SUFFIX, EMPTY_STRING));
        if (!CollectionUtils.isEmpty(contractEntityList)) {
            ContractEntity contractEntity = contractEntityList.get(0);
            if (null != contractEntity && null != contractEntity.getLtp()) {
                if (contractEntity.getTickerAskBidLtpValuesUpdateTimestamp() == null || (new Date().getTime() - contractEntity.getTickerAskBidLtpValuesUpdateTimestamp().getTime()) > 5000) {
                    throw new Exception("Stale LTP data for contract entity [ " + ticker + " ]");
                }
                return contractEntity.getLtp();
            }
        }
        String error = "Contract entity [ " + ticker + " ] not found in database";
        throw new IllegalArgumentException(error);
    }

    public BaseService getBaseService() {
        return baseService;
    }

    public void setBaseService(BaseService baseService) {
        this.baseService = baseService;
    }

    public OrderRepository getOrderRepository() {
        return orderRepository;
    }

    public void setOrderRepository(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    private double roundOffDoubleForPriceDecimalFormat(double price) {
        return Math.round(price * 100.0d) / 100.0d;
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

    private boolean isDivergenceOrderEnabled() {
        return Boolean.TRUE.equals(getSystemConfigService().getBoolean("divergence.order.enabled"));
    }

    private boolean isDivergenceOrderRsiFilterEnabled() {
        return Boolean.TRUE.equals(getSystemConfigService().getBoolean("divergence.order.rsi.filter.enabled"));
    }

    private boolean isOutOfHoursOrderEnabled() {
        return Boolean.TRUE.equals(getSystemConfigService().getBoolean("out.of.hours.order.enabled"));
    }

    private boolean isPessimisticOrderExitPriceEnabled() {
        return Boolean.TRUE.equals(getSystemConfigService().getBoolean("order.pessimisticOrderExitPrice"));
    }

    private Double getDefaultOrderValue() {
        return getSystemConfigService().getDouble("default.order.value");
    }

    private Integer getTradingStartHour() {
        return getSystemConfigService().getInteger("trading.start.hour");
    }

    private Integer getTradingEndHour() {
        return getSystemConfigService().getInteger("trading.end.hour");
    }

    private Integer getTradingStartMinute() {
        return getSystemConfigService().getInteger("trading.start.minute");
    }

    private Integer getTradingEndMinute() {
        return getSystemConfigService().getInteger("trading.end.minute");
    }

    private String getDivergenceOrderType() {
        return getSystemConfigService().getString("divergence.order.type");
    }

    public ContractRepository getContractRepository() {
        return contractRepository;
    }

    public void setContractRepository(ContractRepository contractRepository) {
        this.contractRepository = contractRepository;
    }

    public ContractService getContractService() {
        return contractService;
    }

    public void setContractService(ContractService contractService) {
        this.contractService = contractService;
    }
}

