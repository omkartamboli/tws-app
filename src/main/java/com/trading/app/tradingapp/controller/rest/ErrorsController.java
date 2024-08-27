package com.trading.app.tradingapp.controller.rest;
import com.trading.app.tradingapp.service.impl.BaseServiceImpl;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;

@RestController
@RequestMapping("/errors")
public class ErrorsController {

    @Resource
    private BaseServiceImpl baseServiceImpl;

    @PostMapping("/simulateErrorMessage/{errorCode}")
    @ResponseBody
    public String CreatePivotBreakOrder(@PathVariable final String errorCode){
        if(null == errorCode || !errorCode.startsWith("ErrorID")){
            return "Invalid error code";
        } else {
            try {
                int errorCodeNumber = Integer.parseInt(errorCode.replace("ErrorID", ""));
                getBaseServiceImpl().error(-1, errorCodeNumber, "Simulated Error !!!");
                return "Error Simulated";
            } catch (NumberFormatException e) {
                return "Invalid error code";
            }
        }
    }

    public BaseServiceImpl getBaseServiceImpl() {
        return baseServiceImpl;
    }

    public void setBaseServiceImpl(BaseServiceImpl baseServiceImpl) {
        this.baseServiceImpl = baseServiceImpl;
    }
}
