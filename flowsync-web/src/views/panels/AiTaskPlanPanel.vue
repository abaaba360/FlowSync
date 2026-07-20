<template>
  <div class="page-container">
    <PageHeader title="AI 任务拆解" />

    <!-- 输入区域 -->
    <div class="content-card" style="margin-bottom: 20px">
      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="90px"
        label-position="right"
      >
        <el-form-item label="选择项目" prop="projectId">
          <el-select
            v-model="form.projectId"
            placeholder="请选择项目"
            style="width: 100%"
            filterable
          >
            <el-option
              v-for="p in projectList"
              :key="p.id"
              :label="p.name"
              :value="p.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="任务目标" prop="goal">
          <el-input
            v-model="form.goal"
            type="textarea"
            placeholder="请描述任务目标，例如：两周内完成系统开发和答辩准备"
            :rows="3"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="补充说明" prop="description">
          <el-input
            v-model="form.description"
            type="textarea"
            placeholder="补充说明（可选），例如：团队共有5人..."
            :rows="3"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="aiLoading" @click="handleGenerate">
            <el-icon><MagicStick /></el-icon> 开始 AI 拆解
          </el-button>
          <el-button @click="handleClearInput">清空</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- AI 生成中状态 -->
    <div class="content-card" v-if="aiLoading" style="text-align: center; padding: 60px">
      <el-icon class="is-loading" :size="40" color="#409eff"><Loading /></el-icon>
      <p style="margin-top: 16px; color: #606266; font-size: 15px">AI 正在分析任务目标...</p>
      <p style="color: #c0c4cc; font-size: 13px">这可能需要几秒钟，请耐心等待</p>
    </div>

    <!-- AI 结果区域 -->
    <div class="content-card" v-if="aiResult && !aiLoading">
      <!-- AI 概述 -->
      <div class="ai-summary" v-if="aiResult.summary">
        <el-icon :size="20" color="#409eff"><MagicStick /></el-icon>
        <span>{{ aiResult.summary }}</span>
      </div>

      <!-- 操作条 -->
      <div style="display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px">
        <div>
          <span style="font-size: 14px; color: #606266">
            共 <strong>{{ aiItems.length }}</strong> 个任务
          </span>
          <el-checkbox
            v-model="selectAll"
            style="margin-left: 16px"
            :indeterminate="isIndeterminate"
            @change="handleSelectAll"
          >
            全选
          </el-checkbox>
        </div>
        <div>
          <el-button type="primary" @click="handleImport" :loading="importLoading">
            <el-icon><Upload /></el-icon> 导入选中任务
          </el-button>
          <el-button @click="handleClearResult">清空结果</el-button>
        </div>
      </div>

      <!-- 任务表格 -->
      <el-table :data="aiItems" border stripe style="width: 100%">
        <el-table-column width="55" align="center">
          <template #default="{ row }">
            <el-checkbox v-model="row.selected" />
          </template>
        </el-table-column>
        <el-table-column prop="title" label="任务标题" min-width="160" show-overflow-tooltip />
        <el-table-column prop="description" label="任务说明" min-width="220" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.description || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="90" align="center">
          <template #default="{ row }">
            <StatusTag :status="row.priority" type="priority" />
          </template>
        </el-table-column>
        <el-table-column prop="suggestedDays" label="建议天数" width="100" align="center">
          <template #default="{ row }">
            {{ row.suggestedDays ? row.suggestedDays + ' 天' : '-' }}
          </template>
        </el-table-column>
        <el-table-column label="推荐负责人" width="180">
          <template #default="{ row }">
            <el-select
              v-model="row.assigneeId"
              placeholder="请选择负责人"
              size="small"
              style="width: 100%"
              clearable
            >
              <el-option
                v-for="u in userList"
                :key="u.id"
                :label="u.realName || u.username"
                :value="u.id"
              />
            </el-select>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80" align="center">
          <template #default="{ $index }">
            <el-button type="danger" size="small" text @click="removeItem($index)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 无结果时的空状态 -->
    <EmptyState
      v-if="!aiResult && !aiLoading"
      description="请输入项目、任务目标和补充说明，然后点击「开始 AI 拆解」"
    />

    <!-- 导入成功后的跳转提示 -->
    <div class="content-card" v-if="importSuccess" style="text-align: center; padding: 32px; margin-top: 20px">
      <el-icon :size="48" color="#67c23a"><CircleCheckFilled /></el-icon>
      <p style="margin-top: 12px; font-size: 16px; color: #303133">
        成功导入 {{ importedCount }} 个任务！
      </p>
      <div style="margin-top: 16px">
        <el-button type="primary" @click="$router.push('/home/tasks')">前往任务管理</el-button>
        <el-button @click="handleClearAll">继续拆解</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import PageHeader from '@/components/common/PageHeader.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import { getAiTaskPlan, importAiTasks } from '@/api/ai'
import { getProjectList } from '@/api/project'
import { getUserList } from '@/api/user'
import { getCurrentUserId } from '@/utils/auth'

const router = useRouter()
const formRef = ref(null)

// 输入表单
const form = reactive({
  projectId: null,
  goal: '',
  description: ''
})

const rules = {
  projectId: [{ required: true, message: '请选择项目', trigger: 'change' }],
  goal: [{ required: true, message: '请输入任务目标', trigger: 'blur' }]
}

// 数据列表
const projectList = ref([])
const userList = ref([])

// AI 状态
const aiLoading = ref(false)
const aiResult = ref(null)
const aiItems = ref([])
const importLoading = ref(false)
const importSuccess = ref(false)
const importedCount = ref(0)

// 全选
const selectAll = ref(true)
const isIndeterminate = computed(() => {
  const items = aiItems.value
  if (!items.length) return false
  const selectedCount = items.filter((i) => i.selected).length
  return selectedCount > 0 && selectedCount < items.length
})

// 获取项目和用户数据
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

// 获取当前项目的名称
const getProjectName = () => {
  const p = projectList.value.find((p) => p.id === form.projectId)
  return p ? p.name : ''
}

// 开始 AI 拆解
const handleGenerate = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  aiLoading.value = true
  importSuccess.value = false
  try {
    const params = {
      projectId: form.projectId,
      operatorId: getCurrentUserId(),
      projectName: getProjectName(),
      goal: form.goal,
      description: form.description || ''
    }
    const result = await getAiTaskPlan(params)
    aiResult.value = result

    // 为任务补充临时字段
    const items = Array.isArray(result.items) ? result.items : []
    aiItems.value = items.map((item) => ({
      ...item,
      selected: true,
      assigneeId: item.assigneeId || null
    }))
    selectAll.value = true
  } catch {
    // 错误已在拦截器中提示
  } finally {
    aiLoading.value = false
  }
}

// 全选/取消全选
const handleSelectAll = (val) => {
  aiItems.value.forEach((item) => {
    item.selected = val
  })
}

// 删除本地结果中的某一行
const removeItem = (index) => {
  aiItems.value.splice(index, 1)
  // 如果全部删除则清空结果
  if (aiItems.value.length === 0) {
    aiResult.value = null
  }
}

// 导入前校验
const validateImport = () => {
  const selected = aiItems.value.filter((i) => i.selected)
  if (selected.length === 0) {
    ElMessage.warning('请至少选择一项任务')
    return false
  }
  const noAssignee = selected.some((i) => !i.assigneeId)
  if (noAssignee) {
    ElMessage.warning('每个选中的任务都必须指定负责人')
    return false
  }
  const emptyTitle = selected.some((i) => !i.title)
  if (emptyTitle) {
    ElMessage.warning('任务标题不能为空')
    return false
  }
  return true
}

// 导入任务
const handleImport = () => {
  if (!validateImport()) return

  const selected = aiItems.value.filter((i) => i.selected)
  ElMessageBox.confirm(
    `确认导入 ${selected.length} 个选中任务到项目吗？`,
    '确认导入',
    {
      confirmButtonText: '确认导入',
      cancelButtonText: '取消',
      type: 'info'
    }
  )
    .then(async () => {
      importLoading.value = true
      try {
        const items = selected.map((item) => ({
          title: item.title,
          description: item.description || '',
          priority: item.priority || '中',
          suggestedDays: item.suggestedDays || 0,
          assigneeId: item.assigneeId
        }))
        await importAiTasks({
          projectId: form.projectId,
          creatorId: getCurrentUserId(),
          items
        })
        importedCount.value = items.length
        importSuccess.value = true
        ElMessage.success(`成功导入 ${items.length} 个任务`)
      } catch {
        // 错误已在拦截器中提示
      } finally {
        importLoading.value = false
      }
    })
    .catch(() => {})
}

// 清空输入
const handleClearInput = () => {
  form.projectId = null
  form.goal = ''
  form.description = ''
  formRef.value?.clearValidate()
}

// 清空 AI 结果
const handleClearResult = () => {
  aiResult.value = null
  aiItems.value = []
  importSuccess.value = false
}

// 全部清空
const handleClearAll = () => {
  handleClearInput()
  handleClearResult()
}

onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.ai-summary {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 16px;
  background: #ecf5ff;
  border-radius: 8px;
  margin-bottom: 20px;
  font-size: 14px;
  color: #409eff;
  line-height: 1.6;
}
</style>
