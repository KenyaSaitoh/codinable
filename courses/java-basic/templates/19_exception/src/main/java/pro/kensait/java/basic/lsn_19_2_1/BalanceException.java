package pro.kensait.java.basic.lsn_19_2_1;

import java.math.BigDecimal;

public class BalanceException extends Exception { //【1】
    private BigDecimal balance; //【2】
    public BalanceException(String message) {
        super(message);
    }
    public BalanceException(Throwable cause) { //【4】
        super(cause);
    }
    public BalanceException(String message, Throwable cause) { //【3】
        super(message, cause);
    }
    public BalanceException(String message, BigDecimal balance) {
        super(message);
        this.balance = balance;
    }
    public BigDecimal getBlance() {
        return balance;
    }
    public void setBlance(BigDecimal balance) {
        this.balance = balance;
    }
}