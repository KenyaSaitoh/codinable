package pro.kensait.jpa.company.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

/*
 * パートタイマーの機能を提供するクラス
 */
@Entity
@DiscriminatorValue(value = "2")
public class Parttimer extends Employee {
    // 時給（パート）
    @Column(name = "PARTTIMER_PAYMENT")
    private Integer parttimerPayment;

    // 引数なしのコンストラクタ
    public Parttimer() {
        super();
    }

    // コンストラクタ
    public Parttimer(Integer employeeId, String employeeName, Department department,
            Integer employeeType, LocalDate entranceDate, Integer parttimerPayment) {
        super(employeeId, employeeName, department, employeeType, entranceDate);
        this.parttimerPayment = parttimerPayment;
    }

    // アクセサメソッド
    public Integer getParttimerPayment() {
        return parttimerPayment;
    }

    // パートタイマー決済の設定
    public void setParttimerPayment(Integer parttimerPayment) {
        this.parttimerPayment = parttimerPayment;
    }

    // 文字列表現の生成
    @Override
    public String toString() {
        return "Parttimer [parttimerPayment=" + parttimerPayment + ", employeeId="
                + employeeId + ", employeeName=" + employeeName + ", department="
                + department + ", entranceDate=" + entranceDate + "]";
    }
}
