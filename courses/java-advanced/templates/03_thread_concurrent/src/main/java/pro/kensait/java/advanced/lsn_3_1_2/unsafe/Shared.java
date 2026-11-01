package pro.kensait.java.advanced.lsn_3_1_2.unsafe;

import static pro.kensait.java.advanced.common.ThreadUtil.*;

public class Shared {
    private int data;

    public Shared(int data) {
        this.data = data;
    }

    // dataフィールドへの更新処理
    // この処理は「アトミックな処理」ではないため、本来は
    // synchronizedキーワードによってこのメソッドを同期化する必要がある
    public void addData(int num) {
        int tmp = this.data + num; //【1】
        sleepAWhile(10); //【2】
        this.data = tmp; //【3】
    }

    public int getData() {
        return data;
    }
}