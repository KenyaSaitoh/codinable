package pro.kensait.spring.calc.service;

/*
 * 計算種別
 */
public enum CalcType {
    ADD("足し算"),
    SUBTRACT("引き算"),
    MULTIPLY("掛け算"),
    DIVIDE("割り算");

    private final String status;

    // 計算型の初期化
    CalcType(String status) {
        this.status = status;
    }

    // 文字列表現の生成
    public String toString() {
        return status;
    }
}
