package com.trading.app.tradingapp.persistance.repository;

import com.trading.app.tradingapp.persistance.entity.SystemConfigEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SystemConfigRepository extends CrudRepository<SystemConfigEntity, String> {
    List<SystemConfigEntity> findByProperty(String property);

}
