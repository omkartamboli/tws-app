package com.trading.app.tradingapp.util;

import com.ib.client.Contract;
import com.ib.client.EClientSocket;
import com.ib.client.TagValue;
import com.ib.client.Types;
import com.trading.app.tradingapp.persistance.entity.ContractEntity;
import com.trading.app.tradingapp.persistance.repository.ContractRepository;
import com.trading.app.tradingapp.service.impl.BaseServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Optional;


public class RequestMarketDataThread extends Thread {

    private Contract contract;

    private ContractRepository contractRepository;

    private EClientSocket connection;

    private static final Logger LOGGER = LoggerFactory.getLogger(BaseServiceImpl.class);

    public Contract getContract() {
        return contract;
    }

    public ContractRepository getContractRepository() {
        return contractRepository;
    }

    public EClientSocket getConnection() {
        return connection;
    }

    public RequestMarketDataThread(Contract contract, ContractRepository contractRepository, EClientSocket connection) {
        super("RequestMarketDataThread for " + contract.symbol());
        this.contract = contract;
        this.contractRepository = contractRepository;
        this.connection = connection;
    }

    @Override
    public void run() {
        Optional<ContractEntity> contractEntityOptional = getContractRepository().findById(getContract().symbol());
        if (contractEntityOptional.isPresent()) {
//            if(Types.SecType.FUT.name().equalsIgnoreCase(contract.secType().name())){
//                contract.exchange("CBOE");
//                LOGGER.info("Changing the exchange name for [{}] before requesting market data", contract.symbol());
//            }
            getConnection().reqMktData(contractEntityOptional.get().getTickerId(), contract, null, false, false, new ArrayList<TagValue>());
            LOGGER.info("Requested market data for [{}] in thread [{}]", contract.symbol(), this.getName());
        }
    }
}
