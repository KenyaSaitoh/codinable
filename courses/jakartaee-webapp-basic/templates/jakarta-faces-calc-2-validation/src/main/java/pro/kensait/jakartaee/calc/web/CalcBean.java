package pro.kensait.jakartaee.calc.web;

import java.math.BigDecimal;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import pro.kensait.jakartaee.calc.service.CalcService;

@Named
@RequestScoped
public class CalcBean {
    @Inject
    private CalcService calcService;

    @NotNull
    @Min(-1000)
    @Max(1000)
    private BigDecimal param1;

    @NotNull
    @Min(-1000)
    @Max(1000)
    private BigDecimal param2;

    private BigDecimal result;

    // 変換と Bean Validation がすべて成功したときだけ呼ばれる
    public String add() {
        result = calcService.add(param1, param2);
        return "CalcOutputPage";
    }

    public String subtract() {
        result = calcService.subtract(param1, param2);
        return "CalcOutputPage";
    }

    public BigDecimal getParam1() {
        return param1;
    }

    public void setParam1(BigDecimal param1) {
        this.param1 = param1;
    }

    public BigDecimal getParam2() {
        return param2;
    }

    public void setParam2(BigDecimal param2) {
        this.param2 = param2;
    }

    public BigDecimal getResult() {
        return result;
    }
}
