// admin.js - 管理员后台页面逻辑

var BASE_URL = window.BASE_URL || '';

// 页面加载完成后初始化动画
document.addEventListener('DOMContentLoaded', function() {
    initParticleCanvas();
    initRevealAnimations();
    // 显示管理员名字
    var userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
    if (userInfo.name) {
        document.getElementById('adminName').innerHTML = '👤 管理员 ' + userInfo.name;
    }
    window.loadDashboard();
    window.showTab('dashboard');
});

// 粒子背景动画
function initParticleCanvas() {
    var canvas = document.getElementById('particleCanvas');
    if (!canvas) return;
    var ctx = canvas.getContext('2d');
    var particles = [];
    var w = canvas.width = window.innerWidth;
    var h = canvas.height = window.innerHeight;
    window.addEventListener('resize', function() {
        w = canvas.width = window.innerWidth;
        h = canvas.height = window.innerHeight;
    });
    for (var i = 0; i < 50; i++) {
        particles.push({
            x: Math.random() * w,
            y: Math.random() * h,
            vx: (Math.random() - 0.5) * 0.3,
            vy: (Math.random() - 0.5) * 0.3,
            size: Math.random() * 2 + 1,
            opacity: Math.random() * 0.5 + 0.1
        });
    }
    function animate() {
        ctx.clearRect(0, 0, w, h);
        particles.forEach(function(p) {
            p.x += p.vx;
            p.y += p.vy;
            if (p.x < 0 || p.x > w) p.vx *= -1;
            if (p.y < 0 || p.y > h) p.vy *= -1;
            ctx.beginPath();
            ctx.arc(p.x, p.y, p.size, 0, Math.PI * 2);
            ctx.fillStyle = 'rgba(59, 130, 246, ' + p.opacity + ')';
            ctx.fill();
        });
        requestAnimationFrame(animate);
    }
    animate();
}

// 滚动 reveal 动画
function initRevealAnimations() {
    var elements = document.querySelectorAll('.stat-card, .chart-box, .section');
    var observer = new IntersectionObserver(function(entries) {
        entries.forEach(function(entry, index) {
            if (entry.isIntersecting) {
                setTimeout(function() {
                    entry.target.classList.add('reveal');
                }, index * 80);
                observer.unobserve(entry.target);
            }
        });
    }, { threshold: 0.1, rootMargin: '0px 0px -50px 0px' });
    elements.forEach(function(el) {
        observer.observe(el);
    });
    setTimeout(function() {
        var rows = document.querySelectorAll('tbody tr');
        rows.forEach(function(row, index) {
            setTimeout(function() {
                row.classList.add('reveal');
            }, index * 60);
        });
    }, 300);
}

// 数字滚动动画
function animateValue(element, start, end, duration) {
    var startTime = null;
    function step(timestamp) {
        if (!startTime) startTime = timestamp;
        var progress = Math.min((timestamp - startTime) / duration, 1);
        var easeOut = 1 - Math.pow(1 - progress, 3);
        var current = Math.floor(easeOut * (end - start) + start);
        element.textContent = current;
        if (progress < 1) {
            requestAnimationFrame(step);
        }
    }
    requestAnimationFrame(step);
}

function apiGet(url, callback) {
    console.log('[apiGet] 开始请求:', BASE_URL + url);
    axios.get(BASE_URL + url, { responseType: 'json' })
        .then(function(res) {
            console.log('[apiGet] 响应状态:', res.status);
            console.log('[apiGet] 响应数据:', res.data);
            console.log('[apiGet] code:', res.data ? res.data.code : 'undefined');
            console.log('[apiGet] data字段:', res.data ? res.data.data : 'undefined');
            var data = res.data;
            if (data && data.code === 1) {
                console.log('[apiGet] 业务成功，调用回调');
                callback(data.data || data);
            } else {
                console.warn('[apiGet] 业务失败:', data);
                alert(data ? (data.msg || '请求失败') : '请求失败');
            }
        })
        .catch(function(err) {
            console.error('[apiGet] 请求失败:', err);
            alert('请求失败，请检查网络');
        });
}

function apiPost(url, data, callback) {
    axios.post(BASE_URL + url, data, {
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
    })
        .then(function(res) {
            if (res.data.code === 1) {
                if (callback) callback(res.data);
            } else {
                alert(res.data.msg || '操作失败');
            }
        })
        .catch(function(err) {
            console.error('请求失败:', err);
            alert('请求失败，请检查网络');
        });
}

// 切换 Tab
window.showTab = function(tabName) {
    document.querySelectorAll('.tab-content').forEach(function(el) {
        el.classList.remove('active');
    });
    document.querySelectorAll('.sidebar-nav a').forEach(function(el) {
        el.classList.remove('active');
    });

    var targetTab = document.getElementById('tab-' + tabName);
    if (targetTab) targetTab.classList.add('active');

    var titleMap = {
        'dashboard': '数据概览',
        'enrollments': '报名审核',
        'bookings': '练车记录',
        'staff': '员工管理',
        'students': '学员管理'
    };
    document.getElementById('pageTitle').innerText = titleMap[tabName] || '数据概览';

    document.querySelectorAll('.sidebar-nav a').forEach(function(el) {
        if (el.getAttribute('onclick') && el.getAttribute('onclick').includes("'" + tabName + "'")) {
            el.classList.add('active');
        }
    });

    setTimeout(function() {
        var elements = targetTab.querySelectorAll('.stat-card, .chart-box, .section');
        elements.forEach(function(el, index) {
            el.classList.remove('reveal');
            setTimeout(function() {
                el.classList.add('reveal');
            }, index * 80);
        });
        // 不再处理 tbody tr，因为数据加载函数已经处理了
    }, 100);

    // 加载对应数据
    if (tabName === 'staff') window.loadStaffList();
    if (tabName === 'students') window.loadStudents();
    if (tabName === 'enrollments') {
        window.loadPendingEnrollments();
        window.loadAllEnrollments();
    }
    if (tabName === 'bookings') window.loadBookings();
    if (tabName === 'dashboard') window.loadDashboard();
};

// ==================== 员工管理 ====================
window.loadStaffList = function() {
    console.log('[loadStaffList] 开始执行');
    var container = document.getElementById('staffList');
    console.log('[loadStaffList] container:', container);
    container.innerHTML = '<div class="empty-tip loading">加载中...</div>';

    apiGet('/admin/staff?action=list', function(list) {
        console.log('[loadStaffList] 收到数据:', list);
        console.log('[loadStaffList] 数据类型:', typeof list);
        console.log('[loadStaffList] 数据长度:', list ? list.length : 'null');
        if (!list || list.length === 0) {
            console.log('[loadStaffList] 数据为空');
            container.innerHTML = '<div class="empty-tip">暂无员工数据</div>';
            return;
        }
        console.log('[loadStaffList] 开始渲染', list.length, '条数据');
        var html = '<table class="data-table"><thead><tr>' +
            '<th>姓名</th><th>手机号</th><th>角色</th><th>科目</th><th>操作</th>' +
            '</tr></thead><tbody>';
        list.forEach(function(item, index) {
            console.log('[loadStaffList] 渲染item:', item);
            var roleText = item.role === 'admin' ? '管理员' : '教练';
            var subjectText = item.subject || '-';
            html += '<tr class="reveal" style="transition-delay: ' + (index * 60) + 'ms">' +
                '<td>' + (item.name || '') + '</td>' +
                '<td>' + (item.phone || '') + '</td>' +
                '<td>' + roleText + '</td>' +
                '<td>' + subjectText + '</td>' +
                '<td>' +
                '<button class="btn btn-sm" onclick="window.editStaff(\'' + item.id + '\')" style="margin-right:5px;background:rgba(255,255,255,0.1)">编辑</button>' +
                '<button class="btn btn-sm btn-danger" onclick="window.deleteStaff(\'' + item.id + '\')">删除</button>' +
                '</td>' +
                '</tr>';
        });
        html += '</tbody></table>';
        container.innerHTML = html;
        console.log('[loadStaffList] 渲染完成');
    });
};

window.openAddStaffModal = function() {
    document.getElementById('editStaffId').value = '';
    document.getElementById('modalStaffName').value = '';
    document.getElementById('modalStaffPhone').value = '';
    document.getElementById('modalStaffRole').value = 'coach';
    document.getElementById('modalStaffSubject').value = 'C1';
    document.getElementById('modalStaffPassword').value = '';
    document.getElementById('modalStaffUsb').value = '';
    document.getElementById('staffModalTitle').innerText = '新增员工';
    document.getElementById('modalPasswordGroup').style.display = 'block';
    window.toggleSubjectField();
    document.getElementById('staffModal').classList.add('show');
};

window.closeModal = function() {
    document.querySelectorAll('.modal-overlay').forEach(function(el) {
        el.classList.remove('show');
    });
};

window.toggleSubjectField = function() {
    var role = document.getElementById('modalStaffRole').value;
    var subjectGroup = document.getElementById('modalSubjectGroup');
    var usbGroup = document.getElementById('modalUsbGroup');
    if (subjectGroup) {
        subjectGroup.style.display = (role === 'coach') ? 'block' : 'none';
    }
    if (usbGroup) {
        usbGroup.style.display = (role === 'admin') ? 'block' : 'none';
    }
};

window.saveStaff = function() {
    var id = document.getElementById('editStaffId').value;
    var name = document.getElementById('modalStaffName').value.trim();
    var phone = document.getElementById('modalStaffPhone').value.trim();
    var role = document.getElementById('modalStaffRole').value;
    var subject = document.getElementById('modalStaffSubject').value;
    var password = document.getElementById('modalStaffPassword').value;
    var usbToken = document.getElementById('modalStaffUsb').value;

    if (!name || !phone) {
        alert('请填写姓名和手机号');
        return;
    }
    if (role === 'admin' && !usbToken) {
        alert('管理员必须设置USB安全令牌');
        return;
    }

    var params = new URLSearchParams();
    params.append('name', name);
    params.append('phone', phone);
    params.append('role', role);
    params.append('subject', role === 'coach' ? subject : '');
    if (password) params.append('password', password);
    if (usbToken) params.append('usbToken', usbToken);

    var url = id ? '/admin/staff?action=update&id=' + id : '/admin/staff?action=add';
    apiPost(url, params.toString(), function() {
        window.closeModal();
        window.loadStaffList();
    });
};

window.editStaff = function(id) {
    apiGet('/admin/staff?action=detail&id=' + id, function(data) {
        document.getElementById('editStaffId').value = data.id;
        document.getElementById('modalStaffName').value = data.name || '';
        document.getElementById('modalStaffPhone').value = data.phone || '';
        document.getElementById('modalStaffRole').value = data.role || 'coach';
        document.getElementById('modalStaffSubject').value = data.subject || 'C1';
        document.getElementById('modalStaffPassword').value = '';
        document.getElementById('staffModalTitle').innerText = '编辑员工';
        document.getElementById('modalPasswordGroup').style.display = 'none';
        window.toggleSubjectField();
        document.getElementById('staffModal').classList.add('show');
    });
};

window.deleteStaff = function(id) {
    if (!confirm('确定删除该员工吗？')) return;
    apiPost('/admin/staff?action=delete&id=' + id, '', function() {
        window.loadStaffList();
    });
};

// ==================== 学员管理 ====================
window.loadStudents = function() {
    console.log('[loadStudents] 开始执行');
    var container = document.getElementById('studentList');
    console.log('[loadStudents] container:', container);
    container.innerHTML = '<div class="empty-tip loading">加载中...</div>';

    apiGet('/admin/query?action=students', function(data) {
        console.log('[loadStudents] 收到数据:', data);
        console.log('[loadStudents] 数据类型:', typeof data);
        console.log('[loadStudents] 数据长度:', data ? data.length : 'null');
        if (!data || data.length === 0) {
            console.log('[loadStudents] 数据为空');
            container.innerHTML = '<div class="empty-tip">暂无学员数据</div>';
            return;
        }
        console.log('[loadStudents] 开始渲染', data.length, '条数据');
        var statusMap = { 'pending': '待审核', 'approved': '已通过', 'rejected': '已拒绝' };
        var html = '<table class="data-table"><thead><tr>' +
            '<th>姓名</th><th>手机号</th><th>身份证号</th><th>报名状态</th>' +
            '</tr></thead><tbody>';
        data.forEach(function(item, index) {
            console.log('[loadStudents] 渲染item:', item);
            var statusText = statusMap[item.enrollStatus] || '未报名';
            html += '<tr class="reveal" style="transition-delay: ' + (index * 60) + 'ms">' +
                '<td>' + (item.name || '') + '</td>' +
                '<td>' + (item.phone || '') + '</td>' +
                '<td>' + (item.idCard || '') + '</td>' +
                '<td>' + statusText + '</td>' +
                '</tr>';
        });
        html += '</tbody></table>';
        container.innerHTML = html;
        console.log('[loadStudents] 渲染完成');
    });
};

// ==================== 报名审核 ====================
window.loadPendingEnrollments = function() {
    var container = document.getElementById('pendingList');
    if (!container) return;
    container.innerHTML = '<div class="empty-tip loading">加载中...</div>';
    apiGet('/admin/query?action=enrollments', function(data) {
        if (!data || data.length === 0) {
            container.innerHTML = '<div class="empty-tip">暂无待审核报名</div>';
            return;
        }
        var pending = data.filter(function(item) { return item.status === 'pending'; });
        if (pending.length === 0) {
            container.innerHTML = '<div class="empty-tip">暂无待审核报名</div>';
            return;
        }
        var html = '<table class="data-table"><thead><tr>' +
            '<th><input type="checkbox" onclick="window.toggleAllEnrollment(this)"></th>' +
            '<th>学员</th><th>教练</th><th>科目</th><th>状态</th><th>操作</th>' +
            '</tr></thead><tbody>';
        pending.forEach(function(item) {
            var statusText = { pending: '待审核', approved: '已通过', rejected: '已拒绝' }[item.status] || item.status;
            html += '<tr>' +
                '<td><input type="checkbox" class="enroll-checkbox" value="' + item.id + '"></td>' +
                '<td>' + (item.studentName || '') + '</td>' +
                '<td>' + (item.coachName || '') + '</td>' +
                '<td>' + (item.subjectType || '') + '</td>' +
                '<td>' + statusText + '</td>' +
                '<td>' +
                '<button class="btn btn-sm btn-primary" onclick="window.auditEnrollment(\'' + item.id + '\', \'approved\')" style="margin-right:5px">通过</button>' +
                '<button class="btn btn-sm btn-danger" onclick="window.auditEnrollment(\'' + item.id + '\', \'rejected\')">拒绝</button>' +
                '</td>' +
                '</tr>';
        });
        html += '</tbody></table>' +
            '<div style="margin-top:10px;">' +
            '<button class="btn btn-sm btn-primary" onclick="window.batchAudit(\'approved\')">批量通过</button> ' +
            '<button class="btn btn-sm btn-danger" onclick="window.batchAudit(\'rejected\')">批量拒绝</button>' +
            '</div>';
        container.innerHTML = html;
    });
};

window.toggleAllEnrollment = function(master) {
    document.querySelectorAll('.enroll-checkbox').forEach(function(cb) {
        cb.checked = master.checked;
    });
};

window.loadAllEnrollments = function() {
    var container = document.getElementById('allEnrollmentList');
    if (!container) return;
    container.innerHTML = '<div class="empty-tip loading">加载中...</div>';
    apiGet('/admin/query?action=enrollments', function(data) {
        if (!data || data.length === 0) {
            container.innerHTML = '<div class="empty-tip">暂无报名记录</div>';
            return;
        }
        var html = '<table class="data-table"><thead><tr>' +
            '<th>学员</th><th>教练</th><th>科目</th><th>状态</th>' +
            '</tr></thead><tbody>';
        data.forEach(function(item) {
            var statusText = { pending: '待审核', approved: '已通过', rejected: '已拒绝' }[item.status] || item.status;
            html += '<tr>' +
                '<td>' + (item.studentName || '') + '</td>' +
                '<td>' + (item.coachName || '') + '</td>' +
                '<td>' + (item.subjectType || '') + '</td>' +
                '<td>' + statusText + '</td>' +
                '</tr>';
        });
        html += '</tbody></table>';
        container.innerHTML = html;
    });
};

window.auditEnrollment = function(id, status) {
    var remark = prompt(status === 'approved' ? '请输入通过意见（可选）：' : '请输入拒绝原因（可选）：');
    var params = new URLSearchParams();
    params.append('action', 'single');
    params.append('enrollmentId', id);
    params.append('status', status);
    if (remark) params.append('remark', remark);
    apiPost('/admin/audit', params.toString(), function() {
        loadPendingEnrollments();
        loadAllEnrollments();
    });
};

window.batchAudit = function(status) {
    var checkboxes = document.querySelectorAll('.enroll-checkbox:checked');
    if (checkboxes.length === 0) {
        alert('请选择要审核的报名记录');
        return;
    }
    var ids = Array.from(checkboxes).map(function(cb) { return cb.value; }).join(',');
    var remark = prompt(status === 'approved' ? '请输入批量通过意见（可选）：' : '请输入批量拒绝原因（可选）：');
    var params = new URLSearchParams();
    params.append('action', 'batch');
    params.append('ids', ids);
    params.append('status', status);
    if (remark) params.append('remark', remark);
    apiPost('/admin/audit', params.toString(), function(data) {
        alert(data.msg || '操作完成');
        loadPendingEnrollments();
        loadAllEnrollments();
    });
};

// ==================== 练车记录 ====================
window.loadBookings = function() {
    var container = document.getElementById('bookingList');
    if (!container) return;
    container.innerHTML = '<div class="empty-tip loading">加载中...</div>';

    apiGet('/admin/query?action=bookings', function(data) {
        if (!data || data.length === 0) {
            container.innerHTML = '<div class="empty-tip">暂无练车记录</div>';
            return;
        }
        var html = '<table class="data-table"><thead><tr>' +
            '<th>学员</th><th>教练</th><th>科目</th><th>开始时间</th><th>结束时间</th><th>状态</th><th>学员评分</th><th>教练评分</th>' +
            '</tr></thead><tbody>';
        data.forEach(function(item) {
            var statusText = { approved: '已通过', rejected: '已拒绝' }[item.status] || item.status;
            html += '<tr>' +
                '<td>' + (item.studentName || '') + '</td>' +
                '<td>' + (item.coachName || '') + '</td>' +
                '<td>' + (item.subjectType || '') + '</td>' +
                '<td>' + formatDateTime(item.startTime) + '</td>' +
                '<td>' + formatDateTime(item.endTime) + '</td>' +
                '<td>' + statusText + '</td>' +
                '<td>' + (item.studentScore || '-') + '</td>' +
                '<td>' + (item.coachScore || '-') + '</td>' +
                '</tr>';
        });
        html += '</tbody></table>';
        container.innerHTML = html;
    });
};

function formatDateTime(timestamp) {
    if (!timestamp) return '-';
    var date = new Date(timestamp);
    var pad = function(n) { return n < 10 ? '0' + n : n; };
    return date.getFullYear() + '-' + pad(date.getMonth() + 1) + '-' + pad(date.getDate()) + ' ' +
        pad(date.getHours()) + ':' + pad(date.getMinutes());
}

// ==================== 数据概览 & ECharts 可视化 ====================
var chartInstances = {};

function getChartOption(title, type, labels, values, colorList) {
    var common = {
        backgroundColor: 'transparent',
        textStyle: { color: '#94a3b8', fontSize: 11 },
        tooltip: {
            trigger: type === 'pie' ? 'item' : 'axis',
            backgroundColor: '#1e293b',
            borderColor: 'rgba(255,255,255,0.1)',
            textStyle: { color: '#e2e8f0' }
        }
    };
    if (type === 'pie') {
        var pieData = [];
        for (var i = 0; i < labels.length; i++) {
            pieData.push({ name: labels[i], value: values[i] });
        }
        return Object.assign(common, {
            series: [{
                type: 'pie',
                radius: ['40%', '70%'],
                center: ['50%', '55%'],
                itemStyle: { borderRadius: 6, borderColor: '#1e293b', borderWidth: 2 },
                label: { color: '#94a3b8', fontSize: 11, formatter: '{b}: {c} ({d}%)' },
                data: pieData,
                color: colorList || ['#60a5fa', '#34d399', '#fbbf24', '#f87171', '#a78bfa']
            }]
        });
    }
    if (type === 'line') {
        return Object.assign(common, {
            grid: { top: 30, right: 20, bottom: 30, left: 40 },
            xAxis: {
                type: 'category',
                data: labels,
                axisLine: { lineStyle: { color: 'rgba(255,255,255,0.1)' } },
                axisLabel: { color: '#94a3b8', fontSize: 10 }
            },
            yAxis: {
                type: 'value',
                axisLine: { show: false },
                splitLine: { lineStyle: { color: 'rgba(255,255,255,0.05)' } },
                axisLabel: { color: '#94a3b8', fontSize: 10 }
            },
            series: [{
                type: 'line',
                data: values,
                smooth: true,
                symbol: 'circle',
                symbolSize: 6,
                lineStyle: { width: 3, color: '#60a5fa' },
                itemStyle: { color: '#60a5fa' },
                areaStyle: {
                    color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                        { offset: 0, color: 'rgba(96,165,250,0.3)' },
                        { offset: 1, color: 'rgba(96,165,250,0.01)' }
                    ])
                }
            }]
        });
    }
    if (type === 'bar') {
        return Object.assign(common, {
            grid: { top: 30, right: 20, bottom: 30, left: 40 },
            xAxis: {
                type: 'category',
                data: labels,
                axisLine: { lineStyle: { color: 'rgba(255,255,255,0.1)' } },
                axisLabel: { color: '#94a3b8', fontSize: 10 }
            },
            yAxis: {
                type: 'value',
                axisLine: { show: false },
                splitLine: { lineStyle: { color: 'rgba(255,255,255,0.05)' } },
                axisLabel: { color: '#94a3b8', fontSize: 10 }
            },
            series: [{
                type: 'bar',
                data: values,
                barWidth: '50%',
                itemStyle: {
                    borderRadius: [4, 4, 0, 0],
                    color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                        { offset: 0, color: '#34d399' },
                        { offset: 1, color: '#059669' }
                    ])
                }
            }]
        });
    }
}

window.loadDashboard = function() {
    apiGet('/admin/query?action=stats', function(data) {
        if (!data) return;
        animateValue(document.getElementById('statStudents'), 0, data.totalStudents || 0, 1000);
        animateValue(document.getElementById('statCoaches'), 0, data.totalCoaches || 0, 1000);
        animateValue(document.getElementById('statPendingEnroll'), 0, data.pendingEnrollments || 0, 1000);
        animateValue(document.getElementById('statApprovedEnroll'), 0, data.approvedEnrollments || 0, 1000);
        animateValue(document.getElementById('statBookings'), 0, data.totalBookings || 0, 1000);
        animateValue(document.getElementById('statApprovedBook'), 0, data.approvedBookings || 0, 1000);
    });

    apiGet('/admin/query?action=statsDetail', function(detail) {
        if (!detail) return;

        var ss = detail.studentSubject || {};
        var ssLabels = Object.keys(ss);
        var ssValues = Object.values(ss);
        if (ssLabels.length === 0) { ssLabels = ['暂无数据']; ssValues = [0]; }
        var chartSS = echarts.init(document.getElementById('chartStudentSubject'));
        chartSS.setOption(getChartOption(null, 'pie', ssLabels, ssValues, ['#60a5fa', '#34d399', '#fbbf24']));
        chartInstances.studentSubject = chartSS;

        var cs = detail.coachSubject || {};
        var csLabels = Object.keys(cs);
        var csValues = Object.values(cs);
        if (csLabels.length === 0) { csLabels = ['暂无数据']; csValues = [0]; }
        var chartCS = echarts.init(document.getElementById('chartCoachSubject'));
        chartCS.setOption(getChartOption(null, 'pie', csLabels, csValues, ['#f87171', '#a78bfa', '#fbbf24']));
        chartInstances.coachSubject = chartCS;

        var bm = detail.bookingMonth || {};
        var bmLabels = Object.keys(bm);
        var bmValues = Object.values(bm);
        if (bmLabels.length === 0) { bmLabels = ['近6个月']; bmValues = [0]; }
        var chartBM = echarts.init(document.getElementById('chartBookingMonth'));
        chartBM.setOption(getChartOption(null, 'line', bmLabels, bmValues));
        chartInstances.bookingMonth = chartBM;

        var stuScore = detail.studentScore || {};
        var coaScore = detail.coachScore || {};
        var scoreLabels = ['1分', '2分', '3分', '4分', '5分'];
        var stuValues = [];
        var coaValues = [];
        for (var s = 1; s <= 5; s++) {
            stuValues.push(stuScore[String(s)] || 0);
            coaValues.push(coaScore[String(s)] || 0);
        }
        var chartScore = echarts.init(document.getElementById('chartScore'));
        chartScore.setOption({
            backgroundColor: 'transparent',
            textStyle: { color: '#94a3b8', fontSize: 11 },
            tooltip: {
                trigger: 'axis',
                backgroundColor: '#1e293b',
                borderColor: 'rgba(255,255,255,0.1)',
                textStyle: { color: '#e2e8f0' }
            },
            legend: {
                data: ['学员评分', '教练评分'],
                textStyle: { color: '#94a3b8', fontSize: 11 },
                top: 0
            },
            grid: { top: 30, right: 20, bottom: 30, left: 40 },
            xAxis: {
                type: 'category',
                data: scoreLabels,
                axisLine: { lineStyle: { color: 'rgba(255,255,255,0.1)' } },
                axisLabel: { color: '#94a3b8', fontSize: 10 }
            },
            yAxis: {
                type: 'value',
                axisLine: { show: false },
                splitLine: { lineStyle: { color: 'rgba(255,255,255,0.05)' } },
                axisLabel: { color: '#94a3b8', fontSize: 10 }
            },
            series: [
                {
                    name: '学员评分',
                    type: 'bar',
                    data: stuValues,
                    barWidth: '30%',
                    itemStyle: { borderRadius: [4, 4, 0, 0], color: '#60a5fa' }
                },
                {
                    name: '教练评分',
                    type: 'bar',
                    data: coaValues,
                    barWidth: '30%',
                    itemStyle: { borderRadius: [4, 4, 0, 0], color: '#34d399' }
                }
            ]
        });
        chartInstances.score = chartScore;

        // 新增图表：教练排行、报名趋势、预约状态
        var coachRank = detail.coachBookingRank || {};
        var coachNames = Object.keys(coachRank);
        var coachCounts = Object.values(coachRank);
        if (coachNames.length === 0) { coachNames = ['暂无数据']; coachCounts = [0]; }
        if (coachNames.length > 10) {
            coachNames = coachNames.slice(0, 10);
            coachCounts = coachCounts.slice(0, 10);
        }
        var chartRank = echarts.init(document.getElementById('chartCoachRank'));
        chartRank.setOption({
            backgroundColor: 'transparent',
            textStyle: { color: '#94a3b8', fontSize: 11 },
            tooltip: {
                trigger: 'axis',
                backgroundColor: '#1e293b',
                borderColor: 'rgba(255,255,255,0.1)',
                textStyle: { color: '#e2e8f0' }
            },
            grid: { top: 30, right: 20, bottom: 50, left: 50 },
            xAxis: {
                type: 'category',
                data: coachNames,
                axisLine: { lineStyle: { color: 'rgba(255,255,255,0.1)' } },
                axisLabel: { color: '#94a3b8', fontSize: 10, interval: 0, rotate: 25 }
            },
            yAxis: {
                type: 'value',
                axisLine: { show: false },
                splitLine: { lineStyle: { color: 'rgba(255,255,255,0.05)' } },
                axisLabel: { color: '#94a3b8', fontSize: 10 }
            },
            series: [{
                type: 'bar',
                data: coachCounts,
                barWidth: '45%',
                itemStyle: {
                    borderRadius: [4, 4, 0, 0],
                    color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                        { offset: 0, color: '#f59e0b' },
                        { offset: 1, color: '#d97706' }
                    ])
                }
            }]
        });
        chartInstances.coachRank = chartRank;

        var enrollMonth = detail.enrollMonth || {};
        var emLabels = Object.keys(enrollMonth);
        var emValues = Object.values(enrollMonth);
        if (emLabels.length === 0) { emLabels = ['近6个月']; emValues = [0]; }
        var chartEM = echarts.init(document.getElementById('chartEnrollMonth'));
        chartEM.setOption({
            backgroundColor: 'transparent',
            textStyle: { color: '#94a3b8', fontSize: 11 },
            tooltip: {
                trigger: 'axis',
                backgroundColor: '#1e293b',
                borderColor: 'rgba(255,255,255,0.1)',
                textStyle: { color: '#e2e8f0' }
            },
            grid: { top: 30, right: 20, bottom: 30, left: 40 },
            xAxis: {
                type: 'category',
                data: emLabels,
                axisLine: { lineStyle: { color: 'rgba(255,255,255,0.1)' } },
                axisLabel: { color: '#94a3b8', fontSize: 10 }
            },
            yAxis: {
                type: 'value',
                axisLine: { show: false },
                splitLine: { lineStyle: { color: 'rgba(255,255,255,0.05)' } },
                axisLabel: { color: '#94a3b8', fontSize: 10 }
            },
            series: [{
                type: 'line',
                data: emValues,
                smooth: true,
                symbol: 'circle',
                symbolSize: 6,
                lineStyle: { width: 3, color: '#a78bfa' },
                itemStyle: { color: '#a78bfa' },
                areaStyle: {
                    color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                        { offset: 0, color: 'rgba(167,139,250,0.3)' },
                        { offset: 1, color: 'rgba(167,139,250,0.01)' }
                    ])
                }
            }]
        });
        chartInstances.enrollMonth = chartEM;

        var bookingStatus = detail.bookingStatus || {};
        var statusMap = { 'approved': '已通过', 'pending': '待审核', 'rejected': '已拒绝' };
        var statusLabels = [];
        var statusValues = [];
        for (var key in bookingStatus) {
            statusLabels.push(statusMap[key] || key);
            statusValues.push(bookingStatus[key]);
        }
        if (statusLabels.length === 0) { statusLabels = ['暂无数据']; statusValues = [0]; }
        var chartBS = echarts.init(document.getElementById('chartBookingStatus'));
        chartBS.setOption({
            backgroundColor: 'transparent',
            textStyle: { color: '#94a3b8', fontSize: 11 },
            tooltip: {
                trigger: 'item',
                backgroundColor: '#1e293b',
                borderColor: 'rgba(255,255,255,0.1)',
                textStyle: { color: '#e2e8f0' }
            },
            series: [{
                type: 'pie',
                radius: ['40%', '70%'],
                center: ['50%', '55%'],
                itemStyle: { borderRadius: 6, borderColor: '#1e293b', borderWidth: 2 },
                label: { color: '#94a3b8', fontSize: 11, formatter: '{b}: {c} ({d}%)' },
                data: statusLabels.map(function(label, index) {
                    return { name: label, value: statusValues[index] };
                }),
                color: ['#34d399', '#fbbf24', '#f87171']
            }]
        });
        chartInstances.bookingStatus = chartBS;
    });
};

window.addEventListener('resize', function() {
    Object.values(chartInstances).forEach(function(c) { c && c.resize(); });
});

window.logout = function() {
    if (confirm('确定退出登录吗？')) {
        localStorage.removeItem('userInfo');
        window.location.href = BASE_URL + '/logout';
    }
};

