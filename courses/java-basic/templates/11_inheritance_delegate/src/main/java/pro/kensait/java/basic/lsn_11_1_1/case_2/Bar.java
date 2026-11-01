package pro.kensait.java.basic.lsn_11_1_1.case_2;

public class Bar extends Foo {
    private int y;
    public Bar(int x, int y) { //【1】
        super(x); //【2】
        this.y = y; //【3】
    }
    public int subtract() {
        return base - getX() - y; //【4】
    }
}