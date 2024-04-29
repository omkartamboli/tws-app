package com.trading.app.tradingapp.persistance.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.sql.Timestamp;

@Entity
public class ContractEntity {

    @Id
    @Column(nullable = false)
    private String symbol;

    @Column(nullable = false)
    private Integer tickerId;

    private Integer defaultQuantity;

    @Column(nullable = false)
    private String currency;

    @Column(nullable = false)
    private String exchange;

    @Column(nullable = false)
    private String secType;

    private Double ltp;

    private Double lastAsk;

    private Double lastBid;

    private Timestamp tickerAskBidLtpValuesUpdateTimestamp;

    private boolean active;

    private double step1;

    private double step2;

    private double step3;

    private double step4;

    private double step5;

    private String nextFutDate;

    private Double defaultStopLoss;

    private Integer ocaHedgeMultiplier;

    private boolean useOcaHedgeOrder;


    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Integer getTickerId() {
        return tickerId;
    }

    public void setTickerId(Integer tickerId) {
        this.tickerId = tickerId;
    }

    public Integer getDefaultQuantity() {
        return defaultQuantity;
    }

    public void setDefaultQuantity(Integer defaultQuantity) {
        this.defaultQuantity = defaultQuantity;
    }

    public Double getLtp() {
        return ltp;
    }

    public void setLtp(Double ltp) {
        this.ltp = ltp;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public String getSecType() {
        return secType;
    }

    public void setSecType(String secType) {
        this.secType = secType;
    }

    public Double getLastAsk() {
        return lastAsk;
    }

    public void setLastAsk(Double lastAsk) {
        this.lastAsk = lastAsk;
    }

    public Double getLastBid() {
        return lastBid;
    }

    public void setLastBid(Double lastBid) {
        this.lastBid = lastBid;
    }

    public double getStep1() {
        return step1;
    }

    public void setStep1(double step1) {
        this.step1 = step1;
    }

    public double getStep2() {
        return step2;
    }

    public void setStep2(double step2) {
        this.step2 = step2;
    }

    public double getStep3() {
        return step3;
    }

    public void setStep3(double step3) {
        this.step3 = step3;
    }

    public double getStep4() {
        return step4;
    }

    public void setStep4(double step4) {
        this.step4 = step4;
    }

    public double getStep5() {
        return step5;
    }

    public void setStep5(double step5) {
        this.step5 = step5;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getNextFutDate() {
        return nextFutDate;
    }

    public void setNextFutDate(String nextFutDate) {
        this.nextFutDate = nextFutDate;
    }

    public Double getDefaultStopLoss() {
        return defaultStopLoss;
    }

    public void setDefaultStopLoss(Double defaultStopLoss) {
        this.defaultStopLoss = defaultStopLoss;
    }

    public Integer getOcaHedgeMultiplier() {
        return ocaHedgeMultiplier;
    }

    public void setOcaHedgeMultiplier(Integer ocaHedgeMultiplier) {
        this.ocaHedgeMultiplier = ocaHedgeMultiplier;
    }

    public boolean isUseOcaHedgeOrder() {
        return useOcaHedgeOrder;
    }

    public void setUseOcaHedgeOrder(boolean useOcaHedgeOrder) {
        this.useOcaHedgeOrder = useOcaHedgeOrder;
    }

    public Timestamp getTickerAskBidLtpValuesUpdateTimestamp() {
        return tickerAskBidLtpValuesUpdateTimestamp;
    }

    public void setTickerAskBidLtpValuesUpdateTimestamp(Timestamp tickerAskBidLtpValuesUpdateTimestamp) {
        this.tickerAskBidLtpValuesUpdateTimestamp = tickerAskBidLtpValuesUpdateTimestamp;
    }
}
