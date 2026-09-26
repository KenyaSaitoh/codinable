package pro.kensait.spring.employee.config;

import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.HandleBeforeSave;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.stereotype.Component;

import pro.kensait.spring.employee.entity.Employee;
/** 自動公開APIにも、保存前イベントでアプリ固有の処理を追加できる */
@Component
@RepositoryEventHandler
public class EmployeeEventHandler {
    // normalize名称の実行
    @HandleBeforeCreate
    @HandleBeforeSave
    public void normalizeName(Employee employee) {
        if (employee.getEmployeeName() != null) {
            employee.setEmployeeName(employee.getEmployeeName().strip());
        }
    }
}
