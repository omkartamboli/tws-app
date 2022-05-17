package com.trading.app.tradingapp.controller.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    private static final String HELLO = "Hello ";

    @GetMapping("/hello/{name}")
    public String getMarketData(@PathVariable String name) {
        return HELLO+name;
    }
}
