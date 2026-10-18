package pro.kensait.jakartaee.tx;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** 演習用: /baz?params=10,-3,20 のように、BazBean に複数の値を 1 件ずつ処理させる */
@WebServlet("/baz")
public class BazServlet extends HttpServlet {
    @Inject
    private BazBean bazBean;

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        List<Integer> params = Arrays.stream(request.getParameter("params").split(","))
                .map(String::strip).map(Integer::valueOf).toList();
        int committed;
        try {
            committed = bazBean.doEach(params);
        } catch (Exception e) {
            throw new ServletException(e);
        }
        response.setContentType("text/plain; charset=UTF-8");
        response.getWriter().print("COMMITTED " + committed + " / " + params.size());
    }
}
