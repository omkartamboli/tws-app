package com.trading.app.tradingapp.service;

public interface SystemConfigService {

    Boolean getBoolean(String property);

    Double getDouble(String property);

    String getString(String property);

    Integer getInteger(String property);

    Long getLong(String property);

    boolean isPropertyExists(String property);

}
