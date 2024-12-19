package com.trading.app.tradingapp.util;

import com.ib.client.Contract;
import com.ib.client.OrderType;
import com.trading.app.tradingapp.persistance.entity.TriggerOrderEntity;
import com.trading.app.tradingapp.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessTriggerOrderThread extends Thread {

    private final Long triggerOrderPk;

    private final OrderService orderService;

    private final Contract contract;

    private final TriggerOrderEntity triggerOrder;

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessTriggerOrderThread.class);


    public ProcessTriggerOrderThread(String name, Long triggerOrderPk, OrderService orderService, Contract contract, TriggerOrderEntity triggerOrder) {
        super(name);
        this.triggerOrderPk = triggerOrderPk;
        this.orderService = orderService;
        this.contract = contract;
        this.triggerOrder = triggerOrder;
    }

    @Override
    public void run() {
        try {
            //entityManager.lock(triggerOrder, LockModeType.PESSIMISTIC_WRITE, properties);
            //entityManager.refresh(triggerOrder);

            LOGGER.info("ProcessTriggerOrderThread.run(). Triggered order type is [{}]", triggerOrder.getOrderType());


            if (OrderType.STP_LMT.getApiString().equals(triggerOrder.getOrderType())) {
                LOGGER.info("Creating STP LMT order for trigger order [{}]", triggerOrder.getPk());
                getOrderService().createSLOrderFromTriggerOrder(triggerOrder, contract);
            } else if (OrderType.TRAIL_LIMIT.getApiString().equals(triggerOrder.getOrderType())) {
                getOrderService().createTrailingTPOrderFromTriggerOrder(triggerOrder, contract);
            } else {
                LOGGER.error("Invalid order type for Trigger order with entry id {}", triggerOrder.getSequenceId());
            }

        } catch (Exception e) {
            LOGGER.error("Error while processing Trigger order [{}]", triggerOrderPk, e);
        }
    }

    public OrderService getOrderService() {
        return orderService;
    }
}
