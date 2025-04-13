import axios from 'axios'
import store from '@/store'

const adminApi = axios.create({
  baseURL: 'http://localhost:8081/admin', // 直接指定完整后端地址
  timeout: 5000
})

// 统一请求拦截器
adminApi.interceptors.request.use(config => {
  const token = store.getters['user/token'] || config.token
  if (token) {
    config.headers['Authorization'] = `Bearer ${token}`
  }
  return config
})

// 统一响应拦截器
adminApi.interceptors.response.use(
  response => response.data,
  error => {
    if (error.response?.status === 403) {
      // 阻止页面自动刷新，改为显示错误提示
      store.commit('user/setLoginError', '需要管理员权限才能访问此页面')
      return Promise.reject(error)
    }
    return Promise.reject(error)
  }
)

// 添加命名导出
export const checkIsAdminApi = {
  checkIsAdmin: () => adminApi.get('/check-admin')
}

export default {
  // ====================== 用户管理 ======================
  getUsers(page, size) {
    return adminApi.get('/users', { params: { page, size } })
  },
  searchUsers(page, size, username, email) {
    return adminApi.get('/users/search', { params: { page, size, username, email } })
  },
  setUserRole(userId, role) {
    return adminApi.put(`/users/${userId}/role`, { role })
  },
  updateUserStatus(userId, enabled) {
    return adminApi.patch(`/users/${userId}/status`, { enabled })
  },
  updateUserPreferences(userId, tags) {
    return adminApi.post(`/users/${userId}/preferences`, { tags })
  },
  createAdmin(adminData) {
    return adminApi.post('/users/admin', adminData)
  },

  // ====================== 游戏管理 ======================
  getApps(page, size, keyword, tag) {
    return adminApi.get('/apps', { params: { page, size, keyword, tag } })
  },
  createApp(appData) {
    return adminApi.post('/apps', appData)
  },
  updateApp(appId, appData) {
    return adminApi.put(`/apps/${appId}`, appData)
  },
  deleteApp(appId) {
    return adminApi.delete(`/apps/${appId}`)
  },
  getHighRatingApps(page, size, minRating) {
    return adminApi.get('/apps/high-rating', { params: { page, size, minRating } })
  },
  getLowRatingApps(page, size, maxRating) {
    return adminApi.get('/apps/low-rating', { params: { page, size, maxRating } })
  },
  getAppsByRatingRange(page, size, minRating, maxRating) {
    return adminApi.get('/apps/rating-range', { params: { page, size, minRating, maxRating } })
  },

  // ====================== 推荐系统 ======================
  getRecommendParams() {
    return adminApi.get('/recommend-params')
  },
  updateRecommendParams(params) {
    return adminApi.post('/recommend-params', params)
  },
  getRecommendConfig() {
    return adminApi.get('/recommend-config')
  },
  updateRecommendConfig(config) {
    return adminApi.post('/recommend-config', config)
  },

  // ====================== 系统功能 ======================
  getStatistics() {
    return adminApi.get('/statistics')
  },
  // 检查管理员权限
  checkIsAdmin() {
    return adminApi.get('/check-admin')
  }
}