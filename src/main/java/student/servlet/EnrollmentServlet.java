package student.servlet;

import com.alibaba.fastjson.JSONObject;
import common.database.StaffDAO;
import common.entity.Enrollment;
import common.entity.Staff;
import common.entity.User;
import common.util.Result;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import student.exception.StudentException;
import student.service.StudentService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@WebServlet("/student/enrollment")
public class EnrollmentServlet extends BaseStudentServlet {
    private final StudentService studentService = new StudentService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User loginUser = getLoginUser(request, response);
        if (loginUser == null) return;
        Result result;
        try {
            String type = request.getParameter("type");
            if ("coach".equals(type)) {
                List<Staff> coachList = studentService.listAllCoach();
                result = Result.success(coachList);
            } else if ("myEnroll".equals(type)) {
                Enrollment enroll = studentService.getMyEnroll(loginUser.getId());
                result = Result.success(enroll);
            } else if ("myEnroll".equals(type)) {
            Enrollment enroll = studentService.getMyEnroll(loginUser.getId());
            JSONObject data = new JSONObject();
            if (enroll != null) {
                // 将 Enrollment 转为 JSONObject
                String jsonStr = JSONObject.toJSONString(enroll);
                data = JSONObject.parseObject(jsonStr);
                // 查询教练姓名
                Staff coach = new StaffDAO().findById(enroll.getCoachId());
                String coachName = (coach != null && coach.getName() != null) ? coach.getName() : enroll.getCoachId();
                data.put("coachName", coachName);
            }
            result = Result.success(data);
        }
            else {
                result = Result.error("type参数非法");
            }
        } catch (StudentException e) {
            result = Result.error(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            result = Result.error("查询报名信息异常");
        }
        writeJson(response, result);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User loginUser = getLoginUser(request, response);
        if (loginUser == null) return;
        Result result;
        try {
            Map<String, String> paramMap = parseJsonParam(request.getReader());
            String coachId = paramMap.get("coachId");
            String subjectType = paramMap.get("subjectType");
            int rows = studentService.applyEnroll(loginUser.getId(), coachId, subjectType);
            if (rows > 0) {
                result = Result.success("报考申请提交成功，等待教练审核");
            } else {
                result = Result.error("提交失败");
            }
        } catch (StudentException e) {
            result = Result.error(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            result = Result.error("提交报考异常");
        }
        writeJson(response, result);
    }
}