package pro.kensait.spring.employee.retry.web;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import pro.kensait.spring.employee.retry.service.EmployeeClientService;
import pro.kensait.spring.employee.retry.service.EmployeeTO;
import pro.kensait.spring.employee.retry.service.EmployeeResult;

/*
 * リトライ教材のコントローラーを担うクラス
 */
@RestController
public class RetryController {
    private static final Logger logger = LoggerFactory.getLogger(
            RetryController.class);

    // インジェクションポイント
    @Autowired
    private EmployeeClientService employeeClientService;

    // コンストラクタ
    public RetryController(EmployeeClientService employeeClientService) {
        this.employeeClientService = employeeClientService;
    }

    // APIメソッド：リトライ付きでREST APIを呼び出し、全社員リストの取得
    @GetMapping("/retry-demo")
    public ResponseEntity<EmployeeResult> retryDemo() {
        logger.info("[ RetryController#retryDemo ]");

        // ビジネスロジックを呼び出す
        // （バックエンド停止中はリトライ→リカバリーが実行され、fallback=trueの代替応答が返る）
        EmployeeResult employees = employeeClientService.getEmployees();

        // ステータスが200でボディにEmployeeTOリストを保持するResponseEntityを生成し、返す
        return ResponseEntity.ok().body(employees);
    }
}
