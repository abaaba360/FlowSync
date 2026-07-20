<template>
  <div class="page-container">
    <PageHeader title="项目管理">
      <template #extra>
        <el-button type="primary" @click="handleCreate" v-if="isLeader">
          <el-icon><Plus /></el-icon> 新建项目
        </el-button>
      </template>
    </PageHeader>

    <!-- 筛选区域 -->
    <div class="filter-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索项目名称"
        clearable
        @clear="fetchProjects"
      />
      <el-select
        v-model="filterStatus"
        placeholder="状态筛选"
        clearable
        @change="fetchProjects"
      >
        <el-option
          v-for="item in PROJECT_STATUS_OPTIONS"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>
      <el-select
        v-model="filterPriority"
        placeholder="优先级筛选"
        clearable
        @change="fetchProjects"
      >
        <el-option
          v-for="item in PRIORITY_OPTIONS"
          :key="item.value"
          :label="item.label"
          :value="item.value"
        />
      </el-select>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <!-- 表格区域 -->
    <div class="content-card" v-loading="loading">
      <el-table :data="filteredProjects" border stripe style="width: 100%" v-if="filteredProjects.length > 0">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="name" label="项目名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="description" label="项目说明" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.description || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <StatusTag :status="row.status" type="project" />
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="90" align="center">
          <template #default="{ row }">
            <StatusTag :status="row.priority" type="priority" />
          </template>
        </el-table-column>
        <el-table-column prop="ownerName" label="负责人" width="110" show-overflow-tooltip />
        <el-table-column prop="startDate" label="开始日期" width="110" align="center">
          <template #default="{ row }">
            {{ formatDate(row.startDate) || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="endDate" label="结束日期" width="110" align="center">
          <template #default="{ row }">
            {{ formatDate(row.endDate) || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" align="center">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160" align="center" fixed="right" v-if="isLeader">
          <template #default="{ row }">
            <div class="table-actions">
              <el-button type="primary" size="small" text @click="handleEdit(row)">编辑</el-button>
              <el-button type="danger" size="small" text @click="handleDelete(row)">删除</el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>
      <EmptyState v-else description="暂无项目数据" />
    </div>

    <!-- 项目弹窗 -->
    <ProjectDialog
      v-model="dialogVisible"
      :project="currentProject"
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
import ProjectDialog from '@/components/project/ProjectDialog.vue'
import { getProjectList, saveProject, deleteProject } from '@/api/project'
import { isLeader, getCurrentUserId } from '@/utils/auth'
import { PROJECT_STATUS_OPTIONS, PRIORITY_OPTIONS } from '@/utils/constants'
import { formatDate, formatDateTime } from '@/utils/format'

const loading = ref(false)
const dialogVisible = ref(false)
const projectList = ref([])
const currentProject = ref(null)

// 筛选条件
const searchKeyword = ref('')
const filterStatus = ref('')
const filterPriority = ref('')

// 过滤后的项目列表
const filteredProjects = computed(() => {
  let list = projectList.value
  if (searchKeyword.value) {
    const kw = searchKeyword.value.toLowerCase()
    list = list.filter((p) => p.name && p.name.toLowerCase().includes(kw))
  }
  if (filterStatus.value) {
    list = list.filter((p) => p.status === filterStatus.value)
  }
  if (filterPriority.value) {
    list = list.filter((p) => p.priority === filterPriority.value)
  }
  return [...list].sort((a, b) => new Date(a.createTime) - new Date(b.createTime))
})

// 获取项目列表
const fetchProjects = async () => {
  loading.value = true
  try {
    const data = await getProjectList()
    projectList.value = Array.isArray(data) ? data : []
  } finally {
    loading.value = false
  }
}

// 新建项目
const handleCreate = () => {
  currentProject.value = null
  dialogVisible.value = true
}

// 编辑项目
const handleEdit = (row) => {
  currentProject.value = row
  dialogVisible.value = true
}

// 删除项目
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定删除项目"${row.name}"吗？`, '确认删除', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(async () => {
      try {
        await deleteProject(row.id)
        ElMessage.success('删除成功')
        fetchProjects()
      } catch {
        // 错误已在拦截器中提示
      }
    })
    .catch(() => {})
}

// 提交表单
const handleSubmit = async (data) => {
  try {
    // 创建时使用当前用户作为负责人
    if (!data.id) {
      data.ownerId = getCurrentUserId()
    }
    await saveProject(data)
    ElMessage.success(data.id ? '编辑成功' : '创建成功')
    dialogVisible.value = false
    fetchProjects()
  } catch {
    // 错误已在拦截器中提示
  }
}

// 重置筛选
const handleReset = () => {
  searchKeyword.value = ''
  filterStatus.value = ''
  filterPriority.value = ''
  fetchProjects()
}

onMounted(() => {
  fetchProjects()
})
</script>
