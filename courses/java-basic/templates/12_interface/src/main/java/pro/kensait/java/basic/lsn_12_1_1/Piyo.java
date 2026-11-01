package pro.kensait.java.basic.lsn_12_1_1;

public class Piyo implements Foo, Bar {
    @Override
    public void sayYes() { //【1】
        System.out.println("Piyo says YES!");
    }

    @Override
    public void sayHello() { //【2】
        System.out.println("Piyo says Hello!");
    }

    @Override
    public void sayGoodbye() {
        System.out.println("Piyo says Goodbye!");
    }
    
    public void sayGoodnight() { //【3】
        System.out.println("Piyo says Goodnight!");
    }
}