package pro.kensait.jpa.entity;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/*
 * アカウント主キーの機能を提供するクラス
 */
@Embeddable
public class AccountPK implements Serializable {
    @Column(name = "BRANCH_NUM")
    private Integer branchNum;

    @Column(name = "ACCOUNT_NUM")
    private Integer accountNum;

    // アカウント主キーの初期化
    public AccountPK() {
    }

    // アカウント主キーの初期化
    public AccountPK(Integer branchNum, Integer accountNum) {
        this.branchNum = branchNum;
        this.accountNum = accountNum;
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

    // 一意性を保証するために、必ずequalsメソッドをオーバーライド
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountPK balanceId = (AccountPK) o;
        return Objects.equals(branchNum, balanceId.branchNum)
                && Objects.equals(accountNum, balanceId.accountNum);
    }

    // equalsメソッドに合わせて、hashcodeメソッドもオーバーライド
    @Override
    public int hashCode() {
        return Objects.hash(branchNum, accountNum);
    }

    // 文字列表現の生成
    @Override
    public String toString() {
        return "BalancePK [branchNum=" + branchNum + ", accountNum=" + accountNum + "]";
    }
}
