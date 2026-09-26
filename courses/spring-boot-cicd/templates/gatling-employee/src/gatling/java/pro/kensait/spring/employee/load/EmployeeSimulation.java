package pro.kensait.spring.employee.load;

import static io.gatling.javaapi.core.CoreDsl.css;
import static io.gatling.javaapi.core.CoreDsl.csv;
import static io.gatling.javaapi.core.CoreDsl.details;
import static io.gatling.javaapi.core.CoreDsl.global;
import static io.gatling.javaapi.core.CoreDsl.rampUsers;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

import io.gatling.javaapi.core.FeederBuilder;
import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
/** MVCアプリの一覧・検索・編集フォームを読む短い負荷シナリオ */
public class EmployeeSimulation extends Simulation {
    private static final int USERS = 20;
    private static final int RAMP_SECONDS = 10;
    private static final int P95_LIMIT_MILLIS = 1000;
    private final String baseUrl = System.getProperty("baseUrl", "http://localhost:8080");
    private final FeederBuilder.FileBased<String> employeeFeeder = csv("employees.csv").circular();
    private final HttpProtocolBuilder httpProtocol = http.baseUrl(baseUrl)
            .disableWarmUp().disableCaching().disableFollowRedirect()
            .acceptHeader("text/html").userAgentHeader("Gatling employee sample");
    private final ScenarioBuilder browseEmployees = scenario("社員一覧・部署検索・編集フォーム")
            .feed(employeeFeeder)
            .exec(http("社員一覧画面").get("/employees")
                    .check(status().is(200), css("h1").is("社員管理"),
                            css("#paging").is("1/2ページ（10件）"),
                            css("#employees tr td:nth-child(1)").findAll()
                                    .is(List.of("Alice", "Bob", "Carol", "Dave", "Ellen"))))
            .pause(Duration.ofMillis(300))
            .exec(http("部署別社員検索").get("/employees").queryParam("departmentId", "#{departmentId}")
                    .check(status().is(200),
                            css("#departmentId option[selected]", "value").isEL("#{departmentId}"),
                            css("#employees tr td:nth-child(1)").findAll()
                                    .is(session -> Arrays.asList(session.getString("departmentEmployeeNames")
                                            .split("\\|")))))
            .exec(http("社員編集フォーム").get("/employees/#{employeeId}/edit")
                    .check(status().is(200), css("h2").is("更新"),
                            css("#employeeForm", "action").isEL("/employees/#{employeeId}"),
                            css("#employeeName", "value").isEL("#{employeeName}"),
                            css("#formDepartmentId option[selected]", "value").isEL("#{departmentId}"),
                            css("#formJobId option[selected]", "value").isEL("#{jobId}"),
                            css("#salary", "value").isEL("#{salary}"),
                            css("#entranceDate", "value").isEL("#{entranceDate}"),
                            css("input[name=version]", "value").is("0")));

    {
        // 合計20人を10秒で到着させるopen model常時20人を同時実行する指定ではない
        setUp(browseEmployees.injectOpen(rampUsers(USERS).during(Duration.ofSeconds(RAMP_SECONDS))))
                .protocols(httpProtocol)
                .assertions(global().allRequests().count().is(USERS * 3L),
                        global().failedRequests().count().is(0L),
                        global().responseTime().percentile(95.0).lt(P95_LIMIT_MILLIS),
                        details("社員一覧画面").allRequests().count().is((long) USERS),
                        details("部署別社員検索").allRequests().count().is((long) USERS),
                        details("社員編集フォーム").allRequests().count().is((long) USERS));
    }
}
