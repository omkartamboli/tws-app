package com.trading.app.tradingapp.util;

import com.trading.app.tradingapp.dto.SequenceTracker;
import com.trading.app.tradingapp.persistance.repository.ContractRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.transaction.Transactional;


public class UpdateSequenceTrackerThread extends Thread {

    private SequenceTracker sequenceTracker;

    @Resource
    private ContractRepository contractRepository;

    private String ticker;

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateSequenceTrackerThread.class);


    @Override
    @Transactional
    public void run() {
        getContractRepository().findById(ticker).ifPresent(contractEntity -> {
            getSequenceTracker().setLtp(contractEntity.getLtp());
            LOGGER.info("Update LTP in sequence tracker for ticker [{}], LTP [{}]", ticker, contractEntity.getLtp());
        });
    }

    public UpdateSequenceTrackerThread(SequenceTracker sequenceTracker, ContractRepository contractRepository, String ticker) {
        super();
        this.sequenceTracker = sequenceTracker;
        this.contractRepository = contractRepository;
        this.ticker = ticker;
    }

    public SequenceTracker getSequenceTracker() {
        return sequenceTracker;
    }

    public void setSequenceTracker(SequenceTracker sequenceTracker) {
        this.sequenceTracker = sequenceTracker;
    }

    public ContractRepository getContractRepository() {
        return contractRepository;
    }

    public void setContractRepository(ContractRepository contractRepository) {
        this.contractRepository = contractRepository;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }
}
