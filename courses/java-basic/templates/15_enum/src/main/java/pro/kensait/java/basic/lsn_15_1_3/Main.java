package pro.kensait.java.basic.lsn_15_1_3;

public class Main {
    public static void main(String[] args) {
        {
            System.out.println("***** snippet_1 *****");
            CustomerType ct = CustomerType.valueOf("GENERAL"); // 一般会員
            System.out.println(ct);
            System.out.println("=> end");
        }
        {
            System.out.println("***** snippet_2 *****");
            int customerTypeNum = 1; // ゴールド会員
            CustomerType found = null;
            for (CustomerType ct2 : CustomerType.values()) {
                if (customerTypeNum == ct2.ordinal()) {
                    found = ct2;
                    break;
                }
            }
            System.out.println(found);
            System.out.println("=> end");
        }
    }
}
