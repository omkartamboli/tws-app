package com.trading.app.tradingapp.persistance.entity;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;

@Entity
public class OrderEntity {

    @Column(nullable = false)
    private String symbol;

    @Id
    @Column(nullable = false)
    private Integer orderId;

    @Column(nullable = true)
    private String sequenceId;

    @Column(nullable = true)
    private String otsOrderType;

    @Column(nullable = false)
    private String orderType; // LMT, MKT etc.

    @Column(nullable = false)
    private String orderAction; // BUY or SELL

    private String orderTrigger;

    private String orderTriggerInterval;

    private String timeInForce;

    private Boolean outsideRth;

    @Column(nullable = false)
    private Double quantity;

    @Column(nullable = false)
    private Double transactionPrice;

    private Double targetPrice;

    private Double stopLossPrice;

    private Double stopLossTriggerPrice;

    @ManyToOne
    private OrderEntity parentOrder;

    @ManyToOne
    private OrderEntity parentOcaOrder;

    @Column(nullable = false)
    private String currency;

    private Boolean isOptionsOrder;

    private Double optionStrikePrice;

    private String optionExpiryDate;

    private String optionType; // CALL or PUT

    @Column(nullable = false)
    private Timestamp createdTimestamp;

    private String orderStatus;

    private Double unrealizedPNL;

    private Double realizedPNL;

    private Double filled;

    private Double remaining;

    private Double avgFillPrice;

    private String whyHeld;

    private Double mktCapPrice;

    private Boolean transmit;

    private Timestamp statusUpdateTimestamp;

    private Integer ocaHedgeMultiplier;

    @OneToMany(targetEntity=OrderEntity.class, mappedBy="parentOcaOrder", fetch=FetchType.EAGER)
    @ElementCollection
    private List<OrderEntity> ocaOrders;

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

    public OrderEntity getParentOrder() {
        return parentOrder;
    }

    public void setParentOrder(OrderEntity parentOrder) {
        this.parentOrder = parentOrder;
    }

    public Timestamp getCreatedTimestamp() {
        return createdTimestamp;
    }

    public void setCreatedTimestamp(Timestamp createdTimestamp) {
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

    public Timestamp getStatusUpdateTimestamp() {
        return statusUpdateTimestamp;
    }

    public void setStatusUpdateTimestamp(Timestamp statusUpdateTimestamp) {
        this.statusUpdateTimestamp = statusUpdateTimestamp;
    }

    public Boolean getTransmit() {
        return transmit;
    }

    public void setTransmit(Boolean transmit) {
        this.transmit = transmit;
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

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getOrderTrigger() {
        return orderTrigger;
    }

    public void setOrderTrigger(String orderTrigger) {
        this.orderTrigger = orderTrigger;
    }

    public String getOrderTriggerInterval() {
        return orderTriggerInterval;
    }

    public void setOrderTriggerInterval(String orderTriggerInterval) {
        this.orderTriggerInterval = orderTriggerInterval;
    }

    public Boolean getOptionsOrder() {
        return Boolean.TRUE.equals(isOptionsOrder);
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

    public Double getStopLossTriggerPrice() {
        return stopLossTriggerPrice;
    }

    public void setStopLossTriggerPrice(Double stopLossTriggerPrice) {
        this.stopLossTriggerPrice = stopLossTriggerPrice;
    }

    public List<OrderEntity> getOcaOrders() {
        return ocaOrders;
    }

    public void setOcaOrders(List<OrderEntity> ocaOrders) {
        this.ocaOrders = ocaOrders;
    }

    public OrderEntity getParentOcaOrder() {
        return parentOcaOrder;
    }

    public void setParentOcaOrder(OrderEntity parentOcaOrder) {
        this.parentOcaOrder = parentOcaOrder;
    }

    public Integer getOcaHedgeMultiplier() {
        return ocaHedgeMultiplier;
    }

    public void setOcaHedgeMultiplier(Integer ocaHedgeMultiplier) {
        this.ocaHedgeMultiplier = ocaHedgeMultiplier;
    }

    public String getSequenceId() {
        return sequenceId;
    }

    public void setSequenceId(String sequenceId) {
        this.sequenceId = sequenceId;
    }

    public String getOtsOrderType() {
        return otsOrderType;
    }

    public void setOtsOrderType(String otsOrderType) {
        this.otsOrderType = otsOrderType;
    }
}
