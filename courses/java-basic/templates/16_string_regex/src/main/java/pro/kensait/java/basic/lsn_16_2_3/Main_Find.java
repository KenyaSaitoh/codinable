package pro.kensait.java.basic.lsn_16_2_3;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main_Find {
    public static void main(String[] args) {
        String str = "FooCompany is better than BarCompany";
        Pattern pattern = Pattern.compile("([A-Z].*?)Company");
        Matcher matcher = pattern.matcher(str);
        boolean find1 = matcher.find(); // true
        String group1 = matcher.group(); // "FooCompany"
        boolean find2 = matcher.find(); // true
        String group2 = matcher.group(); // "BarCompany"
        boolean find3 = matcher.find(); // false
        System.out.println(find1);
        System.out.println(group1);
        System.out.println(find2);
        System.out.println(group2);
        System.out.println(find3);
    }
}