package pro.kensait.java.advanced.lsn_7_1_6.boundary_2;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main_2 {
    public static void main(String[] args) {
        List<Integer> list1 = Arrays.asList(1, 2, 3);
        // MyNumberUtil_2.process(list1); コンパイルエラー

        List<Number> list2 = new ArrayList<>(Arrays.asList(1, 2, 3));
        MyNumberUtil_2.process(list2);

        List<Object> list3 = new ArrayList<>(Arrays.asList(1, 2, 3));
        MyNumberUtil_2.process(list3);
    }
}