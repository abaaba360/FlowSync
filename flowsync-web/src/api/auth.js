import request from '@/utils/request'

// 登录
export function login(username, password) {
  return request.post('/auth/login', { username, password }).then((data) => {
    // 适配后端不同的返回格式
    let user = null
    let token = ''

    if (data.user) {
      user = data.user
      token = data.token || ''
    } else if (data.currentUser) {
      user = data.currentUser
      token = data.token || ''
    } else {
      // 后端直接返回用户对象
      user = data
      token = data.token || ''
    }

    return { user, token }
  })
}
