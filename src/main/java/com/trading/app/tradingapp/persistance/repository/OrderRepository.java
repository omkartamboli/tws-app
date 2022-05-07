package com.trading.app.tradingapp.persistance.repository;

import com.trading.app.tradingapp.persistance.entity.OrderEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface OrderRepository extends CrudRepository<OrderEntity, Integer> {

    List<OrderEntity> findBySymbol(String symbol);

    List<OrderEntity> findByOrderId(Integer orderId);

    List<OrderEntity> findBySymbolAndOrderStatusNotIn(String symbol, List<String> orderStatuses);

}
