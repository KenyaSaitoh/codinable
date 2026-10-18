package pro.kensait.jakartaee.calc.web;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;

/**
 * 計算アプリケーションの最初の版のバッキングBean
 * 入力エラーを扱う処理はまだ無い（空欄で送ると param1 が null のまま足し算して例外になる）
 */
@Named
@RequestScoped
public class CalcBean {
    private Double param1;
    private Double param2;
    private Double result;

    // 入力画面の「足し算」ボタン（ポストバック）のアクションメソッド
    public String add() {
        result = param1 + param2;
        return "CalcOutputPage";
    }

    // CalcAddByGetPage.xhtml の f:viewAction。GET のクエリの変換と検証が成功したときだけ呼ばれる
    public String addByGet() {
        result = param1 + param2;
        return null;
    }

    public Double getParam1() {
        return param1;
    }

    public void setParam1(Double param1) {
        this.param1 = param1;
    }

    public Double getParam2() {
        return param2;
    }

    public void setParam2(Double param2) {
        this.param2 = param2;
    }

    public Double getResult() {
        return result;
    }
}
