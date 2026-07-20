import request from '@/utils/request'

// 获取系统总览统计
export function getOverview() {
  return request.get('/overview')
}
