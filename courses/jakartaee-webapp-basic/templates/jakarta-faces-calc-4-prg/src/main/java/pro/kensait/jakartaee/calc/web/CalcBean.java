package pro.kensait.jakartaee.calc.web;

import java.math.BigDecimal;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.Flash;
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

    // リダイレクト先のリクエストへ値を持ち越す Flash（Faces 2.3 以降は @Inject で注入できる）
    @Inject
    private Flash flash;

    @NotNull @Min(-1000) @Max(1000)
    private BigDecimal param1;

    @NotNull @Min(-1000) @Max(1000)
    private BigDecimal param2;

    public String add() {
        BigDecimal result = calcService.add(param1, param2);
        flash.put("result", result);
        return "CalcOutputPage?faces-redirect=true";
    }

    public String subtract() {
        BigDecimal result = calcService.subtract(param1, param2);
        flash.put("result", result);
        return "CalcOutputPage?faces-redirect=true";
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
}
