package pro.kensait.java.basic.lsn_21_1_3;

public class ScoreManager {
    private int[] array;
    public ScoreManager(int... array) {
        this.array = array;
    }
    public int getMax() { // 最大値を返す
        return 0;
    }
    public double getAvg() { // 平均値を返す
        return 0;
    }
    public class ArrayIterator { // 【1】メンバークラス
        private int index = 0; // インデックス
        public int next() {
            return array[index++]; // 【2】
        }
    }
    public ArrayIterator getIterator() { // 【3】
        return new ArrayIterator();
    }
}
