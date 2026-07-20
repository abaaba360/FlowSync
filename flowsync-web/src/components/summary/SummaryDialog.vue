<template>
  <el-dialog
    v-model="visible"
    title="新增总结"
    width="560px"
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
      <el-form-item label="关联任务" prop="taskId">
        <el-select
          v-model="form.taskId"
          placeholder="请选择任务（可选）"
          style="width: 100%"
          clearable
        >
          <el-option
            v-for="t in filteredTasks"
            :key="t.id"
            :label="t.title"
            :value="t.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="总结类型" prop="summaryType">
        <el-select v-model="form.summaryType" placeholder="请选择总结类型" style="width: 100%">
          <el-option
            v-for="item in SUMMARY_TYPE_OPTIONS"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="总结内容" prop="content">
        <el-input
          v-model="form.content"
          type="textarea"
          placeholder="请输入总结内容"
          maxlength="2000"
          show-word-limit
          :rows="5"
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
import { SUMMARY_TYPE_OPTIONS } from '@/utils/constants'
import { getCurrentUserId } from '@/utils/auth'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  projectList: {
    type: Array,
    default: () => []
  },
  taskList: {
    type: Array,
    default: () => []
  }
})

const emit = defineEmits(['update:modelValue', 'submit'])

const visible = ref(false)
const submitting = ref(false)
const formRef = ref(null)

const defaultForm = () => ({
  projectId: null,
  taskId: null,
  summaryType: '',
  content: ''
})

const form = reactive(defaultForm())

// 根据所选项目过滤任务
const filteredTasks = computed(() => {
  if (!form.projectId) return props.taskList || []
  return (props.taskList || []).filter((t) => t.projectId === form.projectId)
})

const rules = {
  projectId: [{ required: true, message: '请选择项目', trigger: 'change' }],
  summaryType: [{ required: true, message: '请选择总结类型', trigger: 'change' }],
  content: [
    { required: true, message: '请输入总结内容', trigger: 'blur' },
    { max: 2000, message: '内容最长 2000 字', trigger: 'blur' }
  ]
}

watch(
  () => props.modelValue,
  (val) => {
    visible.value = val
    if (val) {
      Object.assign(form, defaultForm())
      setTimeout(() => {
        formRef.value?.clearValidate()
      }, 0)
    }
  }
)

watch(visible, (val) => {
  emit('update:modelValue', val)
})

// 切换项目时清空关联任务
const onProjectChange = () => {
  form.taskId = null
}

const handleClosed = () => {
  Object.assign(form, defaultForm())
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const data = { ...form }
    data.createdBy = getCurrentUserId()
    emit('submit', data)
  } finally {
    submitting.value = false
  }
}
</script>
