package pro.kensait.spring.employee.rest.api;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import pro.kensait.spring.employee.rest.entity.Department;
import pro.kensait.spring.employee.rest.entity.Job;
import pro.kensait.spring.employee.rest.service.EmployeeService;
/** MVCの選択肢に相当する部署・役職マスタ */
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
