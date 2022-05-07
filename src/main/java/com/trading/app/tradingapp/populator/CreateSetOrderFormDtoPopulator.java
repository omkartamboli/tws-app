package com.trading.app.tradingapp.populator;


import com.trading.app.tradingapp.dto.form.CreateSetOrderFormDto;

import com.trading.app.tradingapp.persistance.entity.ContractEntity;
import org.springframework.stereotype.Component;

@Component
public class CreateSetOrderFormDtoPopulator {

    public CreateSetOrderFormDto populate(ContractEntity contractEntity) {
        if (contractEntity != null) {
            CreateSetOrderFormDto createSetOrderFormDto = new CreateSetOrderFormDto();


            createSetOrderFormDto.setTicker(contractEntity.getSymbol());
            createSetOrderFormDto.setQuantity(contractEntity.getDefaultQuantity());
            createSetOrderFormDto.setTransactionPrice(contractEntity.getLtp());

            createSetOrderFormDto.setTargetPriceOffsetBuyStep1(contractEntity.getStep1());
            createSetOrderFormDto.setTargetPriceOffsetBuyStep2(contractEntity.getStep2());
            createSetOrderFormDto.setTargetPriceOffsetBuyStep3(contractEntity.getStep3());
            createSetOrderFormDto.setTargetPriceOffsetBuyStep4(contractEntity.getStep4());
            createSetOrderFormDto.setTargetPriceOffsetBuyStep5(contractEntity.getStep5());
            createSetOrderFormDto.setStartBuyRunMargin(contractEntity.getStep4());

            createSetOrderFormDto.setTargetPriceOffsetSellStep1(contractEntity.getStep1());
            createSetOrderFormDto.setTargetPriceOffsetSellStep2(contractEntity.getStep2());
            createSetOrderFormDto.setTargetPriceOffsetSellStep3(contractEntity.getStep3());
            createSetOrderFormDto.setTargetPriceOffsetSellStep4(contractEntity.getStep4());
            createSetOrderFormDto.setTargetPriceOffsetSellStep5(contractEntity.getStep5());
            createSetOrderFormDto.setStartSellRunMargin(contractEntity.getStep4());

            return createSetOrderFormDto;
        }
        return null;
    }
}
