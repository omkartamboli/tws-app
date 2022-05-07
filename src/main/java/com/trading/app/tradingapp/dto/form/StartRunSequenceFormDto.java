package com.trading.app.tradingapp.dto.form;

public class StartRunSequenceFormDto {

    private String tickerSymbol;

    private Double tpMargin;

    private Integer quantity;

    public StartRunSequenceFormDto(String tickerSymbol, Double tpMargin, Integer quantity) {
        this.tickerSymbol = tickerSymbol;
        this.tpMargin = tpMargin;
        this.quantity = quantity;
    }

    public String getTickerSymbol() {
        return tickerSymbol;
    }

    public void setTickerSymbol(String tickerSymbol) {
        this.tickerSymbol = tickerSymbol;
    }

    public Double getTpMargin() {
        return tpMargin;
    }

    public void setTpMargin(Double tpMargin) {
        this.tpMargin = tpMargin;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}
