package com.trading.app.tradingapp.controller.rest;

import com.trading.app.tradingapp.dto.form.StartRunSequenceFormDto;
import com.trading.app.tradingapp.dto.response.GetMarketDataResponseDto;
import com.trading.app.tradingapp.service.ContractService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/contracts")
public class ContractsController {

    @Resource
    private ContractService contractService;

    @GetMapping("/getmarketdata/{tickerSymbol}")
    @ResponseBody
    public GetMarketDataResponseDto getMarketData(@PathVariable String tickerSymbol) {
        return getContractService().getMarketData(tickerSymbol);
    }

    @PostMapping("/createcontractentity/{tickerSymbol}")
    public void createContractEntity(@PathVariable String tickerSymbol) {
        getContractService().createContractEntity(tickerSymbol);
    }


    @PostMapping("/startbuysequence")
    public void startBuySequence(@RequestBody StartRunSequenceFormDto startRunSequenceFormDto) {
        getContractService().startBuySequence(startRunSequenceFormDto.getTickerSymbol(), startRunSequenceFormDto.getTpMargin(), startRunSequenceFormDto.getQuantity());
    }

    @PostMapping("/stopBuySequence/{tickerSymbol}")
    public void stopBuySequence(@PathVariable String tickerSymbol) {
        getContractService().stopBuySequence(tickerSymbol);
    }


    public ContractService getContractService() {
        return contractService;
    }

    public void setContractService(ContractService contractService) {
        this.contractService = contractService;
    }
}
