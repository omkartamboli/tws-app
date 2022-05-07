package com.trading.app.tradingapp.service;

import com.trading.app.tradingapp.dto.form.CreateSetOrderFormDto;
import com.trading.app.tradingapp.dto.form.TickerFormsGroup;

import java.util.Map;

public interface DashboardService {

    Map<CreateSetOrderFormDto , TickerFormsGroup> getTickerOrderModelMap();

}
