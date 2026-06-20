package coach.servlet;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import common.database.CoachScheduleDAO;
import common.entity.CoachSchedule;
import common.entity.Staff;
import common.util.UUIDUtil;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.Time;
import java.util.List;

@WebServlet("/coach/schedule")
public class ScheduleServlet extends HttpServlet {
    private final CoachScheduleDAO dao = new CoachScheduleDAO();

    // 查询当前教练的排班列表
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        JSONObject result = new JSONObject();

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("staff") == null) {
            result.put("code", 0);
            result.put("msg", "未登录");
            resp.getWriter().write(result.toString());
            return;
        }
        Staff staff = (Staff) session.getAttribute("staff");
        if (!"coach".equals(staff.getRole())) {
            result.put("code", 0);
            result.put("msg", "权限不足");
            resp.getWriter().write(result.toString());
            return;
        }

        String coachId = req.getParameter("coachId");
        if (coachId == null || coachId.isEmpty()) {
            coachId = staff.getId();
        }
        // 只能查看自己的排班
        if (!coachId.equals(staff.getId())) {
            result.put("code", 0);
            result.put("msg", "只能查看自己的排班");
            resp.getWriter().write(result.toString());
            return;
        }

        List<CoachSchedule> list = dao.findByCoachId(coachId);
        JSONArray array = JSONArray.parseArray(JSONArray.toJSONString(list));
        result.put("code", 1);
        result.put("data", array);
        resp.getWriter().write(result.toString());
    }

    // 添加排班
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        JSONObject result = new JSONObject();

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("staff") == null) {
            result.put("code", 0);
            result.put("msg", "未登录");
            resp.getWriter().write(result.toString());
            return;
        }
        Staff staff = (Staff) session.getAttribute("staff");
        if (!"coach".equals(staff.getRole())) {
            result.put("code", 0);
            result.put("msg", "权限不足");
            resp.getWriter().write(result.toString());
            return;
        }

        String weekdayStr = req.getParameter("weekday");
        String startTime = req.getParameter("startTime");
        String endTime = req.getParameter("endTime");

        if (weekdayStr == null || startTime == null || endTime == null) {
            result.put("code", 0);
            result.put("msg", "参数不完整");
            resp.getWriter().write(result.toString());
            return;
        }

        int weekday;
        try {
            weekday = Integer.parseInt(weekdayStr);
            if (weekday < 1 || weekday > 7) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            result.put("code", 0);
            result.put("msg", "星期数需为1-7");
            resp.getWriter().write(result.toString());
            return;
        }

        Time start, end;
        try {
            start = Time.valueOf(startTime + ":00");
            end = Time.valueOf(endTime + ":00");
            if (start.after(end)) {
                result.put("code", 0);
                result.put("msg", "开始时间不能晚于结束时间");
                resp.getWriter().write(result.toString());
                return;
            }
        } catch (Exception e) {
            result.put("code", 0);
            result.put("msg", "时间格式错误，请使用 HH:mm");
            resp.getWriter().write(result.toString());
            return;
        }

        CoachSchedule schedule = new CoachSchedule();
        schedule.setId(UUIDUtil.getUUID());
        schedule.setCoachId(staff.getId());
        schedule.setWeekday(weekday);
        schedule.setStartTime(start);
        schedule.setEndTime(end);

        int rows = dao.insert(schedule);
        if (rows > 0) {
            result.put("code", 1);
            result.put("msg", "排班添加成功");
        } else {
            result.put("code", 0);
            result.put("msg", "添加失败");
        }
        resp.getWriter().write(result.toString());
    }

    // 删除排班
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        JSONObject result = new JSONObject();

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("staff") == null) {
            result.put("code", 0);
            result.put("msg", "未登录");
            resp.getWriter().write(result.toString());
            return;
        }
        Staff staff = (Staff) session.getAttribute("staff");
        if (!"coach".equals(staff.getRole())) {
            result.put("code", 0);
            result.put("msg", "权限不足");
            resp.getWriter().write(result.toString());
            return;
        }

        String id = req.getParameter("id");
        if (id == null || id.isEmpty()) {
            result.put("code", 0);
            result.put("msg", "缺少排班ID");
            resp.getWriter().write(result.toString());
            return;
        }

        int rows = dao.delete(id);
        if (rows > 0) {
            result.put("code", 1);
            result.put("msg", "删除成功");
        } else {
            result.put("code", 0);
            result.put("msg", "删除失败");
        }
        resp.getWriter().write(result.toString());
    }
}