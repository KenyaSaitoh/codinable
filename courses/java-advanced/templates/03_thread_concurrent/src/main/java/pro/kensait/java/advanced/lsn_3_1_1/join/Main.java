package pro.kensait.java.advanced.lsn_3_1_1.join;

public class Main {
    public static void main(String[] args) throws Exception {
        System.out.println("メインスレッド開始");

        // 一つ目のスレッドを生成し、起動する
        MyParaThread t1 = new MyParaThread("foo", 8000L); // 8秒間一時停止するスレッド
        t1.start();
        // 二つ目のスレッドを生成し、起動する
        MyParaThread t2 = new MyParaThread("bar", 5000L); // 5秒間一時停止するスレッド
        t2.start();

        // 待ち合わせ
        t1.join(); //【1】
        t2.join(); //【2】

        System.out.println("メインスレッド終了"); //【3】
    }
}