package pro.kensait.jakartaee.calc.web;

import java.io.Serializable;
import java.math.BigDecimal;

import jakarta.enterprise.context.Conversation;
import jakarta.enterprise.context.ConversationScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import pro.kensait.jakartaee.calc.service.CalcService;
import pro.kensait.jakartaee.calc.service.TaxService;

@Named
@ConversationScoped
public class CalcConversation implements Serializable {
    private static final long serialVersionUID = 1L;

    @Inject
    private Conversation conversation;
    @Inject
    private CalcService calcService;
    @Inject
    private TaxService taxService;

    @NotNull @Min(-1000) @Max(1000)
    private BigDecimal param1;
    @NotNull @Min(-1000) @Max(1000)
    private BigDecimal param2;
    private BigDecimal result;

    public String add() {
        if (conversation.isTransient()) {
            conversation.begin();
        }
        result = calcService.add(param1, param2);
        return "CalcOutputPage?faces-redirect=true";
    }

    public String calcTax() {
        BigDecimal afterTaxAmount = taxService.calcTax(result);
        conversation.end();
        // 会話の Bean はこのリクエストの終わりに破棄されるので、結果は Flash でリダイレクト先へ渡す
        FacesContext.getCurrentInstance().getExternalContext()
                .getFlash().put("afterTaxAmount", afterTaxAmount);
        return "TaxOutputPage?faces-redirect=true";
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
