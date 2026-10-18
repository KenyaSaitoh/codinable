package pro.kensait.jakartaee.tx;

import java.io.IOException;

import jakarta.inject.Inject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/fuga")
public class FugaServlet extends HttpServlet {
    @Inject
    private FooBean fooBean;

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws IOException {
        int param = Integer.parseInt(request.getParameter("param"));
        String result;
        try {
            fooBean.doBusiness(param);
            result = "NORMAL END";
        } catch (RuntimeException e) {
            result = "ERROR OCCURRED!!!";
        }
        response.setContentType("text/plain; charset=UTF-8");
        response.getWriter().print(result);
    }
}
