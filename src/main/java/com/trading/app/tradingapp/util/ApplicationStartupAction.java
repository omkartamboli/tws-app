package com.trading.app.tradingapp.util;

import com.trading.app.tradingapp.persistance.entity.SystemConfigEntity;
import com.trading.app.tradingapp.persistance.repository.ContractRepository;
import com.trading.app.tradingapp.persistance.repository.SystemConfigRepository;
import com.trading.app.tradingapp.service.BaseService;
import com.trading.app.tradingapp.service.ContractService;
import com.trading.app.tradingapp.service.OrderService;
import com.trading.app.tradingapp.service.SystemConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.List;

@Component
public class ApplicationStartupAction implements ApplicationListener<ApplicationReadyEvent> {

    @Resource
    private ContractRepository contractRepository;

    @Resource
    private ContractService contractService;

    @Resource
    private OrderService orderService;

    @Resource
    private BaseService baseService;

    @Value("${default.order.value}")
    private Double defaultOrderValue;

    @Value("${tws.trading.account}")
    private String tradingAccount;

    @Value("${divergence.order.enabled}")
    private Boolean divergenceOrderEnabled;

    @Value("${divergence.order.rsi.filter.enabled}")
    private Boolean divergenceOrderRsiFilterEnabled;

    @Value("${out.of.hours.order.enabled}")
    private Boolean outOfHoursOrderEnabled;

    @Value("${trading.start.hour}")
    private Integer tradingStartHour;

    @Value("${trading.start.minute}")
    private Integer tradingStartMinute;

    @Value("${trading.end.hour}")
    private Integer tradingEndHour;

    @Value("${trading.end.minute}")
    private Integer tradingEndMinute;

    @Value("${divergence.order.type}")
    private String divergenceOrderType;

    @Resource
    private SystemConfigRepository systemConfigRepository;

    @Resource
    private SystemConfigService systemConfigService;

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationStartupAction.class);

    /**
     * This event is executed as late as conceivably possible to indicate that
     * the application is ready to service requests.
     */
    @Override
    public void onApplicationEvent(final ApplicationReadyEvent event) {

        // store properties file config in DB
        initiateSystemConfig();

        // delete all inactive orders
        getOrderService().deleteAllInactiveOrders();

        // Get TWS connection and start market data Feed
        try{
            getBaseService().getConnection();
        }catch(Exception ex){
            LOGGER.error("Could not connect to TWS. EX: ", ex);
        }
    }


    public void initiateSystemConfig(){
        List<SystemConfigEntity> allConfigEntities = new ArrayList<>();
        if(!getSystemConfigService().isPropertyExists("default.order.value")) {
            allConfigEntities.add(new SystemConfigEntity("default.order.value", defaultOrderValue));
        }

        if(!getSystemConfigService().isPropertyExists("tws.trading.account")) {
            allConfigEntities.add(new SystemConfigEntity("tws.trading.account", tradingAccount));
        }

        if(!getSystemConfigService().isPropertyExists("divergence.order.enabled")) {
            allConfigEntities.add(new SystemConfigEntity("divergence.order.enabled", divergenceOrderEnabled));
        }

        if(!getSystemConfigService().isPropertyExists("divergence.order.rsi.filter.enabled")) {
            allConfigEntities.add(new SystemConfigEntity("divergence.order.rsi.filter.enabled", divergenceOrderRsiFilterEnabled));
        }

        if(!getSystemConfigService().isPropertyExists("out.of.hours.order.enabled")) {
            allConfigEntities.add(new SystemConfigEntity("out.of.hours.order.enabled", outOfHoursOrderEnabled));
        }

        if(!getSystemConfigService().isPropertyExists("trading.start.hour")) {
            allConfigEntities.add(new SystemConfigEntity("trading.start.hour", tradingStartHour));
        }

        if(!getSystemConfigService().isPropertyExists("trading.start.minute")) {
            allConfigEntities.add(new SystemConfigEntity("trading.start.minute", tradingStartMinute));
        }

        if(!getSystemConfigService().isPropertyExists("trading.end.hour")) {
            allConfigEntities.add(new SystemConfigEntity("trading.end.hour", tradingEndHour));
        }

        if(!getSystemConfigService().isPropertyExists("trading.end.minute")) {
            allConfigEntities.add(new SystemConfigEntity("trading.end.minute", tradingEndMinute));
        }

        if(!getSystemConfigService().isPropertyExists("divergence.order.type")) {
            allConfigEntities.add(new SystemConfigEntity("divergence.order.type", divergenceOrderType));
        }

        getSystemConfigRepository().saveAllAndFlush(allConfigEntities);

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

    public SystemConfigRepository getSystemConfigRepository() {
        return systemConfigRepository;
    }

    public void setSystemConfigRepository(SystemConfigRepository systemConfigRepository) {
        this.systemConfigRepository = systemConfigRepository;
    }

    public SystemConfigService getSystemConfigService() {
        return systemConfigService;
    }

    public void setSystemConfigService(SystemConfigService systemConfigService) {
        this.systemConfigService = systemConfigService;
    }

    public ContractService getContractService() {
        return contractService;
    }

    public void setContractService(ContractService contractService) {
        this.contractService = contractService;
    }

    public OrderService getOrderService() {
        return orderService;
    }

    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }
}
