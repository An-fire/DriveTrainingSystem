// admin.js - 管理员后台页面逻辑

var BASE_URL = window.BASE_URL || '';

function apiGet(url, callback) {
    axios.get(BASE_URL + url)
        .then(function(res) {
            if (res.data.code === 1) {
                callback(res.data.data || res.data);
            } else {
                alert(res.data.msg || '请求失败');
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
    container.innerHTML = '<div class="empty-tip">加载中...</div>';

    apiGet('/admin/staff?action=list', function(data) {
        if (!data || data.length === 0) {
            container.innerHTML = '<div class="empty-tip">暂无员工数据</div>';
            return;
        }
        var html = '<table class="data-table"><thead><tr>' +
            '<th>姓名</th><th>手机号</th><th>角色</th><th>科目</th><th>操作</th>' +
            '</tr></thead><tbody>';
        data.forEach(function(item) {
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
    var group = document.getElementById('modalSubjectGroup');
    if (group) {
        group.style.display = (role === 'coach') ? 'block' : 'none';
    }
};

window.saveStaff = function() {
    var id = document.getElementById('editStaffId').value;
    var name = document.getElementById('modalStaffName').value.trim();
    var phone = document.getElementById('modalStaffPhone').value.trim();
    var role = document.getElementById('modalStaffRole').value;
    var subject = document.getElementById('modalStaffSubject').value;
    var password = document.getElementById('modalStaffPassword').value;

    if (!name || !phone) {
        alert('请填写姓名和手机号');
        return;
    }

    var params = new URLSearchParams();
    params.append('name', name);
    params.append('phone', phone);
    params.append('role', role);
    params.append('subject', role === 'coach' ? subject : '');
    if (password) params.append('password', password);

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
    container.innerHTML = '<div class="empty-tip">加载中...</div>';

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
    container.innerHTML = '<div class="empty-tip">加载中...</div>';

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

// ==================== 练车记录 ====================
window.loadBookings = function() {
    var container = document.getElementById('bookingList');
    if (!container) return;
    container.innerHTML = '<div class="empty-tip">加载中...</div>';

    apiGet('/admin/query?action=bookings', function(data) {
        if (!data || data.length === 0) {
            container.innerHTML = '<div class="empty-tip">暂无练车记录</div>';
            return;
        }
        var html = '<table class="data-table"><thead><tr>' +
            '<th>学员</th><th>教练</th><th>科目</th><th>时间</th><th>状态</th><th>学员评分</th><th>教练评分</th>' +
            '</tr></thead><tbody>';
        data.forEach(function(item) {
            var statusText = { approved: '已通过', rejected: '已拒绝' }[item.status] || item.status;
            html += '<tr>' +
                '<td>' + (item.studentName || '') + '</td>' +
                '<td>' + (item.coachName || '') + '</td>' +
                '<td>' + (item.subjectType || '') + '</td>' +
                '<td>' + (item.startTime || '') + '</td>' +
                '<td>' + statusText + '</td>' +
                '<td>' + (item.studentScore || '-') + '</td>' +
                '<td>' + (item.coachScore || '-') + '</td>' +
                '</tr>';
        });
        html += '</tbody></table>';
        container.innerHTML = html;
    });
};

// ==================== 数据概览 ====================
window.loadDashboard = function() {
    apiGet('/admin/query?action=stats', function(data) {
        if (!data) return;
        var elTotal = document.getElementById('statTotalStudents');
        var elCoaches = document.getElementById('statTotalCoaches');
        var elBookings = document.getElementById('statTotalBookings');
        var elPending = document.getElementById('statPendingEnrollments');
        if (elTotal) elTotal.innerText = data.totalStudents || 0;
        if (elCoaches) elCoaches.innerText = data.totalCoaches || 0;
        if (elBookings) elBookings.innerText = data.totalBookings || 0;
        if (elPending) elPending.innerText = data.pendingEnrollments || 0;
    });
};

// ==================== 初始化 ====================
document.addEventListener('DOMContentLoaded', function() {
    window.loadDashboard();
    window.showTab('dashboard');
});
