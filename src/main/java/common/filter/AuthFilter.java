package common.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

public class AuthFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;
        
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        
        // 获取相对路径
        String path = uri;
        if (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)) {
            path = uri.substring(contextPath.length());
        }
        
        // ========== 必须放行的资源（简单粗暴，全部放行）==========
        
        // 1. 所有 HTML 页面
        if (path.endsWith(".html")) {
            chain.doFilter(req, resp);
            return;
        }
        
        // 2. 所有 CSS 文件
        if (path.endsWith(".css")) {
            chain.doFilter(req, resp);
            return;
        }
        
        // 3. 所有 JS 文件
        if (path.endsWith(".js")) {
            chain.doFilter(req, resp);
            return;
        }
        
        // 4. 所有图片文件
        if (path.endsWith(".png") || path.endsWith(".jpg") || path.endsWith(".jpeg") || 
            path.endsWith(".gif") || path.endsWith(".svg") || path.endsWith(".ico") ||
            path.endsWith(".webp") || path.endsWith(".bmp")) {
            chain.doFilter(req, resp);
            return;
        }
        
        // 5. 所有字体文件
        if (path.endsWith(".woff") || path.endsWith(".woff2") || path.endsWith(".ttf") || 
            path.endsWith(".eot") || path.endsWith(".otf")) {
            chain.doFilter(req, resp);
            return;
        }
        
        // 6. 登录注册相关接口
        if (path.startsWith("/login") || path.startsWith("/register")) {
            chain.doFilter(req, resp);
            return;
        }
        
        // 7. 根路径
        if (path.equals("/") || path.isEmpty()) {
            chain.doFilter(req, resp);
            return;
        }
        
        // ========== 其他所有请求需要登录验证 ==========
        
        HttpSession session = request.getSession(false);
        
        if (session == null || (session.getAttribute("user") == null && session.getAttribute("staff") == null)) {
            response.sendRedirect(request.getContextPath() + "/pages/login.html");
            return;
        }
        
        chain.doFilter(req, resp);
    }

    @Override
    public void init(FilterConfig filterConfig) {}
    
    @Override
    public void destroy() {}
}
