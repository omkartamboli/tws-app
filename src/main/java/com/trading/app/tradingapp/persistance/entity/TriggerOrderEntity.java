package com.trading.app.tradingapp.persistance.entity;

import org.hibernate.annotations.ColumnDefault;

import javax.persistence.*;

@Entity
@Table(indexes = {
        @Index(name = "id_index", columnList = "pk", unique = true),
        @Index(name = "symbol_index", columnList = "symbol"),
        @Index(name = "active_index", columnList = "active"),
        @Index(name = "transaction_price_index", columnList = "transactionPrice"),
        @Index(name = "order_type_index", columnList = "orderType"),
        @Index(name = "order_action_index", columnList = "orderAction"),
        @Index(name = "sequence_id_index", columnList = "sequenceId"),
})
public class TriggerOrderEntity {

    @Column(nullable = false)
    private String symbol;

    @Id @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long pk;

    private Integer orderId;

    @Column(nullable = true)
    private String sequenceId;

    @Column(nullable = true)
    private String orderTriggerInterval;

    @Column(nullable = false)
    private String orderType; // LMT, MKT etc.

    @Column(nullable = false)
    private String orderAction; // BUY or SELL

    @Column(nullable = false)
    private Double orderTriggerPrice;

    @Column(nullable = false)
    private Double transactionPrice;

    private Double trailingAmount;

    @Column(nullable = false)
    private Integer originalQuantity;

    private Integer filledQuantity;

    @OneToOne
    private TriggerOrderEntity ocaTriggerOrderEntity;

    @ManyToOne
    private OrderEntity parentOrder;

    private Boolean active;

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

    public String getSequenceId() {
        return sequenceId;
    }

    public void setSequenceId(String sequenceId) {
        this.sequenceId = sequenceId;
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

    public Double getOrderTriggerPrice() {
        return orderTriggerPrice;
    }

    public void setOrderTriggerPrice(Double orderTriggerPrice) {
        this.orderTriggerPrice = orderTriggerPrice;
    }

    public Double getTransactionPrice() {
        return transactionPrice;
    }

    public void setTransactionPrice(Double transactionPrice) {
        this.transactionPrice = transactionPrice;
    }

    public Double getTrailingAmount() {
        return trailingAmount;
    }

    public void setTrailingAmount(Double trailingAmount) {
        this.trailingAmount = trailingAmount;
    }

    public TriggerOrderEntity getOcaTriggerOrderEntity() {
        return ocaTriggerOrderEntity;
    }

    public void setOcaTriggerOrderEntity(TriggerOrderEntity ocaTriggerOrderEntity) {
        this.ocaTriggerOrderEntity = ocaTriggerOrderEntity;
    }

    public OrderEntity getParentOrder() {
        return parentOrder;
    }

    public void setParentOrder(OrderEntity parentOrder) {
        this.parentOrder = parentOrder;
    }

    public Long getPk() {
        return pk;
    }

    public void setPk(Long pk) {
        this.pk = pk;
    }

    public Integer getOriginalQuantity() {
        return originalQuantity;
    }

    public void setOriginalQuantity(Integer originalQuantity) {
        this.originalQuantity = originalQuantity;
    }

    public Integer getFilledQuantity() {
        return filledQuantity;
    }

    public void setFilledQuantity(Integer filledQuantity) {
        this.filledQuantity = filledQuantity;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getOrderTriggerInterval() {
        return orderTriggerInterval;
    }

    public void setOrderTriggerInterval(String orderTriggerInterval) {
        this.orderTriggerInterval = orderTriggerInterval;
    }
}
