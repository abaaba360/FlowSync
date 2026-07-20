<template>
  <el-tag :type="tagType" :size="size">
    {{ status }}
  </el-tag>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  status: {
    type: String,
    required: true
  },
  // 映射类型：project | task | priority | role
  type: {
    type: String,
    default: 'project'
  },
  size: {
    type: String,
    default: 'default'
  }
})

const tagType = computed(() => {
  if (props.type === 'project' || props.type === 'task') {
    const map = {
      '未开始': 'info',
      '进行中': '',
      '已完成': 'success'
    }
    return map[props.status] || 'info'
  }
  if (props.type === 'priority') {
    const map = {
      '低': 'info',
      '中': 'warning',
      '高': 'danger'
    }
    return map[props.status] || 'info'
  }
  if (props.type === 'role') {
    const map = {
      '管理员': 'danger',
      '负责人': 'warning',
      '成员': 'info'
    }
    return map[props.status] || 'info'
  }
  return 'info'
})
</script>
