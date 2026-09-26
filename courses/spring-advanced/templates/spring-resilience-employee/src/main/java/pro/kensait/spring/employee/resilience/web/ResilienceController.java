package pro.kensait.spring.employee.resilience.web;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import pro.kensait.spring.employee.resilience.service.EmployeeClientService;
import pro.kensait.spring.employee.resilience.service.EmployeeTO;
import pro.kensait.spring.employee.resilience.service.EmployeeResult;

/*
 * Resilience教材のコントローラーを担うクラス
 */
@RestController
public class ResilienceController {
    private static final Logger logger = LoggerFactory.getLogger(
            ResilienceController.class);

    // インジェクションポイント
    @Autowired
    private EmployeeClientService employeeClientService;

    // コンストラクタ
    public ResilienceController(EmployeeClientService employeeClientService) {
        this.employeeClientService = employeeClientService;
    }

    // APIメソッド：サーキットブレーカー経由でREST APIを呼び出し、全社員リストの取得
    @GetMapping("/resilience-demo")
    public ResponseEntity<EmployeeResult> resilienceDemo() {
        logger.info("[ ResilienceController#resilienceDemo ]");

        // ビジネスロジックを呼び出す
        // （バックエンド停止中に呼び出しを繰り返すとサーキットがOPENに遷移し、
        //   以降はバックエンドを呼び出さず即座にフォールバックする）
        EmployeeResult employees = employeeClientService.getEmployees();

        // ステータスが200でボディにEmployeeTOリストを保持するResponseEntityを生成し、返す
        return ResponseEntity.ok().body(employees);
    }

    // APIメソッド：レートリミッター経由でREST APIを呼び出し、社員を1件取得
    @GetMapping("/ratelimit-demo")
    public ResponseEntity<EmployeeTO> rateLimitDemo() {
        logger.info("[ ResilienceController#rateLimitDemo ]");

        // ビジネスロジックを呼び出す
        // （短時間に連打すると、上限超過分はRequestNotPermittedが送出される）
        EmployeeTO employee = employeeClientService.getEmployee(1);

        // ステータスが200でボディにEmployeeTOを保持するResponseEntityを生成し、返す
        return ResponseEntity.ok().body(employee);
    }

    // 例外ハンドラー：レートリミッターの上限を超過した場合はステータス429で応答
    @ExceptionHandler(RequestNotPermitted.class)
    public ResponseEntity<String> handleRequestNotPermitted(
            RequestNotPermitted ex) {
        logger.warn("[ ResilienceController#handleRequestNotPermitted ] {}",
                ex.getMessage());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .body("レートリミッターにより呼び出しが制限されました");
    }
}
