package com.trading.app.tradingapp.dto;

public enum OrderType {
    BUY, SELL;

    public static OrderType getOrderType(String type) {
        for (OrderType orderType : OrderType.values()) {
            if (orderType.toString().equalsIgnoreCase(type)) {
                return orderType;
            }
        }
        return null;
    }
}
