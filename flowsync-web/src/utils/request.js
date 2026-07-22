import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'
import { getCurrentUser, clearCurrentUser, getToken } from '@/utils/auth'
import { EXCLUDE_CURRENT_USER_APIS } from '@/utils/constants'

const request = axios.create({
  baseURL: '/api',
  timeout: 20000
})

// 请求拦截器：自动附加 currentUserId
request.interceptors.request.use(
  (config) => {
    const user = getCurrentUser()
    const token = getToken()
    const url = config.url || ''

    // 判断是否需要自动附加 currentUserId
    const isExcluded = EXCLUDE_CURRENT_USER_APIS.some(
      (api) => url === api || url.startsWith(api)
    )

    if (token && !isExcluded) {
      config.headers = config.headers || {}
      config.headers.Authorization = `Bearer ${token}`
    }

    if (user && user.id && !isExcluded) {
      // 保留接口已有的查询参数
      config.params = {
        currentUserId: user.id,
        ...config.params
      }
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器：统一处理响应和错误
request.interceptors.response.use(
  (response) => {
    const res = response.data

    // 兼容后端直接返回原始数据的情况
    if (res && typeof res === 'object' && 'success' in res) {
      if (res.success === true) {
        return res.data
      } else {
        ElMessage.error(res.message || '操作失败')
        return Promise.reject(new Error(res.message || '操作失败'))
      }
    }

    // 后端直接返回数据（无统一包装）
    return res
  },
  (error) => {
    if (error.response) {
      const { status, data } = error.response
      if (status === 401) {
        clearCurrentUser()
        ElMessage.error('登录已失效，请重新登录')
        router.push('/login')
      } else {
        const msg =
          (data && data.message) || error.message || '网络请求失败'
        ElMessage.error(msg)
      }
    } else if (error.message) {
      ElMessage.error(error.message)
    } else {
      ElMessage.error('网络请求失败，请检查后端服务')
    }
    return Promise.reject(error)
  }
)

export default request
