
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
            '<td>' + escapeHtml(rec.timeSlot || rec.startTime || '—') + '</td>' +
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
        listEl.innerHTML = '<li style="color:#95a5a6;">暂无学员评价</li>';
        return;
    }

    var html = '';
    comments.forEach(function(c) {
        var stars = '⭐'.repeat(c.studentScore || 0) + '☆'.repeat(5 - (c.studentScore || 0));
        html += '<li style="padding:8px 0;border-bottom:1px solid #f0f0f0;">' +
            '<strong>' + escapeHtml(c.studentName || '未知学员') + '</strong> ' +
            '<span style="color:#f39c12;">' + stars + '</span> ' +
            (c.studentScore || 0) + '星 &nbsp;·&nbsp; ' +
            '<span style="color:#7f8c8d;">' + escapeHtml(c.comment || '无文字评价') + '</span>' +
            (c.createTime ? ' <span style="color:#bdc3c7;font-size:12px;">' + escapeHtml(c.createTime) + '</span>' : '') +
        '</li>';
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

// ============================================================
// 8. 页面初始化
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
});

// 暴露必要函数到全局
window.loadPendingScores = loadPendingScores;
window.loadComments = loadComments;
window.getCoachId = getCoachId;