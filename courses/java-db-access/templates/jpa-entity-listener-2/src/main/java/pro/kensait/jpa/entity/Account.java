package pro.kensait.jpa.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/*
 * アカウントの機能を提供するクラス
 */
@Entity
@Table(name = "ACCOUNT")
@EntityListeners({AccountListener.class})
public class Account {
    @EmbeddedId
    private AccountPK id;

    @ManyToOne(targetEntity = CIF.class,
            cascade = CascadeType.ALL,
            fetch = FetchType.EAGER)
    @JoinColumn(name = "CIF_NUM",
            referencedColumnName = "CIF_NUM")
    private CIF cif;

    @Column(name = "BALANCE")
    private BigDecimal balance;

    @Column(name = "LAST_TRAN_DATE")
    private LocalDate lastTranDate;

    @Column(name = "LAST_TRAN_NUM")
    private Integer lastTranNum;

    // アカウントの初期化
    public Account() {
    }

    // アカウントの初期化
    public Account(AccountPK id, CIF cif, BigDecimal balance, LocalDate lastTranDate,
            Integer lastTranNum) {
        this.id = id;
        this.cif = cif;
        this.balance = balance;
        this.lastTranDate = lastTranDate;
        this.lastTranNum = lastTranNum;
    }

    // IDの取得
    public AccountPK getId() {
        return id;
    }

    // IDの設定
    public void setId(AccountPK id) {
        this.id = id;
    }

    // cifの取得
    public CIF getCif() {
        return cif;
    }

    // cifの設定
    public void setCif(CIF cif) {
        this.cif = cif;
    }

    // 残高の取得
    public BigDecimal getBalance() {
        return balance;
    }

    // 残高の設定
    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    // 末尾tran日付の取得
    public LocalDate getLastTranDate() {
        return lastTranDate;
    }

    // 末尾tran日付の設定
    public void setLastTranDate(LocalDate lastTranDate) {
        this.lastTranDate = lastTranDate;
    }

    // 末尾trannumの取得
    public Integer getLastTranNum() {
        return lastTranNum;
    }

    // 末尾trannumの設定
    public void setLastTranNum(Integer lastTranNum) {
        this.lastTranNum = lastTranNum;
    }

    // 文字列表現の生成
    @Override
    public String toString() {
        return "Account [id=" + id + ", balance=" + balance
                + ", lastTranDate=" + lastTranDate + ", lastTranNum=" + lastTranNum + "]";
    }
}
