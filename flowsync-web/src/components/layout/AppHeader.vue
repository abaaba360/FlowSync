<template>
  <div class="app-header">
    <div class="header-left">
      <h1 class="header-title">FlowSync 小组任务协同管理系统</h1>
    </div>
    <div class="header-right">
      <div class="user-info">
        <el-avatar :size="36" icon="UserFilled" />
        <div class="user-detail">
          <span class="user-name">{{ currentUser.realName || currentUser.username }}</span>
          <el-tag :type="roleType" size="small">{{ currentUser.role }}</el-tag>
        </div>
      </div>
      <el-button type="danger" text @click="handleLogout">退出登录</el-button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { getCurrentUser, clearCurrentUser } from '@/utils/auth'
import { ROLE_COLOR_MAP } from '@/utils/constants'

const router = useRouter()

const currentUser = computed(() => {
  return getCurrentUser() || {}
})

const roleType = computed(() => {
  return ROLE_COLOR_MAP[currentUser.value.role] || 'info'
})

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(() => {
      clearCurrentUser()
      ElMessage.success('已退出登录')
      router.push('/login')
    })
    .catch(() => {})
}
</script>

<style scoped>
.app-header {
  height: 60px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.05);
  flex-shrink: 0;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  white-space: nowrap;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-detail {
  display: flex;
  flex-direction: column;
  line-height: 1.2;
}

.user-name {
  font-size: 14px;
  color: #303133;
  font-weight: 500;
}
</style>
