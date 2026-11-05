package pro.kensait.spring.employee.client;

import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;
import reactor.core.publisher.Mono;

/*
 * @HttpExchange で宣言した社員 API の通信の契約（HTTP インタフェース）
 * 実装は HttpServiceProxyFactory が生成する。戻り値が Mono のメソッドは購読したときに要求を送る
 */
@HttpExchange("/employees") // 【1】
public interface EmployeeHttpClient {
    @GetExchange("/{id}")
    Mono<EmployeeTO> find(@PathVariable("id") int id); // 【2】

    @GetExchange("/query_by_department")
    Mono<List<EmployeeTO>> byDepartment(@RequestParam("departmentId") int departmentId); // 【3】
}
