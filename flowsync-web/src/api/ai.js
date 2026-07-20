import request from '@/utils/request'

// 单任务 AI 建议
export function getTaskSuggestion(projectName, taskTitle, taskDescription) {
  return request.post('/ai/task-suggestion', {
    projectName,
    taskTitle,
    taskDescription
  })
}

// AI 任务拆解
export function getAiTaskPlan(params) {
  return request.post('/ai/task-plan', params)
}

// 导入 AI 任务
export function importAiTasks(data) {
  return request.post('/ai/task-plan/import', data)
}
