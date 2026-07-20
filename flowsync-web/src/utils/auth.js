// 获取当前用户
export function getCurrentUser() {
  try {
    const userStr = sessionStorage.getItem('currentUser')
    if (userStr) {
      return JSON.parse(userStr)
    }
  } catch (e) {
    console.error('解析用户信息失败', e)
    clearCurrentUser()
  }
  return null
}

// 设置当前用户
export function setCurrentUser(user) {
  if (user) {
    sessionStorage.setItem('currentUser', JSON.stringify(user))
  }
}

// 清除当前用户
export function clearCurrentUser() {
  sessionStorage.removeItem('currentUser')
  sessionStorage.removeItem('token')
}

// 获取 token
export function getToken() {
  return sessionStorage.getItem('token') || ''
}

// 设置 token
export function setToken(token) {
  sessionStorage.setItem('token', token || '')
}

// 是否已登录
export function isLoggedIn() {
  return !!getCurrentUser()
}

// 是否为负责人或管理员
export function isLeader() {
  const user = getCurrentUser()
  if (!user) return false
  return user.role === '负责人' || user.role === '管理员'
}

// 获取当前用户 ID
export function getCurrentUserId() {
  const user = getCurrentUser()
  return user ? user.id : null
}
