package com.trading.app.tradingapp.persistance.repository;

import com.trading.app.tradingapp.persistance.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<OrderEntity, Integer> {

    List<OrderEntity> findBySymbol(String symbol);

    List<OrderEntity> findByOrderId(Integer orderId);

    List<OrderEntity> findBySequenceId(String sequenceId);

    List<OrderEntity> findBySequenceIdAndOtsOrderType(String sequenceId, String otsOrderType);

    @Query("select order from OrderEntity order where order.parentOcaOrder = :order")
    List<OrderEntity> findByParentOcaOrder(@Param("order") OrderEntity order);

    @Query("select order from OrderEntity order where order.slCheckSequenceId = :slCheckSequenceId")
    List<OrderEntity> findBySlCheckSequenceId(@Param("slCheckSequenceId") Long slCheckSequenceId);
    
    @Query("select sum(order_a.filled) from OrderEntity as order_a where order_a.orderTrigger = :trigger and order_a.symbol = :symbol and order_a.orderAction = 'BUY' and order_a.orderTriggerInterval = :interval")
    Integer getTotalBuyQtyForTickerWithSpecificOrderTrigger(@Param("symbol")String symbol, @Param("trigger")String trigger, @Param("interval")String interval);

    @Query("select sum(order_a.filled) from OrderEntity as order_a where order_a.orderTrigger = :trigger and order_a.symbol = :symbol and order_a.orderAction = 'SELL' and order_a.orderTriggerInterval = :interval")
    Integer getTotalSellQtyForTickerWithSpecificOrderTrigger(@Param("symbol")String symbol, @Param("trigger")String trigger, @Param("interval")String interval);

    @Query("select o from OrderEntity as o where (o.filled is null or o.filled < o.quantity) and o.orderStatus <> 'Cancelled' and o.symbol = :symbol and o.orderTrigger = :trigger and o.orderTriggerInterval = :interval")
    List<OrderEntity> findUnFilledOrders(@Param("symbol")String symbol, @Param("trigger")String trigger, @Param("interval")String interval);



    List<OrderEntity> findBySymbolAndOrderStatusNotIn(String symbol, List<String> orderStatuses);

    List<OrderEntity> findByOrderStatusIn(List<String> orderStatuses);



}
