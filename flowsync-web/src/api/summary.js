import request from '@/utils/request'
import { arrayKeysToCamelCase, keysToCamelCase } from '@/utils/format'

// 获取总结列表
export function getSummaryList(params) {
  return request.get('/summaries', { params }).then((data) => {
    return arrayKeysToCamelCase(data)
  })
}

// 新增总结
export function addSummary(summary) {
  return request.post('/summaries', summary).then((data) => {
    return keysToCamelCase(data)
  })
}
