package pro.kensait.jakartaee.calc.web;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet(urlPatterns = {"/add", "/addByGet"})
public class CalcServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        calculate(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        calculate(request, response);
    }

    private void calculate(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String param1 = request.getParameter("param1");
        String param2 = request.getParameter("param2");
        if (param1 == null || param2 == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        // 計算結果はローカル変数に置く（インスタンス変数は全リクエストで共有される）
        double result = Double.parseDouble(param1) + Double.parseDouble(param2);

        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println("<!DOCTYPE html><html><body>");
        out.println("<h2>CalcOutputPage</h2><hr />");
        out.println("<div>計算結果 ---> " + result + "</div>");
        out.println("</body></html>");
    }
}
