package pro.kensait.jakartaee.tx;

import java.io.IOException;

import jakarta.annotation.sql.DataSourceDefinition;
import jakarta.annotation.sql.DataSourceDefinitions;
import jakarta.inject.Inject;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// 演習用: HSQLDB の JDBCXADataSource は、user・password を指定すると Payara からの接続を拒む
// （getXAConnection(user, password) がデータソース自身の user・password と照合する）ため、接続情報は URL に書いている
@DataSourceDefinitions({
    @DataSourceDefinition(
            name = "java:app/jdbc/TxDS1",
            className = "org.hsqldb.jdbc.pool.JDBCXADataSource",
            url = "jdbc:hsqldb:mem:testdb;hsqldb.tx=mvcc;user=SA;password="),
    @DataSourceDefinition(
            name = "java:app/jdbc/TxDS2",
            className = "org.hsqldb.jdbc.pool.JDBCXADataSource",
            url = "jdbc:hsqldb:mem:testdb;hsqldb.tx=mvcc;user=SA;password=")
})
@WebServlet("/jta")
public class JtaServlet extends HttpServlet {
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
