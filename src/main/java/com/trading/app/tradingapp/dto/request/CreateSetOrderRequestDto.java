package com.trading.app.tradingapp.dto.request;

import com.trading.app.tradingapp.dto.OrderType;
import com.trading.app.tradingapp.dto.TradingType;

public class CreateSetOrderRequestDto {

    private String ticker;

    private Double transactionPrice;

    private Double targetPrice;

    private Double stopLossPrice;

    private Double trailingStopLossAmount;

    private OrderType orderType;

    private Integer quantity;

    private TradingType tradingType;

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public Double getTransactionPrice() {
        return transactionPrice;
    }

    public void setTransactionPrice(Double transactionPrice) {
        this.transactionPrice = transactionPrice;
    }

    public Double getTargetPrice() {
        return targetPrice;
    }

    public void setTargetPrice(Double targetPrice) {
        this.targetPrice = targetPrice;
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

    public Double getStopLossPrice() {
        return stopLossPrice;
    }

    public void setStopLossPrice(Double stopLossPrice) {
        this.stopLossPrice = stopLossPrice;
    }

    public Double getTrailingStopLossAmount() {
        return trailingStopLossAmount;
    }

    public void setTrailingStopLossAmount(Double trailingStopLossAmount) {
        this.trailingStopLossAmount = trailingStopLossAmount;
    }
}
