package pro.kensait.jakartaee.employee.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

/*
 * JSON-B の既定の変換と、JsonbConfigResolver の設定による変換を比べる
 * （Payara Server と同じ実装の Yasson を使う。サーバーは起動しない）
 */
class JsonbConfigResolverTest {
    private final EmployeeTO victor = new EmployeeTO(10014, "Victor", null, null,
            "ASSOCIATE", 220000, LocalDate.of(2017, 4, 1));

    @Test
    void defaultConfigOmitsNullsAndSortsProperties() throws Exception {
        try (Jsonb jsonb = JsonbBuilder.create()) {
            // null の項目は省略され、項目は辞書順、LocalDate は ISO 8601 の文字列になる
            assertEquals("{\"employeeId\":10014,\"employeeName\":\"Victor\",\"entranceDate\":\"2017-04-01\","
                    + "\"jobName\":\"ASSOCIATE\",\"salary\":220000}", jsonb.toJson(victor));
        }
    }

    @Test
    void resolverWritesNullValues() {
        Jsonb jsonb = new JsonbConfigResolver().getContext(EmployeeTO.class);
        // 部署なしが、項目の省略ではなく明示的な null になる
        assertEquals("{\"departmentId\":null,\"departmentName\":null,\"employeeId\":10014,"
                + "\"employeeName\":\"Victor\",\"entranceDate\":\"2017-04-01\",\"jobName\":\"ASSOCIATE\","
                + "\"salary\":220000}", jsonb.toJson(victor));
    }

    @Test
    void unknownPropertiesAreIgnoredWhenReading() throws Exception {
        try (Jsonb jsonb = JsonbBuilder.create()) {
            // Java の型に無い項目（nickname）は、既定では無視される
            EmployeeTO read = jsonb.fromJson("{\"employeeId\":10001,\"employeeName\":\"Alice\","
                    + "\"nickname\":\"Ali\",\"entranceDate\":\"2012-04-01\"}", EmployeeTO.class);
            assertEquals("Alice", read.employeeName());
            assertEquals(LocalDate.of(2012, 4, 1), read.entranceDate());
            assertEquals(null, read.departmentId());
        }
    }
}
