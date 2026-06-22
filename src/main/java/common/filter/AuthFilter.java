package common.filter;

import common.entity.User;
import common.entity.Staff;
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

    // 不需要登录即可访问的 API 接口
    private static final Set<String> PUBLIC_API = new HashSet<>(Arrays.asList(
            "/login",
            "/register",
            "/register-staff",
            "/logout",
            "/coach/comments"
    ));

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;

        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();

        // 获取相对路径
        String path = uri;
        if (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)) {
            path = uri.substring(contextPath.length());
        }

        // ========== 1. 静态资源全部放行 ==========
        if (path.endsWith(".css") || path.endsWith(".js") ||
                path.endsWith(".png") || path.endsWith(".jpg") || path.endsWith(".jpeg") ||
                path.endsWith(".gif") || path.endsWith(".svg") || path.endsWith(".ico") ||
                path.endsWith(".webp") || path.endsWith(".bmp") ||
                path.endsWith(".woff") || path.endsWith(".woff2") || path.endsWith(".ttf") ||
                path.endsWith(".eot") || path.endsWith(".otf")) {
            chain.doFilter(req, resp);
            return;
        }

        // ========== 2. 公开 API 接口放行 ==========
        for (String api : PUBLIC_API) {
            if (path.startsWith(api)) {
                chain.doFilter(req, resp);
                return;
            }
        }

        // ========== 3. 根路径放行 ==========
        if (path.equals("/") || path.isEmpty()) {
            chain.doFilter(req, resp);
            return;
        }

        // ========== 4. HTML 页面处理 ==========
        if (path.endsWith(".html")) {
            // 公开页面直接放行
            if (PUBLIC_PAGES.contains(path)) {
                chain.doFilter(req, resp);
                return;
            }

            // 需要登录验证
            HttpSession session = request.getSession(false);
            if (session == null) {
                response.sendRedirect(request.getContextPath() + "/pages/login.html");
                return;
            }

            // 获取登录用户信息
            User user = (User) session.getAttribute("user");
            Staff staff = (Staff) session.getAttribute("staff");

            // ========== ✅ 新增：角色路径匹配 ==========
            if (path.equals("/pages/admin.html")) {
                // 只有管理员可以访问
                if (staff == null || !"admin".equals(staff.getRole())) {
                    response.sendRedirect(request.getContextPath() + "/pages/login.html");
                    return;
                }
            } else if (path.equals("/pages/coach.html")) {
                // 只有教练可以访问
                if (staff == null || !"coach".equals(staff.getRole())) {
                    response.sendRedirect(request.getContextPath() + "/pages/login.html");
                    return;
                }
            } else if (path.equals("/pages/student.html")) {
                // 只有学员可以访问
                if (user == null || !"student".equals(user.getRole())) {
                    response.sendRedirect(request.getContextPath() + "/pages/login.html");
                    return;
                }
            }

            chain.doFilter(req, resp);
            return;
        }

        // ========== 5. API 接口角色权限检查 ==========
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.sendRedirect(request.getContextPath() + "/pages/login.html");
            return;
        }

        User user = (User) session.getAttribute("user");
        Staff staff = (Staff) session.getAttribute("staff");

        // 检查 API 路径权限
        if (path.startsWith("/admin/") || path.startsWith("/admin")) {
            if (staff == null || !"admin".equals(staff.getRole())) {
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":0,\"msg\":\"权限不足，需要管理员权限\"}");
                return;
            }
        } else if (path.startsWith("/coach/")) {
            if (staff == null || !"coach".equals(staff.getRole())) {
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":0,\"msg\":\"权限不足，需要教练权限\"}");
                return;
            }
        } else if (path.startsWith("/student/")) {
            if (user == null || !"student".equals(user.getRole())) {
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\":0,\"msg\":\"权限不足，需要学员权限\"}");
                return;
            }
        }

        chain.doFilter(req, resp);
    }

    @Override
    public void init(FilterConfig filterConfig) {}

    @Override
    public void destroy() {}
}