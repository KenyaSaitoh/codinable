package pro.kensait.spring.employee.rest.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import pro.kensait.spring.employee.rest.entity.Department;
import pro.kensait.spring.employee.rest.repository.DepartmentRepository;

/*
 * 部署に対するビジネスロジックを表すクラス
 */
@Service
public class DepartmentService {
    private static final Logger logger = LoggerFactory.getLogger(
            DepartmentService.class);

    // インジェクションポイント
    @Autowired
    private DepartmentRepository departmentRepository;

    // コンストラクタ
    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
    }

    // サービスメソッド：全部署の取得
    public List<Department> getDepartmentsAll() {
        logger.info("[ DepartmentService#getDepartmentsAll ]");
        return departmentRepository.findAll();
    }
}
