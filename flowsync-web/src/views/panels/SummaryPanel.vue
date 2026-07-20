<template>
  <div class="page-container">
    <PageHeader title="总结中心">
      <template #extra>
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon> 新增总结
        </el-button>
      </template>
    </PageHeader>

    <!-- 筛选区域 -->
    <div class="filter-bar">
      <el-select v-model="filterProjectId" placeholder="项目筛选" clearable>
        <el-option v-for="p in projectList" :key="p.id" :label="p.name" :value="p.id" />
      </el-select>
      <el-select v-model="filterSummaryType" placeholder="总结类型筛选" clearable>
        <el-option v-for="item in SUMMARY_TYPE_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <el-select v-model="filterCreator" placeholder="创建人筛选" clearable>
        <el-option v-for="u in userList" :key="u.id" :label="u.realName || u.username" :value="u.id" />
      </el-select>
    </div>

    <!-- 卡片列表 -->
    <div v-loading="loading">
      <div v-if="filteredSummaries.length > 0" class="summary-cards">
        <div
          v-for="item in filteredSummaries"
          :key="item.id"
          class="summary-card"
        >
          <div class="summary-card-header">
            <div class="summary-card-meta">
              <el-tag
                :type="item.summaryType === '最终总结' ? 'success' : ''"
                size="small"
              >
                {{ item.summaryType || '总结' }}
              </el-tag>
              <span class="summary-project">{{ item.projectName || '-' }}</span>
              <span v-if="item.taskTitle" class="summary-task">
                <el-icon><Connection /></el-icon>
                {{ item.taskTitle }}
              </span>
            </div>
          </div>
          <div class="summary-card-body">
            {{ item.content }}
          </div>
          <div class="summary-card-footer">
            <span class="summary-creator">
              <el-icon><User /></el-icon> {{ item.creatorName || '-' }}
            </span>
            <span class="summary-time">
              <el-icon><Clock /></el-icon> {{ formatDateTime(item.createTime) }}
            </span>
          </div>
        </div>
      </div>
      <EmptyState v-else description="暂无总结数据" />
    </div>

    <!-- 新增总结弹窗 -->
    <SummaryDialog
      v-model="dialogVisible"
      :project-list="projectList"
      :task-list="taskList"
      @submit="handleSubmit"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/common/PageHeader.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import SummaryDialog from '@/components/summary/SummaryDialog.vue'
import { getSummaryList, addSummary } from '@/api/summary'
import { getProjectList } from '@/api/project'
import { getTaskList } from '@/api/task'
import { getUserList } from '@/api/user'
import { SUMMARY_TYPE_OPTIONS } from '@/utils/constants'
import { formatDateTime } from '@/utils/format'

const loading = ref(false)
const dialogVisible = ref(false)
const summaryList = ref([])
const projectList = ref([])
const taskList = ref([])
const userList = ref([])

// 筛选
const filterProjectId = ref(null)
const filterSummaryType = ref('')
const filterCreator = ref(null)

const filteredSummaries = computed(() => {
  let list = summaryList.value
  if (filterProjectId.value) {
    list = list.filter((s) => s.projectId === filterProjectId.value)
  }
  if (filterSummaryType.value) {
    list = list.filter((s) => s.summaryType === filterSummaryType.value)
  }
  if (filterCreator.value) {
    list = list.filter((s) => s.createdBy === filterCreator.value)
  }
  return [...list].sort((a, b) => new Date(a.createTime) - new Date(b.createTime))
})

// 获取总结列表
const fetchSummaries = async () => {
  loading.value = true
  try {
    const data = await getSummaryList()
    summaryList.value = Array.isArray(data) ? data : []
  } finally {
    loading.value = false
  }
}

// 获取项目和用户数据
const fetchData = async () => {
  try {
    const [projects, tasks, users] = await Promise.all([
      getProjectList(),
      getTaskList(),
      getUserList()
    ])
    projectList.value = Array.isArray(projects) ? projects : []
    taskList.value = Array.isArray(tasks) ? tasks : []
    userList.value = Array.isArray(users) ? users : []
  } catch {
    // 错误已在拦截器中提示
  }
}

const handleCreate = () => {
  dialogVisible.value = true
}

const handleSubmit = async (data) => {
  try {
    await addSummary(data)
    ElMessage.success('总结创建成功')
    dialogVisible.value = false
    fetchSummaries()
  } catch {
    // 错误已在拦截器中提示
  }
}

onMounted(() => {
  fetchSummaries()
  fetchData()
})
</script>

<style scoped>
.summary-cards {
  display: grid;
  gap: 16px;
}

.summary-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.06);
  transition: box-shadow 0.2s;
}

.summary-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.summary-card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.summary-card-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 13px;
  color: #606266;
}

.summary-project {
  font-weight: 500;
}

.summary-task {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #909399;
}

.summary-card-body {
  font-size: 14px;
  color: #303133;
  line-height: 1.7;
  margin-bottom: 16px;
  white-space: pre-wrap;
}

.summary-card-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
  color: #c0c4cc;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
}

.summary-creator,
.summary-time {
  display: flex;
  align-items: center;
  gap: 4px;
}
</style>
