package pro.kensait.java.advanced.lsn_9_1_3;

import pro.kensait.java.advanced.lsn_9_1_1.Person;

// レッスン9.1.3 のコードを動かすクラス（原稿に出典行は無い。describe() と area() は原稿のコードのまま）
public class Main {
    record Point(int x, int y) {}  // Personはlsn_9_1_1と同じレコード

    static String describe(Object obj) {
        return switch (obj) {
            case Person(String name, int age, String gender) when age < 20 -> name + "さんは未成年です"; //【1】
            case Person(String name, int age, _) -> name + "さんは" + age + "歳です"; //【2】
            case Point(int x, int y) -> "座標(" + x + ", " + y + ")";
            default -> "不明なオブジェクト"; //【3】
        };
    }

    sealed interface Shape permits Circle, Square {}
    record Circle(double r) implements Shape {}
    record Square(double side) implements Shape {}

    static double area(Shape shape) {
        return switch (shape) { //【1】
            case Circle(double r) -> Math.PI * r * r;
            case Square(double side) -> side * side;
        };
    }

    public static void main(String[] args) {
        System.out.println(describe(new Person("Bob", 15, "male")));
        System.out.println(describe(new Person("Alice", 25, "female")));
        System.out.println(describe(new Point(3, 4)));
        System.out.println(describe("foo"));
        System.out.println(area(new Square(2.0)));
        System.out.println(area(new Circle(1.0)));
    }
}
