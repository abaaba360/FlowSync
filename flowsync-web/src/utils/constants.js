// 用户角色
export const USER_ROLES = {
  ADMIN: '管理员',
  LEADER: '负责人',
  MEMBER: '成员'
}

// 项目状态选项
export const PROJECT_STATUS_OPTIONS = [
  { label: '未开始', value: '未开始' },
  { label: '进行中', value: '进行中' },
  { label: '已完成', value: '已完成' }
]

// 任务状态选项
export const TASK_STATUS_OPTIONS = [
  { label: '未开始', value: '未开始' },
  { label: '进行中', value: '进行中' },
  { label: '已完成', value: '已完成' }
]

// 优先级选项
export const PRIORITY_OPTIONS = [
  { label: '低', value: '低' },
  { label: '中', value: '中' },
  { label: '高', value: '高' }
]

// 总结类型选项
export const SUMMARY_TYPE_OPTIONS = [
  { label: '阶段总结', value: '阶段总结' },
  { label: '最终总结', value: '最终总结' }
]

// 项目状态颜色映射
export const PROJECT_STATUS_COLOR_MAP = {
  '未开始': 'info',
  '进行中': '',
  '已完成': 'success'
}

// 优先级颜色映射
export const PRIORITY_COLOR_MAP = {
  '低': 'info',
  '中': 'warning',
  '高': 'danger'
}

// 任务状态颜色映射
export const TASK_STATUS_COLOR_MAP = {
  '未开始': 'info',
  '进行中': '',
  '已完成': 'success'
}

// 角色颜色映射
export const ROLE_COLOR_MAP = {
  '管理员': 'danger',
  '负责人': 'warning',
  '成员': 'info'
}

// 不需要自动附加 currentUserId 的接口
export const EXCLUDE_CURRENT_USER_APIS = [
  '/auth/login',
  '/ai/task-suggestion',
  '/ai/task-plan',
  '/ai/task-plan/import'
]
