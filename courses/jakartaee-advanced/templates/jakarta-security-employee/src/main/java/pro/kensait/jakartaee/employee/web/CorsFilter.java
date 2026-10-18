package pro.kensait.jakartaee.employee.web;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.Provider;
import java.util.Set;

/*
 * React の画面（5173）から 8080 へ直接要求する構成に備えた CORS の応答ヘッダー
 * この演習の画面は Vite の転送で同じオリジンから呼ぶので、ふだんは働かない
 */
@Provider
public class CorsFilter implements ContainerResponseFilter {
    private static final Set<String> ALLOWED_ORIGINS = Set.of("http://localhost:5173");

    @Override
    public void filter(ContainerRequestContext request, ContainerResponseContext response) {
        String origin = request.getHeaderString("Origin");
        if (origin == null || !ALLOWED_ORIGINS.contains(origin)) {
            return; // 【1】
        }
        MultivaluedMap<String, Object> headers = response.getHeaders();
        headers.putSingle("Access-Control-Allow-Origin", origin); // 【2】
        headers.putSingle("Access-Control-Allow-Credentials", "true"); // 【3】
        headers.putSingle("Access-Control-Allow-Headers", "Content-Type, " + CsrfFilter.HEADER_NAME);
        headers.putSingle("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE");
        headers.add("Vary", "Origin"); // 【4】
    }
}
