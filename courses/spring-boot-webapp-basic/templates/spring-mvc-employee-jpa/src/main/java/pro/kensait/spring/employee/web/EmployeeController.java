package pro.kensait.spring.employee.web;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import pro.kensait.spring.employee.entity.Department;
import pro.kensait.spring.employee.entity.EmployeeSearchCriteria;
import pro.kensait.spring.employee.entity.Job;
import pro.kensait.spring.employee.service.ConflictException;
import pro.kensait.spring.employee.service.EmployeeService;
import pro.kensait.spring.employee.service.EmployeePage;
import pro.kensait.spring.employee.service.NotFoundException;
import pro.kensait.spring.employee.service.RangeException;
/** 社員管理画面のコントローラー */
@Controller
public class EmployeeController {
    private final EmployeeService employeeService;

    // 社員の初期化
    @SuppressFBWarnings(value = "EI_EXPOSE_REP2",
            justification = "Spring manages the injected collaborator")
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }
    // 初期画面の表示
    @GetMapping({"/", "/employees"})
    public String index(@ModelAttribute EmployeeSearchCriteria criteria,
            @RequestParam(name = "page", defaultValue = "1") int page,
            Model model) {
        return list(criteria.normalized(), page, model, List.of());
    }

    // 登録画面の表示
    @GetMapping("/employees/new")
    public String newForm(Model model) {
        return form(new EmployeeParam(), List.of(), model);
    }

    // 編集画面の表示
    @GetMapping("/employees/{employeeId}/edit")
    public String editForm(@PathVariable("employeeId") Integer employeeId, Model model) {
        try {
            return form(EmployeeParam.of(employeeService.get(employeeId)), List.of(), model);
        } catch (NotFoundException exception) {
            return list(EmployeeSearchCriteria.empty(), 1, model,
                    List.of(exception.getMessage()));
        }
    }

    // APIメソッド：Employeeの新規作成
    @PostMapping("/employees")
    public String create(@ModelAttribute("employeeParam") EmployeeParam employeeParam,
            Model model) {
        return save(employeeParam, model, true);
    }

    // APIメソッド：Employeeの更新
    @PostMapping("/employees/{employeeId}")
    public String update(@PathVariable("employeeId") Integer employeeId,
            @ModelAttribute("employeeParam") EmployeeParam employeeParam,
            Model model) {
        employeeParam.setEmployeeId(employeeId);
        return save(employeeParam, model, false);
    }

    // データの削除
    @PostMapping("/employees/{employeeId}/delete")
    public String delete(@PathVariable("employeeId") Integer employeeId, Model model) {
        List<String> errors = List.of();
        try {
            employeeService.delete(employeeId);
        } catch (NotFoundException exception) {
            errors = List.of(exception.getMessage());
        }
        return list(EmployeeSearchCriteria.empty(), 1, model, errors);
    }

    // データの保存
    private String save(EmployeeParam employeeParam, Model model, boolean creating) {
        // 部署と役職は画面から ID で受け取り、エンティティを取得して関連に設定する
        Department department = employeeService.department(employeeParam.getDepartmentId());
        Job job = employeeService.job(employeeParam.getJobId());
        List<String> errors = employeeParam.validate(department, job);
        if (!errors.isEmpty()) {
            return form(employeeParam, errors, model);
        }
        try {
            if (creating) {
                employeeService.create(employeeParam.toEmployee(department, job));
            } else {
                employeeService.update(employeeParam.getEmployeeId(),
                        employeeParam.toEmployee(department, job), employeeParam.getVersion());
            }
        } catch (ConflictException | NotFoundException exception) {
            return form(employeeParam, List.of(exception.getMessage()), model);
        }
        return list(EmployeeSearchCriteria.empty(), 1, model, List.of());
    }

    // フォームの実行
    private String form(EmployeeParam employeeParam, List<String> errors, Model model) {
        model.addAttribute("employeeParam", employeeParam);
        model.addAttribute("departments", employeeService.departments());
        model.addAttribute("jobs", employeeService.jobs());
        model.addAttribute("errors", errors);
        return "EmployeeFormPage";
    }

    // データ一覧の取得
    private String list(EmployeeSearchCriteria criteria, int page,
            Model model, List<String> errors) {
        List<String> messages = new ArrayList<>(errors);
        EmployeePage found;
        try {
            found = employeeService.search(criteria, page);
        } catch (RangeException exception) {
            messages.add(exception.getMessage());
            found = EmployeePage.empty();
        }
        // 一覧にはエンティティをそのまま渡し、部署名・役職名は画面で関連をたどって表示する
        model.addAttribute("employees", found.content());
        model.addAttribute("keyword",
                criteria.getKeyword() == null ? "" : criteria.getKeyword());
        model.addAttribute("departmentId", criteria.getDepartmentId());
        model.addAttribute("jobId", criteria.getJobId());
        model.addAttribute("salaryFrom", criteria.getSalaryFrom());
        model.addAttribute("salaryTo", criteria.getSalaryTo());
        model.addAttribute("departments", employeeService.departments());
        model.addAttribute("jobs", employeeService.jobs());
        int current = Math.max(page, 1);
        model.addAttribute("page", current);
        model.addAttribute("totalPages", found.totalPages());
        model.addAttribute("totalCount", found.totalElements());
        model.addAttribute("hasPrev", current > 1);
        model.addAttribute("hasNext", current < found.totalPages());
        model.addAttribute("errors", messages);
        return "EmployeeListPage";
    }
}
