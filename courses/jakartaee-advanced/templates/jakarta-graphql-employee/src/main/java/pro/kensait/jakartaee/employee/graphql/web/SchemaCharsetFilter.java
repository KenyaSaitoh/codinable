package pro.kensait.jakartaee.employee.graphql.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/*
 * /graphql/schema.graphql の応答の文字コードを UTF-8 にする
 * （SmallRye GraphQL のスキーマの出力は文字コードを指定しないため、@Description の日本語が ? に化ける）
 */
@WebFilter("/graphql/schema.graphql")
public class SchemaCharsetFilter extends HttpFilter {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        response.setCharacterEncoding("UTF-8");
        chain.doFilter(request, response);
    }
}
