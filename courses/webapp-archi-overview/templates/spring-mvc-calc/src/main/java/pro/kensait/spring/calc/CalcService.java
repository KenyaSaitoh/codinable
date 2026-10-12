package pro.kensait.spring.calc;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Service;

/*
 * 計算のビジネスロジック
 *
 * 画面や HTTP のことは一切知らない (Model にあたる層)
 * こうしておくと、Controller を通さずに単体テストできる
 */
@Service
public class CalcService {

    /*
     * 計算履歴 (新しいものを先頭に保持する)
     *
     * メモリ上に持つだけなので、サーバーを再起動すると消え、全利用者で共有される
     * 実際のアプリケーションではデータベースに保存する
     * Service は 1 つのインスタンスが複数のスレッドから同時に呼ばれるため、
     * スレッドセーフな CopyOnWriteArrayList を使う
     */
    private final List<String> history = new CopyOnWriteArrayList<>();

    public double add(double param1, double param2) {
        return record(param1, "+", param2, param1 + param2);
    }

    public double subtract(double param1, double param2) {
        return record(param1, "-", param2, param1 - param2);
    }

    public double multiply(double param1, double param2) {
        return record(param1, "×", param2, param1 * param2);
    }

    public double divide(double param1, double param2) {
        if (param2 == 0) {
            throw new IllegalArgumentException("0 で割ることはできません");
        }
        return record(param1, "÷", param2, param1 / param2);
    }

    // 新しい順に limit 件までの履歴を返す (0 以下が指定されたら既定の 10 件)
    public List<String> getHistory(int limit) {
        return history.stream().limit(limit > 0 ? limit : 10).toList();
    }

    // 計算式と結果を履歴の先頭に追加し、結果をそのまま返す
    private double record(double param1, String operator, double param2, double result) {
        history.add(0, param1 + " " + operator + " " + param2 + " = " + result);
        return result;
    }
}
