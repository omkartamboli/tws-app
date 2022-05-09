package com.trading.app.tradingapp.service;

import com.ib.client.Contract;
import com.ib.client.EClientSocket;

import com.trading.app.tradingapp.dto.OrderType;
import com.trading.app.tradingapp.dto.response.MarketDataDto;
import com.trading.app.tradingapp.persistance.entity.ContractEntity;
import com.trading.app.tradingapp.util.RequestMarketDataThread;

public interface BaseService {

    EClientSocket getConnection() throws Exception;

    int getNextOrderId() throws Exception;

    MarketDataDto getMarketDataForContract(Contract contract, boolean acceptStaleData) throws Exception;

    RequestMarketDataThread startMarketDataStreamForContract(Contract contract) throws Exception;

    Contract createStockContract(String ticker);

    Contract createOptionsContract(String ticker, Double strike, String dateYYYYMMDD, String callOrPut);

    Contract createOptionsStraddleContract(String ticker, Double strike, String dateYYYYMMDD, OrderType orderType);

    ContractEntity createContractEntity(Contract contract);

    ContractEntity getContractByTickerSymbol(String tickerSymbol);
}
