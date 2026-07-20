<template>
  <div class="page-container">
    <PageHeader title="任务管理">
      <template #extra>
        <el-button type="primary" @click="handleCreate" v-if="isLeader">
          <el-icon><Plus /></el-icon> 新建任务
        </el-button>
      </template>
    </PageHeader>

    <!-- 筛选区域 -->
    <div class="filter-bar">
      <el-select v-model="filterProjectId" placeholder="项目筛选" clearable>
        <el-option v-for="p in projectList" :key="p.id" :label="p.name" :value="p.id" />
      </el-select>
      <el-select v-model="filterStatus" placeholder="状态筛选" clearable>
        <el-option v-for="item in TASK_STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <el-select v-model="filterPriority" placeholder="优先级筛选" clearable>
        <el-option v-for="item in PRIORITY_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
      </el-select>
      <el-select v-model="filterAssignee" placeholder="负责人筛选" clearable>
        <el-option v-for="u in userList" :key="u.id" :label="u.realName || u.username" :value="u.id" />
      </el-select>
      <el-input v-model="searchKeyword" placeholder="搜索任务标题" clearable style="width: 200px" />
      <el-button @click="handleReset">重置</el-button>
    </div>

    <!-- 表格 -->
    <div class="content-card" v-loading="loading">
      <el-table :data="filteredTasks" border stripe style="width: 100%" v-if="filteredTasks.length > 0">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="title" label="任务标题" min-width="160" show-overflow-tooltip />
        <el-table-column prop="projectName" label="所属项目" min-width="130" show-overflow-tooltip />
        <el-table-column prop="parentTitle" label="父任务" min-width="130" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.parentTitle || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="assigneeName" label="负责人" width="100" />
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <StatusTag :status="row.status" type="task" />
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="90" align="center">
          <template #default="{ row }">
            <StatusTag :status="row.priority" type="priority" />
          </template>
        </el-table-column>
        <el-table-column prop="dueDate" label="截止日期" width="110" align="center">
          <template #default="{ row }">
            <span :class="{ 'overdue-text': isOverdue(row.dueDate, row.status) }">
              {{ formatDate(row.dueDate) || '-' }}
              <el-tag v-if="isOverdue(row.dueDate, row.status)" type="danger" size="small" style="margin-left: 4px">已逾期</el-tag>
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="creatorName" label="创建人" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="160" align="center">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <div class="table-actions">
              <!-- 负责人/管理员：编辑和删除 -->
              <template v-if="isLeader">
                <el-button type="primary" size="small" text @click="handleEdit(row)">编辑</el-button>
                <el-button type="danger" size="small" text @click="handleDelete(row)">删除</el-button>
              </template>
              <!-- 普通成员：仅自己负责的任务显示更新状态 -->
              <template v-else-if="row.assigneeId === currentUserId">
                <el-button type="primary" size="small" text @click="handleEdit(row)">更新状态</el-button>
              </template>
              <span v-else style="color: #c0c4cc">-</span>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <EmptyState v-else description="暂无任务数据" />
    </div>

    <!-- 任务弹窗 -->
    <TaskDialog
      v-model="dialogVisible"
      :task="currentTask"
      :project-list="projectList"
      :user-list="userList"
      :all-tasks="taskList"
      @submit="handleSubmit"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/common/PageHeader.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import TaskDialog from '@/components/task/TaskDialog.vue'
import { getTaskList, saveTask, deleteTask } from '@/api/task'
import { getProjectList } from '@/api/project'
import { getUserList } from '@/api/user'
import { isLeader, getCurrentUserId } from '@/utils/auth'
import { TASK_STATUS_OPTIONS, PRIORITY_OPTIONS } from '@/utils/constants'
import { formatDate, formatDateTime, isOverdue } from '@/utils/format'

const loading = ref(false)
const dialogVisible = ref(false)
const taskList = ref([])
const currentTask = ref(null)
const projectList = ref([])
const userList = ref([])
const currentUserId = computed(() => getCurrentUserId())

// 筛选条件
const filterProjectId = ref(null)
const filterStatus = ref('')
const filterPriority = ref('')
const filterAssignee = ref(null)
const searchKeyword = ref('')

// 过滤后的任务列表
const filteredTasks = computed(() => {
  let list = taskList.value
  if (filterProjectId.value) {
    list = list.filter((t) => t.projectId === filterProjectId.value)
  }
  if (filterStatus.value) {
    list = list.filter((t) => t.status === filterStatus.value)
  }
  if (filterPriority.value) {
    list = list.filter((t) => t.priority === filterPriority.value)
  }
  if (filterAssignee.value) {
    list = list.filter((t) => t.assigneeId === filterAssignee.value)
  }
  if (searchKeyword.value) {
    const kw = searchKeyword.value.toLowerCase()
    list = list.filter((t) => t.title && t.title.toLowerCase().includes(kw))
  }
  return [...list].sort((a, b) => new Date(a.createTime) - new Date(b.createTime))
})

// 获取数据
const fetchTasks = async () => {
  loading.value = true
  try {
    const data = await getTaskList()
    taskList.value = Array.isArray(data) ? data : []
  } finally {
    loading.value = false
  }
}

const fetchData = async () => {
  try {
    const [projects, users] = await Promise.all([
      getProjectList(),
      getUserList()
    ])
    projectList.value = Array.isArray(projects) ? projects : []
    userList.value = Array.isArray(users) ? users : []
  } catch {
    // 错误已在拦截器中提示
  }
}

const handleCreate = () => {
  currentTask.value = null
  dialogVisible.value = true
}

const handleEdit = (row) => {
  currentTask.value = row
  dialogVisible.value = true
}

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除任务"${row.title}"吗？`, '确认删除', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        await deleteTask(row.id)
        ElMessage.success('删除成功')
        fetchTasks()
      } catch {
        // 错误已在拦截器中提示
      }
    })
    .catch(() => {})
}

const handleSubmit = async (data) => {
  try {
    await saveTask(data)
    ElMessage.success(data.id ? '任务更新成功' : '任务创建成功')
    dialogVisible.value = false
    fetchTasks()
  } catch {
    // 错误已在拦截器中提示
  }
}

const handleReset = () => {
  filterProjectId.value = null
  filterStatus.value = ''
  filterPriority.value = ''
  filterAssignee.value = null
  searchKeyword.value = ''
}

onMounted(() => {
  fetchTasks()
  fetchData()
})
</script>

<style scoped>
.overdue-text {
  color: #f56c6c;
}
</style>
