package com.trading.app.tradingapp.util;

import com.ib.client.EClientSocket;
import com.trading.app.tradingapp.persistance.entity.ContractEntity;
import com.trading.app.tradingapp.persistance.repository.ContractRepository;
import com.trading.app.tradingapp.service.BaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import java.util.List;

@Component
public class ApplicationStartupAction implements ApplicationListener<ApplicationReadyEvent> {

    @Resource
    private ContractRepository contractRepository;

    @Resource
    private BaseService baseService;

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationStartupAction.class);

    /**
     * This event is executed as late as conceivably possible to indicate that
     * the application is ready to service requests.
     */
    @Override
    public void onApplicationEvent(final ApplicationReadyEvent event) {

        List<ContractEntity> contractEntityList = contractRepository.findAllByOrderBySymbolAsc();

        if (contractEntityList == null || contractEntityList.isEmpty()) {
            return;
        }

        try {

            final EClientSocket connection = getBaseService().getConnection();

            if (null != connection) {
                contractEntityList.forEach(contractEntity -> {
                    try {
                        RequestMarketDataThread requestMarketDataThread = new RequestMarketDataThread(getBaseService().createStockContract(contractEntity.getSymbol()), getContractRepository(), connection);
                        requestMarketDataThread.start();
                        LOGGER.info("Initiate Market Data stream for ticker [{}] after application startup", contractEntity.getSymbol());
                    } catch (Exception ex) {
                        LOGGER.error("Could not initiate Market Data stream for ticker [{}]. Exception: [{}]", contractEntity.getSymbol(), ex.getMessage());
                    }
                });
            }
        } catch (Exception ex) {
            LOGGER.error("Could not connect with TWS app.");
        }
    }


    public ContractRepository getContractRepository() {
        return contractRepository;
    }

    public void setContractRepository(ContractRepository contractRepository) {
        this.contractRepository = contractRepository;
    }

    public BaseService getBaseService() {
        return baseService;
    }

    public void setBaseService(BaseService baseService) {
        this.baseService = baseService;
    }
}
