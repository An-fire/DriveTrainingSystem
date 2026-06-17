
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

// 输出加载信息
console.log('✅ common.js 加载成功，BASE_URL =', BASE_URL);