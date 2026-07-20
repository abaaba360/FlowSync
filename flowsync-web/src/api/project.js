import request from '@/utils/request'
import { arrayKeysToCamelCase, keysToCamelCase } from '@/utils/format'

// 获取项目列表
export function getProjectList() {
  return request.get('/projects').then((data) => {
    return arrayKeysToCamelCase(data)
  })
}

// 创建或编辑项目（不传 id 为创建，传 id 为编辑）
export function saveProject(project) {
  return request.post('/projects', project).then((data) => {
    return keysToCamelCase(data)
  })
}

// 删除项目
export function deleteProject(id) {
  return request.delete(`/projects/${id}`)
}
