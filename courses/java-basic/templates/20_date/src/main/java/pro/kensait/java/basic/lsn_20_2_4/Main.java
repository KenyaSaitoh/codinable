package pro.kensait.java.basic.lsn_20_2_4;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;

public class Main {
    public static void main(String[] args) throws Exception {
        {
            System.out.println("***** snippet_1 *****");
            long remaining = ChronoUnit.DAYS.between(
                    LocalDate.of(2022, 11, 5), LocalDate.of(2023, 3, 31)); // 【1】146
            System.out.println("残日数 => " + remaining);
            System.out.println("=> end");
        }
        {
            System.out.println("***** snippet_2 *****");
            LocalTime start = LocalTime.of(9, 0); 
            LocalTime now = LocalTime.of(15, 20);
            long elapsed = ChronoUnit.MINUTES.between(start, now); //【1】
            System.out.println("経過時間（分） => " + elapsed);
            System.out.println("=> end");
        }
        {
            System.out.println("***** snippet_3 *****");
            Period period = Period.between(
                    LocalDate.of(2012, 4, 1), LocalDate.of(2022, 11, 5)); // 【2】10年7カ月4日
            System.out.println(period.getYears() + "年" +
                    period.getMonths() + "カ月" +
                    period.getDays() + "日"); //【2】
            System.out.println("=> end");
        }
        {
            System.out.println("***** snippet_4 *****");
            Duration duration = Duration.between(
                    LocalTime.of(15, 20), LocalTime.of(21, 30)); // 【3】6時間10分
            System.out.println(duration.toHoursPart() + "時間" +
                    duration.toMinutesPart() + "分"); //【2】
            System.out.println("=> end");
        }
    }
}