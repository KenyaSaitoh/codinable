package pro.kensait.java.basic.lsn_11_1_4;

public class Main_4 {
    public static void main(String[] args) {
        Foo foo = new Qux();
        doSomething(foo);
        doSomething2(foo);
    }

    static void doSomething(Foo foo) {
        if (foo instanceof Qux) {
            Qux qux = (Qux) foo; //【1】
            System.out.println("OK");
        }
    }

    static void doSomething2(Foo foo) {
        if (foo instanceof Qux qux) { //【2】
            // 変数quxに対する処理
            System.out.println("OK");
        }
    }
}