package com.trading.app.tradingapp.dto.request;

public class RKLTradeOrderRequestDto {

    String ticker;
    String time;
    Double entry;
    Double sl;
    Double tp1;
    Double tp2;
    Integer qty;
    String interval;


    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Double getEntry() {
        return entry;
    }

    public void setEntry(Double entry) {
        this.entry = entry;
    }

    public Double getSl() {
        return sl;
    }

    public void setSl(Double sl) {
        this.sl = sl;
    }

    public Double getTp1() {
        return tp1;
    }

    public void setTp1(Double tp1) {
        this.tp1 = tp1;
    }

    public Double getTp2() {
        return tp2;
    }

    public void setTp2(Double tp2) {
        this.tp2 = tp2;
    }

    public Integer getQty() {
        return qty;
    }

    public void setQty(Integer qty) {
        this.qty = qty;
    }

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }
}
