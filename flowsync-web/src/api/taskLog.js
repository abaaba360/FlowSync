import request from '@/utils/request'
import { arrayKeysToCamelCase, keysToCamelCase } from '@/utils/format'

// 获取进度记录列表
export function getTaskLogList(params) {
  return request.get('/task-logs', { params }).then((data) => {
    return arrayKeysToCamelCase(data)
  })
}

// 新增进度记录
export function addTaskLog(log) {
  return request.post('/task-logs', log).then((data) => {
    return keysToCamelCase(data)
  })
}
