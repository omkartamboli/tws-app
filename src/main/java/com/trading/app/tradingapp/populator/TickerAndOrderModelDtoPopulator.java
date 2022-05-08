package com.trading.app.tradingapp.populator;

import com.trading.app.tradingapp.dto.OrderType;
import com.trading.app.tradingapp.dto.form.CreateSetOrderFormDto;
import com.trading.app.tradingapp.dto.form.TickerFormsGroup;
import com.trading.app.tradingapp.dto.model.OrderModelDto;
import com.trading.app.tradingapp.dto.request.CreateOptionsOrderRequestDto;
import com.trading.app.tradingapp.persistance.entity.ContractEntity;
import com.trading.app.tradingapp.persistance.entity.OrderEntity;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;


@Component
public class TickerAndOrderModelDtoPopulator {

    @Resource
    private CreateSetOrderFormDtoPopulator createSetOrderFormDtoPopulator;

    @Resource
    private OrderModelDtoPopulator orderModelDtoPopulator;


    public Map<CreateSetOrderFormDto , TickerFormsGroup> populate(Map<ContractEntity, List<OrderEntity>> contractOrdersMap) {

        if (contractOrdersMap != null) {
            Map<CreateSetOrderFormDto , TickerFormsGroup> tickerOrderModelMap = new HashMap<>(contractOrdersMap.size());
            contractOrdersMap.forEach((key, value) -> {

                TickerFormsGroup tickerFormsGroup = new TickerFormsGroup();
                CreateSetOrderFormDto createSetOrderFormDto = getCreateSetOrderFormDtoPopulator().populate(key);
                tickerFormsGroup.setCreateSetOrderFormDto(createSetOrderFormDto);
                CreateOptionsOrderRequestDto createOptionsOrderRequestDto = new CreateOptionsOrderRequestDto();
                createOptionsOrderRequestDto.setTicker(key.getSymbol());
                createOptionsOrderRequestDto.setQuantity(1);
                createOptionsOrderRequestDto.setDateYYYYMMDD(getNextFridayDateAsYYYYMMDD());
                tickerFormsGroup.setCreateOptionsOrderRequestDto(createOptionsOrderRequestDto);
                tickerFormsGroup.setOrderModelDtoList(value.stream().map(order -> orderModelDtoPopulator.populate(order)).collect(Collectors.toList()));

                tickerOrderModelMap.put(createSetOrderFormDto,tickerFormsGroup);
            });

            return sortAndReturn(tickerOrderModelMap);
        }

        return null;
    }

    public CreateSetOrderFormDtoPopulator getCreateSetOrderFormDtoPopulator() {
        return createSetOrderFormDtoPopulator;
    }

    public void setCreateSetOrderFormDtoPopulator(CreateSetOrderFormDtoPopulator createSetOrderFormDtoPopulator) {
        this.createSetOrderFormDtoPopulator = createSetOrderFormDtoPopulator;
    }

    public OrderModelDtoPopulator getOrderModelDtoPopulator() {
        return orderModelDtoPopulator;
    }

    public void setOrderModelDtoPopulator(OrderModelDtoPopulator orderModelDtoPopulator) {
        this.orderModelDtoPopulator = orderModelDtoPopulator;
    }

    private Map<CreateSetOrderFormDto , TickerFormsGroup> sortAndReturn(Map<CreateSetOrderFormDto , TickerFormsGroup> tickerOrderModelMap) {
        Map<CreateSetOrderFormDto , TickerFormsGroup> treeMap = new TreeMap<>(
                Comparator.comparing(CreateSetOrderFormDto::getTicker));

        treeMap.putAll(tickerOrderModelMap);
        return treeMap;
    }

    private String getNextFridayDateAsYYYYMMDD(){
        LocalDate d = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.FRIDAY));
        return new StringBuilder(d.getYear()).append(d.getMonth()).append(d.getDayOfMonth()).toString();


    }
}
