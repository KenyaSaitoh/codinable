package pro.kensait.spring.employee.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import pro.kensait.spring.employee.entity.Department;
import pro.kensait.spring.employee.entity.Job;
import pro.kensait.spring.employee.service.EmployeeService;

/*
 * 社員情報で使用するマスターデータのREST API
 */
@RestController
public class MasterDataApi {
    @Autowired
    private EmployeeService employeeService;

    // 部署一覧の取得
    @GetMapping("/departments")
    public List<Department> departments() {
        return employeeService.departments();
    }

    // 役職一覧の取得
    @GetMapping("/jobs")
    public List<Job> jobs() {
        return employeeService.jobs();
    }
}
