package pro.kensait.jakartaee.employee.web;

import java.io.IOException;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import pro.kensait.jakartaee.employee.service.EmployeeService;

@WebServlet("/employees/delete")
public class EmployeeDeleteServlet extends HttpServlet {
    private EmployeeService employeeService;

    @Override
    public void init() {
        employeeService = (EmployeeService) getServletContext()
                .getAttribute("employeeService");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        int employeeId = Integer.parseInt(request.getParameter("employeeId"));
        employeeService.delete(employeeId);
        response.sendRedirect(request.getContextPath() + "/employees",
                HttpServletResponse.SC_SEE_OTHER);
    }
}
