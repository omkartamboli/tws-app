package com.trading.app.tradingapp.util;


import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class TradingAppRestTemplate extends RestTemplate{

    public TradingAppRestTemplate() {
        super();
    }
}
