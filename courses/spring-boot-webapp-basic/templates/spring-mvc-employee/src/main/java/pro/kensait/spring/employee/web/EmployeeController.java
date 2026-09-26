package pro.kensait.spring.employee.web;

import java.util.List;
import java.util.Locale;

import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import pro.kensait.spring.employee.service.Employee;
import pro.kensait.spring.employee.service.EmployeeService;
/** 関連を使わない社員管理アプリのコントローラー */
@Controller
@SessionAttributes("employeeSession")
public class EmployeeController {
    private final EmployeeService employeeService;
    private final MessageSource messageSource;

    // 社員の初期化
    @SuppressFBWarnings(value = "EI_EXPOSE_REP2",
            justification = "Spring manages the injected collaborators")
    public EmployeeController(EmployeeService employeeService, MessageSource messageSource) {
        this.employeeService = employeeService;
        this.messageSource = messageSource;
    }

    // initセッションの実行
    @ModelAttribute("employeeSession")
    public EmployeeSession initSession() {
        return new EmployeeSession();
    }

    // アクションメソッド：入力画面への遷移
    @GetMapping("/")
    public String index() {
        return "redirect:/viewList";
    }

    // アクションメソッド：入力画面への遷移
    @PostMapping("/toCreate")
    public String toCreate(SessionStatus sessionStatus) {
        sessionStatus.setComplete();
        return "EmployeeInputPage";
    }

    // アクションメソッド：確認画面への遷移
    @PostMapping("/toConfirm")
    public String toConfirm(@Validated EmployeeSession employeeSession,
            BindingResult errors, Model model) {
        if (errors.hasErrors()) {
            String errorMessage = messageSource.getMessage("error.occured", null,
                    Locale.JAPANESE);
            model.addAttribute("errorMessage", errorMessage);
            return "EmployeeInputPage";
        }
        return "EmployeeUpdatePage";
    }

    // アクションメソッド：入力画面に戻る
    @GetMapping("/back")
    public String back() {
        return "EmployeeInputPage";
    }

    // アクションメソッド：人物を更新・追加
    @PostMapping("/update")
    public String update(@Validated EmployeeSession employeeSession,
            SessionStatus sessionStatus) {
        Employee employee = toEmployee(employeeSession);
        if (employee.getEmployeeId() == null) {
            employeeService.createEmployee(employee);
        } else {
            employeeService.updateEmployee(employee);
        }
        sessionStatus.setComplete();
        return "redirect:/viewList";
    }

    // アクションメソッド：人物を編集
    @PostMapping("/edit")
    public String edit(@RequestParam("employeeId") Integer employeeId,
            EmployeeSession employeeSession) {
        Employee employee = employeeService.getEmployee(employeeId);
        employeeSession.setEmployeeId(employee.getEmployeeId());
        employeeSession.setEmployeeName(employee.getEmployeeName());
        employeeSession.setDepartmentName(employee.getDepartmentName());
        employeeSession.setSalary(employee.getSalary());
        return "EmployeeInputPage";
    }

    // アクションメソッド：人物の削除
    @PostMapping("/remove")
    public String remove(@RequestParam("employeeId") Integer employeeId) {
        employeeService.removeEmployee(employeeId);
        return "redirect:/viewList";
    }

    // アクションメソッド：人物リストの表示
    @GetMapping("/viewList")
    public String viewList(Model model) {
        List<Employee> employees = employeeService.getEmployeesAll();
        model.addAttribute("employeeList", employees);
        return "EmployeeTablePage";
    }
    // viewによるクエリの実行
    @GetMapping("/employees/by-query")
    public String viewByQuery(
            @RequestParam("employeeId") Integer employeeId, Model model) {
        return showEmployee(employeeId, "クエリパラメータ", model);
    }
    // viewによるpathの実行
    @GetMapping("/employees/{employeeId}")
    public String viewByPath(
            @PathVariable("employeeId") Integer employeeId, Model model) {
        return showEmployee(employeeId, "パス変数", model);
    }

    // 社員の表示
    private String showEmployee(Integer employeeId, String parameterType, Model model) {
        model.addAttribute("employee", employeeService.getEmployee(employeeId));
        model.addAttribute("parameterType", parameterType);
        return "EmployeeDetailPage";
    }

    // 転送オブジェクトをエンティティに詰め替える
    private Employee toEmployee(EmployeeSession session) {
        return new Employee(session.getEmployeeId(), session.getEmployeeName(),
                session.getDepartmentName(), session.getSalary());
    }
}
