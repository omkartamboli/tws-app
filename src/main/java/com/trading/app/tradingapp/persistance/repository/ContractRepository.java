package com.trading.app.tradingapp.persistance.repository;

import com.trading.app.tradingapp.persistance.entity.ContractEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContractRepository extends JpaRepository<ContractEntity, String> {

    List<ContractEntity> findByTickerId(Integer tickerId);

    List<ContractEntity> findBySymbol(String symbol);

    List<ContractEntity> findAllByOrderBySymbolAsc();
}
