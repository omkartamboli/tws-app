package com.trading.app.tradingapp.service.impl;


import com.trading.app.tradingapp.dto.form.CreateSetOrderFormDto;
import com.trading.app.tradingapp.dto.form.StartRunSequenceFormDto;
import com.trading.app.tradingapp.dto.form.TickerFormsGroup;
import com.trading.app.tradingapp.dto.model.OrderModelDto;

import com.trading.app.tradingapp.persistance.entity.ContractEntity;
import com.trading.app.tradingapp.persistance.entity.OrderEntity;
import com.trading.app.tradingapp.persistance.repository.ContractRepository;
import com.trading.app.tradingapp.persistance.repository.OrderRepository;
import com.trading.app.tradingapp.populator.TickerAndOrderModelDtoPopulator;
import com.trading.app.tradingapp.service.DashboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardServiceImpl implements DashboardService {

    @Resource
    private TickerAndOrderModelDtoPopulator tickerAndOrderModelDtoPopulator;

    @Resource
    private OrderRepository orderRepository;

    @Resource
    private ContractRepository contractRepository;

    // Excluded statues for order query
    private List<String> orderStatuses = Arrays.asList("Cancelled", "Filled", "Inactive", "ApiCancelled");

    @Override
    public Map<CreateSetOrderFormDto, TickerFormsGroup> getTickerOrderModelMap() {
        Map<ContractEntity, List<OrderEntity>> contractOrderEntityMap = new HashMap<>();

        getContractRepository().findAllByOrderBySymbolAsc().forEach(contractEntity ->
                {
                    if (contractEntity.isActive()) {
                        contractOrderEntityMap.put(contractEntity, orderRepository.findBySymbol(contractEntity.getSymbol()).stream().filter(orderEntity -> orderEntity.getOrderStatus() != null && !orderStatuses.contains(orderEntity.getOrderStatus())).collect(Collectors.toList()));
                    }
                }
        );
        return tickerAndOrderModelDtoPopulator.populate(contractOrderEntityMap);
    }

    public TickerAndOrderModelDtoPopulator getTickerAndOrderModelDtoPopulator() {
        return tickerAndOrderModelDtoPopulator;
    }

    public void setTickerAndOrderModelDtoPopulator(TickerAndOrderModelDtoPopulator tickerAndOrderModelDtoPopulator) {
        this.tickerAndOrderModelDtoPopulator = tickerAndOrderModelDtoPopulator;
    }

    public OrderRepository getOrderRepository() {
        return orderRepository;
    }

    public void setOrderRepository(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public ContractRepository getContractRepository() {
        return contractRepository;
    }

    public void setContractRepository(ContractRepository contractRepository) {
        this.contractRepository = contractRepository;
    }
}
