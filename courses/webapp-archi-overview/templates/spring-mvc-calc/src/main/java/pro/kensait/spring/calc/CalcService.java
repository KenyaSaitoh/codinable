package pro.kensait.spring.calc;

import org.springframework.stereotype.Service;

/*
 * 計算のビジネスロジック。
 *
 * 画面や HTTP のことは一切知らない (Model にあたる層)。
 * こうしておくと、Controller を通さずに単体テストできる。
 */
@Service
public class CalcService {

    public double add(double param1, double param2) {
        return param1 + param2;
    }

    public double subtract(double param1, double param2) {
        return param1 - param2;
    }

    public double multiply(double param1, double param2) {
        return param1 * param2;
    }

    public double divide(double param1, double param2) {
        if (param2 == 0) {
            throw new IllegalArgumentException("0 で割ることはできません");
        }
        return param1 / param2;
    }
}
