package com.trading.app.tradingapp.service.impl;

import com.trading.app.tradingapp.persistance.entity.SystemConfigEntity;
import com.trading.app.tradingapp.persistance.repository.SystemConfigRepository;
import com.trading.app.tradingapp.service.SystemConfigService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;

@Service
public class SystemConfigServiceImpl implements SystemConfigService {

    @Resource
    private SystemConfigRepository systemConfigRepository;

    @Override
    public Boolean getBoolean(String property) {
        Optional<SystemConfigEntity> entity = getFirstInstanceOfPropertyConfig(property);
        return entity.map(SystemConfigEntity::getBooleanValue).orElse(null);
    }

    @Override
    public Double getDouble(String property) {
        Optional<SystemConfigEntity> entity = getFirstInstanceOfPropertyConfig(property);
        return entity.map(SystemConfigEntity::getDoubleValue).orElse(null);

    }

    @Override
    public String getString(String property) {
        Optional<SystemConfigEntity> entity = getFirstInstanceOfPropertyConfig(property);
        return entity.map(SystemConfigEntity::getStringValue).orElse(null);

    }

    @Override
    public Integer getInteger(String property) {
        Optional<SystemConfigEntity> entity = getFirstInstanceOfPropertyConfig(property);
        return entity.map(SystemConfigEntity::getIntValue).orElse(null);

    }

    @Override
    public Long getLong(String property) {
        Optional<SystemConfigEntity> entity = getFirstInstanceOfPropertyConfig(property);
        return entity.map(SystemConfigEntity::getLongValue).orElse(null);

    }

    private Optional<SystemConfigEntity> getFirstInstanceOfPropertyConfig(String property) {
        List<SystemConfigEntity> systemConfigEntityList = getSystemConfigRepository().findByProperty(property);
        return systemConfigEntityList.stream().findFirst();
    }

    @Override
    public boolean isPropertyExists(String property) {
        return getFirstInstanceOfPropertyConfig(property).isPresent() ? true : false;
    }

    public SystemConfigRepository getSystemConfigRepository() {
        return systemConfigRepository;
    }

    public void setSystemConfigRepository(SystemConfigRepository systemConfigRepository) {
        this.systemConfigRepository = systemConfigRepository;
    }
}
