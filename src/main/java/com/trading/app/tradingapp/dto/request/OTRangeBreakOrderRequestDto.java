package com.trading.app.tradingapp.dto.request;

public class OTRangeBreakOrderRequestDto {
    String time;
    String interval;
    String ticker;
    String orderType;
    Integer qty;
    Double price;
    String position;
    String direction;
    String entryId;
    String exitId;
    Double stop;
    Double trailPrice;
    Double trailOffset;

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public String getOrderType() {
        return orderType;
    }

    public void setOrderType(String orderType) {
        this.orderType = orderType;
    }

    public Integer getQty() {
        return qty;
    }

    public void setQty(Integer qty) {
        this.qty = qty;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public String getEntryId() {
        return entryId;
    }

    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }

    public String getExitId() {
        return exitId;
    }

    public void setExitId(String exitId) {
        this.exitId = exitId;
    }

    public Double getStop() {
        return stop;
    }

    public void setStop(Double stop) {
        this.stop = stop;
    }

    public Double getTrailPrice() {
        return trailPrice;
    }

    public void setTrailPrice(Double trailPrice) {
        this.trailPrice = trailPrice;
    }

    public Double getTrailOffset() {
        return trailOffset;
    }

    public void setTrailOffset(Double trailOffset) {
        this.trailOffset = trailOffset;
    }
}
