import dayjs from 'dayjs'

// 格式化日期 YYYY-MM-DD
export function formatDate(date) {
  if (!date) return ''
  return dayjs(date).format('YYYY-MM-DD')
}

// 格式化日期时间 YYYY-MM-DD HH:mm
export function formatDateTime(date) {
  if (!date) return ''
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

// 判断是否逾期（截止日期早于今天且状态不是已完成）
export function isOverdue(dueDate, status) {
  if (!dueDate || status === '已完成') return false
  return dayjs(dueDate).isBefore(dayjs(), 'day')
}

// 兼容 snake_case 转 camelCase
function toCamelCase(str) {
  return str.replace(/_([a-z])/g, (_, letter) => letter.toUpperCase())
}

// 将对象 key 从 snake_case 转为 camelCase（浅层转换）
export function keysToCamelCase(obj) {
  if (!obj || typeof obj !== 'object') return obj
  if (Array.isArray(obj)) return obj.map(keysToCamelCase)
  const result = {}
  for (const key of Object.keys(obj)) {
    const camelKey = toCamelCase(key)
    result[camelKey] = obj[key]
  }
  return result
}

// 将数组内对象 key 从 snake_case 转为 camelCase
export function arrayKeysToCamelCase(arr) {
  if (!Array.isArray(arr)) return arr
  return arr.map(keysToCamelCase)
}
