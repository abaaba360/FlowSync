<template>
  <div class="page-container">
    <PageHeader title="进度跟踪">
      <template #extra>
        <el-button type="primary" @click="handleCreate">
          <el-icon><Plus /></el-icon> 新增进度
        </el-button>
        <el-button @click="fetchTaskLogs">
          <el-icon><Refresh /></el-icon> 刷新
        </el-button>
      </template>
    </PageHeader>

    <!-- 筛选区域 -->
    <div class="filter-bar">
      <el-select v-model="filterTaskId" placeholder="任务筛选" clearable filterable>
        <el-option v-for="t in taskList" :key="t.id" :label="t.title" :value="t.id" />
      </el-select>
      <el-select v-model="filterOperator" placeholder="记录人筛选" clearable>
        <el-option v-for="u in userList" :key="u.id" :label="u.realName || u.username" :value="u.id" />
      </el-select>
    </div>

    <!-- 表格 -->
    <div class="content-card" v-loading="loading">
      <el-table :data="filteredLogs" border stripe style="width: 100%" v-if="filteredLogs.length > 0">
        <el-table-column prop="taskTitle" label="任务" min-width="160" show-overflow-tooltip />
        <el-table-column label="进度" width="200">
          <template #default="{ row }">
            <el-progress
              :percentage="row.progressPercent ?? 0"
              :color="row.progressPercent >= 100 ? '#67c23a' : '#409eff'"
              :stroke-width="14"
            />
          </template>
        </el-table-column>
        <el-table-column prop="content" label="进度说明" min-width="250" show-overflow-tooltip />
        <el-table-column prop="operatorName" label="记录人" width="100" align="center" />
        <el-table-column prop="createTime" label="记录时间" width="160" align="center">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
      </el-table>
      <EmptyState v-else description="暂无进度记录" />
    </div>

    <!-- 新增进度弹窗 -->
    <TaskLogDialog
      v-model="dialogVisible"
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
import TaskLogDialog from '@/components/task-log/TaskLogDialog.vue'
import { getTaskLogList, addTaskLog } from '@/api/taskLog'
import { getTaskList } from '@/api/task'
import { getUserList } from '@/api/user'
import { formatDateTime } from '@/utils/format'

const loading = ref(false)
const dialogVisible = ref(false)
const taskLogList = ref([])
const taskList = ref([])
const userList = ref([])

// 筛选
const filterTaskId = ref(null)
const filterOperator = ref(null)

const filteredLogs = computed(() => {
  let list = taskLogList.value
  if (filterTaskId.value) {
    list = list.filter((l) => l.taskId === filterTaskId.value)
  }
  if (filterOperator.value) {
    list = list.filter((l) => l.operatorId === filterOperator.value)
  }
  return [...list].sort((a, b) => new Date(a.createTime) - new Date(b.createTime))
})

// 获取进度记录
const fetchTaskLogs = async () => {
  loading.value = true
  try {
    const data = await getTaskLogList()
    taskLogList.value = Array.isArray(data) ? data : []
  } finally {
    loading.value = false
  }
}

// 获取任务和用户数据
const fetchData = async () => {
  try {
    const [tasks, users] = await Promise.all([
      getTaskList(),
      getUserList()
    ])
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
    await addTaskLog(data)
    ElMessage.success('进度记录成功')
    dialogVisible.value = false
    fetchTaskLogs()
  } catch {
    // 错误已在拦截器中提示
  }
}

onMounted(() => {
  fetchTaskLogs()
  fetchData()
})
</script>
