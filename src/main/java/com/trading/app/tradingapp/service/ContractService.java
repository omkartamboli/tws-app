package com.trading.app.tradingapp.service;

import com.ib.client.EClientSocket;
import com.trading.app.tradingapp.dto.SequenceTracker;
import com.trading.app.tradingapp.dto.response.GetMarketDataResponseDto;

import java.util.Map;

public interface ContractService {

    GetMarketDataResponseDto getMarketData(String ticker);

    void createContractEntity(String ticker);

    void startBuySequence(String ticker, Double tpMargin, Integer quantity);

    void stopBuySequence(String tickerSymbol);

    void startSellSequence(String ticker, Double tpMargin, Integer quantity);

    void stopSellSequence(String tickerSymbol);

    void startMarketDataFeed(EClientSocket connection);

    Map<String, SequenceTracker> getTickerSequenceTrackerMap();

    Double getLatestTickerLTP(String ticker) throws Exception;

    Double getLatestTickerLTPFromDB(String ticker) throws Exception;

}
