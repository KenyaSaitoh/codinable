package pro.kensait.jakartaee.employee.web;

import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.security.enterprise.SecurityContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import pro.kensait.jakartaee.employee.service.Employee;
import pro.kensait.jakartaee.employee.service.EmployeeService;

/*
 * 社員一覧（employees/list.xhtml）のバッキングBean
 */
@Named
@RequestScoped
public class EmployeeListBean {
    @Inject
    private EmployeeService service;
    @Inject
    private SecurityContext securityContext;
    @Inject
    private FacesContext facesContext;

    public List<Employee> getEmployees() {
        return service.findAll();
    }

    public String getCaller() {
        return securityContext.getCallerPrincipal().getName();
    }

    public String logout() throws ServletException {
        ExternalContext external = facesContext.getExternalContext();
        ((HttpServletRequest) external.getRequest()).logout();
        external.invalidateSession();
        return "/login.xhtml?faces-redirect=true";
    }
}
