package com.trading.app.tradingapp.service;

import com.trading.app.tradingapp.dto.request.*;
import com.trading.app.tradingapp.dto.response.CreateOptionsOrderResponseDto;
import com.trading.app.tradingapp.dto.response.CreateOrderResponseDto;
import com.trading.app.tradingapp.dto.response.CreateSetOrderResponseDto;
import com.trading.app.tradingapp.dto.response.UpdateSetOrderResponseDto;


public interface OrderService {
    CreateSetOrderResponseDto createOrder (CreateSetOrderRequestDto createSetOrderRequestDto, String orderTrigger, String orderTriggerInterval);

    CreateSetOrderResponseDto createSLOrder (CreateSetOrderRequestDto createSetOrderRequestDto, String orderTrigger, String orderTriggerInterval);

    CreateOptionsOrderResponseDto createOptionsOrder (CreateOptionsOrderRequestDto createOptionsOrderRequestDto, String orderTrigger, String orderTriggerInterval, boolean isStraddle);

    CreateOrderResponseDto createOrder (CreateLtpOrderRequestDto createLtpOrderRequestDto, String orderTrigger, String orderTriggerInterval);

    CreateSetOrderResponseDto createOrder(CreateDivergenceTriggerOrderRequestDto createDivergenceTriggerOrderRequestDto, String orderTrigger);

    CreateSetOrderResponseDto createOrder(CreatePivotBreakOrderRequestDto createPivotBreakOrderRequestDto, String orderTrigger);

    CreateSetOrderResponseDto createOrder(RKLTradeOrderRequestDto rklTradeOrderRequestDto, String orderTrigger);

    CreateSetOrderResponseDto createOTSOrder(OtStarTradeOrderRequestDto otStarTradeOrderRequestDto, String orderTrigger);

    UpdateSetOrderResponseDto updateOrder(UpdateSetOrderRequestDto updateSetOrderRequestDto);

    UpdateSetOrderResponseDto cancelOrder(UpdateSetOrderRequestDto updateSetOrderRequestDto);

    void deleteAllInactiveOrders();
}
