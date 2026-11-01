package pro.kensait.java.advanced.lsn_4_1_6;

import java.util.function.Consumer;

public class Main_5 {
    public static void main(String[] args) {
        Printer printer = new Printer();
        // snippet_1：ラムダ式
        Consumer<String> c1 = msg -> printer.print(msg);
        c1.accept("Hello, World!");
        // snippet_2：メソッド参照
        Consumer<String> c2 = printer::print;
        c2.accept("Hello, World!");
    }
}