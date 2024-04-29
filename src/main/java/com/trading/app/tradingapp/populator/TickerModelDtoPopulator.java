package com.trading.app.tradingapp.populator;

import com.trading.app.tradingapp.dto.model.TickerModelDto;
import com.trading.app.tradingapp.persistance.entity.ContractEntity;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class TickerModelDtoPopulator {

    @Resource
    private CreateSetOrderFormDtoPopulator createSetOrderFormDtoPopulator;

    public TickerModelDto populate(ContractEntity contractEntity){
        if(contractEntity !=null) {
            TickerModelDto tickerModelDto = new TickerModelDto();

            tickerModelDto.setSymbol(contractEntity.getSymbol());
            tickerModelDto.setCurrency(contractEntity.getCurrency());
            tickerModelDto.setDefaultQuantity(contractEntity.getDefaultQuantity());
            tickerModelDto.setExchange(contractEntity.getExchange());
            tickerModelDto.setLtp(contractEntity.getLtp());
            tickerModelDto.setLtpTimestamp(contractEntity.getTickerAskBidLtpValuesUpdateTimestamp());
            tickerModelDto.setSecType(contractEntity.getSecType());
            tickerModelDto.setTickerId(contractEntity.getTickerId());
            tickerModelDto.setCreateSetOrderFormDto(getCreateSetOrderFormDtoPopulator().populate(contractEntity));
            return tickerModelDto;
        }
        return null;
    }

    public CreateSetOrderFormDtoPopulator getCreateSetOrderFormDtoPopulator() {
        return createSetOrderFormDtoPopulator;
    }

    public void setCreateSetOrderFormDtoPopulator(CreateSetOrderFormDtoPopulator createSetOrderFormDtoPopulator) {
        this.createSetOrderFormDtoPopulator = createSetOrderFormDtoPopulator;
    }
}
