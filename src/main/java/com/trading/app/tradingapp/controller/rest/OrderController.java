package com.trading.app.tradingapp.controller.rest;

import com.trading.app.tradingapp.dto.request.*;
import com.trading.app.tradingapp.dto.response.CreateOptionsOrderResponseDto;
import com.trading.app.tradingapp.dto.response.CreateOrderResponseDto;
import com.trading.app.tradingapp.dto.response.CreateSetOrderResponseDto;
import com.trading.app.tradingapp.dto.response.UpdateSetOrderResponseDto;
import com.trading.app.tradingapp.service.OrderService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;


@RestController
@RequestMapping("/orders")
public class OrderController {

    @Resource
    OrderService orderService;

    private static final String MANUAL_ORDER = "Manual Order";

    private static final String DIVERGENCE_ORDER = "Automated Order - DIVERGENCE_ORDER";

    private static final String PIVOT_BREAK_ORDER = "Automated Order - PIVOT_BREAK_ORDER";

    private static final String PIVOT_AND_VOLUME_BREAK_ORDER = "Automated Order - PIVOT_AND_VOLUME_BREAK_ORDER";

    @PostMapping("/setOrder")
    @ResponseBody
    public CreateSetOrderResponseDto createSetOrder(@RequestBody CreateSetOrderRequestDto createSetOrderRequestDto){
        return orderService.createOrder(createSetOrderRequestDto, MANUAL_ORDER, null);
    }

    @PostMapping("/createOptionsOrder")
    @ResponseBody
    public CreateOptionsOrderResponseDto createOptionsOrder(@RequestBody CreateOptionsOrderRequestDto createOptionsOrderRequestDto){
        return orderService.createOptionsOrder(createOptionsOrderRequestDto, MANUAL_ORDER, null, false);
    }

    @PostMapping("/updateSetOrder")
    @ResponseBody
    public UpdateSetOrderResponseDto updateSetOrder(@RequestBody UpdateSetOrderRequestDto updateSetOrderRequestDto){
        return orderService.updateOrder(updateSetOrderRequestDto);
    }

    @PostMapping("/ltpOrder")
    @ResponseBody
    public CreateOrderResponseDto createLtpOrder(@RequestBody CreateLtpOrderRequestDto createLtpOrderRequestDto){
        return orderService.createOrder(createLtpOrderRequestDto, MANUAL_ORDER, null);
    }

    @PostMapping("/divergenceOrder")
    @ResponseBody
    public CreateSetOrderResponseDto CreateDivergenceTriggerOrder(@RequestBody CreateDivergenceTriggerOrderRequestDto createDivergenceTriggerOrderRequestDto){
        return  orderService.createOrder(createDivergenceTriggerOrderRequestDto, DIVERGENCE_ORDER);
    }

    @PostMapping("/pivotBreakOrder")
    @ResponseBody
    public CreateSetOrderResponseDto CreatePivotBreakOrder(@RequestBody CreatePivotBreakOrderRequestDto createPivotBreakOrderRequestDto){
        return  orderService.createOrder(createPivotBreakOrderRequestDto, PIVOT_BREAK_ORDER);
    }

    @PostMapping("/pivotAndVolumeBreakOrder")
    @ResponseBody
    public CreateSetOrderResponseDto CreatePivotAndVolumeBreakOrder(@RequestBody CreatePivotAndVolumeBreakOrderRequestDto createPivotAndVolumeBreakOrderRequestDto){
        return  orderService.createOrder(createPivotAndVolumeBreakOrderRequestDto, PIVOT_AND_VOLUME_BREAK_ORDER);
    }

}
