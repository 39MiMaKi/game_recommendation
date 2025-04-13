import { createRouter, createWebHistory } from 'vue-router'
import Store from '@/views/store/Store.vue'
import store from '@/store'
import adminApi from '@/api/admin'

// 导入新的组件
import Recommendations from '@/views/store/Recommendations.vue'
import NewReleases from '@/views/store/NewReleases.vue'
import Specials from '@/views/store/Specials.vue'
import FreeGames from '@/views/store/FreeGames.vue'

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: [
        {
            path: '/admin',
            name: 'admin',
            component: () => import('@/views/admin/AdminDashboard.vue'),
            meta: { 
                requiresAuth: true, 
                requiresAdmin: true,
                hideHeader: true  // 添加这个标记
            }
        },
        {
            path: '/',
            name: 'store',
            component: Store
        },
        {
            path: '/app/:appId',
            name: 'app',
            component: () => import('@/views/store/App.vue')
        },
        {
            path: '/wishlist',
            name: 'wishlist',
            component: () => import('@/views/store/WishList.vue'),
            meta: { requiresAuth: true }
        },
        // 添加新的路由
        {
            path: '/recommendations',
            name: 'recommendations',
            component: Recommendations
        },
        {
            path: '/new-releases',
            name: 'newReleases',
            component: NewReleases
        },
        {
            path: '/specials',
            name: 'specials',
            component: Specials
        },
        {
            path: '/free-games',
            name: 'freeGames',
            component: FreeGames
        },
        {
            path: '/community',
            name: 'community',
            component: () => import('@/views/community/Community.vue')
        },
        {
            path: '/profile/:userId',
            name: 'profile',
            component: () => import('@/views/community/Profile.vue')
        },
        {
            path: '/friends',
            name: 'friends',
            component: () => import('@/views/mine/Friends.vue'),
            children: [
                {
                    path: '',
                    name: 'friendList',
                    component: () => import('@/views/mine/friends/List.vue')
                },
                {
                    path: 'add',
                    name: 'friendAdd',
                    component: () => import('@/views/mine/friends/Add.vue')
                },
                {
                    path: 'pending',
                    name: 'friendPending',
                    component: () => import('@/views/mine/friends/Pending.vue')
                }
            ]
        },
        {
            path: '/about',
            name: 'about',
            component: () => import('@/views/mine/About.vue')
        },
        {
            path: '/chat',
            name: 'chat',
            component: () => import('@/views/chat/Chat.vue')
        },
        {
            path: '/join',
            name: 'join',
            component: () => import('@/views/Join.vue')
        },
        {
            path: '/login',
            name: 'login',
            component: () => import('@/views/Login.vue'),
            beforeEnter: (to, from) => {
                const token = store.getters['user/token']
                // 如果已经登录，跳转到重定向页面或首页
                if (token) {
                    return to.query.redir || '/'
                }
                // 只有当没有重定向参数且来源路径不是登录页时才添加重定向
                if (!to.query.redir && from.path !== '/' && from.name !== null) {
                    return {
                        path: '/login',
                        query: { redir: from.path }
                    }
                }
            }
        },
        {
            path: '/admin/login',
            name: 'adminLogin',
            component: () => import('@/views/admin/AdminLogin.vue')
        },
        {
            path: '/:pathMatch(.*)*',
            redirect: '/'
        }
    ]
})

router.beforeEach(async (to) => {
    // 检查是否有token但没有用户信息
    const token = store.getters['user/token']
    const username = store.getters['user/username']
    const userId = store.getters['user/userId']
    
    // 如果有token但没有用户名或用户ID，尝试获取用户信息
    if (token && (!username || !userId)) {
        try {
            await store.dispatch('user/getUserInfo')
        } catch (error) {
            console.error('自动获取用户信息失败:', error)
            // 如果获取失败且访问需要登录的页面，重定向到登录页
            if (to.meta.requiresAuth) {
                return { path: '/login', query: { redir: to.fullPath } }
            }
        }
    }
    
    // 检查管理员权限
    if (to.meta.requiresAdmin && store.getters['user/role'] !== 1) {
        // 如果不是管理员但尝试访问管理员页面，显示错误并重定向到首页
        store.commit('user/setLoginError', '需要管理员权限才能访问此页面')
        return '/'
    }
    
    // 检查是否需要登录
    if (to.meta.requiresAuth && !token) {
        return { path: '/login', query: { redir: to.fullPath } }
    }
})

export default router
