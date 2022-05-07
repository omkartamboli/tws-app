package com.trading.app.tradingapp.dto.model;

import com.trading.app.tradingapp.dto.form.CreateSetOrderFormDto;

import java.util.Date;

public class TickerModelDto {

    private String symbol;

    private Integer tickerId;

    private Integer defaultQuantity;

    private String currency;

    private String exchange;

    private String secType;

    private Double ltp;

    private Date ltpTimestamp;

    private CreateSetOrderFormDto createSetOrderFormDto;

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

    public Double getLtp() {
        return ltp;
    }

    public void setLtp(Double ltp) {
        this.ltp = ltp;
    }

    public Date getLtpTimestamp() {
        return ltpTimestamp;
    }

    public void setLtpTimestamp(Date ltpTimestamp) {
        this.ltpTimestamp = ltpTimestamp;
    }

    public CreateSetOrderFormDto getCreateSetOrderFormDto() {
        return createSetOrderFormDto;
    }

    public void setCreateSetOrderFormDto(CreateSetOrderFormDto createSetOrderFormDto) {
        this.createSetOrderFormDto = createSetOrderFormDto;
    }
}
