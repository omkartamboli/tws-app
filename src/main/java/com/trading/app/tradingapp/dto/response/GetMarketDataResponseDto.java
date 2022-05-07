package com.trading.app.tradingapp.dto.response;

public class GetMarketDataResponseDto {

    private String ticker;

    private Double ltp;

    private Double askPrice;

    private Double bidPrice;

    private Boolean status;

    private String error;

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public Double getLtp() {
        return ltp;
    }

    public void setLtp(Double ltp) {
        this.ltp = ltp;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Double getAskPrice() {
        return askPrice;
    }

    public void setAskPrice(Double askPrice) {
        this.askPrice = askPrice;
    }

    public Double getBidPrice() {
        return bidPrice;
    }

    public void setBidPrice(Double bidPrice) {
        this.bidPrice = bidPrice;
    }
}
