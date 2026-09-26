package pro.kensait.jpa.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/*
 * tran詳細の機能を提供するクラス
 */
@Entity
@Table(name = "TRAN_DETAIL")
public class TranDetail {
    @EmbeddedId
    private TranDetailPK id;

    @Column(name = "PAY_REC_TYPE")
    @Enumerated(EnumType.STRING) 
    private PayRecType payRecType;

    @Column(name = "AMOUNT")
    private BigDecimal amount;

    // tran詳細の初期化
    public TranDetail() {
    }

    // tran詳細の初期化
    public TranDetail(TranDetailPK id, PayRecType payRecType, BigDecimal amount) {
        this.id = id;
        this.payRecType = payRecType;
        this.amount = amount;
    }

    // IDの取得
    public TranDetailPK getId() {
        return id;
    }

    // IDの設定
    public void setId(TranDetailPK id) {
        this.id = id;
    }

    // payrec型の取得
    public PayRecType getPayRecType() {
        return payRecType;
    }

    // payrec型の設定
    public void setPayRecType(PayRecType payRecType) {
        this.payRecType = payRecType;
    }

    // amountの取得
    public BigDecimal getAmount() {
        return amount;
    }

    // amountの設定
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    // 文字列表現の生成
    @Override
    public String toString() {
        return "TranDetail [id=" + id + ", payRecType=" + payRecType + ", amount="
                + amount + "]";
    }
}
