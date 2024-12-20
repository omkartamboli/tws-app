package com.trading.app.tradingapp.persistance.repository;

import com.trading.app.tradingapp.persistance.entity.OrderEntity;
import com.trading.app.tradingapp.persistance.entity.TriggerOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.persistence.LockModeType;
import java.util.List;

public interface TriggerOrderRepository extends JpaRepository<TriggerOrderEntity, Long> {

    List<TriggerOrderEntity> findBySymbol(String symbol);

    List<TriggerOrderEntity> findByOrderId(Integer orderId);

    List<TriggerOrderEntity> findByPk(Long pk);

    List<TriggerOrderEntity> findBySequenceId(String sequenceId);

    List<TriggerOrderEntity> findByParentOrder(OrderEntity parentOrder);

    List<TriggerOrderEntity> findByParentOrderAndActive(OrderEntity parentOrder, Boolean active);

    List<TriggerOrderEntity> findByParentOrderAndOrderType(OrderEntity parentOrder, String orderType);


    //@Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(     "select to                                                                                                    " +
                "from TriggerOrderEntity as to                                                                                " +
                "where to.symbol = :symbol                                                                                    " +
                "and to.active = true                                                                                         " +
                "and    (                                                                                                     " +
                "           (to.orderTriggerPrice < :ltp and to.orderType = 'STP LMT' and to.orderAction = 'BUY')         or  " +
                "           (to.orderTriggerPrice > :ltp and to.orderType = 'STP LMT' and to.orderAction = 'SELL')        or  " +
                "           (to.orderTriggerPrice <= :ltp and to.orderType = 'TRAIL LIMIT' and to.orderAction = 'SELL')   or  " +
                "           (to.orderTriggerPrice >= :ltp and to.orderType = 'TRAIL LIMIT' and to.orderAction = 'BUY')        " +
                "       )                                                                                                     ")
    List<TriggerOrderEntity> findTriggeredOrdersForTicker(@Param("symbol")String symbol, @Param("ltp")Double ltp);
}
