
// 使用组长 common.js 定义的 BASE_URL
var BASE_URL = window.BASE_URL || '';

// ============================================================
// 1. 获取当前登录教练的 ID（从 localStorage 读取）
// ============================================================
function getCoachId() {
    var userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
    var coachId = userInfo.id || null;
    if (!coachId) {
        console.warn('未找到教练ID，请先登录');
    }
    return coachId;
}

// ============================================================
// 2. 加载待评分记录
// ============================================================
function loadPendingScores() {
    var coachId = getCoachId();
    if (!coachId) {
        document.getElementById('pendingBody').innerHTML =
            '<tr><td colspan="5" style="color:#e74c3c;">❌ 未登录或登录信息失效，请重新登录</td></tr>';
        return;
    }

    var url = BASE_URL + '/coach/pending?coachId=' + encodeURIComponent(coachId);
    axios.get(url)
        .then(function(response) {
            var result = response.data;
            if (result.code === 200) {
                var records = result.data || [];
                renderPendingTable(records);
            } else {
                document.getElementById('pendingBody').innerHTML =
                    '<tr><td colspan="5" style="color:#e74c3c;">加载失败：' + (result.message || '未知错误') + '</td></tr>';
            }
        })
        .catch(function(error) {
            console.error('加载待评分记录失败:', error);
            // 如果接口未就绪，显示友好提示
            document.getElementById('pendingBody').innerHTML =
                '<tr><td colspan="5" style="color:#95a5a6;">⏳ 暂无待评分记录（或后端接口未就绪）</td></tr>';
        });
}

// ============================================================
// 3. 渲染待评分表格
// ============================================================
function renderPendingTable(records) {
    var tbody = document.getElementById('pendingBody');
    if (!tbody) return;

    if (!records || records.length === 0) {
        tbody.innerHTML = '<tr><td colspan="5" style="color:#95a5a6;text-align:center;">🎉 暂无待评分记录</td></tr>';
        return;
    }

    var html = '';
    records.forEach(function(rec) {
        // 使用 bookingId 作为行标识
        var rowId = 'row_' + rec.bookingId;
        html += '<tr id="' + rowId + '">' +
            '<td>' + escapeHtml(rec.studentName || '未知学员') + '</td>' +
            '<td>' + escapeHtml(rec.timeSlot || '—') + '</td>' +
            '<td>' +
                '<input type="number" id="score_' + rec.bookingId + '" min="1" max="5" step="1" value="3" style="width:60px;padding:4px 8px;border-radius:6px;border:1px solid #d1d5db;">' +
            '</td>' +
            '<td>' +
                '<input type="checkbox" id="exam_' + rec.bookingId + '" style="width:18px;height:18px;">' +
            '</td>' +
            '<td>' +
                '<button class="btn btn-primary" onclick="submitScore(\'' + rec.bookingId + '\')">提交评分</button>' +
            '</td>' +
        '</tr>';
    });
    tbody.innerHTML = html;
}

// ============================================================
// 4. 提交评分（核心接口）
// ============================================================
window.submitScore = function(bookingId) {
    var scoreInput = document.getElementById('score_' + bookingId);
    var examCheck = document.getElementById('exam_' + bookingId);

    if (!scoreInput) {
        alert('系统错误：找不到评分输入框');
        return;
    }

    var score = parseInt(scoreInput.value, 10);
    var canExam = examCheck ? examCheck.checked : false;

    // 前端校验
    if (isNaN(score) || score < 1 || score > 5) {
        alert('请输入 1-5 之间的有效分数');
        return;
    }

    // 获取教练 ID
    var coachId = getCoachId();
    if (!coachId) {
        alert('登录信息失效，请重新登录');
        return;
    }

    // 发送请求（参数名使用组长约定的字段）
    var url = BASE_URL + '/coach/score';
    axios.post(url, {
        bookingId: bookingId,
        coachId: coachId,          // 后端可能需要校验教练身份
        score: score,              // 注意：字段名是 score，不是 coachScore
        canExam: canExam
    })
    .then(function(response) {
        var result = response.data;
        if (result.code === 200) {
            alert('✅ 评分成功！' + (canExam ? ' 该学员已获得考试资格。' : ''));
            // 刷新列表（移除已评分的行或重新加载）
            loadPendingScores();
            // 可选：刷新评价列表
            loadComments();
        } else {
            alert('❌ 评分失败：' + (result.message || '未知错误'));
        }
    })
    .catch(function(error) {
        console.error('提交评分失败:', error);
        // 尝试从 error 中提取更具体的信息
        var errMsg = '网络错误，请稍后重试';
        if (error.response && error.response.data && error.response.data.message) {
            errMsg = error.response.data.message;
        }
        alert('❌ 评分失败：' + errMsg);
    });
};

// ============================================================
// 5. 加载学员对教练的评价
// ============================================================
function loadComments() {
    var coachId = getCoachId();
    if (!coachId) {
        document.getElementById('commentList').innerHTML = '<li style="color:#95a5a6;">请先登录</li>';
        return;
    }

    var url = BASE_URL + '/coach/comments?coachId=' + encodeURIComponent(coachId);
    axios.get(url)
        .then(function(response) {
            var result = response.data;
            var listEl = document.getElementById('commentList');
            if (!listEl) return;

            if (result.code === 200) {
                var comments = result.data || [];
                renderCommentList(comments);
            } else {
                listEl.innerHTML = '<li style="color:#e74c3c;">加载失败：' + (result.message || '未知错误') + '</li>';
            }
        })
        .catch(function(error) {
            console.error('加载评价失败:', error);
            document.getElementById('commentList').innerHTML =
                '<li style="color:#95a5a6;">⏳ 暂无评价（或后端接口未就绪）</li>';
        });
}

// ============================================================
// 6. 渲染评价列表
// ============================================================
function renderCommentList(comments) {
    var listEl = document.getElementById('commentList');
    if (!listEl) return;

    if (!comments || comments.length === 0) {
        listEl.innerHTML = '<div style="text-align:center; padding:20px 0; color:#94a3b8;">暂无学员评价</div>';
        return;
    }

    var html = '';
    comments.forEach(function(c) {
        var stars = '⭐'.repeat(c.studentScore || 0) + '☆'.repeat(5 - (c.studentScore || 0));
        var commentText = c.comment && c.comment.trim() !== '' ? c.comment : '（无文字评价）';
        html += '<div style="padding:12px 16px; border-bottom:1px solid rgba(255,255,255,0.06);">' +
            '<div style="display:flex; justify-content:space-between; align-items:center;">' +
            '<strong style="color:#e2e8f0;">' + escapeHtml(c.studentName || '匿名学员') + '</strong>' +
            '<span style="color:#f59e0b;">' + stars + ' ' + (c.studentScore || 0) + '分</span>' +
            '</div>' +
            '<div style="color:#94a3b8; font-size:14px; margin-top:6px;">' + escapeHtml(commentText) + '</div>' +
            '<div style="color:#64748b; font-size:11px; margin-top:4px;">' + (c.createTime || '') + '</div>' +
            '</div>';
    });
    listEl.innerHTML = html;
}

// ============================================================
// 7. 辅助函数
// ============================================================

// 防 XSS 转义
function escapeHtml(text) {
    if (!text) return '';
    var div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// 格式化日期时间
function formatDateTime(timestamp) {
    if (!timestamp) return '-';
    var date = new Date(timestamp);
    var year = date.getFullYear();
    var month = String(date.getMonth() + 1).padStart(2, '0');
    var day = String(date.getDate()).padStart(2, '0');
    var hours = String(date.getHours()).padStart(2, '0');
    var minutes = String(date.getMinutes()).padStart(2, '0');
    return year + '-' + month + '-' + day + ' ' + hours + ':' + minutes;
}

// ===== 加载排班列表 =====
function loadSchedule() {
    var coachId = getCoachId();
    if (!coachId) {
        document.getElementById('scheduleList').innerHTML = '<div class="empty-tip">请先登录</div>';
        return;
    }

    axios.get(BASE_URL + '/coach/schedule?coachId=' + coachId)
        .then(function(res) {
            if (res.data.code === 1) {
                var list = res.data.data || [];
                var container = document.getElementById('scheduleList');
                if (list.length === 0) {
                    container.innerHTML = '<div class="empty-tip">暂无排班数据</div>';
                    return;
                }
                var weekMap = {1:'周一', 2:'周二', 3:'周三', 4:'周四', 5:'周五', 6:'周六', 7:'周日'};
                var html = '<ul style="list-style:none; padding:0; margin:0;">';
                list.forEach(function(item) {
                    html += '<li style="display:flex; justify-content:space-between; align-items:center; padding:10px 0; border-bottom:1px solid rgba(255,255,255,0.06);">' +
                        '<span>' + weekMap[item.weekday] + ' ' + item.startTime + ' - ' + item.endTime + '</span>' +
                        '<button class="btn btn-sm btn-danger" onclick="deleteSchedule(\'' + item.id + '\')">删除</button>' +
                        '</li>';
                });
                html += '</ul>';
                container.innerHTML = html;
            }
        })
        .catch(function(err) {
            console.error('加载排班失败:', err);
        });
}

// ===== 添加排班 =====
function addSchedule() {
    var weekday = document.getElementById('scheduleWeekday').value;
    var start = document.getElementById('scheduleStart').value;
    var end = document.getElementById('scheduleEnd').value;

    if (!start || !end) {
        alert('请填写完整的时间段');
        return;
    }
    if (start >= end) {
        alert('开始时间不能晚于结束时间');
        return;
    }

    var params = new URLSearchParams();
    params.append('weekday', weekday);
    params.append('startTime', start);
    params.append('endTime', end);

    axios.post(BASE_URL + '/coach/schedule', params.toString(), {
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' }
    })
        .then(function(res) {
            if (res.data.code === 1) {
                alert('✅ 排班添加成功');
                loadSchedule();
                // 清空输入
                document.getElementById('scheduleStart').value = '';
                document.getElementById('scheduleEnd').value = '';
            } else {
                alert('❌ ' + (res.data.msg || '添加失败'));
            }
        })
        .catch(function(err) {
            alert('网络请求失败，请稍后重试');
            console.error(err);
        });
}

// ===== 删除排班 =====
function deleteSchedule(id) {
    if (!confirm('确定删除该排班吗？')) return;
    axios.delete(BASE_URL + '/coach/schedule?id=' + id)
        .then(function(res) {
            if (res.data.code === 1) {
                alert('✅ 删除成功');
                loadSchedule();
            } else {
                alert('❌ ' + (res.data.msg || '删除失败'));
            }
        })
        .catch(function(err) {
            alert('网络请求失败，请稍后重试');
            console.error(err);
        });
}

// ============================================================
// 加载教练的已预约时间段
// ============================================================
window.loadCoachBookings = function() {
    console.log('[loadCoachBookings] 开始执行');
    var coachId = getCoachId();
    if (!coachId) {
        document.getElementById('coachBookingsList').innerHTML = '<div class="empty-tip">请先登录</div>';
        return;
    }

    var date = document.getElementById('bookingDate') ? document.getElementById('bookingDate').value : '';
    var url = BASE_URL + '/coach/bookings?coachId=' + encodeURIComponent(coachId);
    if (date) {
        url += '&date=' + encodeURIComponent(date);
    }
    console.log('[loadCoachBookings] 请求URL:', url);

    var container = document.getElementById('coachBookingsList');
    if (!container) return;
    container.innerHTML = '<div class="empty-tip loading">加载中...</div>';

    axios.get(url)
        .then(function(response) {
            var result = response.data;
            if (result.code === 1) {
                var list = result.data || [];
                renderCoachBookings(list);
            } else {
                container.innerHTML = '<div class="empty-tip">加载失败：' + (result.msg || '未知错误') + '</div>';
            }
        })
        .catch(function(error) {
            console.error('[loadCoachBookings] 请求失败:', error);
            container.innerHTML = '<div class="empty-tip">加载失败，请稍后重试</div>';
        });
};

function renderCoachBookings(list) {
    var container = document.getElementById('coachBookingsList');
    if (!container) return;

    if (!list || list.length === 0) {
        container.innerHTML = '<div class="empty-tip">暂无预约记录</div>';
        return;
    }

    var html = '<table class="data-table"><thead><tr>' +
        '<th>学员</th><th>科目</th><th>开始时间</th><th>结束时间</th><th>状态</th>' +
        '</tr></thead><tbody>';

    list.forEach(function(item) {
        var statusText = item.status === 'approved' ? '已通过' : (item.status === 'pending' ? '待审核' : '已拒绝');
        var studentName = item.studentName || (item.studentId ? item.studentId.substring(0, 8) : '');
        var startTime = item.startTime ? formatDateTime(item.startTime) : '-';
        var endTime = item.endTime ? formatDateTime(item.endTime) : '-';

        html += '<tr>' +
            '<td>' + escapeHtml(studentName) + '</td>' +
            '<td>' + escapeHtml(item.subjectType || '-') + '</td>' +
            '<td>' + startTime + '</td>' +
            '<td>' + endTime + '</td>' +
            '<td>' + statusText + '</td>' +
            '</tr>';
    });
    html += '</tbody></table>';
    container.innerHTML = html;
}

// 页面初始化
// ============================================================
document.addEventListener('DOMContentLoaded', function() {
    // 检查 axios 是否可用
    if (typeof axios === 'undefined') {
        alert('❌ axios 未加载，请确保 common.js 正确引入');
        return;
    }

    // 检查是否登录
    var userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}');
    if (!userInfo.id) {
        // 如果未登录，显示提示并跳转
        alert('请先登录');
        window.location.href = 'login.html';
        return;
    }

    // 显示教练名字
    var nameSpan = document.getElementById('coachName');
    if (nameSpan && userInfo.name) {
        nameSpan.innerText = userInfo.name;
    }

    // 加载数据
    loadPendingScores();
    loadComments();
    loadSchedule();
    loadCoachBookings();  // 加载教练的已预约时间段
});

// 暴露到全局
window.loadCoachBookings = loadCoachBookings;
window.loadPendingScores = loadPendingScores;
window.loadComments = loadComments;
window.getCoachId = getCoachId;
