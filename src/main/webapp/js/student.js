/**
 * 通用弹窗提示
 */
function showMsg(text, isError = false) {
    let tip = document.createElement("div");
    tip.style.position = "fixed";
    tip.top = "30px";
    tip.left = "50%";
    tip.transform = "translateX(-50%)";
    tip.padding = "8px 20px";
    tip.borderRadius = "6px";
    tip.color = "#fff";
    tip.background = isError ? "#f53f3f" : "#67c23a";
    tip.zIndex = "9999";
    tip.innerText = text;
    document.body.appendChild(tip);
    setTimeout(() => {
        document.body.removeChild(tip);
    }, 2500);
}

/**
 * 通用网络请求
 */
async function request(url, method = "GET", data = null) {
    const options = {
        method: method,
        headers: {
            "Content-Type": "application/json"
        }
    };
    if (method === "POST" && data) {
        options.body = JSON.stringify(data);
    }
    // ✅ 使用 window.BASE_URL
    let res = await fetch(window.BASE_URL + url, options);
    return await res.json();
}

// ===================== 登录逻辑 =====================
async function login() {
    let phone = document.getElementById("phone").value.trim();
    let pwd = document.getElementById("password").value.trim();
    let type = document.querySelector("input[name='role']:checked").value;

    if (!phone || !pwd) {
        showMsg("手机号和密码不能为空", true);
        return;
    }
    if (!/^\d{11}$/.test(phone)) {
        showMsg("手机号必须是11位数字", true);
        return;
    }

    let res = await request("/login", "POST", {
        phone: phone,
        password: pwd,
        type: type
    });
    if (res.code === 1) {
        showMsg("登录成功");
        setTimeout(() => {
            location.href = res.url;
        }, 1000);
    } else {
        showMsg(res.msg, true);
    }
}

// ===================== 学员注册逻辑 =====================
async function register() {
    let name = document.getElementById("regName").value.trim();
    let idCard = document.getElementById("regIdCard").value.trim();
    let phone = document.getElementById("regPhone").value.trim();
    let pwd = document.getElementById("regPwd").value.trim();
    let subject = document.getElementById("regSubject").value.trim();

    if (!name || !idCard || !phone || !pwd || !subject) {
        showMsg("所有信息不能为空", true);
        return;
    }
    if (idCard.length !== 18) {
        showMsg("身份证必须为18位", true);
        return;
    }
    if (!/^\d{11}$/.test(phone)) {
        showMsg("手机号格式错误", true);
        return;
    }
    if (pwd.length < 6) {
        showMsg("密码长度不能少于6位", true);
        return;
    }

    let res = await request("/student/register", "POST", {
        name: name,
        idCard: idCard,
        phone: phone,
        password: pwd,
        subject: subject
    });
    if (res.success) {
        showMsg("注册成功，请前往登录");
        setTimeout(() => {
            location.href = "login.html";
        }, 1000);
    } else {
        showMsg(res.msg, true);
    }
}

// ===================== 加载教练列表 =====================
async function loadCoach() {
    let res = await request("/student/enrollment?type=coach");
    console.log("教练接口返回数据：", res);

    const enrollCoach = document.getElementById("enrollCoach");
    const bookCoach = document.getElementById("bookCoach");
    enrollCoach.innerHTML = "";
    bookCoach.innerHTML = "";

    if (!res.success || !res.data || res.data.length === 0) {
        enrollCoach.innerHTML = "<option>暂无教练数据</option>";
        bookCoach.innerHTML = "<option>暂无教练数据</option>";
        return;
    }

    res.data.forEach(item => {
        let opt1 = new Option(item.name + " | " + item.subject, item.id);
        let opt2 = new Option(item.name + " | " + item.subject, item.id);
        enrollCoach.appendChild(opt1);
        bookCoach.appendChild(opt2);
    });
}

// ===================== 提交驾校报名 =====================
async function submitEnroll() {
    let coachId = document.getElementById("enrollCoach").value;
    let subjectType = document.getElementById("enrollSubject").value;

    let res = await request("/student/enrollment", "POST", {
        coachId: coachId,
        subjectType: subjectType
    });
    if (res.success) {
        showMsg("报名提交成功，等待审核");
        loadMyEnrollInfo();
    } else {
        showMsg(res.msg, true);
    }
}

// ===================== 加载个人报名信息 =====================
async function loadMyEnrollInfo() {
    let res = await request("/student/enrollment?type=myEnroll");
    let infoBox = document.getElementById("enrollInfoBox");
    if (!res.data) {
        infoBox.innerHTML = "暂无报名记录";
        return;
    }
    let data = res.data;
    let statusText = "";
    switch (data.status) {
        case "pending": statusText = "待审核"; break;
        case "approved": statusText = "审核通过"; break;
        case "rejected": statusText = "审核驳回"; break;
        default: statusText = data.status;
    }
    infoBox.innerHTML = `
        报名ID：${data.id}<br>
        教练ID：${data.coachId}<br>
        报考科目：${data.subjectType}<br>
        当前状态：${statusText}<br>
        申请时间：${data.applyTime}
    `;
}

// ===================== 提交练车预约 =====================
async function submitBook() {
    let coachId = document.getElementById("bookCoach").value;
    let subjectType = document.getElementById("bookSubject").value;
    let startTime = document.getElementById("startTime").value;
    let endTime = document.getElementById("endTime").value;

    if (!coachId || !subjectType) {
        showMsg("请选择教练和科目", true);
        return;
    }
    if (!startTime || !endTime) {
        showMsg("请选择预约起止时间", true);
        return;
    }
    if (startTime >= endTime) {
        showMsg("结束时间不能早于开始时间", true);
        return;
    }

    let sTime = startTime.replace("T", " ");
    let eTime = endTime.replace("T", " ");

    try {
        let res = await request("/student/booking", "POST", {
            coachId: coachId,
            subjectType: subjectType,
            startTime: sTime,
            endTime: eTime
        });
        if (res.success) {
            showMsg("预约成功！");
            document.getElementById("startTime").value = '';
            document.getElementById("endTime").value = '';
            loadMyBooking();
        } else {
            showMsg(res.msg || "预约失败", true);
        }
    } catch (error) {
        console.error("提交预约出错：", error);
        showMsg("提交预约异常，请稍后重试", true);
    }
}

// ===================== 加载个人预约记录 =====================
async function loadMyBooking() {
    let res = await request("/student/booking");
    let tbody = document.getElementById("bookTbody");
    tbody.innerHTML = "";

    if (!res.data || res.data.length === 0) {
        tbody.innerHTML = "<tr><td colspan='6' style='color:#94a3b8;text-align:center;'>暂无预约记录</td></tr>";
        return;
    }

    // 获取当前登录用户ID
    let userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
    let currentStudentId = userInfo.id;

    // 只显示当前学员的记录
    let myBookings = res.data.filter(item => item.studentId === currentStudentId);

    if (myBookings.length === 0) {
        tbody.innerHTML = "<tr><td colspan='6' style='color:#94a3b8;text-align:center;'>暂无预约记录</td></tr>";
        return;
    }

    // 获取教练列表用于映射
    let coachRes = await request("/student/enrollment?type=coach");
    let coachMap = {};
    if (coachRes.success && coachRes.data) {
        coachRes.data.forEach(item => {
            coachMap[item.id] = item.name;
        });
    }

    myBookings.forEach(item => {
        let statusText = item.status === "approved" ? "已通过" : "已拒绝";
        let coachName = coachMap[item.coachId] || item.coachId.substring(0, 8) + '...';
        let startTime = formatTimestamp(item.startTime);
        let endTime = formatTimestamp(item.endTime);

        let tr = document.createElement("tr");
        tr.innerHTML = `
            <td>${item.id.substring(0, 8)}...</td>
            <td>${coachName}</td>
            <td>${item.subjectType}</td>
            <td>${startTime}</td>
            <td>${endTime}</td>
            <td>${statusText}</td>
        `;
        tbody.appendChild(tr);
    });
}

// ===================== 时间格式化 =====================
function formatTimestamp(timestamp) {
    if (!timestamp) return '-';
    let date = new Date(timestamp);
    let year = date.getFullYear();
    let month = String(date.getMonth() + 1).padStart(2, '0');
    let day = String(date.getDate()).padStart(2, '0');
    let hours = String(date.getHours()).padStart(2, '0');
    let minutes = String(date.getMinutes()).padStart(2, '0');
    return year + '-' + month + '-' + day + ' ' + hours + ':' + minutes;
}

// ===================== 标签页切换 =====================
function switchTab(tabName) {
    let tabBtns = document.querySelectorAll(".tab-btn");
    let tabContents = document.querySelectorAll(".content-card");

    tabBtns.forEach(btn => btn.classList.remove("active"));
    tabContents.forEach(content => content.classList.remove("active"));

    let targetBtn = document.querySelector(`.tab-btn[data-target="${tabName}"]`);
    let targetContent = document.getElementById("content-" + tabName);

    if (targetBtn) targetBtn.classList.add("active");
    if (targetContent) targetContent.classList.add("active");
}

// ===================== 取消预约 =====================
async function cancelBook(id) {
    let res = await fetch(window.BASE_URL + `/student/booking?bookingId=${id}`, {
        method: "DELETE"
    });
    let json = await res.json();
    alert(json.msg);
    loadMyBooking();
}

// ===================== 冲突预校验 =====================
async function checkConflict(coach, start, end) {
    let res = await fetch(window.BASE_URL + `/student/booking/conflict?coachId=${coach}&start=${start}&end=${end}`);
    let json = await res.json();
    if (json.data) alert("时段冲突");
}

// ===================== 退出登录 =====================
function logout() {
    if (confirm("确定要退出登录吗？")) {
        localStorage.removeItem("userInfo");
        location.href = "login.html";
    }
}

// 页面初始化
window.onload = function () {
    loadCoach();
    loadMyEnrollInfo();
    loadMyBooking();
}