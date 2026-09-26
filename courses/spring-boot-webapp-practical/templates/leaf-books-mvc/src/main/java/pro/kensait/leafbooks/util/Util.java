package pro.kensait.leafbooks.util;

/*
 * ユーティリティの機能を提供するクラス
 */
public class Util {
    // sleeprandomの実行
    public static void sleepRandom(long from, long to) {
        long diff = to - from + 1;
        long sleepTime = from + (long) (Math.random() * diff);
        try {
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
        }
    }
}
