// 全局上下文路径，根据项目自行调整
const ctx = "";

/**
 * 通用弹窗提示
 * @param text 提示文字
 * @param isError 是否错误提示
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
    let res = await fetch(ctx + url, options);
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

// ===================== 标签页切换（修复ID匹配问题） =====================
function switchTab(tabName) {
    // 清空所有激活状态
    let tabItems = document.querySelectorAll(".tab-item");
    let tabContents = document.querySelectorAll(".tab-content");
    tabItems.forEach(item => item.classList.remove("active"));
    tabContents.forEach(content => content.classList.remove("active"));

    // 激活对应标签和内容
    document.getElementById("tab-" + tabName).classList.add("active");
    document.getElementById("content-" + tabName).classList.add("active");
}

// ===================== 加载教练列表（核心修复下拉框空白） =====================
async function loadCoach() {
    let res = await request("/student/enrollment?type=coach");
    console.log("教练接口返回数据：", res); // 调试：浏览器F12查看数据，判断后端是否有教练

    const enrollCoach = document.getElementById("enrollCoach");
    const bookCoach = document.getElementById("bookCoach");
    // 先清空原有选项，避免重复
    enrollCoach.innerHTML = "";
    bookCoach.innerHTML = "";

    // 无教练数据提示
    if (!res.success || !res.data || res.data.length === 0) {
        enrollCoach.innerHTML = "<option>暂无教练数据</option>";
        bookCoach.innerHTML = "<option>暂无教练数据</option>";
        return;
    }

    // 循环渲染教练选项
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
        case "pending": status = "待审核"; break;
        case "approved": status = "审核通过"; break;
        case "rejected": status = "审核驳回"; break;
        default: status = data.status;
    }
    infoBox.innerHTML = `
        报名ID：${data.id}<br>
        教练ID：${data.coachId}<br>
        报考科目：${data.subjectType}<br>
        当前状态：${status}<br>
        申请时间：${data.applyTime}
    `;
}

// ===================== 提交练车预约 =====================
async function submitBook() {
    let coachId = document.getElementById("bookCoach").value;
    let subjectType = document.getElementById("bookSubject").value;
    let startTime = document.getElementById("startTime").value;
    let endTime = document.getElementById("endTime");

    if (!startTime || !endTime.value) {
        showMsg("请选择预约起止时间", true);
        return;
    }
    if (startTime >= endTime.value) {
        showMsg("结束时间不能早于开始时间", true);
    }

    let sTime = startTime.replace("T", " ");
    let eTime = endTime.value.replace("T", " ");
    let res = await request("/student/booking", "POST", {
        coachId: coachId,
        subjectType: subjectType,
        startTime: sTime,
        endTime: eTime
    });
    if (res.success) {
        showMsg("预约成功");
        loadMyBooking();
    } else {
        showMsg(res.msg, true);
    }
}

// ===================== 加载个人预约记录 =====================
async function loadMyBooking() {
    let res = await request("/student/booking");
    let tbody = document.getElementById("bookTbody");
    tbody.innerHTML = "";

    if (!res.data || res.data.length === 0) {
        tbody.innerHTML = "<tr><td colspan='6'>暂无预约记录</td>";
        return;
    }

    res.data.forEach(item => {
        let statusText = item.status === "approved" ? "已通过" : "已拒绝";
        let tr = document.createElement("tr");
        tr.innerHTML = `
            <td>${item.id}</td>
            <td>${item.coachId}</td>
            <td>${item.subjectType}</td>
            <td>${item.startTime}</td>
            <td>${item.endTime}</td>
            <td>${statusText}</td>
        `;
        tbody.appendChild(tr);
    });
}

//===================== 取消预约 =====================
async function cancelBook(id){
    let res = await fetch(`/student/booking?bookingId=${id}`,{
        method:"DELETE"
    })
    let json = await res.json();
    alert(json.msg);
    loadMyBooking();
}

// ===================== 冲突预校验 =====================
async function checkConflict(coach,start,end){
    let res = await fetch(`/student/booking/conflict?coachId=${coach}&start=${start}&end=${end}`)
    let json = await res.json();
    if(json.data) alert("时段冲突");
}

// ===================== 退出登录 =====================
function logout() {
    if (confirm("确定要退出登录吗？")) {
        location.href = "login.html";
    }
}

// 页面初始化（加载教练、报名、预约数据）
window.onload = function () {
    loadCoach();
    loadMyEnrollInfo();
    loadMyBooking();
}