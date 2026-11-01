package pro.kensait.java.advanced.lsn_4_1_4.closure;

public class Main {
    public static void main(String[] args) {
        int base = 100; //【1】

        // ラムダ式で関数型インタフェースを実装する
        CalcFunction cf = (x, y) -> {
            return base + x + y; //【2】
        };

        // 遅延実行する
        Processor p = new Processor();
        p.process(cf); //【3】
    }
}