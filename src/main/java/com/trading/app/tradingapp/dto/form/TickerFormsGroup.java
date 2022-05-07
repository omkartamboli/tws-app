package com.trading.app.tradingapp.dto.form;

import com.trading.app.tradingapp.dto.model.OrderModelDto;
import com.trading.app.tradingapp.dto.request.CreateOptionsOrderRequestDto;

import java.util.List;

public class TickerFormsGroup {

    private CreateOptionsOrderRequestDto createOptionsOrderRequestDto;

    private CreateSetOrderFormDto createSetOrderFormDto;

    private List<OrderModelDto> orderModelDtoList;


    public CreateOptionsOrderRequestDto getCreateOptionsOrderRequestDto() {
        return createOptionsOrderRequestDto;
    }

    public void setCreateOptionsOrderRequestDto(CreateOptionsOrderRequestDto createOptionsOrderRequestDto) {
        this.createOptionsOrderRequestDto = createOptionsOrderRequestDto;
    }

    public CreateSetOrderFormDto getCreateSetOrderFormDto() {
        return createSetOrderFormDto;
    }

    public void setCreateSetOrderFormDto(CreateSetOrderFormDto createSetOrderFormDto) {
        this.createSetOrderFormDto = createSetOrderFormDto;
    }

    public List<OrderModelDto> getOrderModelDtoList() {
        return orderModelDtoList;
    }

    public void setOrderModelDtoList(List<OrderModelDto> orderModelDtoList) {
        this.orderModelDtoList = orderModelDtoList;
    }
}
