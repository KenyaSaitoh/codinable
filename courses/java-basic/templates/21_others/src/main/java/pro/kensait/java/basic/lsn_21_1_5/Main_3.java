package pro.kensait.java.basic.lsn_21_1_5;

public class Main_3 {
    public static void main(String[] args) {
        int base = 100;
        // 匿名クラスのインスタンスを生成する
        CalcFunction cf = new CalcFunction() { // 【1】
            @Override
            public int calc(int x, int y) {
                return base + x + y; // 【2】
            }
        };
        Processor p = new Processor();
        p.process(cf); // 【3】
    }
}