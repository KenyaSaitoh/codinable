class Person3 {
    String name;
    int age;
    String nationality;
    String email;

    Person3() {
    }

    Person3(String name) {
        this.name = name;
    }

    Person3(String name, int age) { //【1】
        this.name = name;
        this.age = age;
    }

    Person3(String name, int age, String nationality) { //【2】
        this(name, age); //【3】
        this.nationality = nationality;
    }

    // 副作用
    void increaseAge() { //【1】
        age += 1;
    }

    Person3 withAge(int age) {
        this.age = age;
        return this; //【1】
    }

    Person3 withNationality(String nationality) {
        this.nationality = nationality;
        return this;
    }

    // boolean型を返す
    boolean isAdult() { //【2】
        return 20 <= age;
    }

    String createGreetMessage(String language) {
        String message = null;
        if (language.equals("JAPANESE")) {
            message = "おはよう！私は" + name + "、" + age + "歳です。";
        } else {
            message = "Good Morning! I'm " + name + "," + age + " years old.";
        }
        return message;
    }

    /*
    String createGreetMessage(String language, boolean isMorning) {
        String message = null;
        if (isMorning) {
            if (language.equals("JAPANESE")) {
                message = "おはよう！私は" + name + "、" + age + "歳です。";
            } else {
                message = "Good Morning! I'm " + name + "," + age + " years old.";
            }
        } else {
            if (language.equals("JAPAN")) {
                message = "こんにちは！私は" + name + "、" + age + "歳です。";
            } else {
                message = "Good Afternoon! I'm " + name + "," + age + " years old.";
            }
        }
        return message;
    }
    */

    String createGreetMessage(String language, boolean isMorning) {
        if (isMorning) {
            return createGreetMessage(language); //【1】
        }
        String message = null;
        if (language.equals("JAPANESE")) {
            message = "こんにちは！私は" + name + "、" + age + "歳です。";
        } else {
            message = "Good Afternoon! I'm " + name + "," + age + " years old.";
        }
        return message;
    }
}