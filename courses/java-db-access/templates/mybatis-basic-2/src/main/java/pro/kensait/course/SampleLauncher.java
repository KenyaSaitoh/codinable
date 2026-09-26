package pro.kensait.course;

import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
 * Codinable の「実行」ボタンから教材の main を動かすための起動クラス
 *
 * 元の教材は HSQLDB サーバーの testdb を setupHsqldb で初期化してから、
 * main を 1 つずつ -PmainClass で選んで実行する。Codinable はサーバーも引数も使えないので、
 * このクラスが次をまとめて行う（教材の main 自体は変更しない）
 *   1. mains.txt に書かれた main を上から順に読む（-PmainClass を付けたときはその 1 つだけ）
 *   2. main ごとに sql/hsqldb/*.sql をインメモリ DB（jdbc:hsqldb:mem:testdb）へ流し、初期状態に戻す
 *   3. main を呼び出す。例外で終わっても次の main へ進み、最後に一覧を出す
 *
 * 「A & B」と書いた行は、A を起動して数秒後に B を別スレッドで起動する。
 * 元の教材で 2 つのターミナルから同時に動かしていたロックの確認を、
 * 1 回の実行の中で（別々の DB セッションとして）再現するためのもの
 */
public final class SampleLauncher {

    // 2 つ目の main を起動するまでの待ち時間（1 つ目が sleep に入るのを待つ）
    private static final long SECOND_START_DELAY_MILLIS = 3000;

    // 同時実行の経過時間を測る起点
    private static volatile long pairStart = System.nanoTime();

    private SampleLauncher() {
    }

    public static void main(String[] args) throws Exception {
        List<String> entries = args.length > 0 ? List.of(String.join(" ", args)) : readEntries();
        if (entries.isEmpty()) {
            System.out.println("mains.txt に実行する main がありません（すべて # で始まっています）。");
            return;
        }

        List<String> failures = new ArrayList<>();
        int index = 0;
        for (String entry : entries) {
            index++;
            resetDatabase();
            String[] classNames = entry.split("&");
            if (classNames.length == 1) {
                banner(index, entries.size(), entry.trim());
                runMain(entry.trim(), failures);
            } else {
                banner(index, entries.size(), entry.trim().replaceAll("\\s*&\\s*", " と ")
                        + " を同時に実行");
                runConcurrently(classNames[0].trim(), classNames[1].trim(), failures);
            }
        }

        System.out.println();
        System.out.println("===== 実行した行: " + entries.size() + " / 例外で終わった main: "
                + failures.size() + " =====");
        failures.forEach(name -> System.out.println("  - " + name));
    }

    // mains.txt から空行と # で始まる行を除いて読む
    private static List<String> readEntries() throws Exception {
        Path file = Path.of(System.getProperty("sample.mains", "mains.txt"));
        return Files.readAllLines(file, StandardCharsets.UTF_8).stream()
                .map(String::trim)
                .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                .toList();
    }

    // 教材の DDL と初期データを流し直す（テスト用の SampleDatabase と同じ手順）
    private static void resetDatabase() throws Exception {
        String url = System.getProperty("sample.db.url", "jdbc:hsqldb:mem:testdb");
        Path dir = Path.of(System.getProperty("sample.sql.dir", "sql/hsqldb"));
        try (var connection = DriverManager.getConnection(url, "SA", "");
                var statement = connection.createStatement();
                var paths = Files.list(dir)) {
            for (Path path : paths.filter(p -> p.toString().endsWith(".sql")).sorted().toList()) {
                String sql = Files.readString(path).replaceAll("(?m)--[^\\r\\n]*", "");
                for (String command : sql.split(";")) {
                    if (!command.isBlank()) {
                        statement.execute(command);
                    }
                }
            }
        }
    }

    private static void banner(int index, int total, String title) {
        System.out.println();
        System.out.println("===== [" + index + "/" + total + "] " + title + " =====");
    }

    // main を 1 つ呼び出す。例外は表示して記録し、次へ進む
    private static void runMain(String className, List<String> failures) {
        try {
            Class.forName(className).getMethod("main", String[].class)
                    .invoke(null, (Object) new String[0]);
        } catch (InvocationTargetException e) {
            System.out.flush();
            System.err.println("!! " + className + " は例外で終わりました: " + e.getCause());
            e.getCause().printStackTrace();
            failures.add(className + " (" + e.getCause().getClass().getSimpleName() + ")");
        } catch (ReflectiveOperationException e) {
            System.err.println("!! " + className + " を起動できません: " + e);
            failures.add(className + " (起動できない)");
        }
    }

    // 1 つ目を起動し、少し待ってから 2 つ目を別スレッドで起動する
    private static void runConcurrently(String first, String second, List<String> failures)
            throws InterruptedException {
        List<String> shared = Collections.synchronizedList(failures);
        pairStart = System.nanoTime();
        Thread a = start(first, shared);
        Thread.sleep(SECOND_START_DELAY_MILLIS);
        Thread b = start(second, shared);
        a.join();
        b.join();
    }

    private static Thread start(String className, List<String> failures) {
        String simpleName = className.substring(className.lastIndexOf('.') + 1);
        Thread thread = new Thread(() -> {
            log(simpleName + " を開始");
            runMain(className, failures);
            log(simpleName + " が終了");
        }, simpleName);
        thread.start();
        return thread;
    }

    private static void log(String message) {
        double seconds = (System.nanoTime() - pairStart) / 1_000_000_000.0;
        System.out.printf("[%6.1f 秒] %s%n", seconds, message);
    }
}
