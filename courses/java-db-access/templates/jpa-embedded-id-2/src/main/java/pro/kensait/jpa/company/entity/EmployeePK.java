package pro.kensait.jpa.company.entity;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/*
 * 社員主キーの機能を提供するクラス
 */
@Embeddable
public class EmployeePK implements Serializable {
    private static final long serialVersionUID = 1L;
    // 分社ID
    @Column(name = "SUBSIDIARY_ID")
    private Integer subsidiaryId;

    // 社員ID
    @Column(name = "EMPLOYEE_ID")
    private Integer employeeId;

    // 引数なしのコンストラクタ
    public EmployeePK() {
    }

    // コンストラクタ
    public EmployeePK(Integer subsidiaryId, Integer employeeId) {
        this.subsidiaryId = subsidiaryId;
        this.employeeId = employeeId;
    }

    // アクセサメソッド
    public Integer getSubsidiaryId() {
        return subsidiaryId;
    }

    // subsidiaryIDの設定
    public void setSubsidiaryId(Integer subsidiaryId) {
        this.subsidiaryId = subsidiaryId;
    }

    // アクセサメソッド
    public Integer getEmployeeId() {
        return employeeId;
    }

    // 社員IDの設定
    public void setEmployeeId(Integer employeeId) {
        this.employeeId = employeeId;
    }

    // 文字列表現の生成
    @Override
    public String toString() {
        return "EmployeePK [subsidiaryId=" + subsidiaryId + ", employeeId=" + employeeId + "]";
    }

    // 一意性を保証するために、必ずequalsメソッドをオーバーライド
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof EmployeePK other)) {
            return false;
        }
        return Objects.equals(subsidiaryId, other.subsidiaryId)
                && Objects.equals(employeeId, other.employeeId);
    }

    // equalsメソッドに合わせて、hashcodeメソッドもオーバーライド
    @Override
    public int hashCode() {
        return Objects.hash(subsidiaryId, employeeId);
    }
}
