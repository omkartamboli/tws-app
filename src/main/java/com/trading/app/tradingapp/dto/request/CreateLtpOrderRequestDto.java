package com.trading.app.tradingapp.dto.request;

import com.trading.app.tradingapp.dto.OrderType;
import com.trading.app.tradingapp.dto.TradingType;

public class CreateLtpOrderRequestDto {

    private String ticker;

    private Double targetPriceOffset;

    private Double stopPriceOffset;

    private OrderType orderType;

    private Integer quantity;

    private TradingType tradingType;

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public Double getTargetPriceOffset() {
        return targetPriceOffset;
    }

    public void setTargetPriceOffset(Double targetPriceOffset) {
        this.targetPriceOffset = targetPriceOffset;
    }

    public Double getStopPriceOffset() {
        return stopPriceOffset;
    }

    public void setStopPriceOffset(Double stopPriceOffset) {
        this.stopPriceOffset = stopPriceOffset;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(OrderType orderType) {
        this.orderType = orderType;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public TradingType getTradingType() {
        return tradingType;
    }

    public void setTradingType(TradingType tradingType) {
        this.tradingType = tradingType;
    }
}
