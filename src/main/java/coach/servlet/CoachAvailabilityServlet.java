package coach.servlet;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import common.database.BookingDAO;
import common.database.CoachScheduleDAO;
import common.entity.Booking;
import common.entity.CoachSchedule;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.*;

@WebServlet("/student/availability")
public class CoachAvailabilityServlet extends HttpServlet {

    private final BookingDAO bookingDAO = new BookingDAO();
    private final CoachScheduleDAO scheduleDAO = new CoachScheduleDAO();
    private final SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd");
    private final SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        JSONObject result = new JSONObject();

        String coachId = req.getParameter("coachId");
        String dateStr = req.getParameter("date");

        if (coachId == null || coachId.isEmpty()) {
            result.put("code", 0);
            result.put("msg", "教练ID不能为空");
            resp.getWriter().write(result.toString());
            return;
        }

        try {
            // 解析日期
            java.util.Date targetUtilDate;
            if (dateStr == null || dateStr.isEmpty()) {
                targetUtilDate = new java.util.Date();
            } else {
                targetUtilDate = sdfDate.parse(dateStr);
            }
            // 转换为 java.sql.Date（用于DAO查询）
            Date targetSqlDate = new Date(targetUtilDate.getTime());
            String dateStrFinal = sdfDate.format(targetUtilDate);

            // 获取星期几（1=周一, 7=周日）
            Calendar cal = Calendar.getInstance();
            cal.setTime(targetUtilDate);
            int weekday = cal.get(Calendar.DAY_OF_WEEK);
            int wd = (weekday == 1) ? 7 : weekday - 1;

            // 查询教练当天的排班
            List<CoachSchedule> schedules = scheduleDAO.findByCoachIdAndWeekday(coachId, wd);

            JSONArray freeSlots = new JSONArray();
            JSONArray occupiedSlots = new JSONArray();

            if (schedules == null || schedules.isEmpty()) {
                JSONObject data = new JSONObject();
                data.put("free", freeSlots);
                data.put("occupied", occupiedSlots);
                data.put("msg", "该教练当天无排班");
                result.put("code", 1);
                result.put("data", data);
                resp.getWriter().write(result.toString());
                return;
            }

            List<Booking> occupied = bookingDAO.findByCoachIdAndDate(coachId, targetUtilDate);
            if (occupied == null) {
                occupied = new ArrayList<>();
            }

            // 对每个排班时段，计算空闲和占用
            for (CoachSchedule schedule : schedules) {
                Date scheduleStart = new Date(schedule.getStartTime().getTime());
                Date scheduleEnd = new Date(schedule.getEndTime().getTime());

                Date currentStart = scheduleStart;

                // 检查是否有占用
                for (Booking book : occupied) {
                    java.util.Date bookStartUtil = book.getStartTime();
                    java.util.Date bookEndUtil = book.getEndTime();
                    if (bookStartUtil == null || bookEndUtil == null) continue;

                    Date bookStart = new Date(bookStartUtil.getTime());
                    Date bookEnd = new Date(bookEndUtil.getTime());

                    // 如果预约与排班有重叠
                    if (bookStart.before(scheduleEnd) && bookEnd.after(scheduleStart)) {
                        // 如果预约开始时间在当前区间之后，产生一个空闲段
                        if (bookStart.after(currentStart) && bookStart.before(scheduleEnd)) {
                            JSONObject slot = new JSONObject();
                            slot.put("start", dateStrFinal + " " + sdfTime.format(currentStart));
                            slot.put("end", dateStrFinal + " " + sdfTime.format(bookStart));
                            freeSlots.add(slot);
                        }
                        // 记录占用时段
                        JSONObject occupiedSlot = new JSONObject();
                        occupiedSlot.put("start", dateStrFinal + " " + sdfTime.format(bookStart));
                        occupiedSlot.put("end", dateStrFinal + " " + sdfTime.format(bookEnd));
                        occupiedSlots.add(occupiedSlot);
                        // 更新当前区间起点
                        if (bookEnd.after(currentStart)) {
                            currentStart = bookEnd;
                        }
                    }
                }
                // 剩余空闲时段
                if (currentStart.before(scheduleEnd)) {
                    JSONObject slot = new JSONObject();
                    slot.put("start", dateStrFinal + " " + sdfTime.format(currentStart));
                    slot.put("end", dateStrFinal + " " + sdfTime.format(scheduleEnd));
                    freeSlots.add(slot);
                }
            }

            JSONObject data = new JSONObject();
            data.put("free", freeSlots);
            data.put("occupied", occupiedSlots);
            result.put("code", 1);
            result.put("data", data);

        } catch (Exception e) {
            e.printStackTrace();
            result.put("code", 0);
            result.put("msg", "系统异常：" + e.getMessage());
        }

        resp.getWriter().write(result.toString());
    }
}