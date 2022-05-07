package com.trading.app.tradingapp.populator;

import com.trading.app.tradingapp.dto.model.OrderModelDto;
import com.trading.app.tradingapp.dto.request.UpdateSetOrderRequestDto;
import com.trading.app.tradingapp.persistance.entity.OrderEntity;
import org.springframework.stereotype.Component;

@Component
public class OrderModelDtoPopulator {

    public OrderModelDto populate(OrderEntity orderEntity){
        if(orderEntity != null) {
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
            orderModelDto.setParentOrder(orderEntity.getParentOrder()==null?null:orderEntity.getParentOrder().getOrderId());
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


            UpdateSetOrderRequestDto updateSetOrderRequestDto = new UpdateSetOrderRequestDto();
            updateSetOrderRequestDto.setOrderId(orderEntity.getOrderId());
            updateSetOrderRequestDto.setQuantity(orderEntity.getQuantity().intValue());
            updateSetOrderRequestDto.setTargetPrice(orderEntity.getTransactionPrice());
            updateSetOrderRequestDto.setOptionsOrder(orderModelDto.getOptionsOrder());
            if(orderEntity.getParentOrder()!=null) {
                updateSetOrderRequestDto.setParentOrderId(orderEntity.getParentOrder().getOrderId());
            }

            if(orderModelDto.getOptionsOrder()){
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
}
