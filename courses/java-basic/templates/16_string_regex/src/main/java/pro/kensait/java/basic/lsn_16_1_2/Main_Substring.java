package pro.kensait.java.basic.lsn_16_1_2;

public class Main_Substring {
    public static void main(String[] args) {
        {
            System.out.println("***** snippet_1 *****");
            String str1 = "Foo_Bar_Foobar";
            String str2 = str1.substring(4); // "Bar_Foobar"
            System.out.println(str2);
            System.out.println("=> end");
        }
        {
            System.out.println("***** snippet_2 *****");
            String str1 = "Foo_Bar_Foobar";
            String str3 = str1.substring(4, 7); // "Bar"
            System.out.println(str3);
            System.out.println("=> end");
        }
    }
}