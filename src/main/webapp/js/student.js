/**
 * ============================================================
 * 学员模块 - 所有业务逻辑
 * 依赖：common.js（提供 BASE_URL）
 * ============================================================
 */

// ============================================================
// 1. 通用工具函数
// ============================================================

function showMsg(text, isError = false) {
    var tip = document.createElement("div");
    tip.style.position = "fixed";
    tip.style.top = "30px";
    tip.style.left = "50%";
    tip.style.transform = "translateX(-50%)";
    tip.style.padding = "8px 20px";
    tip.style.borderRadius = "6px";
    tip.style.color = "#fff";
    tip.style.background = isError ? "#f53f3f" : "#67c23a";
    tip.style.zIndex = "9999";
    tip.innerText = text;
    document.body.appendChild(tip);
    setTimeout(function() {
        document.body.removeChild(tip);
    }, 2500);
}

function formatTimestamp(timestamp) {
    if (!timestamp) return '-';
    var date = new Date(timestamp);
    return date.getFullYear() + '-' +
        String(date.getMonth() + 1).padStart(2, '0') + '-' +
        String(date.getDate()).padStart(2, '0') + ' ' +
        String(date.getHours()).padStart(2, '0') + ':' +
        String(date.getMinutes()).padStart(2, '0');
}

async function request(url, method, data) {
    method = method || 'GET';
    var options = {
        method: method,
        headers: {
            'Content-Type': 'application/json'
        }
    };
    if (method === 'POST' && data) {
        options.body = JSON.stringify(data);
    }
    var res = await fetch(window.BASE_URL + url, options);
    return await res.json();
}


// ============================================================
// 2. Tab 切换
// ============================================================

function switchTab(tabName) {
    document.querySelectorAll('.tab-btn').forEach(function(t) {
        t.classList.remove('active');
    });
    document.querySelectorAll('.content-card').forEach(function(c) {
        c.classList.remove('active');
    });
    var targetBtn = document.querySelector('.tab-btn[data-target="' + tabName + '"]');
    var targetContent = document.getElementById('content-' + tabName);
    if (targetBtn) targetBtn.classList.add('active');
    if (targetContent) targetContent.classList.add('active');
}


// ============================================================
// 3. 退出登录
// ============================================================

function safeLogout() {
    if (confirm('确定退出学员账号吗？')) {
        localStorage.removeItem('userInfo');
        window.location.href = window.BASE_URL + '/logout';
    }
}


// ============================================================
// 4. 加载教练列表
// ============================================================

async function loadCoach() {
    var res = await request('/student/enrollment?type=coach');
    var enrollCoach = document.getElementById('enrollCoach');
    var bookCoach = document.getElementById('bookCoach');

    if (enrollCoach) enrollCoach.innerHTML = '';
    if (bookCoach) bookCoach.innerHTML = '';

    if (!res.success || !res.data || res.data.length === 0) {
        var msg = '<option>暂无教练数据</option>';
        if (enrollCoach) enrollCoach.innerHTML = msg;
        if (bookCoach) bookCoach.innerHTML = msg;
        return;
    }

    res.data.forEach(function(item) {
        var text = item.name + ' | ' + item.subject;
        var opt1 = new Option(text, item.id);
        var opt2 = new Option(text, item.id);
        if (enrollCoach) enrollCoach.appendChild(opt1);
        if (bookCoach) bookCoach.appendChild(opt2);
    });
}


// ============================================================
// 5. 提交驾校报名
// ============================================================

async function submitEnroll() {
    var coachId = document.getElementById('enrollCoach').value;
    var subjectType = document.getElementById('enrollSubject').value;

    if (!coachId) {
        showMsg('请选择教练', true);
        return;
    }

    var res = await request('/student/enrollment', 'POST', {
        coachId: coachId,
        subjectType: subjectType
    });

    if (res.success) {
        showMsg('报名提交成功，等待审核');
        loadMyEnrollInfo();
    } else {
        showMsg(res.msg || '提交失败', true);
    }
}


// ============================================================
// 6. 加载我的报名信息
// ============================================================

async function loadMyEnrollInfo() {
    var res = await request('/student/enrollment?type=myEnroll');
    var infoBox = document.getElementById('enrollInfoBox');

    if (!res.data) {
        if (infoBox) infoBox.innerHTML = '暂无报名记录';
        return;
    }

    var d = res.data;
    var statusMap = {
        'pending': '待审核',
        'approved': '审核通过',
        'rejected': '审核驳回'
    };
    var statusText = statusMap[d.status] || d.status;
    var coachName = d.coachName || d.coachId || '未知';

    if (infoBox) {
        infoBox.innerHTML =
            '报名ID：' + d.id + '<br>' +
            '教练：' + coachName + '<br>' +
            '报考科目：' + d.subjectType + '<br>' +
            '当前状态：<strong style="color:#60a5fa;">' + statusText + '</strong><br>' +
            '申请时间：' + formatTimestamp(d.applyTime);
    }
}


// ============================================================
// 7. 提交练车预约
// ============================================================

async function submitBook() {
    var coachId = document.getElementById('bookCoach').value;
    var subjectType = document.getElementById('bookSubject').value;
    var startTime = document.getElementById('startTime').value;
    var endTime = document.getElementById('endTime').value;

    if (!coachId || !subjectType) {
        showMsg('请选择教练和科目', true);
        return;
    }
    if (!startTime || !endTime) {
        showMsg('请选择预约起止时间', true);
        return;
    }
    if (startTime >= endTime) {
        showMsg('结束时间不能早于开始时间', true);
        return;
    }

    var sTime = startTime.replace('T', ' ');
    var eTime = endTime.replace('T', ' ');

    try {
        var res = await request('/student/booking', 'POST', {
            coachId: coachId,
            subjectType: subjectType,
            startTime: sTime,
            endTime: eTime
        });

        if (res.success) {
            showMsg('预约成功！');
            document.getElementById('startTime').value = '';
            document.getElementById('endTime').value = '';
            loadMyBooking();
        } else {
            showMsg(res.msg || '预约失败', true);
        }
    } catch (e) {
        console.error('提交预约出错：', e);
        showMsg('提交预约异常，请稍后重试', true);
    }
}


// ============================================================
// 8. 加载我的预约记录
// ============================================================

async function loadMyBooking() {
    var tbody = document.getElementById('bookTbody');
    if (!tbody) return;

    try {
        var res = await request('/student/booking');
        tbody.innerHTML = '';

        if (!res.data || res.data.length === 0) {
            tbody.innerHTML = '<tr><td colspan="9" style="color:#94a3b8;text-align:center;">暂无预约记录</td></tr>';
            return;
        }

        // 获取教练映射
        var coachRes = await request('/student/enrollment?type=coach');
        var coachMap = {};
        if (coachRes.success && coachRes.data) {
            coachRes.data.forEach(function(item) {
                coachMap[item.id] = item.name;
            });
        }

        var userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
        var currentStudentId = userInfo.id;
        var myBookings = res.data.filter(function(item) {
            return item.studentId === currentStudentId;
        });

        if (myBookings.length === 0) {
            tbody.innerHTML = '<tr><td colspan="9" style="color:#94a3b8;text-align:center;">暂无预约记录</td></tr>';
            return;
        }

        myBookings.forEach(function(item) {
            var statusText = item.status === 'approved' ? '已通过' : '已拒绝';
            var coachName = coachMap[item.coachId] || item.coachId.substring(0, 8) + '...';
            var coachScore = item.coachScore ? item.coachScore + '⭐' : '-';
            var myScore = item.studentScore ? item.studentScore + '⭐' : '-';

            var actionHtml = '-';
            if (item.status === 'approved' && !item.studentScore) {
                actionHtml = '<a href="coach-evaluate.html?coachId=' + item.coachId + '" class="btn-sm-gradient">去评价</a>';
            } else if (item.studentScore) {
                actionHtml = '<span class="btn-sm-success">已评价</span>';
            }

            var tr = document.createElement('tr');
            tr.innerHTML =
                '<td>' + item.id.substring(0, 8) + '...</td>' +
                '<td>' + coachName + '</td>' +
                '<td>' + item.subjectType + '</td>' +
                '<td>' + formatTimestamp(item.startTime) + '</td>' +
                '<td>' + formatTimestamp(item.endTime) + '</td>' +
                '<td>' + statusText + '</td>' +
                '<td>' + coachScore + '</td>' +
                '<td>' + myScore + '</td>' +
                '<td>' + actionHtml + '</td>';
            tbody.appendChild(tr);
        });

    } catch (e) {
        console.error('加载预约记录失败:', e);
    }
}


// ============================================================
// 9. 加载教练空闲时间
// ============================================================

async function loadCoachAvailability() {
    var coachId = document.getElementById('bookCoach').value;
    var date = document.getElementById('bookDate').value;
    var container = document.getElementById('availabilityContainer');
    var slotsDiv = document.getElementById('timeSlots');

    if (!coachId) {
        if (container) container.style.display = 'none';
        return;
    }

    var queryDate = date;
    if (!queryDate) {
        var today = new Date();
        queryDate = today.getFullYear() + '-' +
            String(today.getMonth() + 1).padStart(2, '0') + '-' +
            String(today.getDate()).padStart(2, '0');
        var bookDate = document.getElementById('bookDate');
        if (bookDate) bookDate.value = queryDate;
    }

    try {
        var res = await request('/student/availability?coachId=' + encodeURIComponent(coachId) + '&date=' + encodeURIComponent(queryDate));

        if (res.code !== 1) {
            if (container) container.style.display = 'none';
            return;
        }

        if (container) container.style.display = 'block';
        var occupied = res.data || [];

        var hours = [];
        for (var i = 8; i <= 20; i++) {
            var start = queryDate + ' ' + String(i).padStart(2, '0') + ':00';
            var end = queryDate + ' ' + String(i + 1).padStart(2, '0') + ':00';
            hours.push({ start: start, end: end, label: i + ':00 - ' + (i + 1) + ':00' });
        }

        var html = '';
        hours.forEach(function(h) {
            var isOccupied = occupied.some(function(o) {
                var occStart = o.start.substring(11, 16);
                var occEnd = o.end.substring(11, 16);
                var hStart = h.start.substring(11, 16);
                var hEnd = h.end.substring(11, 16);
                return (hStart < occEnd && hEnd > occStart);
            });
            var cls = isOccupied ? 'occupied' : 'free';
            var label = isOccupied ? '🔴 ' + h.label : '🟢 ' + h.label;
            html += '<span class="time-slot ' + cls + '">' + label + '</span>';
        });

        if (slotsDiv) slotsDiv.innerHTML = html || '<span class="time-slot unknown">暂无时段数据</span>';

    } catch (e) {
        console.error('加载教练空闲时间失败:', e);
    }
}


// ============================================================
// 10. 学员给教练打分（跳转到评价页面）
// ============================================================

function rateBooking(bookingId) {
    // 由 HTML 中的链接直接跳转到 coach-evaluate.html
    // 此函数保留用于兼容
    console.log('rateBooking 通过链接跳转');
}


// ============================================================
// 11. 取消预约
// ============================================================

async function cancelBook(id) {
    if (!confirm('确定要取消该预约吗？')) return;
    var res = await fetch(window.BASE_URL + '/student/booking?bookingId=' + id, {
        method: 'DELETE'
    });
    var json = await res.json();
    alert(json.msg);
    loadMyBooking();
}


// ============================================================
// 12. 冲突预校验
// ============================================================

async function checkConflict(coach, start, end) {
    var res = await fetch(window.BASE_URL + '/student/booking/conflict?coachId=' + coach + '&start=' + start + '&end=' + end);
    var json = await res.json();
    if (json.data) alert('时段冲突');
}


// ============================================================
// 13. 暴露全局函数（供 HTML onclick 调用）
// ============================================================

window.switchTab = switchTab;
window.safeLogout = safeLogout;
window.loadCoach = loadCoach;
window.submitEnroll = submitEnroll;
window.loadMyEnrollInfo = loadMyEnrollInfo;
window.submitBook = submitBook;
window.loadMyBooking = loadMyBooking;
window.loadCoachAvailability = loadCoachAvailability;
window.rateBooking = rateBooking;
window.cancelBook = cancelBook;
window.checkConflict = checkConflict;