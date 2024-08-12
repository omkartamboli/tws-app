package com.trading.app.tradingapp.service;

import com.ib.client.EClientSocket;
import com.trading.app.tradingapp.dto.request.*;
import com.trading.app.tradingapp.dto.response.CreateOptionsOrderResponseDto;
import com.trading.app.tradingapp.dto.response.CreateOrderResponseDto;
import com.trading.app.tradingapp.dto.response.CreateSetOrderResponseDto;
import com.trading.app.tradingapp.dto.response.UpdateSetOrderResponseDto;
import com.trading.app.tradingapp.persistance.entity.OrderEntity;


public interface OrderService {
    CreateSetOrderResponseDto createOrder (CreateSetOrderRequestDto createSetOrderRequestDto, String orderTrigger, String orderTriggerInterval);

    CreateSetOrderResponseDto createSLOrder (CreateSetOrderRequestDto createSetOrderRequestDto, String orderTrigger, String orderTriggerInterval);

    CreateOptionsOrderResponseDto createOptionsOrder (CreateOptionsOrderRequestDto createOptionsOrderRequestDto, String orderTrigger, String orderTriggerInterval, boolean isStraddle);

    CreateOrderResponseDto createOrder (CreateLtpOrderRequestDto createLtpOrderRequestDto, String orderTrigger, String orderTriggerInterval);

    CreateSetOrderResponseDto createOrder(CreateDivergenceTriggerOrderRequestDto createDivergenceTriggerOrderRequestDto, String orderTrigger);

    CreateSetOrderResponseDto createOrder(CreatePivotBreakOrderRequestDto createPivotBreakOrderRequestDto, String orderTrigger);

    CreateSetOrderResponseDto createOrder(RKLTradeOrderRequestDto rklTradeOrderRequestDto, String orderTrigger);

    CreateSetOrderResponseDto createOTMacdOrder(OTMacdOrderRequestDto otMacdOrderRequestDto, String orderTrigger) throws Exception;

    CreateSetOrderResponseDto createOTSOrder(OtStarTradeOrderRequestDto otStarTradeOrderRequestDto, String orderTrigger);

    UpdateSetOrderResponseDto updateOrder(UpdateSetOrderRequestDto updateSetOrderRequestDto);

    UpdateSetOrderResponseDto cancelOrder(UpdateSetOrderRequestDto updateSetOrderRequestDto);

    void cancelOrder(int orderId);

    void deleteAllInactiveOrders();

    OrderEntity getSingleOrderBySlCheckSequenceIdAndTrigger(Long slCheckSequenceId, String trigger);

    int findOutstandingQtyForTickerWithSpecificOrderTrigger(String ticker, String orderTrigger, String interval);

    void updateOrderQuantity(int orderId, OrderEntity orderEntity, double newQuantity, EClientSocket eClientSocket);
}
