// admin.js - 管理员后台页面逻辑

var BASE_URL = window.BASE_URL || '';

// 页面加载完成后初始化动画
document.addEventListener('DOMContentLoaded', function() {
    initParticleCanvas();
    initRevealAnimations();
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
    
    // 表格行动画
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
    axios.get(BASE_URL + url, { responseType: 'json' })
        .then(function(res) {
            var data = res.data;
            console.log('API响应:', data);
            if (data && data.code === 1) {
                callback(data.data || data);
            } else {
                alert(data ? (data.msg || '请求失败') : '请求失败');
            }
        })
        .catch(function(err) {
            console.error('请求失败:', err);
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

    // 找到点击的导航项并高亮（通过onclick属性匹配）
    document.querySelectorAll('.sidebar-nav a').forEach(function(el) {
        if (el.getAttribute('onclick') && el.getAttribute('onclick').includes("'" + tabName + "'")) {
            el.classList.add('active');
        }
    });

    // 切换Tab后重新触发动画
    setTimeout(function() {
        var elements = targetTab.querySelectorAll('.stat-card, .chart-box, .section');
        elements.forEach(function(el, index) {
            el.classList.remove('reveal');
            setTimeout(function() {
                el.classList.add('reveal');
            }, index * 80);
        });
        var rows = targetTab.querySelectorAll('tbody tr');
        rows.forEach(function(row, index) {
            row.classList.remove('reveal');
            setTimeout(function() {
                row.classList.add('reveal');
            }, index * 60);
        });
    }, 100);

    // 加载对应数据
    if (tabName === 'staff') loadStaffList();
    if (tabName === 'students') loadStudents();
    if (tabName === 'enrollments') loadEnrollments();
    if (tabName === 'bookings') loadBookings();
    if (tabName === 'dashboard') loadDashboard();
};

// ==================== 员工管理 ====================
window.loadStaffList = function() {
    var container = document.getElementById('staffList');
    container.innerHTML = '<div class="empty-tip loading">加载中...</div>';

    apiGet('/admin/staff?action=list', function(list) {
        if (!list || list.length === 0) {
            container.innerHTML = '<div class="empty-tip">暂无员工数据</div>';
            return;
        }
        var html = '<table class="data-table"><thead><tr>' +
            '<th>姓名</th><th>手机号</th><th>角色</th><th>科目</th><th>操作</th>' +
            '</tr></thead><tbody>';
        list.forEach(function(item) {
            var roleText = item.role === 'admin' ? '管理员' : '教练';
            var subjectText = item.subject || '-';
            html += '<tr>' +
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
    });
};

window.openStaffModal = function() {
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
    var container = document.getElementById('studentList');
    container.innerHTML = '<div class="empty-tip loading">加载中...</div>';

    apiGet('/admin/query?action=students', function(data) {
        if (!data || data.length === 0) {
            container.innerHTML = '<div class="empty-tip">暂无学员数据</div>';
            return;
        }
        var html = '<table class="data-table"><thead><tr>' +
            '<th>姓名</th><th>手机号</th><th>身份证号</th><th>科目</th>' +
            '</tr></thead><tbody>';
        data.forEach(function(item) {
            html += '<tr>' +
                '<td>' + (item.name || '') + '</td>' +
                '<td>' + (item.phone || '') + '</td>' +
                '<td>' + (item.idCard || '') + '</td>' +
                '<td>' + (item.subject || '-') + '</td>' +
                '</tr>';
        });
        html += '</tbody></table>';
        container.innerHTML = html;
    });
};

// ==================== 报名审核 ====================
window.loadEnrollments = function() {
    var container = document.getElementById('enrollmentList');
    if (!container) return;
    container.innerHTML = '<div class="empty-tip loading">加载中...</div>';

    apiGet('/admin/query?action=enrollments', function(data) {
        if (!data || data.length === 0) {
            container.innerHTML = '<div class="empty-tip">暂无报名记录</div>';
            return;
        }
        var html = '<table class="data-table"><thead><tr>' +
            '<th>学员</th><th>教练</th><th>科目</th><th>状态</th><th>操作</th>' +
            '</tr></thead><tbody>';
        data.forEach(function(item) {
            var statusText = { pending: '待审核', approved: '已通过', rejected: '已拒绝' }[item.status] || item.status;
            html += '<tr>' +
                '<td>' + (item.studentName || '') + '</td>' +
                '<td>' + (item.coachName || '') + '</td>' +
                '<td>' + (item.subjectType || '') + '</td>' +
                '<td>' + statusText + '</td>' +
                '<td>' +
                    (item.status === 'pending' ?
                        '<button class="btn btn-sm btn-primary" onclick="window.auditEnrollment(\'' + item.id + '\', \'approved\')" style="margin-right:5px">通过</button>' +
                        '<button class="btn btn-sm btn-danger" onclick="window.auditEnrollment(\'' + item.id + '\', \'rejected\')">拒绝</button>'
                        : '-') +
                '</td>' +
                '</tr>';
        });
        html += '</tbody></table>';
        container.innerHTML = html;
    });
};

window.auditEnrollment = function(id, status) {
    var params = new URLSearchParams();
    params.append('id', id);
    params.append('status', status);
    apiPost('/admin/enrollment', params.toString(), function() {
        window.loadEnrollments();
    });
};

function formatDateTime(timestamp) {
    if (!timestamp) return '-';
    var date = new Date(timestamp);
    var pad = function(n) { return n < 10 ? '0' + n : n; };
    return date.getFullYear() + '-' + pad(date.getMonth() + 1) + '-' + pad(date.getDate()) + ' ' +
           pad(date.getHours()) + ':' + pad(date.getMinutes());
}

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
    // 1. 加载基础统计卡片
    apiGet('/admin/query?action=stats', function(data) {
        if (!data) return;
        // 使用数字滚动动画
        animateValue(document.getElementById('statStudents'), 0, data.totalStudents || 0, 1000);
        animateValue(document.getElementById('statCoaches'), 0, data.totalCoaches || 0, 1000);
        animateValue(document.getElementById('statPendingEnroll'), 0, data.pendingEnrollments || 0, 1000);
        animateValue(document.getElementById('statApprovedEnroll'), 0, data.approvedEnrollments || 0, 1000);
        animateValue(document.getElementById('statBookings'), 0, data.totalBookings || 0, 1000);
        animateValue(document.getElementById('statApprovedBook'), 0, data.approvedBookings || 0, 1000);
    });

    // 2. 加载图表详细数据
    apiGet('/admin/query?action=statsDetail', function(detail) {
        if (!detail) return;

        // 学员科目分布饼图
        var ss = detail.studentSubject || {};
        var ssLabels = Object.keys(ss);
        var ssValues = Object.values(ss);
        if (ssLabels.length === 0) { ssLabels = ['暂无数据']; ssValues = [0]; }
        var chartSS = echarts.init(document.getElementById('chartStudentSubject'));
        chartSS.setOption(getChartOption(null, 'pie', ssLabels, ssValues, ['#60a5fa', '#34d399', '#fbbf24']));
        chartInstances.studentSubject = chartSS;

        // 教练科目分布饼图
        var cs = detail.coachSubject || {};
        var csLabels = Object.keys(cs);
        var csValues = Object.values(cs);
        if (csLabels.length === 0) { csLabels = ['暂无数据']; csValues = [0]; }
        var chartCS = echarts.init(document.getElementById('chartCoachSubject'));
        chartCS.setOption(getChartOption(null, 'pie', csLabels, csValues, ['#f87171', '#a78bfa', '#fbbf24']));
        chartInstances.coachSubject = chartCS;

        // 预约趋势折线图
        var bm = detail.bookingMonth || {};
        var bmLabels = Object.keys(bm);
        var bmValues = Object.values(bm);
        if (bmLabels.length === 0) { bmLabels = ['近6个月']; bmValues = [0]; }
        var chartBM = echarts.init(document.getElementById('chartBookingMonth'));
        chartBM.setOption(getChartOption(null, 'line', bmLabels, bmValues));
        chartInstances.bookingMonth = chartBM;

        // 评分分布柱状图（合并学员评分和教练评分）
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
    });
};

// 窗口大小变化时重绘图表
window.addEventListener('resize', function() {
    Object.values(chartInstances).forEach(function(c) { c && c.resize(); });
});

// ==================== 初始化 ====================
document.addEventListener('DOMContentLoaded', function() {
    window.loadDashboard();
    window.showTab('dashboard');
});
