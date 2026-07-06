// admin.js - 管理员后台页面逻辑

var BASE_URL = window.BASE_URL || '';

var pageDataCache = {
    staff: null,
    enrollments: null,
    drivingApplications: null,
    bookings: null
};

var isLoading = {
    staff: false,
    enrollments: false,
    drivingApplications: false,
    bookings: false
};

// 页面加载完成后初始化动画
document.addEventListener('DOMContentLoaded', function() {
    initParticleCanvas();
    initRevealAnimations();
    var userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
    if (userInfo.name) {
        var initial = userInfo.name.charAt(0).toUpperCase();
        document.getElementById('adminAvatar').textContent = initial;
        document.getElementById('adminNameText').textContent = userInfo.name;
    }
    window.showTab('enrollments');
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
    var elements = document.querySelectorAll('.section');
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
    axios.get(BASE_URL + url, { responseType: 'json' })
        .then(function(res) {
            var data = res.data;
            if (data && data.code === 1) {
                var resultData = data.data;
                if (resultData === null || resultData === undefined) {
                    resultData = [];
                }
                callback(resultData);
            } else {
                alert(data ? (data.msg || '请求失败') : '请求失败');
            }
        })
        .catch(function(err) {
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
        'enrollments': '报名审核',
        'drivingApplications': '练车申请',
        'bookings': '练车记录',
        'staff': '员工管理'
    };
    document.getElementById('pageTitle').innerText = titleMap[tabName] || '报名审核';

    document.querySelectorAll('.sidebar-nav a').forEach(function(el) {
        if (el.getAttribute('onclick') && el.getAttribute('onclick').includes("'" + tabName + "'")) {
            el.classList.add('active');
        }
    });

    setTimeout(function() {
        var elements = targetTab.querySelectorAll('.section');
        elements.forEach(function(el, index) {
            el.classList.remove('reveal');
            setTimeout(function() {
                el.classList.add('reveal');
            }, index * 30);
        });
    }, 50);

    if (tabName === 'staff') window.loadStaffList(true);
    if (tabName === 'enrollments') {
        window.loadPendingEnrollments(true);
        window.loadAllEnrollments(true);
    }
    if (tabName === 'drivingApplications') window.loadDrivingApplications(true);
    if (tabName === 'bookings') window.loadBookings(true);
};

// ==================== 员工管理 ====================
window.loadStaffList = function(hasCache) {
    var container = document.getElementById('staffList');
    if (!container) return;

    if (hasCache && pageDataCache.staff && !isLoading.staff) {
        renderStaffList(pageDataCache.staff);
        return;
    }

    if (isLoading.staff) return;
    isLoading.staff = true;

    container.innerHTML = '<div class="empty-tip loading">加载中...</div>';

    apiGet('/admin/staff?action=list', function(list) {
        isLoading.staff = false;
        pageDataCache.staff = list;
        renderStaffList(list);
    });
};

function renderStaffList(list) {
    var container = document.getElementById('staffList');
    if (!list || list.length === 0) {
        container.innerHTML = '<div class="empty-tip">暂无员工数据</div>';
        return;
    }
    var html = '<table class="data-table"><thead><tr>' +
        '<th>姓名</th><th>手机号</th><th>角色</th><th>科目</th><th>操作</th>' +
        '</tr></thead><tbody>';
    list.forEach(function(item, index) {
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
        pageDataCache.staff = null;
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
        document.getElementById('modalStaffUsb').value = data.usbToken || '';
        document.getElementById('staffModalTitle').innerText = '编辑员工';
        document.getElementById('modalPasswordGroup').style.display = 'block';
        window.toggleSubjectField();
        document.getElementById('staffModal').classList.add('show');
    });
};

window.deleteStaff = function(id) {
    if (!confirm('确定删除该员工吗？')) return;
    apiPost('/admin/staff?action=delete&id=' + id, '', function() {
        pageDataCache.staff = null;
        window.loadStaffList();
    });
};

// ==================== 报名审核 ====================
window.loadPendingEnrollments = function(hasCache) {
    var container = document.getElementById('pendingList');
    if (!container) return;

    if (hasCache && pageDataCache.enrollments && !isLoading.enrollments) {
        renderPendingEnrollments(pageDataCache.enrollments);
        return;
    }

    if (isLoading.enrollments) return;
    isLoading.enrollments = true;

    container.innerHTML = '<div class="empty-tip loading">加载中...</div>';
    apiGet('/admin/query?action=enrollments', function(data) {
        isLoading.enrollments = false;
        pageDataCache.enrollments = data;
        console.log('报名数据:', data);
        renderPendingEnrollments(data);
    });
};

function renderPendingEnrollments(data) {
    var container = document.getElementById('pendingList');
    if (!data || data.length === 0) {
        container.innerHTML = '<div class="empty-tip">暂无待审核报名</div>';
        return;
    }
    var pending = data.filter(function(item) { return item.status === 'pending'; });

    if (pending.length > 0) {
        var badge = document.getElementById('pendingCountBadge');
        if (badge) {
            badge.textContent = pending.length + ' 人待审';
            badge.style.display = 'inline';
        }
    } else {
        var badge = document.getElementById('pendingCountBadge');
        if (badge) badge.style.display = 'none';
    }

    if (pending.length === 0) {
        container.innerHTML = '<div class="empty-tip">暂无待审核报名</div>';
        return;
    }

    var html = '<div class="card-grid">';
    pending.forEach(function(item, index) {
        var studentName = item.studentName || '未知学员';
        var phone = item.studentPhone || '-';
        var idCard = item.studentIdCard || '-';
        var subjectType = item.subjectType || '-';
        var coachName = item.coachName || '未知教练';
        var applyTime = item.applyTimeStr || '-';
        var initial = studentName.charAt(0).toUpperCase();

        html += '<div class="enrollment-card reveal">' +
            '<span class="status-pending">待审核</span>' +
            '<div class="card-header">' +
            '<div class="avatar">' + initial + '</div>' +
            '<div class="student-info">' +
            '<div class="student-name">' + escapeHtml(studentName) + '</div>' +
            '<div class="student-phone">📞 ' + escapeHtml(phone) + '</div>' +
            '</div></div>' +
            '<div class="info-row"><span class="info-label">身份证</span><span class="info-value">' + escapeHtml(idCard) + '</span></div>' +
            '<div class="info-row"><span class="info-label">科目</span><span class="info-value subject">' + escapeHtml(subjectType) + '</span></div>' +
            '<div class="info-row"><span class="info-label">教练</span><span class="info-value">' + escapeHtml(coachName) + '</span></div>' +
            '<div class="info-row"><span class="info-label">时间</span><span class="info-value">' + escapeHtml(applyTime) + '</span></div>' +
            '<div class="card-actions">' +
            '<button class="btn btn-approve" onclick="window.auditEnrollment(\'' + item.id + '\', \'approved\')">✅ 通过</button>' +
            '<button class="btn btn-reject" onclick="window.auditEnrollment(\'' + item.id + '\', \'rejected\')">❌ 拒绝</button>' +
            '</div></div>';
    });
    html += '</div>';
    container.innerHTML = html;
};

window.toggleAllEnrollment = function(master) {
    document.querySelectorAll('.enroll-checkbox').forEach(function(cb) {
        cb.checked = master.checked;
    });
};

window.loadAllEnrollments = function(hasCache) {
    var container = document.getElementById('allEnrollmentList');
    if (!container) return;

    if (hasCache && pageDataCache.enrollments && !isLoading.enrollments) {
        renderAllEnrollments(pageDataCache.enrollments);
        return;
    }

    if (isLoading.enrollments) return;

    container.innerHTML = '<div class="empty-tip loading">加载中...</div>';
    apiGet('/admin/query?action=enrollments', function(data) {
        pageDataCache.enrollments = data;
        renderAllEnrollments(data);
    });
};

function renderAllEnrollments(data) {
    var container = document.getElementById('allEnrollmentList');
    if (!data || data.length === 0) {
        container.innerHTML = '<div class="empty-tip">暂无报名记录</div>';
        return;
    }
    var html = '<table class="data-table"><thead><tr>' +
        '<th>学员</th><th>教练</th><th>科目</th><th>状态</th>' +
        '</tr></thead><tbody>';
    data.forEach(function(item, index) {
        var statusText = { pending: '待审核', approved: '已通过', rejected: '已拒绝' }[item.status] || item.status;
        html += '<tr class="reveal" style="transition-delay: ' + (index * 60) + 'ms">' +
            '<td>' + (item.studentName || '') + '</td>' +
            '<td>' + (item.coachName || '') + '</td>' +
            '<td>' + (item.subjectType || '') + '</td>' +
            '<td>' + statusText + '</td>' +
            '</tr>';
    });
    html += '</tbody></table>';
    container.innerHTML = html;
};

window.auditEnrollment = function(id, status) {
    var remark = prompt(status === 'approved' ? '请输入通过意见（可选）：' : '请输入拒绝原因（可选）：');
    var params = new URLSearchParams();
    params.append('action', 'single');
    params.append('enrollmentId', id);
    params.append('status', status);
    if (remark) params.append('remark', remark);
    apiPost('/admin/audit', params.toString(), function() {
        pageDataCache.enrollments = null;
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
        pageDataCache.enrollments = null;
        loadPendingEnrollments();
        loadAllEnrollments();
    });
};

// ==================== 练车申请记录 ====================
window.loadDrivingApplications = function(hasCache) {
    var container = document.getElementById('drivingApplicationList');
    if (!container) return;

    var studentName = document.getElementById('appStudentName') ? document.getElementById('appStudentName').value : '';
    var status = document.getElementById('appStatus') ? document.getElementById('appStatus').value : '';
    var startDate = document.getElementById('appStartDate') ? document.getElementById('appStartDate').value : '';
    var endDate = document.getElementById('appEndDate') ? document.getElementById('appEndDate').value : '';

    if (hasCache && !studentName && !status && !startDate && !endDate && pageDataCache.drivingApplications && !isLoading.drivingApplications) {
        renderDrivingApplications(pageDataCache.drivingApplications);
        return;
    }

    if (isLoading.drivingApplications) return;
    isLoading.drivingApplications = true;

    container.innerHTML = '<div class="empty-tip loading">加载中...</div>';

    var params = [];
    if (studentName) params.push('studentName=' + encodeURIComponent(studentName));
    if (status) params.push('status=' + encodeURIComponent(status));
    if (startDate) params.push('startDate=' + encodeURIComponent(startDate));
    if (endDate) params.push('endDate=' + encodeURIComponent(endDate));
    var queryString = params.length > 0 ? '&' + params.join('&') : '';

    apiGet('/admin/query?action=drivingApplications' + queryString, function(data) {
        isLoading.drivingApplications = false;
        if (!studentName && !status && !startDate && !endDate) {
            pageDataCache.drivingApplications = data;
        }
        renderDrivingApplications(data);
    });
};

function renderDrivingApplications(data) {
    var container = document.getElementById('drivingApplicationList');
    if (!container) return;

    var list = [];
    if (Array.isArray(data)) {
        list = data;
    } else if (data && typeof data === 'object') {
        list = data.data || [];
        if (data.pendingCount !== undefined && data.pendingCount > 0) {
            var badge = document.getElementById('pendingApplicationBadge');
            if (badge) {
                badge.textContent = data.pendingCount + ' 待审核';
                badge.style.display = 'inline';
            }
        }
    }

    if (list.length === 0) {
        container.innerHTML = '<div class="empty-tip">暂无练车申请记录</div>';
        return;
    }

    try {
        var html = '<table class="data-table"><thead><tr>' +
            '<th>申请ID</th><th>学员姓名</th><th>教练</th><th>科目</th>' +
            '<th>申请日期</th><th>练车时段</th><th>状态</th>' +
            '<th>可考试</th>' +
            '</tr></thead><tbody>';

        list.forEach(function(item, index) {
            var statusText = { pending: '待审核', approved: '已批准', rejected: '已拒绝' }[item.status] || item.status || '-';
            var statusClass = { pending: 'status-pending', approved: 'status-approved', rejected: 'status-rejected' }[item.status] || '';
            var studentName = item.studentName || (item.studentId ? item.studentId.substring(0, 8) : '');
            var coachName = item.coachName || (item.coachId ? item.coachId.substring(0, 8) : '');
            var createTime = item.createTimeStr || (item.createTime ? formatDateTime(item.createTime) : '-');
            var timeSlot = (item.startTimeStr || '-') + ' 至 ' + (item.endTimeStr || '-');
            var canExam = item.canExam ? '是' : '否';

            html += '<tr class="reveal" style="transition-delay: ' + (index * 40) + 'ms">' +
                '<td>' + escapeHtml(item.id || '-') + '</td>' +
                '<td>' + escapeHtml(studentName) + '</td>' +
                '<td>' + escapeHtml(coachName) + '</td>' +
                '<td>' + escapeHtml(convertBookingSubject(item.subjectType) || '-') + '</td>' +
                '<td>' + createTime + '</td>' +
                '<td>' + timeSlot + '</td>' +
                '<td><span class="status-badge ' + statusClass + '">' + statusText + '</span></td>' +
                '<td>' + canExam + '</td>' +
                '</tr>';
        });
        html += '</tbody></table>';
        container.innerHTML = html;
    } catch (e) {
        console.error('[renderDrivingApplications] 渲染出错:', e);
        container.innerHTML = '<div class="empty-tip">数据渲染出错，请检查控制台</div>';
    }
};

// 清空筛选条件
window.clearDrivingApplicationFilters = function() {
    var nameInput = document.getElementById('appStudentName');
    var statusSelect = document.getElementById('appStatus');
    var startDateInput = document.getElementById('appStartDate');
    var endDateInput = document.getElementById('appEndDate');
    if (nameInput) nameInput.value = '';
    if (statusSelect) statusSelect.value = '';
    if (startDateInput) startDateInput.value = '';
    if (endDateInput) endDateInput.value = '';
    window.loadDrivingApplications();
};

// ==================== 练车记录 ====================
window.loadBookings = function(hasCache) {
    var container = document.getElementById('bookingList');
    if (!container) return;

    if (hasCache && pageDataCache.bookings && !isLoading.bookings) {
        renderBookings(pageDataCache.bookings);
        return;
    }

    if (isLoading.bookings) return;
    isLoading.bookings = true;

    container.innerHTML = '<div class="empty-tip loading">加载中...</div>';

    apiGet('/admin/query?action=bookings', function(data) {
        isLoading.bookings = false;
        pageDataCache.bookings = data;
        renderBookings(data);
    });
};

function renderBookings(data) {
    var container = document.getElementById('bookingList');
    if (!container) return;

    var list = [];
    if (Array.isArray(data)) {
        list = data;
    } else if (data && typeof data === 'object') {
        list = data.data || data.list || data.bookings || data.rows || [];
        if (!Array.isArray(list)) {
            list = [];
        }
    }

    if (list.length === 0) {
        container.innerHTML = '<div class="empty-tip">暂无练车记录</div>';
        return;
    }

    try {
        var html = '<table class="data-table"><thead><tr>' +
            '<th>学员</th><th>教练</th><th>科目</th><th>开始时间</th><th>结束时间</th><th>状态</th>' +
            '<th>学员评分</th><th>教练评分</th><th>评价</th>' +
            '</tr></thead><tbody>';

        list.forEach(function(item, index) {
            var statusText = { approved: '已通过', rejected: '已拒绝', pending: '待审核' }[item.status] || item.status || '-';
            var studentName = item.studentName || (item.studentId ? item.studentId.substring(0, 8) : '');
            var coachName = item.coachName || (item.coachId ? item.coachId.substring(0, 8) : '');
            var startTime = item.startTime ? formatDateTime(item.startTime) : '-';
            var endTime = item.endTime ? formatDateTime(item.endTime) : '-';
            var studentScore = (item.studentScore && item.studentScore > 0) ? item.studentScore + '⭐' : '-';
            var coachScore = (item.coachScore && item.coachScore > 0) ? item.coachScore + '⭐' : '-';

            html += '<tr class="reveal">' +
                '<td>' + escapeHtml(studentName) + '</td>' +
                '<td>' + escapeHtml(coachName) + '</td>' +
                '<td>' + escapeHtml(convertBookingSubject(item.subjectType) || '-') + '</td>' +
                '<td>' + startTime + '</td>' +
                '<td>' + endTime + '</td>' +
                '<td>' + statusText + '</td>' +
                '<td>' + studentScore + '</td>' +
                '<td>' + coachScore + '</td>' +
                '<td>' + escapeHtml(item.comment || '-') + '</td>' +
                '</tr>';
        });
        html += '</tbody></table>';
        container.innerHTML = html;
    } catch (e) {
        console.error('[renderBookings] 渲染出错:', e);
        container.innerHTML = '<div class="empty-tip">数据渲染出错，请检查控制台</div>';
    }
};

// XSS防护函数
function escapeHtml(text) {
    if (!text) return '';
    var div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

function formatDateTime(timestamp) {
    if (!timestamp) return '-';
    var date = new Date(timestamp);
    var pad = function(n) { return n < 10 ? '0' + n : n; };
    return date.getFullYear() + '-' + pad(date.getMonth() + 1) + '-' + pad(date.getDate()) + ' ' +
        pad(date.getHours()) + ':' + pad(date.getMinutes());
}

window.logout = function() {
    if (confirm('确定退出登录吗？')) {
        localStorage.removeItem('userInfo');
        window.location.href = BASE_URL + '/logout';
    }
};

