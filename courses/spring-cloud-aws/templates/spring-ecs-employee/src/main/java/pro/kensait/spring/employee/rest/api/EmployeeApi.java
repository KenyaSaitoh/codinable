package pro.kensait.spring.employee.rest.api;

import java.net.URI;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import pro.kensait.spring.employee.rest.api.dto.EmployeeTO;
import pro.kensait.spring.employee.rest.service.EmployeeService;

/*
 * 社員管理システムのREST APIを表すクラス
 */
@RestController
@RequestMapping("/employees")
@CrossOrigin
public class EmployeeApi {
    private static final Logger logger = LoggerFactory.getLogger(EmployeeApi.class);

    // インジェクションポイント
    @Autowired
    private EmployeeService employeeService;

    // APIメソッド：主キー検索によるEmployee取得
    @GetMapping(path = "/{employeeId}")
    public ResponseEntity<EmployeeTO> get(
            @PathVariable("employeeId") Integer employeeId) {
        logger.info("[ EmployeeApi#get ]");

        // ビジネスロジックを呼び出し、EmployeeTOインスタンスを取得する
        // 対象が存在しない場合はEmployeeNotFoundExceptionが送出され、
        // RestExceptionAdviceによってステータス404で応答する
        EmployeeTO result = employeeService.getEmployee(employeeId);

        // ステータスが200でボディにEmployeeTOを保持するResponseEntityを生成し、返す
        return ResponseEntity.ok().body(result);
    }

    // APIメソッド：全Employeeリスト取得
    @GetMapping
    public ResponseEntity<List<EmployeeTO>> getAll() {
        logger.info("[ EmployeeApi#getAll ]");

        // ビジネスロジックを呼び出し、EmployeeTOインスタンスのリストを取得する
        List<EmployeeTO> resultList = employeeService.getEmployeesAll();

        // ステータスが200でボディにEmployeeTOリストを保持するResponseEntityを生成し、返す
        return ResponseEntity.ok().body(resultList);
    }

    // APIメソッド：条件検索（部署IDをキーに）によるEmployeeリスト取得
    @GetMapping(path = "/query_by_department")
    public ResponseEntity<List<EmployeeTO>> queryByDepartment(
            @RequestParam("departmentId") Integer departmentId) {
        logger.info("[ EmployeeApi#queryByDepartment ]");

        // ビジネスロジックを呼び出し、EmployeeTOインスタンスのリストを取得する
        List<EmployeeTO> resultList =
                employeeService.getEmployeesByDepartment(departmentId);

        // ステータスが200でボディにEmployeeTOリストを保持するResponseEntityを生成し、返す
        return ResponseEntity.ok().body(resultList);
    }

    // APIメソッド：条件検索（給与範囲をキーに）によるEmployeeリスト取得
    @GetMapping(path = "/query_by_salary")
    public ResponseEntity<List<EmployeeTO>> queryBySalary(
            @RequestParam("lowerSalary") Integer lowerSalary,
            @RequestParam("upperSalary") Integer upperSalary) {
        logger.info("[ EmployeeApi#queryBySalary ]");

        // ビジネスロジックを呼び出し、EmployeeTOインスタンスのリストを取得する
        List<EmployeeTO> resultList =
                employeeService.getEmployeesBySalary(lowerSalary, upperSalary);

        // ステータスが200でボディにEmployeeTOリストを保持するResponseEntityを生成し、返す
        return ResponseEntity.ok().body(resultList);
    }

    // APIメソッド：Employeeの新規作成
    @PostMapping
    public ResponseEntity<EmployeeTO> create(
            @RequestBody @Validated EmployeeTO employeeTO,
            BindingResult errors) {
        logger.info("[ EmployeeApi#create ]");

        // 入力値検証の結果を調べ、エラーの場合はステータス400で応答する
        if (errors.hasErrors()) {
            return new ResponseEntity<EmployeeTO>(HttpStatus.BAD_REQUEST);
        }

        // ビジネスロジックを呼び出し、Employeeを新規作成する
        // このとき、DBで新規採番されたIDを含むEmployeeTOインスタンスを取得する
        EmployeeTO result = employeeService.createEmployee(employeeTO);

        // 新規作成されたリソースのURI（Locationヘッダー用）を生成する
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{employeeId}")
                .buildAndExpand(result.employeeId())
                .toUri();

        // ステータスが201でLocationヘッダーとボディにEmployeeTOを保持する
        // ResponseEntityを生成し、返す
        return ResponseEntity.created(location).body(result);
    }

    // APIメソッド：Employeeの更新
    @PutMapping(path = "/{employeeId}")
    public ResponseEntity<EmployeeTO> update(
            @PathVariable("employeeId") Integer employeeId,
            @RequestBody @Validated EmployeeTO employeeTO,
            BindingResult errors) {
        logger.info("[ EmployeeApi#update ]");

        // 入力値検証の結果を調べ、エラーの場合はステータス400で応答する
        if (errors.hasErrors()) {
            return new ResponseEntity<EmployeeTO>(HttpStatus.BAD_REQUEST);
        }

        // パス変数の社員IDをキーに、ビジネスロジックを呼び出してEmployeeを更新する
        EmployeeTO target = new EmployeeTO(employeeId, employeeTO.employeeName(),
                employeeTO.departmentId(), employeeTO.departmentName(),
                employeeTO.jobName(), employeeTO.salary(), employeeTO.hireDate());
        EmployeeTO result = employeeService.updateEmployee(target);

        // ステータスが200でボディにEmployeeTOを保持するResponseEntityを生成し、返す
        return ResponseEntity.ok().body(result);
    }

    // APIメソッド：Employeeの削除
    @DeleteMapping(path = "/{employeeId}")
    public ResponseEntity<Void> delete(
            @PathVariable("employeeId") Integer employeeId) {
        logger.info("[ EmployeeApi#delete ]");

        // ビジネスロジックを呼び出し、Employeeを削除する
        employeeService.removeEmployee(employeeId);

        // ステータスが204でボディを持たないResponseEntityを生成し、返す
        return ResponseEntity.noContent().build();
    }
}
