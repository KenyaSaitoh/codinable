package pro.kensait.jakartaee.calc.web;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import pro.kensait.jakartaee.calc.service.CalcService;
import pro.kensait.jakartaee.calc.service.exception.LimitOverException;
import pro.kensait.jakartaee.calc.service.exception.ZeroDivideException;

@Named
@RequestScoped
public class CalcBean {
    @Inject
    private CalcService calcService;

    // LoggerProducer が作る、このクラスの名前のロガー
    @Inject
    private Logger logger;

    @NotNull
    @Min(-1000)
    @Max(1000)
    private BigDecimal param1;

    @NotNull
    @Min(-1000)
    @Max(1000)
    private BigDecimal param2;

    private BigDecimal result;

    private String errorMessage;

    // 足し算: ゼロの入力を項目のエラーにする（業務検証）
    public String add() {
        FacesContext context = FacesContext.getCurrentInstance();
        rejectZero(context, param1, "calcForm:param1", "パラメータ1");
        rejectZero(context, param2, "calcForm:param2", "パラメータ2");
        if (context.isValidationFailed()) {
            return null;
        }
        result = calcService.add(param1, param2);
        return "CalcOutputPage";
    }

    // 引き算: 2 つの値が等しいときは、項目ではなく組合せのエラー（グローバル）にする
    public String subtract() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (param1.compareTo(param2) == 0) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    message(context, "error.same.value"), null));
            context.validationFailed();
            return null;
        }
        result = calcService.subtract(param1, param2);
        return "CalcOutputPage";
    }

    public String multiply() {
        try {
            result = calcService.multiply(param1, param2);
        } catch (LimitOverException exception) {
            logger.log(Level.WARNING, "極度オーバーが発生しました", exception);
            FacesContext context = FacesContext.getCurrentInstance();
            errorMessage = MessageFormat.format(
                    message(context, "error.limit.over"), param1, param2);
            return "CalcErrorPage";
        }
        return "CalcOutputPage";
    }

    public String divide() {
        try {
            result = calcService.divide(param1, param2);
        } catch (ZeroDivideException exception) {
            logger.log(Level.WARNING, "ゼロ割が発生しました", exception);
            FacesContext context = FacesContext.getCurrentInstance();
            errorMessage = message(context, "error.zero.divide");
            return "CalcErrorPage";
        }
        return "CalcOutputPage";
    }

    private void rejectZero(FacesContext context, BigDecimal value,
            String clientId, String label) {
        if (value.compareTo(BigDecimal.ZERO) == 0) {
            String text = MessageFormat.format(
                    message(context, "error.zero.value"), label);
            context.addMessage(clientId,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, text, null));
            context.validationFailed();
        }
    }

    // メッセージバンドル msg（faces-config.xml の resource-bundle）から文言を取り出す
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

    public BigDecimal getResult() {
        return result;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
