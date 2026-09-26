package pro.kensait.spring.employee.retry.service;

import java.util.Arrays;
import java.time.Duration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.RetryContext;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.retry.support.RetrySynchronizationManager;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

/*
 * 社員管理REST API（rest-employee-restclient-api）を呼び出すクライアントサービスを表すクラス
 * （通信に失敗した場合、Spring Retryによって自動的にリトライされる）
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

    // サービスメソッド：全社員リストを取得する（失敗時は自動リトライ）
    //   retryFor    = ResourceAccessException.class
    //                 : リトライ対象の例外（接続失敗など通信レベルの例外）
    //   maxAttempts = 3 : 初回実行を含めて最大3回まで試行する
    //   delay       = 1000, multiplier = 2
    //                 : 待機時間を1秒→2秒と倍々に延ばす「指数バックオフ」
    //                   （障害中のバックエンドに集中アクセスして追い打ちを
    //                   かけないよう、リトライ間隔を徐々に広げるのが定石）
    @Retryable(retryFor = ResourceAccessException.class, maxAttempts = 3,
            backoff = @Backoff(delay = 1000, multiplier = 2))
    public EmployeeResult getEmployees() {
        // 現在の試行回数を取得し、ログに出力する（リトライの動きを観察するため）
        RetryContext context = RetrySynchronizationManager.getContext();
        int attempt = (context != null ? context.getRetryCount() : 0) + 1;
        logger.info("[ EmployeeClientService#getEmployees ] 試行 {} 回目", attempt);

        // RestTemplateでREST APIを呼び出す
        // （バックエンド停止中はResourceAccessExceptionが送出され、リトライされる）
        EmployeeTO[] employees = restTemplate.getForObject(
                employeeApiUrl + "/employees", EmployeeTO[].class);
        return new EmployeeResult(employees == null ? List.of() : Arrays.asList(employees), false, "取得成功");
    }

    // リカバリーメソッド：リトライ上限まで失敗した場合のフォールバック
    // （@Retryableメソッドと同じ戻り値型で、第1引数に対象例外を受け取る）
    @Recover
    public EmployeeResult recoverEmployees(ResourceAccessException ex) {
        // 警告ログを出力し、fallback=trueの代替応答を返す
        logger.warn("[ EmployeeClientService#recoverEmployees ] "
                + "リトライ上限に達したため代替応答を返します => {}",
                ex.getMessage());
        return new EmployeeResult(List.of(), true, "社員APIを利用できないため代替応答を返しました");
    }
}
