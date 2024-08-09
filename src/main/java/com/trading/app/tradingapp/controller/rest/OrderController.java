package com.trading.app.tradingapp.controller.rest;

import com.trading.app.tradingapp.dto.request.*;
import com.trading.app.tradingapp.dto.response.CreateOptionsOrderResponseDto;
import com.trading.app.tradingapp.dto.response.CreateOrderResponseDto;
import com.trading.app.tradingapp.dto.response.CreateSetOrderResponseDto;
import com.trading.app.tradingapp.dto.response.UpdateSetOrderResponseDto;
import com.trading.app.tradingapp.service.OrderService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Date;


@RestController
@RequestMapping("/orders")
public class OrderController {

    @Resource
    OrderService orderService;

    private static final String MANUAL_ORDER = "Manual Order";

    private static final String DIVERGENCE_ORDER = "Automated Order - DIVERGENCE_ORDER";

    private static final String PIVOT_BREAK_ORDER = "Automated Order - PIVOT_BREAK_ORDER";

    private static final String PIVOT_AND_VOLUME_BREAK_ORDER = "Automated Order - PIVOT_AND_VOLUME_BREAK_ORDER";

    private static final String OT_STAR_ORDER = "Automated Order - OT_STAR_ORDER";

    private static final String OT_MACD_ORDER = "Automated Order - OT_MACD_ORDER";

    private static final String RKL_TRADE = "Automated Order - RKL_TRADE";

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

    @PostMapping("/rkltrade")
    @ResponseBody
    public CreateSetOrderResponseDto CreateRKLTradeOrder(@RequestBody RKLTradeOrderRequestDto rklTradeOrderRequestDto){
        return  orderService.createOrder(rklTradeOrderRequestDto, RKL_TRADE);
    }

    @PostMapping("/otstartrade")
    public void CreateOTStarTradeOrder(@RequestBody OtStarTradeOrderRequestDto otStarTradeOrderRequestDto){
        orderService.createOTSOrder(otStarTradeOrderRequestDto, OT_STAR_ORDER);
    }

    @PostMapping("/otstartradeForPostman")
    @ResponseBody
    public CreateSetOrderResponseDto CreateOTStarTradeOrderForPostman(@RequestBody OtStarTradeOrderRequestDto otStarTradeOrderRequestDto){
        return orderService.createOTSOrder(otStarTradeOrderRequestDto, OT_STAR_ORDER);
    }

    @PostMapping("/otmacdtrade")
    public void CreateOTMacdTradeOrder(@RequestBody OTMacdOrderRequestDto otMacdOrderRequestDto){
        orderService.createOTMacdOrder(otMacdOrderRequestDto, OT_MACD_ORDER + "_" + otMacdOrderRequestDto.getInterval() + "MIN" + "_" + otMacdOrderRequestDto.getTradeStartSequenceId());
    }

    @PostMapping("/otmacdtradeForPostman")
    public CreateSetOrderResponseDto CreateOTMacdTradeOrderForPostman(@RequestBody OTMacdOrderRequestDto otMacdOrderRequestDto){
        return orderService.createOTMacdOrder(otMacdOrderRequestDto, OT_MACD_ORDER + "_" + otMacdOrderRequestDto.getInterval() + "MIN" + "_" + otMacdOrderRequestDto.getTradeStartSequenceId());
    }
}
