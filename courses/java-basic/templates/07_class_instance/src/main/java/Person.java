class Person {
    // フィールド
    String name; //【1】
    int age;

    // コンストラクタ
    Person() { //【2】
    }
    Person(String name, int age) {
        this.name = name;
        this.age = age;
    }

    // メソッド
    void sayHello() { //【3】
        String message = "こんにちは！私は" + name +  "、" + age + "歳です。";
        System.out.println(message);
    }
}