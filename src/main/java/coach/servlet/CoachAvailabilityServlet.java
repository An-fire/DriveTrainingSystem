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

            // 查询已占用的时段
            List<Booking> occupied = bookingDAO.findByCoachIdAndDate(coachId, targetUtilDate);
            System.out.println("[CoachAvailabilityServlet] 占用时段数量: " + (occupied != null ? occupied.size() : 0));

            // ===== 遍历每个排班时段，计算空闲和占用 =====
            for (CoachSchedule schedule : schedules) {
                // ===== 关键修复：跳过无效排班（开始时间 >= 结束时间） =====
                if (schedule.getStartTime().getTime() >= schedule.getEndTime().getTime()) {
                    System.out.println("[CoachAvailabilityServlet] 跳过无效排班: " + schedule.getStartTime() + " - " + schedule.getEndTime());
                    continue;
                }

                java.sql.Time sqlStartTime = schedule.getStartTime();
                java.sql.Time sqlEndTime = schedule.getEndTime();

                Calendar scheduleCal = Calendar.getInstance();
                scheduleCal.setTime(targetUtilDate);

                // 设置排班开始时间
                scheduleCal.set(Calendar.HOUR_OF_DAY, sqlStartTime.getHours());
                scheduleCal.set(Calendar.MINUTE, sqlStartTime.getMinutes());
                scheduleCal.set(Calendar.SECOND, 0);
                scheduleCal.set(Calendar.MILLISECOND, 0);
                Date scheduleStart = scheduleCal.getTime();

                // 设置排班结束时间
                scheduleCal.set(Calendar.HOUR_OF_DAY, sqlEndTime.getHours());
                scheduleCal.set(Calendar.MINUTE, sqlEndTime.getMinutes());
                scheduleCal.set(Calendar.SECOND, 0);
                scheduleCal.set(Calendar.MILLISECOND, 0);
                Date scheduleEnd = scheduleCal.getTime();

                // 再次检查（防止时区问题）
                if (scheduleStart.getTime() >= scheduleEnd.getTime()) {
                    continue;
                }

                Date currentStart = scheduleStart;

                // 检查该排班时段内的占用
                for (Booking book : occupied) {
                    Date bookStart = book.getStartTime();
                    Date bookEnd = book.getEndTime();
                    if (bookStart == null || bookEnd == null) continue;

                    // 预约与排班有重叠
                    if (bookStart.before(scheduleEnd) && bookEnd.after(scheduleStart)) {
                        // ===== 空闲段 [currentStart, bookStart] =====
                        // 只有当前时间 < 预约开始时间 且 时间差 >= 1分钟 才添加
                        if (bookStart.after(currentStart) && bookStart.before(scheduleEnd)) {
                            long diff = bookStart.getTime() - currentStart.getTime();
                            if (diff >= 60000) {
                                JSONObject slot = new JSONObject();
                                slot.put("start", dateStrFinal + " " + sdfTime.format(currentStart));
                                slot.put("end", dateStrFinal + " " + sdfTime.format(bookStart));
                                freeSlots.add(slot);
                            }
                        }

                        // ===== 占用段 =====
                        Date occupyStart = bookStart.after(scheduleStart) ? bookStart : scheduleStart;
                        Date occupyEnd = bookEnd.before(scheduleEnd) ? bookEnd : scheduleEnd;
                        if (occupyStart.getTime() < occupyEnd.getTime()) {
                            JSONObject occupiedSlot = new JSONObject();
                            occupiedSlot.put("start", dateStrFinal + " " + sdfTime.format(occupyStart));
                            occupiedSlot.put("end", dateStrFinal + " " + sdfTime.format(occupyEnd));
                            occupiedSlots.add(occupiedSlot);
                        }

                        // 更新当前区间起点
                        if (bookEnd.after(currentStart)) {
                            currentStart = bookEnd;
                        }
                    }
                }

                // ===== 剩余空闲段 [currentStart, scheduleEnd] =====
                if (currentStart.before(scheduleEnd)) {
                    long diff = scheduleEnd.getTime() - currentStart.getTime();
                    if (diff >= 60000) {
                        JSONObject slot = new JSONObject();
                        slot.put("start", dateStrFinal + " " + sdfTime.format(currentStart));
                        slot.put("end", dateStrFinal + " " + sdfTime.format(scheduleEnd));
                        freeSlots.add(slot);
                    }
                }
            }

            System.out.println("[CoachAvailabilityServlet] 空闲时段: " + freeSlots.size() + ", 占用时段: " + occupiedSlots.size());
            System.out.println("[CoachAvailabilityServlet] 空闲: " + freeSlots.toJSONString());
            System.out.println("[CoachAvailabilityServlet] 占用: " + occupiedSlots.toJSONString());

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