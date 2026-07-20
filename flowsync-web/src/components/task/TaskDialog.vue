<template>
  <el-dialog
    v-model="visible"
    :title="dialogTitle"
    width="600px"
    :close-on-click-modal="false"
    @closed="handleClosed"
  >
    <el-form
      ref="formRef"
      :model="form"
      :rules="rules"
      label-width="90px"
      label-position="right"
    >
      <el-form-item label="所属项目" prop="projectId">
        <el-select
          v-model="form.projectId"
          placeholder="请选择项目"
          style="width: 100%"
          :disabled="isMemberMode"
          @change="onProjectChange"
        >
          <el-option
            v-for="p in projectList"
            :key="p.id"
            :label="p.name"
            :value="p.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="父任务" prop="parentId">
        <el-select
          v-model="form.parentId"
          placeholder="请选择父任务（可选）"
          style="width: 100%"
          clearable
          :disabled="isMemberMode"
        >
          <el-option
            v-for="t in parentTaskOptions"
            :key="t.id"
            :label="t.title"
            :value="t.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="任务标题" prop="title">
        <el-input
          v-model="form.title"
          placeholder="请输入任务标题"
          maxlength="100"
          show-word-limit
          :disabled="isMemberMode"
        />
      </el-form-item>
      <el-form-item label="任务说明" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          placeholder="请输入任务说明"
          maxlength="500"
          show-word-limit
          :rows="3"
          :disabled="isMemberMode"
        />
      </el-form-item>
      <el-form-item label="负责人" prop="assigneeId">
        <el-select
          v-model="form.assigneeId"
          placeholder="请选择负责人"
          style="width: 100%"
          :disabled="isMemberMode"
        >
          <el-option
            v-for="u in userList"
            :key="u.id"
            :label="u.realName || u.username"
            :value="u.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="form.status" placeholder="请选择状态" style="width: 100%">
          <el-option
            v-for="item in TASK_STATUS_OPTIONS"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="优先级" prop="priority">
        <el-select
          v-model="form.priority"
          placeholder="请选择优先级"
          style="width: 100%"
          :disabled="isMemberMode"
        >
          <el-option
            v-for="item in PRIORITY_OPTIONS"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="截止日期" prop="dueDate">
        <el-date-picker
          v-model="form.dueDate"
          type="date"
          placeholder="请选择截止日期"
          value-format="YYYY-MM-DD"
          style="width: 100%"
          :disabled="isMemberMode"
        />
      </el-form-item>

      <!-- AI 建议展示（仅负责人编辑时） -->
      <el-form-item label="AI 建议" v-if="!isMemberMode && form.aiSuggestion">
        <el-input
          v-model="form.aiSuggestion"
          type="textarea"
          :rows="3"
          readonly
          placeholder="AI 建议内容"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { TASK_STATUS_OPTIONS, PRIORITY_OPTIONS } from '@/utils/constants'
import { getCurrentUser, isLeader } from '@/utils/auth'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  task: {
    type: Object,
    default: null
  },
  projectList: {
    type: Array,
    default: () => []
  },
  userList: {
    type: Array,
    default: () => []
  },
  allTasks: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue', 'submit'])

const visible = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const isEdit = ref(false)
const isMemberMode = ref(false)

// 对话框标题
const dialogTitle = computed(() => {
  if (isMemberMode.value) return '更新任务状态'
  return isEdit.value ? '编辑任务' : '新建任务'
})

// 默认表单数据
const defaultForm = () => ({
  projectId: null,
  parentId: null,
  title: '',
  description: '',
  assigneeId: null,
  status: '未开始',
  priority: '中',
  dueDate: '',
  aiSuggestion: ''
})

const form = reactive(defaultForm())

// 父任务选项（当前项目下，排除自身）
const parentTaskOptions = computed(() => {
  const tasks = props.allTasks || []
  let filtered = tasks.filter((t) => t.projectId === form.projectId)
  if (isEdit.value && props.task) {
    filtered = filtered.filter((t) => t.id !== props.task.id)
  }
  return filtered
})

const rules = {
  projectId: [{ required: true, message: '请选择项目', trigger: 'change' }],
  title: [{ required: true, message: '请输入任务标题', trigger: 'blur' }],
  assigneeId: [{ required: true, message: '请选择负责人', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
  priority: [{ required: true, message: '请选择优先级', trigger: 'change' }]
}

// 监听 modelValue 和 task 变化
watch(
  () => props.modelValue,
  (val) => {
    visible.value = val
    if (val) {
      const currentUser = getCurrentUser()
      if (props.task) {
        isEdit.value = true
        Object.assign(form, JSON.parse(JSON.stringify(props.task)))
        // 判断是否为成员更新自己任务
        if (
          !isLeader() &&
          props.task.assigneeId === currentUser?.id
        ) {
          isMemberMode.value = true
        } else {
          isMemberMode.value = false
        }
      } else {
        isEdit.value = false
        isMemberMode.value = false
        Object.assign(form, defaultForm())
      }
      setTimeout(() => {
        formRef.value?.clearValidate()
      }, 0)
    }
  }
)

watch(visible, (val) => {
  emit('update:modelValue', val)
})

// 项目变化时清空父任务
const onProjectChange = () => {
  form.parentId = null
}

const handleClosed = () => {
  Object.assign(form, defaultForm())
  isEdit.value = false
  isMemberMode.value = false
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const data = { ...form }
    if (isEdit.value && props.task) {
      data.id = props.task.id
      if (isMemberMode.value) {
        // 成员模式：只提交必要字段
        emit('submit', {
          id: props.task.id,
          status: data.status,
          // 保留原对象其他字段
          ...JSON.parse(JSON.stringify(props.task)),
          status: data.status
        })
        return
      }
    }
    // 设置创建人
    const currentUser = getCurrentUser()
    if (currentUser) {
      data.creatorId = currentUser.id
    }
    emit('submit', data)
  } finally {
    submitting.value = false
  }
}
</script>
