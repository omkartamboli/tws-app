package com.trading.app.tradingapp.dto;

public enum OptionType {
    CALL, PUT;

    public static OptionType getOptionType(String type) {
        for (OptionType optionType : OptionType.values()) {
            if (optionType.toString().equalsIgnoreCase(type)) {
                return optionType;
            }
        }
        if("C".equalsIgnoreCase(type)){
            return CALL;
        }
        if("P".equalsIgnoreCase(type)){
            return PUT;
        }
        return null;
    }
}
