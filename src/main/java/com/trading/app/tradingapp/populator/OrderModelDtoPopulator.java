package com.trading.app.tradingapp.populator;

import com.ib.client.Contract;
import com.ib.client.Order;
import com.trading.app.tradingapp.dto.model.OrderModelDto;
import com.trading.app.tradingapp.dto.request.UpdateSetOrderRequestDto;
import com.trading.app.tradingapp.persistance.entity.OrderEntity;
import org.springframework.stereotype.Component;

@Component
public class OrderModelDtoPopulator {

    public static final String OPTIONS_TYPE = "OPT";

    public static final String STRADDLE_TYPE = "BAG";

    public OrderModelDto populate(OrderEntity orderEntity) {
        if (orderEntity != null) {
            OrderModelDto orderModelDto = new OrderModelDto();

            orderModelDto.setAvgFillPrice(orderEntity.getAvgFillPrice());
            orderModelDto.setCreatedTimestamp(orderEntity.getCreatedTimestamp());
            orderModelDto.setCurrency(orderEntity.getCurrency());
            orderModelDto.setFilled(orderEntity.getFilled());
            orderModelDto.setMktCapPrice(orderEntity.getMktCapPrice());
            orderModelDto.setOrderAction(orderEntity.getOrderAction());
            orderModelDto.setOrderId(orderEntity.getOrderId());
            orderModelDto.setOrderStatus(orderEntity.getOrderStatus());
            orderModelDto.setOrderType(orderEntity.getOrderType());
            orderModelDto.setOutsideRth(orderEntity.getOutsideRth());
            orderModelDto.setParentOrder(orderEntity.getParentOrder() == null ? null : orderEntity.getParentOrder().getOrderId());
            orderModelDto.setQuantity(orderEntity.getQuantity());
            orderModelDto.setRealizedPNL(orderEntity.getRealizedPNL());
            orderModelDto.setUnrealizedPNL(orderEntity.getUnrealizedPNL());
            orderModelDto.setRemaining(orderEntity.getRemaining());
            orderModelDto.setStatusUpdateTimestamp(orderEntity.getStatusUpdateTimestamp());
            orderModelDto.setStopLossPrice(orderEntity.getStopLossPrice());
            orderModelDto.setSymbol(orderEntity.getSymbol());
            orderModelDto.setTargetPrice(orderEntity.getTargetPrice());
            orderModelDto.setTimeInForce(orderEntity.getTimeInForce());
            orderModelDto.setTransactionPrice(orderEntity.getTransactionPrice());
            orderModelDto.setTransmit(orderEntity.getTransmit());
            orderModelDto.setWhyHeld(orderEntity.getWhyHeld());
            orderModelDto.setOptionsOrder(Boolean.TRUE.equals(orderEntity.getOptionsOrder()));
            orderModelDto.setTriggerPrice(orderEntity.getStopLossTriggerPrice());


            UpdateSetOrderRequestDto updateSetOrderRequestDto = new UpdateSetOrderRequestDto();
            updateSetOrderRequestDto.setOrderId(orderEntity.getOrderId());
            updateSetOrderRequestDto.setQuantity(orderEntity.getQuantity().intValue());
            updateSetOrderRequestDto.setTargetPrice(orderEntity.getTransactionPrice());
            updateSetOrderRequestDto.setOptionsOrder(orderModelDto.getOptionsOrder());
            updateSetOrderRequestDto.setOrderType(orderEntity.getOrderType());
            updateSetOrderRequestDto.setTriggerPrice(orderEntity.getStopLossTriggerPrice());
            if (orderEntity.getParentOrder() != null) {
                updateSetOrderRequestDto.setParentOrderId(orderEntity.getParentOrder().getOrderId());
            }

            if (Boolean.TRUE.equals(orderModelDto.getOptionsOrder())) {
                orderModelDto.setOptionStrikePrice(orderEntity.getOptionStrikePrice());
                orderModelDto.setOptionExpiryDate(orderEntity.getOptionExpiryDate());
                orderModelDto.setOptionType(orderEntity.getOptionType());

                updateSetOrderRequestDto.setOptionStrikePrice(orderEntity.getOptionStrikePrice());
                updateSetOrderRequestDto.setOptionExpiryDate(orderEntity.getOptionExpiryDate());
                updateSetOrderRequestDto.setOptionType(orderEntity.getOptionType());

            }

            orderModelDto.setUpdateOrderForm(updateSetOrderRequestDto);

            return orderModelDto;
        }
        return null;

    }


    public OrderModelDto populate(Order order, Contract contract) {
        if (order != null) {
            OrderModelDto orderModelDto = new OrderModelDto();

            orderModelDto.setCurrency(contract.currency());
            orderModelDto.setFilled(order.filledQuantity());

            orderModelDto.setOrderAction(order.getAction());
            orderModelDto.setOrderId(order.orderId());

            orderModelDto.setOrderType(order.orderType().getApiString());
            orderModelDto.setOutsideRth(order.outsideRth());
            orderModelDto.setParentOrder(order.parentId() > 0 ? order.parentId() : null);
            orderModelDto.setQuantity(order.totalQuantity());


            orderModelDto.setRemaining(order.totalQuantity() - order.filledQuantity());

            orderModelDto.setStopLossPrice(order.auxPrice());
            orderModelDto.setTriggerPrice(order.auxPrice());
            orderModelDto.setSymbol(contract.symbol());

            orderModelDto.setTimeInForce(order.tif().getApiString());
            orderModelDto.setTransactionPrice(order.lmtPrice());
            orderModelDto.setTransmit(order.transmit());
            orderModelDto.setOrderType(order.getOrderType());

            orderModelDto.setOptionsOrder(OPTIONS_TYPE.equalsIgnoreCase(contract.getSecType()) || STRADDLE_TYPE.equalsIgnoreCase(contract.getSecType()));


            UpdateSetOrderRequestDto updateSetOrderRequestDto = new UpdateSetOrderRequestDto();
            updateSetOrderRequestDto.setOrderId(order.orderId());
            updateSetOrderRequestDto.setQuantity((int)order.totalQuantity());
            updateSetOrderRequestDto.setTargetPrice(order.lmtPrice());
            updateSetOrderRequestDto.setOptionsOrder(orderModelDto.getOptionsOrder());
            updateSetOrderRequestDto.setOrderType(order.getOrderType());
            updateSetOrderRequestDto.setTriggerPrice(order.auxPrice());
               updateSetOrderRequestDto.setParentOrderId(orderModelDto.getParentOrder());


            if (Boolean.TRUE.equals(orderModelDto.getOptionsOrder())) {
                orderModelDto.setOptionStrikePrice(contract.strike());
                orderModelDto.setOptionExpiryDate(contract.lastTradeDateOrContractMonth());
                orderModelDto.setOptionType(contract.right().getApiString());

                updateSetOrderRequestDto.setOptionStrikePrice(contract.strike());
                updateSetOrderRequestDto.setOptionExpiryDate(contract.lastTradeDateOrContractMonth());
                updateSetOrderRequestDto.setOptionType(contract.right().getApiString());

            }

            orderModelDto.setUpdateOrderForm(updateSetOrderRequestDto);

            return orderModelDto;
        }
        return null;
    }
}
