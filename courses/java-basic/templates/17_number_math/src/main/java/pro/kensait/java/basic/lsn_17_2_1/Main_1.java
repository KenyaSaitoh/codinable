package pro.kensait.java.basic.lsn_17_2_1;

import java.math.BigDecimal;

public class Main_1 {
    public static void main(String[] args) {
        {
            System.out.println("***** snippet_1 *****");
            BigDecimal val = new BigDecimal(10000);
            System.out.println(val);
            System.out.println(val.precision());
            System.out.println(val.scale());
            System.out.println("=> end");
        }
        {
            System.out.println("***** snippet_2 *****");
            BigDecimal val2 = new BigDecimal("123.4567");    // 123.4567
            System.out.println(val2);
            System.out.println(val2.precision());
            System.out.println(val2.scale());
            System.out.println("=> end");
        }
        {
            System.out.println("***** snippet_3 *****");
            BigDecimal val3 = BigDecimal.valueOf(1234567, 4); // 123.4567
            System.out.println(val3);
            System.out.println(val3.precision());
            System.out.println(val3.scale());
            System.out.println("=> end");
        }
        {
            System.out.println("***** snippet_4 *****");
            BigDecimal val4 = BigDecimal.valueOf(123.4567);   // 123.4567
            System.out.println(val4);
            System.out.println(val4.precision());
            System.out.println(val4.scale());
            System.out.println("=> end");
        }
        {
            System.out.println("***** snippet_5 *****");
            BigDecimal val5 = new BigDecimal(123.4567);
            // 123.4566999999999978854248183779418468475341796875
            System.out.println(val5);
            System.out.println(val5.precision());
            System.out.println(val5.scale());
            System.out.println("=> end");
        }
    }
}