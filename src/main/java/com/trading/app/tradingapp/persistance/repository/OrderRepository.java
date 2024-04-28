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
    
    @Query("select sum(order_a.filled) from OrderEntity as order_a where order_a.orderTrigger = :trigger and order_a.symbol = :symbol and order_a.orderAction = 'BUY'")
    Integer getTotalBuyQtyForTickerWithSpecificOrderTrigger(@Param("symbol")String symbol, @Param("trigger")String trigger);

    @Query("select sum(order_a.filled) from OrderEntity as order_a where order_a.orderTrigger = :trigger and order_a.symbol = :symbol and order_a.orderAction = 'SELL'")
    Integer getTotalSellQtyForTickerWithSpecificOrderTrigger(@Param("symbol")String symbol, @Param("trigger")String trigger);

    @Query("select o from OrderEntity as o where (o.filled is null or o.filled < o.quantity) and o.orderStatus <> 'Cancelled' and o.symbol = :symbol and o.orderTrigger = :trigger")
    List<OrderEntity> findUnFilledOrders(@Param("symbol")String symbol, @Param("trigger")String trigger);

    List<OrderEntity> findBySymbolAndOrderStatusNotIn(String symbol, List<String> orderStatuses);

    List<OrderEntity> findByOrderStatusIn(List<String> orderStatuses);



}
