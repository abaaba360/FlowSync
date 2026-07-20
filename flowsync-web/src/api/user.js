import request from '@/utils/request'
import { arrayKeysToCamelCase } from '@/utils/format'

// 获取用户列表
export function getUserList() {
  return request.get('/users').then((data) => {
    return arrayKeysToCamelCase(data)
  })
}

// 修改密码
export function updatePassword(userId, oldPassword, newPassword) {
  return request.post('/users/update-password', {
    userId,
    oldPassword,
    newPassword
  })
}
