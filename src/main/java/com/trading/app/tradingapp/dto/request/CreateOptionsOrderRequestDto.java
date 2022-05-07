package com.trading.app.tradingapp.dto.request;

import com.trading.app.tradingapp.dto.OptionType;

public class CreateOptionsOrderRequestDto extends CreateSetOrderRequestDto {


    private Double strike;

    private String dateYYYYMMDD;

    private OptionType optionType;


    public Double getStrike() {
        return strike;
    }

    public void setStrike(Double strike) {
        this.strike = strike;
    }

    public String getDateYYYYMMDD() {
        return dateYYYYMMDD;
    }

    public void setDateYYYYMMDD(String dateYYYYMMDD) {
        this.dateYYYYMMDD = dateYYYYMMDD;
    }

    public OptionType getOptionType() {
        return optionType;
    }

    public void setOptionType(OptionType optionType) {
        this.optionType = optionType;
    }
}
