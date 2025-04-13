import { loginApi, getUserInfoApi } from '@/api/user'
import router from '@/router'
// 修改导入方式为使用默认导出
import store from '@/store'
import adminApi from '@/api/admin'
const state = {
    token: (() => {
        try {
            // 检查localStorage中的token和过期时间
            const localStorageToken = JSON.parse(localStorage.getItem('steamToken'));
            const localStorageExpiration = localStorage.getItem('steamTokenExpiration');
            
            // 检查sessionStorage中的token和过期时间
            const sessionStorageToken = JSON.parse(sessionStorage.getItem('steamToken'));
            const sessionStorageExpiration = sessionStorage.getItem('steamTokenExpiration');
            
            // 检查localStorage token是否过期
            if (localStorageToken && localStorageExpiration) {
                const expirationDate = new Date(localStorageExpiration);
                if (new Date() > expirationDate) {
                    localStorage.removeItem('steamToken');
                    localStorage.removeItem('steamTokenExpiration');
                    return null;
                }
                return localStorageToken;
            }
            
            // 检查sessionStorage token是否过期
            if (sessionStorageToken && sessionStorageExpiration) {
                const expirationDate = new Date(sessionStorageExpiration);
                if (new Date() > expirationDate) {
                    sessionStorage.removeItem('steamToken');
                    sessionStorage.removeItem('steamTokenExpiration');
                    return null;
                }
                return sessionStorageToken;
            }
            
            return null;
        } catch (e) {
            localStorage.removeItem('steamToken');
            localStorage.removeItem('steamTokenExpiration');
            sessionStorage.removeItem('steamToken');
            sessionStorage.removeItem('steamTokenExpiration');
            return null;
        }
    })(),
    userId: null,
    username: null,
    nickname: null,
    avatar: null,
    role: 0 // 0表示普通用户，1表示管理员
}

const getters = {
    token: state => state.token,
    userId: state => state.userId,
    username: state => state.username,
    nickname: state => state.nickname,
    avatar: state => state.avatar,
    role: state => state.role,
    isAdmin: state => state.role === 1,
    userInfo: ({ userId, username, nickname, avatar, role }) => {
        return { userId, username, nickname, avatar, role }
    }
}

const mutations = {
    setToken(state, token) {
        state.token = token
    },
    setUserId(state, userId) {
        state.userId = userId
    },
    setUsername(state, username) {
        state.username = username
    },
    setAvatar(state, avatar) {
        state.avatar = avatar
    },
    setRole(state, role) {
        state.role = role
    },
    setUserInfo(state, { userId, username, nickname, avatar, role }) {
        state.userId = userId
        state.username = username
        state.nickname = nickname
        state.avatar = avatar
        // 只有当role有明确值时才更新，避免将管理员角色重置为0
        if (role !== undefined) {
            state.role = role
        }
    }
}

const actions = {
    /**
     * 登出
     */
    logout({ commit }) {
        commit('setToken', null)
        commit('setUserInfo', { userId: null, username: null, nickname: null, avatar: null })
        localStorage.removeItem('steamToken')
        sessionStorage.removeItem('steamToken')
        // 刷新页面
        router.go(0)
    },

    /**
     * 用户登录
     */
    async login({ commit, dispatch }, { username, password, rememberMe }) {
        const { data } = await loginApi(username, password, rememberMe)
        commit('setToken', data.token)
        
        // 确保角色信息正确设置
        console.log('登录响应数据:', data)
        // 先保存登录返回的角色信息
        commit('setRole', data.role) // 直接设置角色，确保不会被覆盖
        commit('setUserId', data.userId)
        
        // 根据'记住我'选项设置不同的存储方式
        if (rememberMe) {
            localStorage.setItem('steamToken', JSON.stringify(data.token))
            // 设置30天过期时间
            const expirationDate = new Date()
            expirationDate.setDate(expirationDate.getDate() + 30)
            localStorage.setItem('steamTokenExpiration', expirationDate.toISOString())
        } else {
            sessionStorage.setItem('steamToken', JSON.stringify(data.token))
            // 设置30分钟过期时间
            const expirationDate = new Date()
            expirationDate.setMinutes(expirationDate.getMinutes() + 30)
            sessionStorage.setItem('steamTokenExpiration', expirationDate.toISOString())
        }
        
        // 登录成功后获取完整的用户信息，但保留角色信息
        try {
            await dispatch('getUserInfo')
            console.log('登录后用户角色:', store.getters['user/role'])
        } catch (error) {
            console.error('获取用户信息失败:', error)
        }
        
        // 注意：不在这里处理跳转，让Login.vue组件处理跳转逻辑
        // 避免与Login.vue中的跳转逻辑冲突
    },

    /**
     * 获取个人资料
     */
    getUserInfo({ commit, state, dispatch }) {
        return new Promise((resolve, reject) => {
            if (!state.token) {
                reject(new Error('未登录'))
            }
            getUserInfoApi(state.userId).then(({ data }) => {
                // 保留已设置的管理员角色，避免被覆盖
                const savedRole = state.role
                commit('setUserInfo', data)
                
                // 如果之前已经设置了管理员角色(1)，则保持该角色
                if (savedRole === 1) {
                    commit('setRole', savedRole)
                }
                
                // 获取用户信息后检查是否为管理员
                dispatch('checkIsAdmin')
                resolve()
            }).catch((reason) => {
                reject(reason)
            })
        })
    },

    /**
     * 检查当前用户是否为管理员
     */
    checkIsAdmin({ state }) {
        return Promise.resolve(state.role === 1)
    }
}


export default {
    namespaced: true,
    state,
    getters,
    mutations,
    actions // 确保所有 actions 都在这里导出
}
