package pro.kensait.jpa.entity;

import jakarta.persistence.PostLoad;
/** コールバックでは通知だけを行い、別の永続化コンテキストを開かない */
public class AccountListener {
    // コールバックメソッド（SELECT文の発行後に呼び出される）
    @PostLoad
    public void postLoad(Account account) {
        System.out.println("Account loaded => " + account);
    }
}
