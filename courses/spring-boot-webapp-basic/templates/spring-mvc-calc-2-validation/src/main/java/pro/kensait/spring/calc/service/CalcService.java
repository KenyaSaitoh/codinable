package pro.kensait.spring.calc.service;

import org.springframework.stereotype.Service;

/*
 * 計算機能のビジネスロジックを表すクラス
 */
@Service
public class CalcService {
    // サービスメソッド：足し算の実行
    public double add(double param1, double param2) {
        double result = param1 + param2;
        return result;
    }

    // サービスメソッド：引き算の実行
    public double subtract(double param1, double param2) {
        double result = param1 - param2;
        return result;
    }
}