package pro.kensait.jakartaee.calc.web;

import java.util.List;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import pro.kensait.jakartaee.calc.service.CalcResult;
import pro.kensait.jakartaee.calc.service.CalcService;

/** 結果画面（CalcOutputPage.xhtml）と履歴画面（CalcHistoryPage.xhtml）のバッキングBean */
@Named
@RequestScoped
public class CalcResultBean {
    @Inject
    private CalcService calcService;

    private Integer id;
    private CalcResult calcResult;

    // f:viewAction。ビューパラメータ id の変換と検証が成功したときだけ呼ばれる
    public String load() {
        calcResult = calcService.getCalcResult(id);
        if (calcResult == null) {
            return "CalcNotFoundPage";
        }
        return null;
    }

    public List<CalcResult> getCalcResults() {
        return calcService.getCalcResults();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public CalcResult getCalcResult() {
        return calcResult;
    }
}
