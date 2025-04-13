import request from '@/utils/request'

export function checkUsernameAvailableApi(username) {
    return request({
        url: '/user/available',
        method: 'GET',
        params: { username }
    })
}

export function checkEmailAvailableApi(email) {
    return request({
        url: '/user/email/available',
        method: 'GET',
        params: { email }
    })
}

export function joinApi({ email, username, password, tags }) {
    return request({
        url: '/user/join',
        method: 'POST',
        data: {
            email,
            username, 
            password,
            tags: Array.isArray(tags) ? tags : [tags] // 确保tags总是数组格式
        }
    })
}

// 修改为统一的登录接口
export function loginApi(username, password, rememberMe) {
    return request({
        url: '/user/login',
        method: 'post',
        data: { username, password, rememberMe }
    })
}

export function getUserInfoApi(userId) {
    return request({
        url: '/user/info',
        method: 'GET',
        params: { userId }
    })
}

export function searchUsersApi(keyword, pageIndex, pageSize) {
    return request({
        url: '/user/search',
        method: 'GET',
        params: { keyword, pageIndex, pageSize }
    })
}

// 更新用户信息（昵称、个人简介、偏好标签）
export function updateUserInfoApi(data) {
    return request({
        url: '/user/update',
        method: 'PUT',
        data
    })
}

// 更新用户头像
export function updateAvatarApi(formData) {
    return request({
        url: '/user/avatar',
        method: 'POST',
        data: formData,
        headers: {
            'Content-Type': 'multipart/form-data'
        }
    })
}

// 获取用户缓存的偏好设置
export function getUserPreferencesFromCache() {
    const cachedPrefs = localStorage.getItem('userPreferences')
    return cachedPrefs ? JSON.parse(cachedPrefs) : null
}

// 保存用户偏好设置到本地缓存
export function saveUserPreferencesToCache(preferences) {
    localStorage.setItem('userPreferences', JSON.stringify(preferences))
    return preferences
}

export function getUserRatings(page, size) {
    return request({
        url: '/user/ratings',
        method: 'GET',
        params: { page, size }
    })
}

export function getUserGameStats() {
    return request({
        url: '/user/stats',
        method: 'GET'
    })
}
