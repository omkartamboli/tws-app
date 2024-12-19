package com.trading.app.tradingapp.dto.response;

public class CreateSetOrderResponseDto {

    Integer parentOrderId;

    Integer tpOrderId;

    Integer slOrderId;

    Integer tpTrailAdjustOrderId;

    Boolean status;

    String error;

    public Integer getParentOrderId() {
        return parentOrderId;
    }

    public void setParentOrderId(Integer parentOrderId) {
        this.parentOrderId = parentOrderId;
    }

    public Integer getTpOrderId() {
        return tpOrderId;
    }

    public void setTpOrderId(Integer tpOrderId) {
        this.tpOrderId = tpOrderId;
    }

    public Integer getSlOrderId() {
        return slOrderId;
    }

    public void setSlOrderId(Integer slOrderId) {
        this.slOrderId = slOrderId;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Integer getTpTrailAdjustOrderId() {
        return tpTrailAdjustOrderId;
    }

    public void setTpTrailAdjustOrderId(Integer tpTrailAdjustOrderId) {
        this.tpTrailAdjustOrderId = tpTrailAdjustOrderId;
    }
}
