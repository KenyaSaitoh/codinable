package pro.kensait.course;

import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

/** 元教材のmainを実際に実行し、SQL・マッピング・表示処理の互換性を検証する */
class SampleMainTest {
    // 「サンプル処理の実行」の検証
    @TestFactory
    Stream<DynamicTest> runsExamples() throws Exception {
        String names;
        try (var input = getClass().getResourceAsStream("/sample-mains.txt")) {
            names = new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
        return names.lines().filter(line -> !line.isBlank() && !line.startsWith("#"))
                .map(name -> DynamicTest.dynamicTest(name, () -> {
                    SampleDatabase.reset();
                    Class.forName(name).getMethod("main", String[].class)
                            .invoke(null, (Object) new String[0]);
                }));
    }
}
