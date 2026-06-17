package student.servlet;

import common.entity.Booking;
import common.entity.User;
import common.util.Result;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import student.exception.StudentException;
import student.service.StudentService;

import java.io.BufferedReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/student/booking")
public class BookingServlet extends HttpServlet {
    private final StudentService studentService = new StudentService();
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private Map<String, String> parseJsonParam(BufferedReader br) throws IOException {
        Map<String, String> map = new HashMap<>();
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        String json = sb.toString();
        json = json.replace("{", "").replace("}", "").replace("\"", "");
        String[] arr = json.split(",");
        for (String item : arr) {
            String[] kv = item.split(":");
            if (kv.length == 2) {
                map.put(kv[0].trim(), kv[1].trim());
            }
        }
        return map;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=utf-8");
        Result result;
        HttpSession session = request.getSession();
        User loginUser = (User) session.getAttribute("user");
        if (loginUser == null) {
            result = Result.error("请先登录");
            String jsonResp = "{\"success\":" + result.isSuccess() + ",\"msg\":\"" + result.getMsg() + "\",\"data\":null}";
            response.getWriter().write(jsonResp);
            return;
        }
        try {
            List<Booking> bookingList = studentService.listMyBooking(loginUser.getId());
            result = Result.success(bookingList);
        } catch (StudentException e) {
            result = Result.error(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            result = Result.error("查询预约异常");
        }
        String jsonResp = "{\"success\":" + result.isSuccess() + ",\"msg\":\"" + result.getMsg() + "\",\"data\":" + (result.getData() == null ? "null" : "\"" + result.getData() + "\"") + "}";
        response.getWriter().write(jsonResp);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json;charset=utf-8");
        Result result;
        HttpSession session = request.getSession();
        User loginUser = (User) session.getAttribute("user");
        if (loginUser == null) {
            result = Result.error("请先登录");
            String jsonResp = "{\"success\":" + result.isSuccess() + ",\"msg\":\"" + result.getMsg() + "\",\"data\":null}";
            response.getWriter().write(jsonResp);
            return;
        }
        try {
            Map<String, String> paramMap = parseJsonParam(request.getReader());
            String coachId = paramMap.get("coachId");
            String subjectType = paramMap.get("subjectType");
            String startStr = paramMap.get("startTime");
            String endStr = paramMap.get("endTime");

            Timestamp startTime = new Timestamp(sdf.parse(startStr).getTime());
            Timestamp endTime = new Timestamp(sdf.parse(endStr).getTime());
            int rows = studentService.addBooking(loginUser.getId(), coachId, subjectType, startTime, endTime);
            if (rows > 0) {
                result = Result.success("预约成功");
            } else {
                result = Result.error("预约失败");
            }
        } catch (ParseException e) {
            result = Result.error("时间格式错误，请选择正确时间");
        } catch (StudentException e) {
            result = Result.error(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            result = Result.error("预约异常");
        }
        String jsonResp = "{\"success\":" + result.isSuccess() + ",\"msg\":\"" + result.getMsg() + "\",\"data\":" + (result.getData() == null ? "null" : "\"" + result.getData() + "\"") + "}";
        response.getWriter().write(jsonResp);
    }
}