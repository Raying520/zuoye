/**
 * data.js — 模拟本地数据存储
 * 用 localStorage 持久化用户、活动、报名记录。
 */

const DB_KEY = 'campus_activity_db';

function getDB() {
    const raw = localStorage.getItem(DB_KEY);
    if (raw) return JSON.parse(raw);
    return initDB();
}

function initDB() {
    const db = {
        users: [
            { id: 1, username: 'teacher01', password: '123456', name: '王老师', role: 'teacher' },
            { id: 2, username: 'student01', password: '123456', name: '张同学', role: 'student' },
            { id: 3, username: 'student02', password: '123456', name: '李同学', role: 'student' }
        ],
        activities: [
            {
                id: 1,
                title: '校园编程马拉松',
                description: '面向全校学生的 24 小时编程挑战赛，组队参加，优胜者将获得证书和奖品。',
                activityTime: '2026-09-25T09:00',
                place: '计算机楼 301',
                capacity: 30,
                status: 'OPEN',
                teacherId: 1,
                createTime: '2026-09-10T10:00'
            },
            {
                id: 2,
                title: '迎新晚会彩排',
                description: '社团联合会迎新晚会第一次彩排，请报名的同学准时到场。',
                activityTime: '2026-09-20T18:30',
                place: '大学生活动中心',
                capacity: 50,
                status: 'CLOSED',
                teacherId: 1,
                createTime: '2026-09-08T14:00'
            }
        ],
        enrollments: [
            { id: 1, activityId: 1, userId: 2, createTime: '2026-09-11T09:30' }
        ],
        nextUserId: 4,
        nextActivityId: 3,
        nextEnrollmentId: 2
    };
    saveDB(db);
    return db;
}

function saveDB(db) {
    localStorage.setItem(DB_KEY, JSON.stringify(db));
}

// ============ 用户操作 ============

function findUserByUsername(username) {
    return getDB().users.find(u => u.username === username);
}

function findUserById(id) {
    return getDB().users.find(u => u.id === id);
}

function register(username, password, name, role) {
    const db = getDB();
    if (db.users.some(u => u.username === username)) {
        return { success: false, message: '账号已存在' };
    }
    db.users.push({ id: db.nextUserId++, username, password, name, role });
    saveDB(db);
    return { success: true };
}

function login(username, password) {
    const user = findUserByUsername(username);
    if (!user || user.password !== password) {
        return { success: false, message: '账号或密码错误' };
    }
    sessionStorage.setItem('currentUser', JSON.stringify(user));
    return { success: true, user };
}

function logout() {
    sessionStorage.removeItem('currentUser');
}

function getCurrentUser() {
    const raw = sessionStorage.getItem('currentUser');
    return raw ? JSON.parse(raw) : null;
}

// ============ 活动操作 ============

function getAllActivities() {
    const db = getDB();
    return db.activities
        .map(act => ({ ...act, enrolledCount: countEnrollmentsByActivity(act.id) }))
        .sort((a, b) => new Date(b.activityTime) - new Date(a.activityTime));
}

function getActivityById(id) {
    const act = getDB().activities.find(a => a.id === id);
    if (!act) return null;
    return { ...act, enrolledCount: countEnrollmentsByActivity(id) };
}

function getActivitiesByTeacher(teacherId) {
    return getAllActivities().filter(a => a.teacherId === teacherId);
}

function createActivity(activity) {
    const db = getDB();
    const newAct = {
        id: db.nextActivityId++,
        ...activity,
        status: 'OPEN',
        createTime: new Date().toISOString()
    };
    db.activities.push(newAct);
    saveDB(db);
    return newAct;
}

function updateActivityStatus(id, status, teacherId) {
    const db = getDB();
    const act = db.activities.find(a => a.id === id);
    if (!act) return { success: false, message: '活动不存在' };
    if (act.teacherId !== teacherId) return { success: false, message: '只能管理自己发布的活动' };
    if (act.status !== 'OPEN') return { success: false, message: '活动当前状态不可更改' };
    act.status = status;
    saveDB(db);
    return { success: true };
}

// ============ 报名操作 ============

function getEnrollmentsByActivity(activityId) {
    const db = getDB();
    return db.enrollments
        .filter(e => e.activityId === activityId)
        .map(e => ({ ...e, userName: findUserById(e.userId)?.name || '未知' }));
}

function getEnrollmentsByUser(userId) {
    return getAllActivities()
        .filter(act => dbHasEnrollment(act.id, userId))
        .map(act => ({
            ...act,
            enrollmentTime: getEnrollment(act.id, userId).createTime
        }));
}

function countEnrollmentsByActivity(activityId) {
    return getDB().enrollments.filter(e => e.activityId === activityId).length;
}

function dbHasEnrollment(activityId, userId) {
    return getDB().enrollments.some(e => e.activityId === activityId && e.userId === userId);
}

function getEnrollment(activityId, userId) {
    return getDB().enrollments.find(e => e.activityId === activityId && e.userId === userId);
}

function enroll(activityId, userId) {
    const db = getDB();
    const act = db.activities.find(a => a.id === activityId);
    if (!act) return { success: false, message: '活动不存在' };
    if (act.status !== 'OPEN') return { success: false, message: '该活动当前不可报名' };
    if (countEnrollmentsByActivity(activityId) >= act.capacity) {
        return { success: false, message: '活动名额已满' };
    }
    if (dbHasEnrollment(activityId, userId)) {
        return { success: false, message: '你已经报名过该活动' };
    }
    db.enrollments.push({
        id: db.nextEnrollmentId++,
        activityId,
        userId,
        createTime: new Date().toISOString()
    });
    saveDB(db);
    return { success: true };
}

function cancelEnrollment(activityId, userId) {
    const db = getDB();
    const act = db.activities.find(a => a.id === activityId);
    if (!act) return { success: false, message: '活动不存在' };
    if (act.status !== 'OPEN') return { success: false, message: '活动当前不可取消报名' };
    const idx = db.enrollments.findIndex(e => e.activityId === activityId && e.userId === userId);
    if (idx === -1) return { success: false, message: '你尚未报名该活动' };
    db.enrollments.splice(idx, 1);
    saveDB(db);
    return { success: true };
}

// ============ 工具函数 ============

function formatDateTime(iso) {
    if (!iso) return '-';
    const d = new Date(iso);
    return `${d.getFullYear()}-${pad(d.getMonth()+1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

function pad(n) { return n < 10 ? '0'+n : n; }

function statusText(status) {
    return { OPEN: '报名中', CLOSED: '已结束', CANCELLED: '已取消' }[status] || status;
}

function statusClass(status) {
    return { OPEN: 'status-open', CLOSED: 'status-closed', CANCELLED: 'status-cancelled' }[status] || '';
}

// 重置数据（调试用）
function resetData() {
    localStorage.removeItem(DB_KEY);
    initDB();
}
