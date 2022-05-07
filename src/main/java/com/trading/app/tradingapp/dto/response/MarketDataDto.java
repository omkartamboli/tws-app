package com.trading.app.tradingapp.dto.response;

public class MarketDataDto {

    private Double ltp;

    private Double lastAsk;

    private Double lastBid;

    public Double getLtp() {
        return ltp;
    }

    public void setLtp(Double ltp) {
        this.ltp = ltp;
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
}
