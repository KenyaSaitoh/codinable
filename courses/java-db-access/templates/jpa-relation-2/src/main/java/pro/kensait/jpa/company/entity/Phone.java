package pro.kensait.jpa.company.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

/*
 * 電話番号の機能を提供するクラス
 */
@Entity
@Table(name = "PHONE")
public class Phone {
    // 所有者ID
    @Id
    @Column(name = "HOLDER_ID")
    private Integer holderId;

    // 所有社員
    @OneToOne(targetEntity = Employee.class)
    @PrimaryKeyJoinColumn()
    private Employee employee;

    // 自宅電話番号
    @Column(name = "HOME_PHONE_NUMBER")
    private String homePhoneNumber;

    // 携帯電話番号
    @Column(name = "MOBILE_PHONE_NUMBER")
    private String mobilePhoneNumber;

    // 引数なしのコンストラクタ
    public Phone() {
    }

    // コンストラクタ
    public Phone(Employee employee, String homePhoneNumber, String mobilePhoneNumber) {
        this.holderId = employee.getEmployeeId();
        this.employee = employee;
        this.homePhoneNumber = homePhoneNumber;
        this.mobilePhoneNumber = mobilePhoneNumber;
    }

    // アクセサメソッド
    public Integer getHolderId() {
        return holderId;
    }

    // 保持IDの設定
    public void setHolderId(Integer holderId) {
        this.holderId = holderId;
    }

    // 社員の取得
    public Employee getEmployee() {
        return employee;
    }

    // 社員の設定
    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    // home電話番号数値の取得
    public String getHomePhoneNumber() {
        return homePhoneNumber;
    }

    // home電話番号数値の設定
    public void setHomePhoneNumber(String homePhoneNumber) {
        this.homePhoneNumber = homePhoneNumber;
    }

    // mobile電話番号数値の取得
    public String getMobilePhoneNumber() {
        return mobilePhoneNumber;
    }

    // mobile電話番号数値の設定
    public void setMobilePhoneNumber(String mobilePhoneNumber) {
        this.mobilePhoneNumber = mobilePhoneNumber;
    }

    // 文字列表現の生成
    @Override
    public String toString() {
        return "Phone [holderId=" + holderId + ", homePhoneNumber=" + homePhoneNumber + ", mobilePhoneNumber="
                + mobilePhoneNumber + "]";
    }
}
