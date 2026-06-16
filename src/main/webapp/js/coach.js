// 教练ID - 暂时硬编码，后续从session获取（请先手动填一个数据库中存在的教练ID）
// 你可以从 t_staff 表查一个 coach 角色的 id，例如 'xxx-xxx-xxx'
const COACH_ID = '821244bc-67c4-11f1-9d8e-00163e119ad8';   // ⚠️ 必须修改！

// 基础URL（如果common.js已配置axios默认baseURL，则可省略）
// const BASE_URL = 'http://localhost:8080/DriveTrainingSystem';

// 加载待评分记录
function loadPendingScores() {
    const url = `/DriveTrainingSystem/coach/pending?coachId=${COACH_ID}`;
    axios.get(url)
        .then(response => {
            const result = response.data;
            if (result.code === 200) {
                const records = result.data || [];
                const tbody = document.getElementById('pendingBody');
                if (records.length === 0) {
                    tbody.innerHTML = '<tr><td colspan="5">暂无待评分记录</td></tr>';
                    return;
                }
                let html = '';
                records.forEach(rec => {
                    html += `<tr>
                        <td>${rec.studentName}</td>
                        <td>${rec.timeSlot}</td>
                        <td><input type="number" id="score_${rec.bookingId}" min="1" max="5" step="1" value="3"></td>
                        <td><input type="checkbox" id="exam_${rec.bookingId}"></td>
                        <td><button onclick="submitScore('${rec.bookingId}')">提交评分</button></td>
                    </tr>`;
                });
                tbody.innerHTML = html;
            } else {
                document.getElementById('pendingBody').innerHTML = `<tr><td colspan="5">加载失败: ${result.message}</td></tr>`;
            }
        })
        .catch(error => {
            console.error('加载待评分记录失败', error);
            document.getElementById('pendingBody').innerHTML = '<tr><td colspan="5">网络错误，请检查后端服务</td></tr>';
        });
}

// 提交评分
window.submitScore = function(bookingId) {
    const score = document.getElementById(`score_${bookingId}`).value;
    const canExam = document.getElementById(`exam_${bookingId}`).checked;
    if (!score || score < 1 || score > 5) {
        alert('请输入1-5之间的分数');
        return;
    }
    const url = `/DriveTrainingSystem/coach/score`;
    axios.post(url, {
        bookingId: bookingId,
        coachScore: parseInt(score),
        canExam: canExam
    })
    .then(response => {
        const result = response.data;
        if (result.code === 200) {
            alert('评分成功');
            loadPendingScores();  // 刷新列表
            loadComments();       // 可选：刷新评价列表
        } else {
            alert('评分失败: ' + result.message);
        }
    })
    .catch(error => {
        console.error('提交评分失败', error);
        alert('网络错误，请稍后重试');
    });
};

// 加载学员对教练的评价
function loadComments() {
    const url = `/DriveTrainingSystem/coach/comments?coachId=${COACH_ID}`;
    axios.get(url)
        .then(response => {
            const result = response.data;
            const listEl = document.getElementById('commentList');
            if (result.code === 200) {
                const comments = result.data || [];
                if (comments.length === 0) {
                    listEl.innerHTML = '<li>暂无学员评价</li>';
                    return;
                }
                let html = '';
                comments.forEach(c => {
                    html += `<li><strong>${c.studentName}</strong> 评分: ${c.studentScore}星 评语: ${c.comment || '无'}</li>`;
                });
                listEl.innerHTML = html;
            } else {
                listEl.innerHTML = `<li>加载失败: ${result.message}</li>`;
            }
        })
        .catch(error => {
            console.error('加载评价失败', error);
            document.getElementById('commentList').innerHTML = '<li>网络错误，无法加载评价</li>';
        });
}

// 页面加载时执行
document.addEventListener('DOMContentLoaded', () => {
    // 检查axios是否存在
    if (typeof axios === 'undefined') {
        alert('axios未加载，请确保common.js正确引入且已加载axios库。');
        return;
    }
    loadPendingScores();
    loadComments();
});