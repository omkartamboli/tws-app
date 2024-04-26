package com.trading.app.tradingapp.persistance.repository;

import com.trading.app.tradingapp.persistance.entity.OrderEntity;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends CrudRepository<OrderEntity, Integer> {

    List<OrderEntity> findBySymbol(String symbol);

    List<OrderEntity> findByOrderId(Integer orderId);

    List<OrderEntity> findBySequenceId(String sequenceId);

    List<OrderEntity> findBySequenceIdAndOtsOrderType(String sequenceId, String otsOrderType);

    @Query("select order from OrderEntity order where order.parentOcaOrder = :order")
    List<OrderEntity> findByParentOcaOrder(@Param("order") OrderEntity order);

    List<OrderEntity> findBySymbolAndOrderStatusNotIn(String symbol, List<String> orderStatuses);

    List<OrderEntity> findByOrderStatusIn(List<String> orderStatuses);



}
