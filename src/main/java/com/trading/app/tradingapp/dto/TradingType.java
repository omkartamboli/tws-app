package com.trading.app.tradingapp.dto;

public enum TradingType {
    LIVE, TEST;

    public static TradingType getTradingType(String type) {
        for (TradingType tradingType : TradingType.values()) {
            if (tradingType.toString().equalsIgnoreCase(type)) {
                return tradingType;
            }
        }
        return null;
    }
}
