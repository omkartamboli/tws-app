package com.trading.app.tradingapp.dto;

public class SequenceTracker {
    private Double lastBuyValue;

    private Double lastSellValue;

    private Double nextSellValue;

    private Double nextBuyValue;

    private Double ltp;

    private Double tpBuyMargin;

    private Double buyMargin1;

    private Double buyMargin2;

    private Double tpSellMargin;

    private Double sellMargin1;

    private Double sellMargin2;

    private boolean buyTpOrderFilled;

    private boolean sellTpOrderFilled;

    private boolean buyRunEnable = false;

    private boolean sellRunEnable = false;

    private Integer buyTpOrderId;

    private Integer sellTpOrderId;

    public Double getLastBuyValue() {
        return lastBuyValue;
    }

    public void setLastBuyValue(Double lastBuyValue) {
        this.lastBuyValue = lastBuyValue;
    }

    public Double getLastSellValue() {
        return lastSellValue;
    }

    public void setLastSellValue(Double lastSellValue) {
        this.lastSellValue = lastSellValue;
    }

    public Double getNextSellValue() {
        return nextSellValue;
    }

    public void setNextSellValue(Double nextSellValue) {
        this.nextSellValue = nextSellValue;
    }

    public Double getNextBuyValue() {
        return nextBuyValue;
    }

    public void setNextBuyValue(Double nextBuyValue) {
        this.nextBuyValue = nextBuyValue;
    }

    public synchronized Double getLtp() {
        return ltp;
    }

    public synchronized void setLtp(Double ltp) {

        // make sure we have lock on ltp before update
        getLtp();

        this.ltp = ltp;
    }

    public boolean isBuyRunEnable() {
        return buyRunEnable;
    }

    public void setBuyRunEnable(boolean buyRunEnable) {
        this.buyRunEnable = buyRunEnable;
    }

    public boolean isSellRunEnable() {
        return sellRunEnable;
    }

    public void setSellRunEnable(boolean sellRunEnable) {
        this.sellRunEnable = sellRunEnable;
    }

    public Double getTpBuyMargin() {
        return tpBuyMargin;
    }

    public void setTpBuyMargin(Double tpBuyMargin) {
        this.tpBuyMargin = tpBuyMargin;
    }

    public Double getBuyMargin1() {
        return buyMargin1;
    }

    public void setBuyMargin1(Double buyMargin1) {
        this.buyMargin1 = buyMargin1;
    }

    public Double getBuyMargin2() {
        return buyMargin2;
    }

    public void setBuyMargin2(Double buyMargin2) {
        this.buyMargin2 = buyMargin2;
    }

    public Double getTpSellMargin() {
        return tpSellMargin;
    }

    public void setTpSellMargin(Double tpSellMargin) {
        this.tpSellMargin = tpSellMargin;
    }

    public Double getSellMargin1() {
        return sellMargin1;
    }

    public void setSellMargin1(Double sellMargin1) {
        this.sellMargin1 = sellMargin1;
    }

    public Double getSellMargin2() {
        return sellMargin2;
    }

    public void setSellMargin2(Double sellMargin2) {
        this.sellMargin2 = sellMargin2;
    }

    public boolean isBuyTpOrderFilled() {
        return buyTpOrderFilled;
    }

    public void setBuyTpOrderFilled(boolean buyTpOrderFilled) {
        this.buyTpOrderFilled = buyTpOrderFilled;
    }

    public boolean isSellTpOrderFilled() {
        return sellTpOrderFilled;
    }

    public void setSellTpOrderFilled(boolean sellTpOrderFilled) {
        this.sellTpOrderFilled = sellTpOrderFilled;
    }

    public Integer getBuyTpOrderId() {
        return buyTpOrderId;
    }

    public void setBuyTpOrderId(Integer buyTpOrderId) {
        this.buyTpOrderId = buyTpOrderId;
    }

    public Integer getSellTpOrderId() {
        return sellTpOrderId;
    }

    public void setSellTpOrderId(Integer sellTpOrderId) {
        this.sellTpOrderId = sellTpOrderId;
    }
}
