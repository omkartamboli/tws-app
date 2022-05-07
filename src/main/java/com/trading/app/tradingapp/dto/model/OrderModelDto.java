package com.trading.app.tradingapp.dto.model;

import com.trading.app.tradingapp.dto.request.UpdateSetOrderRequestDto;

import java.util.Date;

public class OrderModelDto {

    private String symbol;

    private Integer orderId;

    private String orderType; // LMT, MKT etc.

    private String orderAction; // BUY or SELL

    private String timeInForce;

    private Boolean outsideRth;

    private Double quantity;

    private Double transactionPrice;

    private Double targetPrice;

    private Double stopLossPrice;

    private Integer parentOrder;

    private String currency;

    private Date createdTimestamp;

    private String orderStatus;

    private Double unrealizedPNL;

    private Double realizedPNL;

    private Double filled;

    private Double remaining;

    private Double avgFillPrice;

    private String whyHeld;

    private Double mktCapPrice;

    private Boolean transmit;

    private Date statusUpdateTimestamp;

    private Boolean isOptionsOrder;

    private Double optionStrikePrice;

    private String optionExpiryDate;

    private String optionType; // CALL or PUT


    private UpdateSetOrderRequestDto updateOrderForm;

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public String getOrderAction() {
        return orderAction;
    }

    public void setOrderAction(String orderAction) {
        this.orderAction = orderAction;
    }

    public String getTimeInForce() {
        return timeInForce;
    }

    public void setTimeInForce(String timeInForce) {
        this.timeInForce = timeInForce;
    }

    public Boolean getOutsideRth() {
        return outsideRth;
    }

    public void setOutsideRth(Boolean outsideRth) {
        this.outsideRth = outsideRth;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
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

    public Double getStopLossPrice() {
        return stopLossPrice;
    }

    public void setStopLossPrice(Double stopLossPrice) {
        this.stopLossPrice = stopLossPrice;
    }

    public Integer getParentOrder() {
        return parentOrder;
    }

    public void setParentOrder(Integer parentOrder) {
        this.parentOrder = parentOrder;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Date getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(Date createdTimestamp) {
        this.createdTimestamp = createdTimestamp;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public Double getUnrealizedPNL() {
        return unrealizedPNL;
    }

    public void setUnrealizedPNL(Double unrealizedPNL) {
        this.unrealizedPNL = unrealizedPNL;
    }

    public Double getRealizedPNL() {
        return realizedPNL;
    }

    public void setRealizedPNL(Double realizedPNL) {
        this.realizedPNL = realizedPNL;
    }

    public Double getFilled() {
        return filled;
    }

    public void setFilled(Double filled) {
        this.filled = filled;
    }

    public Double getRemaining() {
        return remaining;
    }

    public void setRemaining(Double remaining) {
        this.remaining = remaining;
    }

    public Double getAvgFillPrice() {
        return avgFillPrice;
    }

    public void setAvgFillPrice(Double avgFillPrice) {
        this.avgFillPrice = avgFillPrice;
    }

    public String getWhyHeld() {
        return whyHeld;
    }

    public void setWhyHeld(String whyHeld) {
        this.whyHeld = whyHeld;
    }

    public Double getMktCapPrice() {
        return mktCapPrice;
    }

    public void setMktCapPrice(Double mktCapPrice) {
        this.mktCapPrice = mktCapPrice;
    }

    public Boolean getTransmit() {
        return transmit;
    }

    public void setTransmit(Boolean transmit) {
        this.transmit = transmit;
    }

    public Date getStatusUpdateTimestamp() {
        return statusUpdateTimestamp;
    }

    public void setStatusUpdateTimestamp(Date statusUpdateTimestamp) {
        this.statusUpdateTimestamp = statusUpdateTimestamp;
    }

    public UpdateSetOrderRequestDto getUpdateOrderForm() {
        return updateOrderForm;
    }

    public void setUpdateOrderForm(UpdateSetOrderRequestDto updateOrderForm) {
        this.updateOrderForm = updateOrderForm;
    }

    public Boolean getOptionsOrder() {
        return isOptionsOrder;
    }

    public void setOptionsOrder(Boolean optionsOrder) {
        isOptionsOrder = optionsOrder;
    }

    public Double getOptionStrikePrice() {
        return optionStrikePrice;
    }

    public void setOptionStrikePrice(Double optionStrikePrice) {
        this.optionStrikePrice = optionStrikePrice;
    }

    public String getOptionExpiryDate() {
        return optionExpiryDate;
    }

    public void setOptionExpiryDate(String optionExpiryDate) {
        this.optionExpiryDate = optionExpiryDate;
    }

    public String getOptionType() {
        return optionType;
    }

    public void setOptionType(String optionType) {
        this.optionType = optionType;
    }
}
