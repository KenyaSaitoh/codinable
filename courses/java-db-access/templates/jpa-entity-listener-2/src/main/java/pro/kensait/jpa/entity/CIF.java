package pro.kensait.jpa.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

/*
 * cifの機能を提供するクラス
 */
@Entity
@Table(name = "CIF")
public class CIF {
    @Id
    @Column(name = "CIF_NUM")
    private Integer cifNum;

    @Column(name = "CUSTOMER_NAME")
    private String customerName;

    @OneToMany(targetEntity = Account.class,
            mappedBy = "cif",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY)
    private List<Account> accounts = new ArrayList<>();

    // cifの初期化
    public CIF() {
    }

    // cifの初期化
    public CIF(Integer cifNum, String customerName) {
        this.cifNum = cifNum;
        this.customerName = customerName;
    }

    // cifnumの取得
    public Integer getCifNum() {
        return cifNum;
    }

    // cifnumの設定
    public void setCifNum(Integer cifNum) {
        this.cifNum = cifNum;
    }

    // 顧客名称の取得
    public String getCustomerName() {
        return customerName;
    }

    // 顧客名称の設定
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    // accountsの取得
    public List<Account> getAccounts() {
        return accounts;
    }

    // accountsの設定
    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }

    // 文字列表現の生成
    @Override
    public String toString() {
        return "CIF [cifNum=" + cifNum + ", customerName=" + customerName + ", accounts="
                + accounts + "]";
    }
}
