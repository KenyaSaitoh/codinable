package pro.kensait.jpa.entity;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/*
 * tran詳細主キーの機能を提供するクラス
 */
@Embeddable
public class TranDetailPK implements Serializable {
    @Column(name = "BRANCH_NUM")
    private Integer branchNum;

    @Column(name = "ACCOUNT_NUM")
    private Integer accountNum;

    @Column(name = "TRAN_DATE")
    private LocalDate tranDate;

    @Column(name = "TRAN_NUM")
    private Integer tranNum;

    // tran詳細主キーの初期化
    public TranDetailPK() {
    }

    // tran詳細主キーの初期化
    public TranDetailPK(Integer branchNum, Integer accountNum, LocalDate tranDate,
            Integer tranNum) {
        this.branchNum = branchNum;
        this.accountNum = accountNum;
        this.tranDate = tranDate;
        this.tranNum = tranNum;
    }

    // branchnumの取得
    public Integer getBranchNum() {
        return branchNum;
    }

    // branchnumの設定
    public void setBranchNum(Integer branchNum) {
        this.branchNum = branchNum;
    }

    // アカウントnumの取得
    public Integer getAccountNum() {
        return accountNum;
    }

    // アカウントnumの設定
    public void setAccountNum(Integer accountNum) {
        this.accountNum = accountNum;
    }

    // tran日付の取得
    public LocalDate getTranDate() {
        return tranDate;
    }

    // tran日付の設定
    public void setTranDate(LocalDate tranDate) {
        this.tranDate = tranDate;
    }

    // trannumの取得
    public Integer getTranNum() {
        return tranNum;
    }

    // trannumの設定
    public void setTranNum(Integer tranNum) {
        this.tranNum = tranNum;
    }

    // equalsメソッドに合わせて、hashcodeメソッドもオーバーライド
    @Override
    public int hashCode() {
        return Objects.hash(accountNum, branchNum, tranDate, tranNum);
    }

    // 一意性を保証するために、必ずequalsメソッドをオーバーライド
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TranDetailPK other = (TranDetailPK) obj;
        return Objects.equals(accountNum, other.accountNum)
                && Objects.equals(branchNum, other.branchNum)
                && Objects.equals(tranDate, other.tranDate)
                && Objects.equals(tranNum, other.tranNum);
    }

    // 文字列表現の生成
    @Override
    public String toString() {
        return "TranDetailPK [branchNum=" + branchNum + ", accountNum=" + accountNum
                + ", tranDate=" + tranDate + ", tranNum=" + tranNum + "]";
    }
}
