package common.filter;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class AuthFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;
        
        HttpSession session = request.getSession(false);
        
        // 获取请求路径
        String uri = request.getRequestURI();
        
        // 登录页面和登录接口不需要验证
        if (uri.contains("login.html") || uri.contains("/login")) {
            chain.doFilter(req, resp);
            return;
        }
        
        // 检查是否已登录
        if (session == null || (session.getAttribute("user") == null && session.getAttribute("staff") == null)) {
            response.sendRedirect(request.getContextPath() + "/pages/login.html");
            return;
        }
        
        // 已登录，放行
        chain.doFilter(req, resp);
    }

    @Override
    public void init(FilterConfig filterConfig) {}
    
    @Override
    public void destroy() {}
}