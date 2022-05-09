package com.trading.app.tradingapp.service.impl;

import com.ib.client.*;
import com.trading.app.tradingapp.dto.request.*;
import com.trading.app.tradingapp.dto.response.CreateOptionsOrderResponseDto;
import com.trading.app.tradingapp.dto.response.CreateOrderResponseDto;
import com.trading.app.tradingapp.dto.response.CreateSetOrderResponseDto;
import com.trading.app.tradingapp.dto.response.UpdateSetOrderResponseDto;
import com.trading.app.tradingapp.persistance.entity.OrderEntity;
import com.trading.app.tradingapp.persistance.repository.OrderRepository;
import com.trading.app.tradingapp.service.BaseService;
import com.trading.app.tradingapp.service.OrderService;
import com.trading.app.tradingapp.util.JsonSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Resource
    private BaseService baseService;

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderServiceImpl.class);

    @Resource
    private OrderRepository orderRepository;

    private static final double DEFAULT_ORDER_VALUE = 100000.00d;

    @Value("${default.order.value}")
    private Double defaultOrderValue;

    @Value("${tws.trading.account}")
    private String tradingAccount;


    @Override
    public CreateSetOrderResponseDto createOrder(CreateSetOrderRequestDto createSetOrderRequestDto, String orderTrigger, String orderTriggerInterval) {
        //TODO: Add request object validation
        try {
            EClientSocket eClientSocket = getBaseService().getConnection();
            Contract contract = getBaseService().createStockContract(createSetOrderRequestDto.getTicker());
            List<Order> bracketOrders = createBracketOrderWithTP(getBaseService().getNextOrderId(), createSetOrderRequestDto.getOrderType().toString(), createSetOrderRequestDto.getQuantity(), createSetOrderRequestDto.getTransactionPrice(), createSetOrderRequestDto.getTargetPrice(), contract, orderTrigger, orderTriggerInterval);

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
            if(createOptionsOrderRequestDto.getStopLossPrice() == null) {
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

                Contract contract = Boolean.TRUE.equals(orderToBeUpdated.getOptionsOrder()) ? getBaseService().createOptionsContract(orderToBeUpdated.getSymbol(), orderToBeUpdated.getOptionStrikePrice(), orderToBeUpdated.getOptionExpiryDate(), orderToBeUpdated.getOptionType()) : getBaseService().createStockContract(orderToBeUpdated.getSymbol());

                Order updateOrder = updateOrder(orderToBeUpdated.getOrderId(), orderToBeUpdated.getOrderAction(), updateSetOrderRequestDto.getQuantity(), updateSetOrderRequestDto.getTargetPrice(), contract, orderToBeUpdated.getOrderTrigger(), orderToBeUpdated.getOrderTriggerInterval());

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
    public CreateOrderResponseDto createOrder(CreateLtpOrderRequestDto createLtpOrderRequestDto, String orderTrigger, String orderTriggerInterval) {
        //TODO: Add request object validation
        try {
            EClientSocket eClientSocket = getBaseService().getConnection();
            Contract contract = getBaseService().createStockContract(createLtpOrderRequestDto.getTicker());
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
        try {
            EClientSocket eClientSocket = getBaseService().getConnection();
            Contract contract = getBaseService().createStockContract(createDivergenceTriggerOrderRequestDto.getTicker());
            double tradePrice = createDivergenceTriggerOrderRequestDto.getClose();
            double targetPricePercentage = (null == createDivergenceTriggerOrderRequestDto.getTpOffsetPrice() ? 0.5d : createDivergenceTriggerOrderRequestDto.getTpOffsetPrice());
            double targetPriceOffset = tradePrice * targetPricePercentage / 100.0d * (createDivergenceTriggerOrderRequestDto.getDivergenceDiff() > 0 ? 1 : -1);
            double targetPrice = tradePrice + targetPriceOffset;
            double stopLossPrice = tradePrice - (targetPriceOffset / 2.0d);
            com.trading.app.tradingapp.dto.OrderType orderType = createDivergenceTriggerOrderRequestDto.getDivergenceDiff() > 0 ? com.trading.app.tradingapp.dto.OrderType.BUY : com.trading.app.tradingapp.dto.OrderType.SELL;

            List<Order> bracketOrders = createBracketOrderWithTPSL(getBaseService().getNextOrderId(), orderType.toString(), getQuantity(tradePrice), tradePrice, targetPrice, stopLossPrice, contract, orderTrigger, createDivergenceTriggerOrderRequestDto.getInterval());

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
    public CreateSetOrderResponseDto createOrder(CreatePivotBreakOrderRequestDto createPivotBreakOrderRequestDto, String orderTrigger) {
        LOGGER.info(JsonSerializer.serialize(createPivotBreakOrderRequestDto));

        try {
            EClientSocket eClientSocket = getBaseService().getConnection();
            Contract contract = getBaseService().createStockContract(createPivotBreakOrderRequestDto.getTicker());
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
        double orderSize = null == getDefaultOrderValue() ? DEFAULT_ORDER_VALUE : getDefaultOrderValue();
        return Math.floor(orderSize / tradePrice);
    }

    private List<Order> createBracketOrderWithTPSL(int parentOrderId, String action, double quantity, double limitPrice, double takeProfitLimitPrice, double stopLossPrice, Contract contract, String orderTrigger, String orderTriggerInterval) {

        LOGGER.info("Creating bracket order with orderTrigger [{}], parentOrderId=[{}], type=[{}], quantity=[{}], limitPrice=[{}], tp=[{}], sl=[{}], interval=[{}]", orderTrigger, parentOrderId, action, quantity, limitPrice, takeProfitLimitPrice, stopLossPrice, orderTriggerInterval);


        List<Order> bracketOrder = new ArrayList<>();

        //This will be our main or "parent" order
        Order parent = new Order();
        parent.orderId(parentOrderId);
        parent.action(action);
        parent.orderType(com.ib.client.OrderType.LMT);
        parent.totalQuantity(quantity);
        parent.lmtPrice(roundOffDoubleForPriceDecimalFormat(limitPrice));
        parent.tif(Types.TimeInForce.GTC);
        parent.outsideRth(true);
        parent.account(getTradingAccount());
        parent.transmit(false);

        bracketOrder.add(parent);
        persistOrder(parent, contract, orderTrigger, orderTriggerInterval, false);

        Order takeProfit = new Order();
        takeProfit.orderId(parent.orderId() + 1);
        takeProfit.action(action.equalsIgnoreCase("BUY") ? "SELL" : "BUY");
        takeProfit.orderType(com.ib.client.OrderType.LMT);
        takeProfit.totalQuantity(quantity);
        takeProfit.lmtPrice(roundOffDoubleForPriceDecimalFormat(takeProfitLimitPrice));
        takeProfit.tif(Types.TimeInForce.GTC);
        takeProfit.outsideRth(true);
        takeProfit.parentId(parentOrderId);
        takeProfit.account(getTradingAccount());
        takeProfit.transmit(false);

        bracketOrder.add(takeProfit);
        persistOrder(takeProfit, contract, orderTrigger, orderTriggerInterval, false);

        Order stopLoss = new Order();
        stopLoss.orderId(parent.orderId() + 2);
        stopLoss.action(action.equalsIgnoreCase("BUY") ? "SELL" : "BUY");
        stopLoss.orderType(com.ib.client.OrderType.STP);
        //Stop trigger price
        stopLoss.auxPrice(roundOffDoubleForPriceDecimalFormat(stopLossPrice));
        stopLoss.tif(Types.TimeInForce.GTC);
        stopLoss.outsideRth(true);
        stopLoss.totalQuantity(quantity);
        stopLoss.account(getTradingAccount());
        stopLoss.parentId(parentOrderId);
        stopLoss.transmit(true);

        bracketOrder.add(stopLoss);
        persistOrder(stopLoss, contract, orderTrigger, orderTriggerInterval, true);

        return bracketOrder;
    }

    private List<Order> createBracketOrderWithTP(int parentOrderId, String action, double quantity, double limitPrice, double takeProfitLimitPrice, Contract contract, String orderTrigger, String orderTriggerInterval) {

        LOGGER.info("Creating bracket order with orderTrigger [{}], parentOrderId=[{}], type=[{}], quantity=[{}], limitPrice=[{}], tp=[{}], sl=[{}], interval=[{}]", orderTrigger, parentOrderId, action, quantity, limitPrice, takeProfitLimitPrice, "NOT_APPLICABLE", orderTriggerInterval);
        //This will be our main or "parent" order
        Order parent = new Order();
        parent.orderId(parentOrderId);
        parent.action(action.toUpperCase());
        parent.orderType(com.ib.client.OrderType.LMT);
        parent.totalQuantity(quantity);
        parent.lmtPrice(roundOffDoubleForPriceDecimalFormat(limitPrice));
        parent.tif(Types.TimeInForce.GTC);
        parent.outsideRth(true);
        parent.account(getTradingAccount());
        //The parent and children orders will need this attribute set to false to prevent accidental executions.
        //The LAST CHILD will have it set to true.
        parent.transmit(false);

        persistOrder(parent, contract, orderTrigger, orderTriggerInterval, false);

        List<Order> bracketOrder = new ArrayList<>();
        bracketOrder.add(parent);

        if (takeProfitLimitPrice > 0) {
            Order takeProfit = new Order();
            takeProfit.orderId(parent.orderId() + 1);
            takeProfit.action(action.equalsIgnoreCase("BUY") ? "SELL" : "BUY");
            takeProfit.orderType(com.ib.client.OrderType.LMT);
            takeProfit.totalQuantity(quantity);
            takeProfit.lmtPrice(roundOffDoubleForPriceDecimalFormat(takeProfitLimitPrice));
            takeProfit.tif(Types.TimeInForce.GTC);
            takeProfit.outsideRth(true);
            takeProfit.account(getTradingAccount());
            takeProfit.parentId(parentOrderId);
            takeProfit.transmit(true);

            persistOrder(takeProfit, contract, orderTrigger, orderTriggerInterval, false);

            bracketOrder.add(takeProfit);
        } else {
            parent.transmit(true);
        }

        return bracketOrder;
    }


    private Order updateOrder(int orderId, String action, double quantity, double limitPrice, Contract contract, String orderTrigger, String orderTriggerInterval) {

        LOGGER.info("Updating order with OrderId=[{}], target price=[{}], quantity=[{}]", orderId, limitPrice, quantity);
        Order updateOrder = new Order();
        updateOrder.orderId(orderId);
        updateOrder.action(action.toUpperCase());
        updateOrder.orderType(com.ib.client.OrderType.LMT);
        updateOrder.totalQuantity(quantity);
        updateOrder.lmtPrice(roundOffDoubleForPriceDecimalFormat(limitPrice));
        updateOrder.tif(Types.TimeInForce.GTC);
        updateOrder.outsideRth(true);
        updateOrder.account(getTradingAccount());
        updateOrder.transmit(true);

        persistOrder(updateOrder, contract, orderTrigger, orderTriggerInterval, false);


        return updateOrder;
    }


    private void persistOrder(Order order, Contract contract, String orderTrigger, String orderTriggerInterval, boolean isSlOrder, boolean isOptionsOrder, Double optionStrikePrice, String optionExpiryDate, String optionType) {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setOrderId(order.orderId());
        orderEntity.setSymbol(contract.symbol());
        orderEntity.setOrderType(order.orderType().getApiString());
        orderEntity.setOrderAction(order.getAction());
        orderEntity.setQuantity(order.totalQuantity());
        orderEntity.setTransactionPrice(isSlOrder ? order.auxPrice() : order.lmtPrice());
        orderEntity.setTimeInForce(order.tif().getApiString());
        orderEntity.setOutsideRth(order.outsideRth());
        orderEntity.setTransmit(order.transmit());
        orderEntity.setCurrency(contract.currency());
        orderEntity.setOrderTrigger(orderTrigger);
        orderEntity.setOrderTriggerInterval(orderTriggerInterval);
        orderEntity.setOptionsOrder(isOptionsOrder);

        // Set fields for Options order
        if (isOptionsOrder) {
            orderEntity.setOptionStrikePrice(optionStrikePrice);
            orderEntity.setOptionExpiryDate(optionExpiryDate);
            orderEntity.setOptionType(optionType);
        }

        // set parent order if available
        if (order.parentId() != 0) {
            getOrderRepository().findById(order.parentId()).ifPresent(orderEntity::setParentOrder);
        }
        orderEntity.setCreatedTimestamp(new java.sql.Timestamp(new Date().getTime()));
        getOrderRepository().save(orderEntity);

    }

    private void persistOrder(Order order, Contract contract, String orderTrigger, String orderTriggerInterval, boolean isSlOrder) {
        if (BaseServiceImpl.OPTIONS_TYPE.equalsIgnoreCase(contract.getSecType())) {
            persistOrder(order, contract, orderTrigger, orderTriggerInterval, isSlOrder, true, contract.strike(), contract.lastTradeDateOrContractMonth(), contract.getRight());
        } else {
            persistOrder(order, contract, orderTrigger, orderTriggerInterval, isSlOrder, false, null, null, null);
        }
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

    public String getTradingAccount() {
        return tradingAccount;
    }

    public void setTradingAccount(String tradingAccount) {
        this.tradingAccount = tradingAccount;
    }

    public Double getDefaultOrderValue() {
        return defaultOrderValue;
    }

    public void setDefaultOrderValue(Double defaultOrderValue) {
        this.defaultOrderValue = defaultOrderValue;
    }

    private double roundOffDoubleForPriceDecimalFormat(double price) {
        return Math.round(price * 100.0d) / 100.0d;
    }
}

