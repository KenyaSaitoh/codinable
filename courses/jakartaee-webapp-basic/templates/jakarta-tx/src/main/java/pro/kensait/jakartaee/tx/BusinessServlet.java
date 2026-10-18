package pro.kensait.jakartaee.tx;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import jakarta.annotation.Resource;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** 演習用: BUSINESS テーブルの今の値を表示する（どの更新が確定したかを確かめる） */
@WebServlet("/business")
public class BusinessServlet extends HttpServlet {
    @Resource(lookup = "java:app/jdbc/TxDS")
    private DataSource ds;

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/plain; charset=UTF-8");
        PrintWriter out = response.getWriter();
        try (Connection conn = ds.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(
                        "SELECT NAME, COUNT FROM BUSINESS ORDER BY NAME");
                ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                out.println(rs.getString("NAME") + " COUNT=" + rs.getInt("COUNT"));
            }
        } catch (SQLException e) {
            throw new ServletException(e);
        }
    }
}
