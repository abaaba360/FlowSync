<template>
  <el-dialog
    v-model="visible"
    title="新增进度"
    width="500px"
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
      <el-form-item label="任务" prop="taskId">
        <el-select
          v-model="form.taskId"
          placeholder="请选择任务"
          style="width: 100%"
          filterable
        >
          <el-option
            v-for="t in taskList"
            :key="t.id"
            :label="t.title"
            :value="t.id"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="进度百分比" prop="progressPercent">
        <el-input-number
          v-model="form.progressPercent"
          :min="0"
          :max="100"
          :step="5"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="进度说明" prop="content">
        <el-input
          v-model="form.content"
          type="textarea"
          placeholder="请描述当前进度"
          maxlength="1000"
          show-word-limit
          :rows="4"
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
import { ref, reactive, watch } from 'vue'
import { getCurrentUserId } from '@/utils/auth'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
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
  taskId: null,
  progressPercent: 0,
  content: ''
})

const form = reactive(defaultForm())

const rules = {
  taskId: [{ required: true, message: '请选择任务', trigger: 'change' }],
  progressPercent: [
    { required: true, message: '请输入进度百分比', trigger: 'blur' },
    {
      type: 'number',
      min: 0,
      max: 100,
      message: '进度必须在 0 到 100 之间',
      trigger: 'blur'
    }
  ],
  content: [
    { required: true, message: '请输入进度说明', trigger: 'blur' },
    { max: 1000, message: '说明最长 1000 字', trigger: 'blur' }
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

const handleClosed = () => {
  Object.assign(form, defaultForm())
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const data = { ...form }
    data.operatorId = getCurrentUserId()
    emit('submit', data)
  } finally {
    submitting.value = false
  }
}
</script>
