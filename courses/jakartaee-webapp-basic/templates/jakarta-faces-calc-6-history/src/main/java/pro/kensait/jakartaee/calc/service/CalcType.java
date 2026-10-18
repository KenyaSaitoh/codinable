package pro.kensait.jakartaee.calc.service;

/** 計算種別 */
public enum CalcType {
    ADD("足し算"),
    SUBTRACT("引き算"),
    MULTIPLY("掛け算"),
    DIVIDE("割り算");

    private final String status;

    CalcType(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return status;
    }
}
