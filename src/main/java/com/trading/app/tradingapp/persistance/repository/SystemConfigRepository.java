package com.trading.app.tradingapp.persistance.repository;

import com.trading.app.tradingapp.persistance.entity.SystemConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SystemConfigRepository extends JpaRepository<SystemConfigEntity, String> {
    List<SystemConfigEntity> findByProperty(String property);

}
