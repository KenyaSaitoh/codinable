package pro.kensait.spring.employee.resilience.service;

import java.util.Arrays;
import java.time.Duration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;

/*
 * 社員管理REST API（rest-employee-restclient-api）を呼び出すクライアントサービスを表すクラス
 * （Resilience4jのサーキットブレーカーとレートリミッターを適用する）
 */
@Service
public class EmployeeClientService {
    private static final Logger logger = LoggerFactory.getLogger(
            EmployeeClientService.class);

    // 呼び出し先REST APIのベースURL
    @Value("${employee.api.url}")
    private String employeeApiUrl;

    // REST API呼び出しに利用するRestTemplate
    private final RestTemplate restTemplate = createClient();
    // クライアントの生成
    private static RestTemplate createClient() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(2));
        factory.setReadTimeout(Duration.ofSeconds(3));
        return new RestTemplate(factory);
    }

    // サービスメソッド：全社員リストを取得する（サーキットブレーカー適用）
    // サーキットブレーカーは以下の3つの状態を遷移する
    //   CLOSED    : 通常状態呼び出しをそのまま通し、失敗率を計測する
    //   OPEN      : 失敗率がしきい値を超えた遮断状態バックエンドを呼び出さず
    //               即座にフォールバックする（障害中のバックエンドへの
    //               無駄な呼び出しを止め、障害の連鎖を防ぐ）
    //   HALF_OPEN : OPENから一定時間経過後の試験状態少数の呼び出しだけを通し、
    //               成功すればCLOSEDへ復帰、失敗すればOPENへ戻る
    // 各しきい値はapplication.ymlのresilience4j.circuitbreaker.instances.employeeApiで設定
    @CircuitBreaker(name = "employeeApi", fallbackMethod = "fallbackEmployees")
    public EmployeeResult getEmployees() {
        logger.info("[ EmployeeClientService#getEmployees ]");

        // RestTemplateでREST APIを呼び出す
        // （バックエンド停止中は例外が送出され、失敗としてカウントされる）
        EmployeeTO[] employees = restTemplate.getForObject(
                employeeApiUrl + "/employees", EmployeeTO[].class);
        return new EmployeeResult(employees == null ? List.of() : Arrays.asList(employees), false, "取得成功");
    }

    // フォールバックメソッド：呼び出しに失敗した場合、およびOPEN状態で
    // 遮断された場合に実行される（対象メソッドと同じ戻り値型+末尾に例外引数）
    public EmployeeResult fallbackEmployees(Throwable t) {
        // 警告ログを出力し、fallback=trueの代替応答を返す
        logger.warn("[ EmployeeClientService#fallbackEmployees ] "
                + "フォールバックが実行されました => {}: {}",
                t.getClass().getSimpleName(), t.getMessage());
        return new EmployeeResult(List.of(), true, "社員APIを利用できないため代替応答を返しました");
    }

    // サービスメソッド：主キー検索で社員を取得する（レートリミッター適用）
    // 一定期間内の呼び出し回数が上限（application.ymlのlimit-for-period）を
    // 超えると、RequestNotPermittedが送出
    @RateLimiter(name = "employeeApi")
    public EmployeeTO getEmployee(Integer employeeId) {
        logger.info("[ EmployeeClientService#getEmployee ]");

        // RestTemplateでREST APIを呼び出す
        return restTemplate.getForObject(
                employeeApiUrl + "/employees/" + employeeId, EmployeeTO.class);
    }
}
