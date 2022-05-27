package com.trading.app.tradingapp.controller;

import com.trading.app.tradingapp.dto.OptionType;
import com.trading.app.tradingapp.dto.OrderType;
import com.trading.app.tradingapp.dto.form.CreateSetOrderFormDto;
import com.trading.app.tradingapp.dto.form.TickerFormsGroup;

import com.trading.app.tradingapp.dto.request.CreateOptionsOrderRequestDto;
import com.trading.app.tradingapp.dto.request.CreateSetOrderRequestDto;
import com.trading.app.tradingapp.dto.request.UpdateSetOrderRequestDto;
import com.trading.app.tradingapp.service.ContractService;
import com.trading.app.tradingapp.service.DashboardService;
import com.trading.app.tradingapp.service.OrderService;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.view.RedirectView;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import java.util.Map;

@Controller
public class DashboardController {

    @Resource
    private DashboardService dashboardService;

    @Resource
    private OrderService orderService;

    @Resource
    private ContractService contractService;

    private static final String DEFAULT_SYMBOL = "TSLA";

    //private static final Logger LOGGER = LoggerFactory.getLogger(DashboardController.class);

    private static final String MANUAL_ORDER = "Manual Order";

    @GetMapping("/dashboard/old")
    public String getDashboard(Model model, HttpSession httpSession) {
        // Add Ticker and Orders data
        Map<CreateSetOrderFormDto, TickerFormsGroup> tickerFormsGroupMap = getDashboardService().getTickerOrderModelMap();
        model.addAttribute("tickerFormsGroupMap", tickerFormsGroupMap);
        model.addAttribute("tickerFormsList", tickerFormsGroupMap.keySet());
        model.addAttribute("activeSymbol", getLastAccessedSymbol(httpSession));
        return "dashboardOld";
    }

    @GetMapping("/dashboard")
    public String getDashboardTab(Model model, HttpSession httpSession) {
        // Add Ticker and Orders data
        Map<CreateSetOrderFormDto, TickerFormsGroup> tickerFormsGroupMap = getDashboardService().getTickerOrderModelMap();
        model.addAttribute("tickerFormsGroupMap", tickerFormsGroupMap);
        model.addAttribute("tickerFormsList", tickerFormsGroupMap.keySet());
        model.addAttribute("activeSymbol", getLastAccessedSymbol(httpSession));
        return "dashboard";
    }

    @PostMapping("/dashboard/createSetOrder")
    public RedirectView createSetOrder(@ModelAttribute CreateSetOrderRequestDto createSetOrderRequestDto, Model model) {

        // place order using form
        getOrderService().createOrder(createSetOrderRequestDto, MANUAL_ORDER, null);

        return new RedirectView("/dashboard");
    }

    @PostMapping(value = "/dashboard/createOptionsOrder", params = "callBuy")
    public RedirectView createOptionsOrderCallBuy(@ModelAttribute CreateOptionsOrderRequestDto createOptionsOrderRequestDto, Model model) {

        createOptionsOrderRequestDto.setOrderType(OrderType.BUY);
        createOptionsOrderRequestDto.setOptionType(OptionType.CALL);

        return createOptionsOrder(createOptionsOrderRequestDto, model, false);
    }

    @PostMapping(value = "/dashboard/createOptionsOrder", params = "callSell")
    public RedirectView createOptionsOrderCallSell(@ModelAttribute CreateOptionsOrderRequestDto createOptionsOrderRequestDto, Model model) {

        createOptionsOrderRequestDto.setOrderType(OrderType.SELL);
        createOptionsOrderRequestDto.setOptionType(OptionType.CALL);

        return createOptionsOrder(createOptionsOrderRequestDto, model, false);
    }

    @PostMapping(value = "/dashboard/createOptionsOrder", params = "putBuy")
    public RedirectView createOptionsOrderPutBuy(@ModelAttribute CreateOptionsOrderRequestDto createOptionsOrderRequestDto, Model model) {

        createOptionsOrderRequestDto.setOrderType(OrderType.BUY);
        createOptionsOrderRequestDto.setOptionType(OptionType.PUT);

        return createOptionsOrder(createOptionsOrderRequestDto, model, false);
    }

    @PostMapping(value = "/dashboard/createOptionsOrder", params = "putSell")
    public RedirectView createOptionsOrderPutSell(@ModelAttribute CreateOptionsOrderRequestDto createOptionsOrderRequestDto, Model model) {

        createOptionsOrderRequestDto.setOrderType(OrderType.SELL);
        createOptionsOrderRequestDto.setOptionType(OptionType.PUT);

        return createOptionsOrder(createOptionsOrderRequestDto, model, false);
    }

    @PostMapping(value = "/dashboard/createOptionsOrder", params = "straddleBuy")
    public RedirectView createOptionsOrderStraddleBuy(@ModelAttribute CreateOptionsOrderRequestDto createOptionsOrderRequestDto, Model model) {

        createOptionsOrderRequestDto.setOrderType(OrderType.BUY);

        return createOptionsOrder(createOptionsOrderRequestDto, model, true);
    }

    @PostMapping(value = "/dashboard/createOptionsOrder", params = "straddleSell")
    public RedirectView createOptionsOrderStraddleSell(@ModelAttribute CreateOptionsOrderRequestDto createOptionsOrderRequestDto, Model model) {

        createOptionsOrderRequestDto.setOrderType(OrderType.SELL);

        return createOptionsOrder(createOptionsOrderRequestDto, model, true);
    }


    public RedirectView createOptionsOrder(@ModelAttribute CreateOptionsOrderRequestDto createOptionsOrderRequestDto, Model model, boolean isStraddle) {

        // place order using form
        getOrderService().createOptionsOrder(createOptionsOrderRequestDto, MANUAL_ORDER, null, isStraddle);

        return new RedirectView("/dashboard");
    }

    @PostMapping(value = "/dashboard/updateSetOrder", params = "updateOrder")
    public RedirectView updateSetOrder(@ModelAttribute UpdateSetOrderRequestDto updateSetOrderRequestDto, Model model) {

        // update order using form
        getOrderService().updateOrder(updateSetOrderRequestDto);

        return new RedirectView("/dashboard");
    }

    @PostMapping(value = "/dashboard/updateSetOrder", params = "cancelOrder")
    public RedirectView cancelSetOrder(@ModelAttribute UpdateSetOrderRequestDto updateSetOrderRequestDto, Model model) {

        // cancel order using form
        getOrderService().cancelOrder(updateSetOrderRequestDto);

        return new RedirectView("/dashboard");
    }


    @PostMapping(value = "/dashboard/createStepOrder", params = "buyStep1")
    public RedirectView createStepOrderBuyStep1(@ModelAttribute CreateSetOrderFormDto createSetOrderFormDto, Model model) {

        //LOGGER.info("CreateSetOrderFormDto = {} , {}, {}, {}",createSetOrderFormDto.getTicker(), createSetOrderFormDto.getQuantity(), createSetOrderFormDto.getTransactionPrice(), createSetOrderFormDto.getTargetPriceOffsetBuyStep1());
        // place order using form
        getOrderService().createOrder(getCreateSetOrderRequestDto(createSetOrderFormDto, OrderType.BUY, createSetOrderFormDto.getTargetPriceOffsetBuyStep1()), MANUAL_ORDER, null);

        return new RedirectView("/dashboard");
    }

    @PostMapping(value = "/dashboard/createStepOrder", params = "buyStep2")
    public RedirectView createStepOrderBuyStep2(@ModelAttribute CreateSetOrderFormDto createSetOrderFormDto, Model model) {

        // place order using form
        getOrderService().createOrder(getCreateSetOrderRequestDto(createSetOrderFormDto, OrderType.BUY, createSetOrderFormDto.getTargetPriceOffsetBuyStep2()), MANUAL_ORDER, null);

        return new RedirectView("/dashboard");
    }

    @PostMapping(value = "/dashboard/createStepOrder", params = "buyStep3")
    public RedirectView createStepOrderBuyStep3(@ModelAttribute CreateSetOrderFormDto createSetOrderFormDto, Model model) {

        // place order using form
        getOrderService().createOrder(getCreateSetOrderRequestDto(createSetOrderFormDto, OrderType.BUY, createSetOrderFormDto.getTargetPriceOffsetBuyStep3()), MANUAL_ORDER, null);

        return new RedirectView("/dashboard");
    }

    @PostMapping(value = "/dashboard/createStepOrder", params = "buyStep4")
    public RedirectView createStepOrderBuyStep4(@ModelAttribute CreateSetOrderFormDto createSetOrderFormDto, Model model) {

        // place order using form
        getOrderService().createOrder(getCreateSetOrderRequestDto(createSetOrderFormDto, OrderType.BUY, createSetOrderFormDto.getTargetPriceOffsetBuyStep4()), MANUAL_ORDER, null);

        return new RedirectView("/dashboard");
    }

    @PostMapping(value = "/dashboard/createStepOrder", params = "buyStep5")
    public RedirectView createStepOrderBuyStep5(@ModelAttribute CreateSetOrderFormDto createSetOrderFormDto, Model model) {

        // place order using form
        getOrderService().createOrder(getCreateSetOrderRequestDto(createSetOrderFormDto, OrderType.BUY, createSetOrderFormDto.getTargetPriceOffsetBuyStep5()), MANUAL_ORDER, null);

        return new RedirectView("/dashboard");
    }

    @PostMapping(value = "/dashboard/createStepOrder", params = "startBuyRunAction")
    public RedirectView startBuyRun(@ModelAttribute CreateSetOrderFormDto createSetOrderFormDto, Model model) {

        // start buy run sequence
        getContractService().startBuySequence(createSetOrderFormDto.getTicker(), createSetOrderFormDto.getStartBuyRunMargin(), createSetOrderFormDto.getQuantity());

        return new RedirectView("/dashboard");
    }

    @PostMapping(value = "/dashboard/createStepOrder", params = "stopBuyRunAction")
    public RedirectView stopBuyRun(@ModelAttribute CreateSetOrderFormDto createSetOrderFormDto, Model model) {

        // start buy run sequence
        getContractService().stopBuySequence(createSetOrderFormDto.getTicker());

        return new RedirectView("/dashboard");
    }


    @PostMapping(value = "/dashboard/createStepOrder", params = "sellStep1")
    public RedirectView createStepOrderSellStep1(@ModelAttribute CreateSetOrderFormDto createSetOrderFormDto, Model model) {

        // place order using form
        getOrderService().createOrder(getCreateSetOrderRequestDto(createSetOrderFormDto, OrderType.SELL, createSetOrderFormDto.getTargetPriceOffsetSellStep1()), MANUAL_ORDER, null);

        return new RedirectView("/dashboard");
    }

    @PostMapping(value = "/dashboard/createStepOrder", params = "sellStep2")
    public RedirectView createStepOrderSellStep2(@ModelAttribute CreateSetOrderFormDto createSetOrderFormDto, Model model) {

        // place order using form
        getOrderService().createOrder(getCreateSetOrderRequestDto(createSetOrderFormDto, OrderType.SELL, createSetOrderFormDto.getTargetPriceOffsetSellStep2()), MANUAL_ORDER, null);

        return new RedirectView("/dashboard");
    }

    @PostMapping(value = "/dashboard/createStepOrder", params = "sellStep3")
    public RedirectView createStepOrderSellStep3(@ModelAttribute CreateSetOrderFormDto createSetOrderFormDto, Model model) {

        // place order using form
        getOrderService().createOrder(getCreateSetOrderRequestDto(createSetOrderFormDto, OrderType.SELL, createSetOrderFormDto.getTargetPriceOffsetSellStep3()), MANUAL_ORDER, null);

        return new RedirectView("/dashboard");
    }

    @PostMapping(value = "/dashboard/createStepOrder", params = "sellStep4")
    public RedirectView createStepOrderSellStep4(@ModelAttribute CreateSetOrderFormDto createSetOrderFormDto, Model model) {

        // place order using form
        getOrderService().createOrder(getCreateSetOrderRequestDto(createSetOrderFormDto, OrderType.SELL, createSetOrderFormDto.getTargetPriceOffsetSellStep4()), MANUAL_ORDER, null);

        return new RedirectView("/dashboard");
    }

    @PostMapping(value = "/dashboard/createStepOrder", params = "sellStep5")
    public RedirectView createStepOrderSellStep5(@ModelAttribute CreateSetOrderFormDto createSetOrderFormDto, Model model) {

        // place order using form
        getOrderService().createOrder(getCreateSetOrderRequestDto(createSetOrderFormDto, OrderType.SELL, createSetOrderFormDto.getTargetPriceOffsetSellStep5()), MANUAL_ORDER, null);

        return new RedirectView("/dashboard");
    }

    @PostMapping(value = "/dashboard/createStepOrder", params = "startSellRunAction")
    public RedirectView startSellRun(@ModelAttribute CreateSetOrderFormDto createSetOrderFormDto, Model model) {

        // start buy run sequence
        getContractService().startSellSequence(createSetOrderFormDto.getTicker(), createSetOrderFormDto.getStartSellRunMargin(), createSetOrderFormDto.getQuantity());

        return new RedirectView("/dashboard");
    }

    @PostMapping(value = "/dashboard/createStepOrder", params = "stopSellRunAction")
    public RedirectView stopSellRun(@ModelAttribute CreateSetOrderFormDto createSetOrderFormDto, Model model) {

        // start buy run sequence
        getContractService().stopSellSequence(createSetOrderFormDto.getTicker());

        return new RedirectView("/dashboard");
    }


    private CreateSetOrderRequestDto getCreateSetOrderRequestDto(CreateSetOrderFormDto createSetOrderFormDto, OrderType orderType, double offSet) {

        CreateSetOrderRequestDto createSetOrderRequestDto = new CreateSetOrderRequestDto();

        createSetOrderRequestDto.setQuantity(createSetOrderFormDto.getQuantity());
        createSetOrderRequestDto.setTicker(createSetOrderFormDto.getTicker());
        createSetOrderRequestDto.setTransactionPrice(createSetOrderFormDto.getTransactionPrice());
        createSetOrderRequestDto.setOrderType(orderType);

        if (OrderType.BUY.equals(orderType)) {
            createSetOrderRequestDto.setTargetPrice(createSetOrderRequestDto.getTransactionPrice() + offSet);
        } else if (OrderType.SELL.equals(orderType)) {
            createSetOrderRequestDto.setTargetPrice(createSetOrderRequestDto.getTransactionPrice() - offSet);
        }

        if(null != createSetOrderFormDto.getStopLoss()){
            if (OrderType.BUY.equals(orderType)) {
                createSetOrderRequestDto.setStopLossPrice(createSetOrderRequestDto.getTransactionPrice() - createSetOrderFormDto.getStopLoss());
            } else if (OrderType.SELL.equals(orderType)) {
                createSetOrderRequestDto.setStopLossPrice(createSetOrderRequestDto.getTransactionPrice() + createSetOrderFormDto.getStopLoss());
            }
        }

        createSetOrderRequestDto.setTrailingStopLossAmount(createSetOrderFormDto.getTrailingStopLoss());

        return createSetOrderRequestDto;
    }


    public DashboardService getDashboardService() {
        return dashboardService;
    }

    public void setDashboardService(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    public OrderService getOrderService() {
        return orderService;
    }

    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }

    public ContractService getContractService() {
        return contractService;
    }

    public void setContractService(ContractService contractService) {
        this.contractService = contractService;
    }

    public String getLastAccessedSymbol(HttpSession httpSession) {
        return httpSession.getAttribute("activeSymbol") == null ? DEFAULT_SYMBOL : httpSession.getAttribute("activeSymbol").toString();
    }
}
