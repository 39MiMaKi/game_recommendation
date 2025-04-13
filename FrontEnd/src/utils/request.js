import axios from 'axios'
import store from '@/store'

const service = axios.create({
    baseURL: 'http://localhost:8081',
    timeout: 5000
})

service.interceptors.request.use((config) => {
    config.headers.token = store.getters['user/token'] || null
    // 在请求拦截器中添加角色检查
    service.interceptors.request.use(config => {
      const role = store.getters['user/role']
      if (config.url.startsWith('/admin') && role !== 1) {
        return Promise.reject(new Error('需要管理员权限'))
      }
      return config
    })
    return config
}, (error) => {
    return Promise.reject(error)
})

service.interceptors.response.use((response) => {
    const res = response.data
    if (res.code !== 200) {
        console.error(response.config.url, res)
        return Promise.reject(res)
    }
    return res
}, (error) => {
    console.error(error)
    return Promise.reject(error)
})

export default service
