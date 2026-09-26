package pro.kensait.spring.employee.rest.api;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import pro.kensait.spring.employee.rest.entity.Department;
import pro.kensait.spring.employee.rest.service.DepartmentService;

/*
 * 部署管理のREST APIを表すクラス
 */
@RestController
@RequestMapping("/departments")
@CrossOrigin
public class DepartmentApi {
    private static final Logger logger = LoggerFactory.getLogger(DepartmentApi.class);

    // インジェクションポイント
    @Autowired
    private DepartmentService departmentService;

    // APIメソッド：全Departmentリスト取得
    @GetMapping
    public ResponseEntity<List<Department>> getAll() {
        logger.info("[ DepartmentApi#getAll ]");

        // ビジネスロジックを呼び出し、Departmentインスタンスのリストを取得する
        List<Department> resultList = departmentService.getDepartmentsAll();

        // ステータスが200でボディにDepartmentリストを保持するResponseEntityを生成し、返す
        return ResponseEntity.ok().body(resultList);
    }
}
