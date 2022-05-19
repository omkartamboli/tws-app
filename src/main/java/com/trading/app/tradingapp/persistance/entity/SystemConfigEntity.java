package com.trading.app.tradingapp.persistance.entity;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class SystemConfigEntity {

    @Id
    @Column(nullable = false)
    private String property;

    private String stringValue;

    private Integer intValue;

    private Long longValue;

    private Float floatValue;

    private Double doubleValue;

    private Boolean booleanValue;

    public SystemConfigEntity() {
    }

    public SystemConfigEntity(String property, String stringValue) {
        this.property = property;
        this.stringValue = stringValue;
    }

    public SystemConfigEntity(String property, Integer intValue) {
        this.property = property;
        this.intValue = intValue;
    }

    public SystemConfigEntity(String property, Long longValue) {
        this.property = property;
        this.longValue = longValue;
    }

    public SystemConfigEntity(String property, Float floatValue) {
        this.property = property;
        this.floatValue = floatValue;
    }

    public SystemConfigEntity(String property, Double doubleValue) {
        this.property = property;
        this.doubleValue = doubleValue;
    }

    public SystemConfigEntity(String property, Boolean booleanValue) {
        this.property = property;
        this.booleanValue = booleanValue;
    }

    public String getProperty() {
        return property;
    }

    public String getStringValue() {
        return stringValue;
    }

    public Integer getIntValue() {
        return intValue;
    }

    public Long getLongValue() {
        return longValue;
    }

    public Float getFloatValue() {
        return floatValue;
    }

    public Double getDoubleValue() {
        return doubleValue;
    }

    public Boolean getBooleanValue() {
        return booleanValue;
    }
}
