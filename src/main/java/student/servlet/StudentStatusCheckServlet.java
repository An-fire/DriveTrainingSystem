package student.servlet;

import com.alibaba.fastjson.JSONObject;
import common.database.EnrollmentDAO;
import common.database.UserDAO;
import common.entity.Enrollment;
import common.entity.User;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/student/checkStatus")
public class StudentStatusCheckServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();
    private final EnrollmentDAO enrollmentDAO = new EnrollmentDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        JSONObject result = new JSONObject();

        String name = req.getParameter("name");
        String idCard = req.getParameter("idCard");

        if (name == null || name.isEmpty() || idCard == null || idCard.isEmpty()) {
            result.put("code", 0);
            result.put("msg", "姓名和身份证号不能为空");
            resp.getWriter().write(result.toString());
            return;
        }

        try {
            // 1. 根据身份证号查询用户（身份证唯一）
            User user = userDAO.findByIdCard(idCard);
            if (user == null) {
                // 未注册
                result.put("code", 1);
                result.put("data", new JSONObject() {{
                    put("registered", false);
                    put("enrolled", false);
                    put("enrollStatus", null);
                    put("message", "该身份证号尚未注册，可以注册");
                }});
                resp.getWriter().write(result.toString());
                return;
            }

            // 已注册，检查是否姓名匹配（防止张冠李戴）
            if (!user.getName().equals(name)) {
                result.put("code", 0);
                result.put("msg", "姓名与身份证号不匹配，请检查");
                resp.getWriter().write(result.toString());
                return;
            }

            // 2. 查询报名记录
            Enrollment enroll = enrollmentDAO.findByStudentId(user.getId());
            JSONObject data = new JSONObject();
            data.put("registered", true);
            data.put("userId", user.getId());
            data.put("name", user.getName());
            data.put("phone", user.getPhone());
            data.put("subject", user.getSubject());
            data.put("createTime", user.getCreateTime());

            if (enroll == null) {
                data.put("enrolled", false);
                data.put("enrollStatus", null);
                data.put("message", "已注册但尚未报名，请进行报名");
            } else {
                data.put("enrolled", true);
                data.put("enrollStatus", enroll.getStatus());
                data.put("enrollId", enroll.getId());
                data.put("coachId", enroll.getCoachId());
                data.put("subjectType", enroll.getSubjectType());
                data.put("applyTime", enroll.getApplyTime());
                data.put("auditTime", enroll.getAuditTime());

                String statusMsg;
                switch (enroll.getStatus()) {
                    case "pending": statusMsg = "报名待审核，请耐心等待"; break;
                    case "approved": statusMsg = "报名已通过，可以进行练车预约"; break;
                    case "rejected": statusMsg = "报名被驳回，请重新提交报名"; break;
                    default: statusMsg = "未知状态";
                }
                data.put("message", statusMsg);
            }

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