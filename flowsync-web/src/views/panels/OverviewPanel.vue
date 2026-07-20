<template>
  <div class="page-container">
    <PageHeader title="系统总览" />

    <!-- 统计卡片 -->
    <div class="stat-cards" v-loading="loading">
      <div class="stat-card">
        <div class="stat-icon users">
          <el-icon :size="28"><User /></el-icon>
        </div>
        <div class="stat-info">
          <h4>用户数量</h4>
          <div class="stat-number">{{ overview.userCount ?? 0 }}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon projects">
          <el-icon :size="28"><Folder /></el-icon>
        </div>
        <div class="stat-info">
          <h4>项目数量</h4>
          <div class="stat-number">{{ overview.projectCount ?? 0 }}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon tasks">
          <el-icon :size="28"><List /></el-icon>
        </div>
        <div class="stat-info">
          <h4>任务数量</h4>
          <div class="stat-number">{{ overview.taskCount ?? 0 }}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon summaries">
          <el-icon :size="28"><Document /></el-icon>
        </div>
        <div class="stat-info">
          <h4>总结数量</h4>
          <div class="stat-number">{{ overview.summaryCount ?? 0 }}</div>
        </div>
      </div>
    </div>

    <!-- 任务状态分布（可选增强） -->
    <div class="content-card" v-if="hasTaskDistribution">
      <h3 style="margin-bottom: 16px; font-size: 16px; color: #303133">任务状态分布</h3>
      <el-row :gutter="20">
        <el-col :span="8" v-if="overview.notStartedTaskCount !== undefined">
          <div class="task-status-item">
            <span class="status-label">未开始</span>
            <el-progress
              :percentage="taskPercent(overview.notStartedTaskCount)"
              :color="'#909399'"
              :stroke-width="16"
            />
          </div>
        </el-col>
        <el-col :span="8" v-if="overview.inProgressTaskCount !== undefined">
          <div class="task-status-item">
            <span class="status-label">进行中</span>
            <el-progress
              :percentage="taskPercent(overview.inProgressTaskCount)"
              :color="'#409eff'"
              :stroke-width="16"
            />
          </div>
        </el-col>
        <el-col :span="8" v-if="overview.completedTaskCount !== undefined">
          <div class="task-status-item">
            <span class="status-label">已完成</span>
            <el-progress
              :percentage="taskPercent(overview.completedTaskCount)"
              :color="'#67c23a'"
              :stroke-width="16"
            />
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 加载失败 -->
    <div v-if="loadError" class="content-card" style="text-align: center; padding: 40px">
      <p style="color: #909399; margin-bottom: 12px">数据加载失败</p>
      <el-button type="primary" @click="fetchOverview">重新加载</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import PageHeader from '@/components/common/PageHeader.vue'
import { getOverview } from '@/api/overview'

const loading = ref(false)
const loadError = ref(false)

const overview = reactive({
  userCount: 0,
  projectCount: 0,
  taskCount: 0,
  summaryCount: 0,
  notStartedTaskCount: undefined,
  inProgressTaskCount: undefined,
  completedTaskCount: undefined
})

// 是否有任务分布数据
const hasTaskDistribution = computed(() => {
  return (
    overview.notStartedTaskCount !== undefined ||
    overview.inProgressTaskCount !== undefined ||
    overview.completedTaskCount !== undefined
  )
})

// 计算百分比
const taskPercent = (count) => {
  const total = overview.taskCount || 1
  return Math.round((count / total) * 100)
}

const fetchOverview = async () => {
  loading.value = true
  loadError.value = false
  try {
    const data = await getOverview()
    if (data) {
      overview.userCount = data.userCount ?? 0
      overview.projectCount = data.projectCount ?? 0
      overview.taskCount = data.taskCount ?? 0
      overview.summaryCount = data.summaryCount ?? 0
      overview.notStartedTaskCount = data.notStartedTaskCount
      overview.inProgressTaskCount = data.inProgressTaskCount
      overview.completedTaskCount = data.completedTaskCount
    }
  } catch {
    loadError.value = true
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchOverview()
})
</script>

<style scoped>
.task-status-item {
  padding: 12px 0;
}

.status-label {
  display: block;
  font-size: 13px;
  color: #606266;
  margin-bottom: 8px;
}
</style>
