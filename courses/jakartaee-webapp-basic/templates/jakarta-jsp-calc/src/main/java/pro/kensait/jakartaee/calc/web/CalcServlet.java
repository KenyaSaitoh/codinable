package pro.kensait.jakartaee.calc.web;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import pro.kensait.jakartaee.calc.service.CalcService;

@WebServlet(urlPatterns = {"/calc", "/add"})
public class CalcServlet extends HttpServlet {
    private final CalcService calcService = new CalcService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.getRequestDispatcher("/WEB-INF/views/calcInput.jsp")
                .forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        double param1 = Double.parseDouble(request.getParameter("param1"));
        double param2 = Double.parseDouble(request.getParameter("param2"));
        double result = calcService.add(param1, param2);
        request.setAttribute("result", result);
        request.getSession().setAttribute("lastResult", result);
        request.getRequestDispatcher("/WEB-INF/views/calcOutput.jsp")
                .forward(request, response);
    }
}
