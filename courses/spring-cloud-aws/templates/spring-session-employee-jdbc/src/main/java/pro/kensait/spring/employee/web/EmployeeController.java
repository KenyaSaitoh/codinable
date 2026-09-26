package pro.kensait.spring.employee.web;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.bind.support.SessionStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import pro.kensait.spring.employee.entity.Employee;
import pro.kensait.spring.employee.entity.EmployeeSearchCriteria;
import pro.kensait.spring.employee.service.ConflictException;
import pro.kensait.spring.employee.service.EmployeeService;
import pro.kensait.spring.employee.service.EmployeePage;
import pro.kensait.spring.employee.service.NotFoundException;
import pro.kensait.spring.employee.service.RangeException;
/** 社員管理画面のコントローラー */
@Controller
@SessionAttributes("employeeDraft")
public class EmployeeController {
    private final EmployeeService employeeService;

    // 社員の初期化
    @SuppressFBWarnings(value = "EI_EXPOSE_REP2",
            justification = "Spring manages the injected collaborator")
    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }
    /** 一覧画面の1行 */
    public record EmployeeRow(Integer employeeId, String employeeName, String departmentName,
            String jobName, String salaryText, String entranceDateText) {
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
        EmployeeParam input = new EmployeeParam();
        model.addAttribute("employeeDraft", new EmployeeDraft(input));
        return form(input, List.of(), model);
    }

    // 編集画面の表示
    @GetMapping("/employees/{employeeId}/edit")
    public String editForm(@PathVariable("employeeId") Integer employeeId, Model model) {
        try {
            EmployeeParam input = EmployeeParam.of(employeeService.get(employeeId));
            model.addAttribute("employeeDraft", new EmployeeDraft(input));
            return form(input, List.of(), model);
        } catch (NotFoundException exception) {
            return list(EmployeeSearchCriteria.empty(), 1, model,
                    List.of(exception.getMessage()));
        }
    }

    // ID・バージョン・確認済み状態はブラウザーから変更させない。
    @InitBinder("employeeParam")
    public void bindInput(WebDataBinder binder) {
        binder.setAllowedFields("employeeName", "departmentId", "jobId", "salary", "entranceDate");
    }

    @PostMapping("/employees/confirm")
    public String confirm(
            @SessionAttribute(value = "employeeDraft", required = false) EmployeeDraft draft,
            @ModelAttribute("employeeParam") EmployeeParam input, BindingResult binding,
            @RequestParam("draftToken") String token, Model model) {
        if (draft == null) {
            return "redirect:/employees/new";
        }
        if (!draft.getToken().equals(token)) {
            return "redirect:/employees/draft";
        }
        draft.setConfirmed(false);
        input.setEmployeeId(draft.getInput().getEmployeeId());
        input.setVersion(draft.getInput().getVersion());
        draft.setInput(input);
        List<String> errors = new ArrayList<>(input.validate(
                employeeService.departmentExists(input.getDepartmentId()),
                employeeService.jobExists(input.getJobId())));
        if (binding.hasErrors()) {
            errors.add("部署と役職を選択し直してください");
        }
        if (!errors.isEmpty()) {
            return form(input, errors, model);
        }
        draft.setConfirmed(true);
        return confirmation(draft, model);
    }

    @GetMapping("/employees/confirm")
    public String showConfirmation(
            @SessionAttribute(value = "employeeDraft", required = false) EmployeeDraft draft,
            Model model) {
        if (draft == null) {
            return "redirect:/employees/new";
        }
        return draft.isConfirmed() ? confirmation(draft, model)
                : "redirect:/employees/draft";
    }

    @GetMapping("/employees/draft")
    public String resume(
            @SessionAttribute(value = "employeeDraft", required = false) EmployeeDraft draft,
            Model model) {
        if (draft == null) {
            return "redirect:/employees/new";
        }
        draft.setConfirmed(false);
        return form(draft.getInput(), List.of(), model);
    }

    @PostMapping("/employees/save")
    public String save(
            @SessionAttribute(value = "employeeDraft", required = false) EmployeeDraft draft,
            @RequestParam("draftToken") String token, SessionStatus status, Model model) {
        if (draft == null) {
            return "redirect:/employees/new";
        }
        if (!draft.getToken().equals(token) || !draft.isConfirmed()) {
            return "redirect:/employees/draft";
        }
        EmployeeParam input = draft.getInput();
        try {
            if (input.getEmployeeId() == null) {
                employeeService.create(input.toEmployee());
            } else {
                employeeService.update(input.getEmployeeId(), input.toEmployee(), input.getVersion());
            }
        } catch (ConflictException | NotFoundException exception) {
            draft.setConfirmed(false);
            return form(input, List.of(exception.getMessage()), model);
        }
        status.setComplete();
        return "redirect:/employees";
    }

    @PostMapping("/employees/cancel")
    public String cancel(SessionStatus status) {
        status.setComplete();
        return "redirect:/employees";
    }

    private String confirmation(EmployeeDraft draft, Model model) {
        EmployeeParam input = draft.getInput();
        model.addAttribute("employeeParam", input);
        model.addAttribute("departmentName", employeeService.departmentNameOf(input.getDepartmentId()));
        model.addAttribute("jobName", employeeService.jobNameOf(input.getJobId()));
        return "EmployeeConfirmPage";
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
        model.addAttribute("employees", found.content().stream().map(this::row).toList());
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

    // 行の実行
    private EmployeeRow row(Employee employee) {
        return new EmployeeRow(employee.getEmployeeId(), employee.getEmployeeName(),
                employeeService.departmentNameOf(employee.getDepartmentId()),
                employeeService.jobNameOf(employee.getJobId()),
                String.format("%,d円", employee.getSalary()),
                String.valueOf(employee.getEntranceDate()));
    }
}
