package pro.kensait.jakartaee.calc.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** 計算履歴（ID・2 つの入力値・演算種別・結果・日時） */
public class CalcResult {
    private Integer id;
    private BigDecimal param1;
    private BigDecimal param2;
    private CalcType calcType;
    private BigDecimal result;
    private LocalDateTime dateTime;

    public CalcResult(BigDecimal param1, BigDecimal param2, CalcType calcType,
            BigDecimal result, LocalDateTime dateTime) {
        this.param1 = param1;
        this.param2 = param2;
        this.calcType = calcType;
        this.result = result;
        this.dateTime = dateTime;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public BigDecimal getParam1() {
        return param1;
    }

    public BigDecimal getParam2() {
        return param2;
    }

    public CalcType getCalcType() {
        return calcType;
    }

    public BigDecimal getResult() {
        return result;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    // 画面に表示するための日時の文字列
    public String getDateTimeStr() {
        return dateTime.format(DateTimeFormatter.ofPattern("y/MM/dd HH:mm:ss"));
    }

    @Override
    public String toString() {
        return "CalcResult [id=" + id + ", param1=" + param1 + ", param2=" + param2
                + ", calcType=" + calcType + ", result=" + result + ", dateTime="
                + dateTime + "]";
    }
}
