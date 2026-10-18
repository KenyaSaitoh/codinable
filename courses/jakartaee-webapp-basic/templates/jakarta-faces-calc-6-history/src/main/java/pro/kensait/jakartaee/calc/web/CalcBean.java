package pro.kensait.jakartaee.calc.web;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import pro.kensait.jakartaee.calc.service.CalcService;
import pro.kensait.jakartaee.calc.service.exception.LimitOverException;
import pro.kensait.jakartaee.calc.service.exception.ZeroDivideException;

/** HTTP 入力と画面遷移を受け持つ。結果の値ではなく、保存した履歴の ID を付けてリダイレクトする */
@Named
@RequestScoped
public class CalcBean {
    private static final Logger logger = Logger.getLogger(CalcBean.class.getName());

    @Inject
    private CalcService calcService;

    @NotNull @Min(-1000) @Max(1000)
    private BigDecimal param1;

    @NotNull @Min(-1000) @Max(1000)
    private BigDecimal param2;

    private String errorMessage;

    public String add() {
        int id = calcService.add(param1, param2);
        return "CalcOutputPage?faces-redirect=true&id=" + id;
    }

    public String subtract() {
        int id = calcService.subtract(param1, param2);
        return "CalcOutputPage?faces-redirect=true&id=" + id;
    }

    public String multiply() {
        try {
            int id = calcService.multiply(param1, param2);
            return "CalcOutputPage?faces-redirect=true&id=" + id;
        } catch (LimitOverException exception) {
            return limitOver(exception);
        }
    }

    public String divide() {
        try {
            int id = calcService.divide(param1, param2);
            return "CalcOutputPage?faces-redirect=true&id=" + id;
        } catch (ZeroDivideException exception) {
            return zeroDivide(exception);
        }
    }

    private String limitOver(LimitOverException exception) {
        logger.log(Level.WARNING, "極度オーバーが発生しました", exception);
        FacesContext context = FacesContext.getCurrentInstance();
        errorMessage = MessageFormat.format(
                message(context, "error.limit.over"), param1, param2);
        return "CalcErrorPage";
    }

    private String zeroDivide(ZeroDivideException exception) {
        logger.log(Level.WARNING, "ゼロ割が発生しました", exception);
        errorMessage = message(FacesContext.getCurrentInstance(), "error.zero.divide");
        return "CalcErrorPage";
    }

    private String message(FacesContext context, String key) {
        return context.getApplication().getResourceBundle(context, "msg").getString(key);
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

    public String getErrorMessage() {
        return errorMessage;
    }
}
