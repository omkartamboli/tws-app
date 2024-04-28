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
    
    @Query("select tab_a.sel_filled_a-tab_b.sel_filled_b from (select sum(order_a.filled) as sel_filled_a from OrderEntity order_a where order_a.orderTrigger = :trigger and order_a.symbol = :symbol and order_a.orderAction = 'BUY') as tab_a, (select sum(order_b.filled) as sel_filled_b from OrderEntity order_b where order_b.orderTrigger = :trigger and order_b.symbol = :symbol and order_b.orderAction = 'SELL') as tab_b")
    int findOutstandingQtyForTickerWithSpecificOrderTrigger(@Param("symbol")String symbol, @Param("trigger")String trigger);

    @Query("select order from OrderEntity order where (order.filled is null or order.filled < order.quantity) and order.orderStatus <> 'Cancelled' and order.symbol = :symbol and order.orderTrigger = :trigger")
    List<OrderEntity> findUnFilledOrders(@Param("symbol")String symbol, @Param("trigger")String trigger);

    List<OrderEntity> findBySymbolAndOrderStatusNotIn(String symbol, List<String> orderStatuses);

    List<OrderEntity> findByOrderStatusIn(List<String> orderStatuses);



}
