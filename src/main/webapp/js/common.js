
var BASE_URL = '/DriveTrainingSystem';

// 如果后端接口地址是完整的域名+端口，可以改为：
// var BASE_URL = 'http://localhost:8080/DriveTrainingSystem';

// 暴露到全局
window.BASE_URL = BASE_URL;

// ============================================================
// 2. 检查 axios 是否已加载
// ============================================================
if (typeof axios === 'undefined') {
    console.warn('axios 未加载，请确保在 common.js 之前引入了 axios 库');
    // 如果项目中没有引入 axios，可以在页面中通过 CDN 加载
    // 但建议在 login.html 等页面中用 <script src="https://cdn.jsdelivr.net/npm/axios/dist/axios.min.js"></script>
}

// ============================================================
// 3. 创建 axios 实例（带统一配置）
// ============================================================
var request = axios.create({
    baseURL: BASE_URL,
    timeout: 30000,           // 30秒超时
    headers: {
        'Content-Type': 'application/json'
    },
    withCredentials: true      // 允许携带 Cookie（如果需要 session）
});

// ============================================================
// 4. 请求拦截器（可选：在请求发送前处理）
// ============================================================
request.interceptors.request.use(
    function(config) {
        // 可以在这里添加 token 等
        // var token = localStorage.getItem('token');
        // if (token) config.headers.Authorization = 'Bearer ' + token;
        return config;
    },
    function(error) {
        return Promise.reject(error);
    }
);

// ============================================================
// 5. 响应拦截器（统一处理返回数据）
// ============================================================
request.interceptors.response.use(
    function(response) {
        // 如果后端返回 code 为 401，说明未登录或登录过期
        if (response.data && response.data.code === 401) {
            // 清除本地存储
            localStorage.removeItem('userInfo');
            // 跳转到登录页（如果当前不在登录页）
            if (!window.location.pathname.includes('login.html')) {
                alert('登录已过期，请重新登录');
                window.location.href = 'pages/login.html';
            }
        }
        return response;
    },
    function(error) {
        // 网络错误或服务器错误
        console.error('请求异常:', error);
        return Promise.reject(error);
    }
);

// ============================================================
// 6. 暴露 axios 和 request 到全局
// ============================================================
// 保留原始的 axios，同时也提供封装后的 request
window.axios = axios;           // 继续使用 axios.get/post
window.request = request;       // 使用封装好的 request

// ============================================================
// 7. 通用工具函数（统一抽取，避免重复定义）
// ============================================================

// XSS防护函数
function escapeHtml(text) {
    if (!text) return '';
    var div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}
window.escapeHtml = escapeHtml;

// 格式化时间戳（YYYY-MM-DD HH:MM）
function formatTimestamp(timestamp) {
    if (!timestamp) return '-';
    var date = new Date(timestamp);
    var year = date.getFullYear();
    var month = String(date.getMonth() + 1).padStart(2, '0');
    var day = String(date.getDate()).padStart(2, '0');
    var hours = String(date.getHours()).padStart(2, '0');
    var minutes = String(date.getMinutes()).padStart(2, '0');
    return year + '-' + month + '-' + day + ' ' + hours + ':' + minutes;
}
window.formatTimestamp = formatTimestamp;

// 格式化日期时间（别名，与 formatTimestamp 功能相同）
function formatDateTime(timestamp) {
    return formatTimestamp(timestamp);
}
window.formatDateTime = formatDateTime;

// 格式化时间（用于评价页面）
function formatTime(timeStr) {
    if (!timeStr) return '';
    try {
        var date = new Date(timeStr);
        var year = date.getFullYear();
        var month = String(date.getMonth() + 1).padStart(2, '0');
        var day = String(date.getDate()).padStart(2, '0');
        var hours = String(date.getHours()).padStart(2, '0');
        var minutes = String(date.getMinutes()).padStart(2, '0');
        return year + '-' + month + '-' + day + ' ' + hours + ':' + minutes;
    } catch (e) {
        return timeStr;
    }
}
window.formatTime = formatTime;

// 练车预约科目显示转换（兼容旧数据 C1/C2/C3 → 科目二/科目三）
function convertBookingSubject(subjectType) {
    if (!subjectType) return '-';
    var map = { 'C1': '科目二', 'C2': '科目二', 'C3': '科目三' };
    return map[subjectType] || subjectType;
}
window.convertBookingSubject = convertBookingSubject;

// 输出加载信息
console.log('✅ common.js 加载成功，BASE_URL =', BASE_URL);
console.log('✅ 工具函数已挂载到 window: escapeHtml, formatTimestamp, formatDateTime, formatTime, convertBookingSubject');