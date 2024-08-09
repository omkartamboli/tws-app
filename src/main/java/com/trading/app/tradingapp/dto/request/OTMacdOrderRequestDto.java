package com.trading.app.tradingapp.dto.request;

public class OTMacdOrderRequestDto {

    String sequenceId;
    String otsOrderType;
    String time;
    String orderType;
    Double entry;
    Double high;
    Double low;
    Integer quantity;
    Double slQuantity;
    Double slForAvg;
    Long prevSequenceId;
    Long currentSequenceId;
    Long tradeStartSequenceId;
    String ticker;
    String interval;


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

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public Double getEntry() {
        return entry;
    }

    public void setEntry(Double entry) {
        this.entry = entry;
    }

    public Double getHigh() {
        return high;
    }

    public void setHigh(Double high) {
        this.high = high;
    }

    public Double getLow() {
        return low;
    }

    public void setLow(Double low) {
        this.low = low;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getSlForAvg() {
        return slForAvg;
    }

    public void setSlForAvg(Double slForAvg) {
        this.slForAvg = slForAvg;
    }

    public Long getPrevSequenceId() {
        return prevSequenceId;
    }

    public void setPrevSequenceId(Long prevSequenceId) {
        this.prevSequenceId = prevSequenceId;
    }

    public Long getCurrentSequenceId() {
        return currentSequenceId;
    }

    public void setCurrentSequenceId(Long currentSequenceId) {
        this.currentSequenceId = currentSequenceId;
    }

    public Long getTradeStartSequenceId() {
        return tradeStartSequenceId;
    }

    public void setTradeStartSequenceId(Long tradeStartSequenceId) {
        this.tradeStartSequenceId = tradeStartSequenceId;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

    public Double getSlQuantity() {
        return slQuantity;
    }

    public void setSlQuantity(Double slQuantity) {
        this.slQuantity = slQuantity;
    }
}
