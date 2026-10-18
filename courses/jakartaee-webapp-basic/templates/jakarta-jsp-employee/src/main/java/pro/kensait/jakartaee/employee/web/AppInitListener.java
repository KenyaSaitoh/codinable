package pro.kensait.jakartaee.employee.web;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import pro.kensait.jakartaee.employee.service.EmployeeDAO;
import pro.kensait.jakartaee.employee.service.EmployeeService;

@WebListener
public class AppInitListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent event) {
        EmployeeDAO employeeDao = new EmployeeDAO();
        EmployeeService employeeService = new EmployeeService(employeeDao);
        event.getServletContext().setAttribute("employeeService", employeeService);
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        event.getServletContext().removeAttribute("employeeService");
    }
}
