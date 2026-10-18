package pro.kensait.jakartaee.calc.service;

import java.math.BigDecimal;

import jakarta.enterprise.context.ApplicationScoped;

/** 計算だけを受け持つ業務処理のクラス。状態を持たないのでアプリケーション全体で 1 つを共有する */
@ApplicationScoped
public class CalcService {

    public BigDecimal add(BigDecimal param1, BigDecimal param2) {
        return param1.add(param2);
    }

    public BigDecimal subtract(BigDecimal param1, BigDecimal param2) {
        return param1.subtract(param2);
    }
}
