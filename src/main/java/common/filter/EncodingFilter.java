package common.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

public class EncodingFilter implements Filter {
    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        String uri = request.getRequestURI();

        if (uri.endsWith(".html") || uri.endsWith("/")) {
            resp.setContentType("text/html;charset=utf-8");
        } else if (uri.contains("/admin/") || uri.contains("/coach/") || uri.contains("/student/") || uri.contains("/login") || uri.contains("/register")) {
            resp.setContentType("application/json;charset=UTF-8");
        }

        req.setCharacterEncoding("utf-8");
        chain.doFilter(req, resp);
    }

    @Override
    public void init(FilterConfig filterConfig) {}
    @Override
    public void destroy() {}
}
