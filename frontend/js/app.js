/**
 * app.js — 公共页面逻辑
 */

document.addEventListener('DOMContentLoaded', () => {
    renderHeader();
    renderPage();
});

function renderHeader() {
    const user = getCurrentUser();
    const header = document.getElementById('header-nav');
    if (!header) return;

    let html = '';
    if (user) {
        const roleName = user.role === 'teacher' ? '教师' : '学生';
        html += `<span>${user.name}（${roleName}）</span>`;
        if (user.role === 'teacher') {
            html += `<a href="teacher-home.html">教师工作台</a>`;
        } else {
            html += `<a href="student-home.html">学生主页</a>`;
        }
        html += `<a href="activities.html">活动列表</a>`;
        html += `<a href="#" onclick="handleLogout()">退出登录</a>`;
    } else {
        html += `<a href="index.html">首页</a>`;
        html += `<a href="login.html">登录</a>`;
        html += `<a href="register.html">注册</a>`;
    }
    header.innerHTML = html;
}

function handleLogout() {
    logout();
    window.location.href = 'index.html';
}

function requireLogin(role) {
    const user = getCurrentUser();
    if (!user) {
        alert('请先登录');
        window.location.href = 'login.html';
        return null;
    }
    if (role && user.role !== role) {
        alert('无权访问该页面');
        window.location.href = 'index.html';
        return null;
    }
    return user;
}

function showAlert(id, message, type) {
    const el = document.getElementById(id);
    if (!el) return;
    el.className = `alert alert-${type}`;
    el.textContent = message;
    el.style.display = message ? 'block' : 'none';
}

function renderPage() {
    // 各页面在各自脚本中调用具体渲染函数
}
