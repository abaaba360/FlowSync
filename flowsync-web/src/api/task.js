import request from '@/utils/request'
import { arrayKeysToCamelCase, keysToCamelCase } from '@/utils/format'

// 获取任务列表
export function getTaskList(params) {
  return request.get('/tasks', { params }).then((data) => {
    return arrayKeysToCamelCase(data)
  })
}

// 创建或编辑任务
export function saveTask(task) {
  return request.post('/tasks', task).then((data) => {
    return keysToCamelCase(data)
  })
}

// 删除任务
export function deleteTask(id) {
  return request.delete(`/tasks/${id}`)
}
