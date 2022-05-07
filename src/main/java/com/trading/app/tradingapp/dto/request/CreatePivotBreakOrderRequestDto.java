package com.trading.app.tradingapp.dto.request;

public class CreatePivotBreakOrderRequestDto {
    String time;
    Double close;
    Double low;
    Double high;
    Double tpOffsetPrice;
    Integer pivotbreakval;
    String ticker;
    String interval;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Double getClose() {
        return close;
    }

    public void setClose(Double close) {
        this.close = close;
    }

    public Double getLow() {
        return low;
    }

    public void setLow(Double low) {
        this.low = low;
    }

    public Double getHigh() {
        return high;
    }

    public void setHigh(Double high) {
        this.high = high;
    }

    public Integer getPivotbreakval() {
        return pivotbreakval;
    }

    public void setPivotbreakval(Integer pivotbreakval) {
        this.pivotbreakval = pivotbreakval;
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

    public Double getTpOffsetPrice() {
        return tpOffsetPrice;
    }

    public void setTpOffsetPrice(Double tpOffsetPrice) {
        this.tpOffsetPrice = tpOffsetPrice;
    }
}
