<template>
  <div class="app-sidebar">
    <el-menu
      :default-active="activeMenu"
      :router="true"
      background-color="#304156"
      text-color="#bfcbd9"
      active-text-color="#409eff"
      class="sidebar-menu"
    >
      <!-- 工作台 -->
      <el-menu-item index="/home/overview">
        <el-icon><DataBoard /></el-icon>
        <span>总览</span>
      </el-menu-item>

      <!-- 业务管理 -->
      <el-sub-menu index="business" v-if="showBusinessMenu">
        <template #title>
          <el-icon><Briefcase /></el-icon>
          <span>业务管理</span>
        </template>
        <el-menu-item index="/home/projects">
          <el-icon><Folder /></el-icon>
          <span>项目管理</span>
        </el-menu-item>
        <el-menu-item index="/home/ai-plan" v-if="isLeader">
          <el-icon><MagicStick /></el-icon>
          <span>任务拆解</span>
        </el-menu-item>
        <el-menu-item index="/home/tasks">
          <el-icon><List /></el-icon>
          <span>任务管理</span>
        </el-menu-item>
        <el-menu-item index="/home/task-logs">
          <el-icon><Timer /></el-icon>
          <span>进度跟踪</span>
        </el-menu-item>
        <el-menu-item index="/home/summaries">
          <el-icon><Document /></el-icon>
          <span>总结中心</span>
        </el-menu-item>
      </el-sub-menu>

      <!-- 如果非负责人且不显示业务菜单，直接平铺 -->
      <template v-if="!showBusinessMenu">
        <el-menu-item index="/home/projects">
          <el-icon><Folder /></el-icon>
          <span>项目管理</span>
        </el-menu-item>
        <el-menu-item index="/home/tasks">
          <el-icon><List /></el-icon>
          <span>任务管理</span>
        </el-menu-item>
        <el-menu-item index="/home/task-logs">
          <el-icon><Timer /></el-icon>
          <span>进度跟踪</span>
        </el-menu-item>
        <el-menu-item index="/home/summaries">
          <el-icon><Document /></el-icon>
          <span>总结中心</span>
        </el-menu-item>
      </template>

      <!-- 系统信息 -->
      <el-sub-menu index="system">
        <template #title>
          <el-icon><Setting /></el-icon>
          <span>系统信息</span>
        </template>
        <el-menu-item index="/home/members">
          <el-icon><User /></el-icon>
          <span>成员列表</span>
        </el-menu-item>
        <el-menu-item index="/home/profile">
          <el-icon><UserFilled /></el-icon>
          <span>个人信息</span>
        </el-menu-item>
      </el-sub-menu>
    </el-menu>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { isLeader } from '@/utils/auth'

const route = useRoute()

// 当前激活菜单
const activeMenu = computed(() => {
  return route.path
})

// 负责人/管理员显示分组菜单
const showBusinessMenu = computed(() => {
  return isLeader()
})
</script>

<style scoped>
.app-sidebar {
  width: 220px;
  height: 100%;
  overflow-y: auto;
  flex-shrink: 0;
}

.sidebar-menu {
  border-right: none;
  height: 100%;
}

.sidebar-menu:not(.el-menu--collapse) {
  width: 220px;
}
</style>
