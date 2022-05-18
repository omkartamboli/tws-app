package com.trading.app.tradingapp.dto.form;

public class CreateSetOrderFormDto {

    private String ticker;

    private Double transactionPrice;

    private Double stopLoss;

    private Double trailingStopLoss;

    private Double targetPriceOffsetBuyStep1;

    private Double targetPriceOffsetBuyStep2;

    private Double targetPriceOffsetBuyStep3;

    private Double targetPriceOffsetBuyStep4;

    private Double targetPriceOffsetBuyStep5;

    private Double targetPriceOffsetSellStep1;

    private Double targetPriceOffsetSellStep2;

    private Double targetPriceOffsetSellStep3;

    private Double targetPriceOffsetSellStep4;

    private Double targetPriceOffsetSellStep5;

    private Double startBuyRunMargin;

    private Double startSellRunMargin;

    private Integer quantity;

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public Double getTransactionPrice() {
        return transactionPrice;
    }

    public void setTransactionPrice(Double transactionPrice) {
        this.transactionPrice = transactionPrice;
    }

    public Double getTargetPriceOffsetBuyStep1() {
        return targetPriceOffsetBuyStep1;
    }

    public void setTargetPriceOffsetBuyStep1(Double targetPriceOffsetBuyStep1) {
        this.targetPriceOffsetBuyStep1 = targetPriceOffsetBuyStep1;
    }

    public Double getTargetPriceOffsetBuyStep2() {
        return targetPriceOffsetBuyStep2;
    }

    public void setTargetPriceOffsetBuyStep2(Double targetPriceOffsetBuyStep2) {
        this.targetPriceOffsetBuyStep2 = targetPriceOffsetBuyStep2;
    }

    public Double getTargetPriceOffsetBuyStep3() {
        return targetPriceOffsetBuyStep3;
    }

    public void setTargetPriceOffsetBuyStep3(Double targetPriceOffsetBuyStep3) {
        this.targetPriceOffsetBuyStep3 = targetPriceOffsetBuyStep3;
    }

    public Double getTargetPriceOffsetBuyStep4() {
        return targetPriceOffsetBuyStep4;
    }

    public void setTargetPriceOffsetBuyStep4(Double targetPriceOffsetBuyStep4) {
        this.targetPriceOffsetBuyStep4 = targetPriceOffsetBuyStep4;
    }

    public Double getTargetPriceOffsetBuyStep5() {
        return targetPriceOffsetBuyStep5;
    }

    public void setTargetPriceOffsetBuyStep5(Double targetPriceOffsetBuyStep5) {
        this.targetPriceOffsetBuyStep5 = targetPriceOffsetBuyStep5;
    }

    public Double getTargetPriceOffsetSellStep1() {
        return targetPriceOffsetSellStep1;
    }

    public void setTargetPriceOffsetSellStep1(Double targetPriceOffsetSellStep1) {
        this.targetPriceOffsetSellStep1 = targetPriceOffsetSellStep1;
    }

    public Double getTargetPriceOffsetSellStep2() {
        return targetPriceOffsetSellStep2;
    }

    public void setTargetPriceOffsetSellStep2(Double targetPriceOffsetSellStep2) {
        this.targetPriceOffsetSellStep2 = targetPriceOffsetSellStep2;
    }

    public Double getTargetPriceOffsetSellStep3() {
        return targetPriceOffsetSellStep3;
    }

    public void setTargetPriceOffsetSellStep3(Double targetPriceOffsetSellStep3) {
        this.targetPriceOffsetSellStep3 = targetPriceOffsetSellStep3;
    }

    public Double getTargetPriceOffsetSellStep4() {
        return targetPriceOffsetSellStep4;
    }

    public void setTargetPriceOffsetSellStep4(Double targetPriceOffsetSellStep4) {
        this.targetPriceOffsetSellStep4 = targetPriceOffsetSellStep4;
    }

    public Double getTargetPriceOffsetSellStep5() {
        return targetPriceOffsetSellStep5;
    }

    public void setTargetPriceOffsetSellStep5(Double targetPriceOffsetSellStep5) {
        this.targetPriceOffsetSellStep5 = targetPriceOffsetSellStep5;
    }

    public Double getStartBuyRunMargin() {
        return startBuyRunMargin;
    }

    public void setStartBuyRunMargin(Double startBuyRunMargin) {
        this.startBuyRunMargin = startBuyRunMargin;
    }

    public Double getStartSellRunMargin() {
        return startSellRunMargin;
    }

    public void setStartSellRunMargin(Double startSellRunMargin) {
        this.startSellRunMargin = startSellRunMargin;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Double getStopLoss() {
        return stopLoss;
    }

    public void setStopLoss(Double stopLoss) {
        this.stopLoss = stopLoss;
    }

    public Double getTrailingStopLoss() {
        return trailingStopLoss;
    }

    public void setTrailingStopLoss(Double trailingStopLoss) {
        this.trailingStopLoss = trailingStopLoss;
    }
}
