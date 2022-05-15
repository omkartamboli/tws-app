package com.trading.app.tradingapp.dto.request;

public class UpdateSetOrderRequestDto {

    private int orderId;

    private Double targetPrice;

    private Double triggerPrice;

    private Integer quantity;

    private Integer parentOrderId;

    private Boolean isOptionsOrder;

    private Double optionStrikePrice;

    private String optionExpiryDate;

    private String optionType; // CALL or PUT

    private String orderType;



    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public Double getTargetPrice() {
        return targetPrice;
    }

    public void setTargetPrice(Double targetPrice) {
        this.targetPrice = targetPrice;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getParentOrderId() {
        return parentOrderId;
    }

    public void setParentOrderId(Integer parentOrderId) {
        this.parentOrderId = parentOrderId;
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

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public Double getTriggerPrice() {
        return triggerPrice;
    }

    public void setTriggerPrice(Double triggerPrice) {
        this.triggerPrice = triggerPrice;
    }
}
