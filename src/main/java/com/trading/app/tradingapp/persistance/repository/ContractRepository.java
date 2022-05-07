package com.trading.app.tradingapp.persistance.repository;

import com.trading.app.tradingapp.persistance.entity.ContractEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface ContractRepository extends CrudRepository<ContractEntity, String> {

    List<ContractEntity> findByTickerId(Integer tickerId);

    List<ContractEntity> findBySymbol(String symbol);

    List<ContractEntity> findAllByOrderBySymbolAsc();
}
