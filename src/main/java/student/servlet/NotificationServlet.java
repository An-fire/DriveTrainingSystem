package student.servlet;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import common.database.NotificationDAO;
import common.entity.Notification;
import common.entity.User;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

@WebServlet("/student/notification")
public class NotificationServlet extends BaseStudentServlet {
    private final NotificationDAO dao = new NotificationDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        JSONObject result = new JSONObject();
        User user = getLoginUser(req, resp);
        if (user == null) return;

        String action = req.getParameter("action");
        if ("unread".equals(action)) {
            List<Notification> list = dao.findByUserId(user.getId(), true);
            JSONArray array = JSONArray.parseArray(JSONArray.toJSONString(list));
            result.put("code", 1);
            result.put("data", array);
            result.put("unreadCount", dao.countUnread(user.getId()));
        } else {
            result.put("code", 0);
            result.put("msg", "无效操作");
        }
        resp.getWriter().write(result.toString());
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        JSONObject result = new JSONObject();
        User user = getLoginUser(req, resp);
        if (user == null) return;

        String id = req.getParameter("id");
        if (id != null && !id.isEmpty()) {
            int rows = dao.markAsRead(id);
            result.put("code", rows > 0 ? 1 : 0);
            result.put("msg", rows > 0 ? "已标记已读" : "操作失败");
        } else {
            result.put("code", 0);
            result.put("msg", "缺少通知ID");
        }
        resp.getWriter().write(result.toString());
    }
}