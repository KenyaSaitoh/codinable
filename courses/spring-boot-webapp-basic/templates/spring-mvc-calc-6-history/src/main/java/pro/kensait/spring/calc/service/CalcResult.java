package pro.kensait.spring.calc.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/*
 * 計算結果を表すクラス
 */
public class CalcResult {
    // ID
    private Integer id;
    // パラメータ1
    private BigDecimal param1;
    // パラメータ2
    private BigDecimal param2;
    // 計算種別
    private CalcType calcType;
    // 計算結果
    private BigDecimal result;
    // 計算日時
    private LocalDateTime dateTime;

    // コンストラクタ
    public CalcResult(BigDecimal param1, BigDecimal param2, CalcType calcType,
            BigDecimal result, LocalDateTime dateTime) {
        this.param1 = param1;
        this.param2 = param2;
        this.calcType = calcType;
        this.result = result;
        this.dateTime = dateTime;
    }
    
    // IDの取得
    public Integer getId() {
        return id;
    }

    // IDの設定
    public void setId(Integer id) {
        this.id = id;
    }

    // アクセサメソッド
    public BigDecimal getParam1() {
        return param1;
    }

    // パラメータ1の設定
    public void setParam1(BigDecimal param1) {
        this.param1 = param1;
    }

    // パラメータ2の取得
    public BigDecimal getParam2() {
        return param2;
    }

    // パラメータ2の設定
    public void setParam2(BigDecimal param2) {
        this.param2 = param2;
    }

    // 計算型の取得
    public CalcType getCalcType() {
        return calcType;
    }

    // 計算型の設定
    public void setCalcType(CalcType calcType) {
        this.calcType = calcType;
    }

    // 結果の取得
    public BigDecimal getResult() {
        return result;
    }

    // 結果の設定
    public void setResult(BigDecimal result) {
        this.result = result;
    }

    // 日付時刻の取得
    public LocalDateTime getDateTime() {
        return dateTime;
    }

    // 日付時刻の設定
    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    // 日付時刻strの取得
    public String getDateTimeStr() {
        return dateTime.format(DateTimeFormatter.ofPattern("y/MM/dd HH:mm:ss"));
    }

    // 文字列表現の生成
    @Override
    public String toString() {
        return "CalcResult [id=" + id + ", param1=" + param1 + ", param2=" + param2
                + ", calcType=" + calcType + ", result=" + result + ", dateTime="
                + dateTime + "]";
    }
}
