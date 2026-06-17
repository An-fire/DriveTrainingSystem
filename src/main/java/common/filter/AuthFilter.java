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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AuthFilter implements Filter {

    // 不需要登录即可访问的页面（白名单）
    private static final Set<String> PUBLIC_PAGES = new HashSet<>(Arrays.asList(
            "/pages/login.html",
            "/pages/register.html",
            "/pages/register-student.html",
            "/pages/register-staff.html",
            "/pages/home.html"
    ));

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

        // ========== 1. 静态资源全部放行（CSS/JS/图片/字体） ==========
        if (path.endsWith(".css") || path.endsWith(".js") ||
                path.endsWith(".png") || path.endsWith(".jpg") || path.endsWith(".jpeg") ||
                path.endsWith(".gif") || path.endsWith(".svg") || path.endsWith(".ico") ||
                path.endsWith(".webp") || path.endsWith(".bmp") ||
                path.endsWith(".woff") || path.endsWith(".woff2") || path.endsWith(".ttf") ||
                path.endsWith(".eot") || path.endsWith(".otf")) {
            chain.doFilter(req, resp);
            return;
        }

        // ========== 2. 登录注册相关接口放行 ==========
        if (path.startsWith("/login") || path.startsWith("/register") || path.startsWith("/register-staff")) {
            chain.doFilter(req, resp);
            return;
        }

        // ========== 3. 根路径放行 ==========
        if (path.equals("/") || path.isEmpty()) {
            chain.doFilter(req, resp);
            return;
        }

        // ========== 4. HTML 页面：只放行白名单中的页面 ==========
        if (path.endsWith(".html")) {
            if (PUBLIC_PAGES.contains(path)) {
                chain.doFilter(req, resp);
                return;
            }
            // 其他 HTML 页面需要登录验证
            HttpSession session = request.getSession(false);
            if (session == null || (session.getAttribute("user") == null && session.getAttribute("staff") == null)) {
                response.sendRedirect(request.getContextPath() + "/pages/login.html");
                return;
            }
            chain.doFilter(req, resp);
            return;
        }

        // ========== 5. 其他所有请求（API接口等）需要登录验证 ==========
        HttpSession session = request.getSession(false);
        if (session == null || (session.getAttribute("user") == null && session.getAttribute("staff") == null)) {
            // 判断是否为 AJAX 请求
            String ajaxHeader = request.getHeader("X-Requested-With");
            if ("XMLHttpRequest".equals(ajaxHeader)) {
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":0,\"msg\":\"请先登录\"}");
                return;
            }
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