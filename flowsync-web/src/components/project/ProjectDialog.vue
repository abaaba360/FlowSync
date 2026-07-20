<template>
  <el-dialog
    v-model="visible"
    :title="isEdit ? '编辑项目' : '新建项目'"
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
      <el-form-item label="项目名称" prop="name">
        <el-input v-model="form.name" placeholder="请输入项目名称" maxlength="100" show-word-limit />
      </el-form-item>
      <el-form-item label="项目说明" prop="description">
        <el-input
          v-model="form.description"
          type="textarea"
          placeholder="请输入项目说明"
          maxlength="500"
          show-word-limit
          :rows="3"
        />
      </el-form-item>
      <el-form-item label="状态" prop="status">
        <el-select v-model="form.status" placeholder="请选择状态" style="width: 100%">
          <el-option
            v-for="item in PROJECT_STATUS_OPTIONS"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="优先级" prop="priority">
        <el-select v-model="form.priority" placeholder="请选择优先级" style="width: 100%">
          <el-option
            v-for="item in PRIORITY_OPTIONS"
            :key="item.value"
            :label="item.label"
            :value="item.value"
          />
        </el-select>
      </el-form-item>
      <el-form-item label="开始日期" prop="startDate">
        <el-date-picker
          v-model="form.startDate"
          type="date"
          placeholder="请选择开始日期"
          value-format="YYYY-MM-DD"
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="结束日期" prop="endDate">
        <el-date-picker
          v-model="form.endDate"
          type="date"
          placeholder="请选择结束日期"
          value-format="YYYY-MM-DD"
          style="width: 100%"
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
import { ElMessage } from 'element-plus'
import { PROJECT_STATUS_OPTIONS, PRIORITY_OPTIONS } from '@/utils/constants'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  project: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:modelValue', 'submit'])

const visible = ref(false)
const submitting = ref(false)
const formRef = ref(null)
const isEdit = ref(false)

// 默认表单数据
const defaultForm = () => ({
  name: '',
  description: '',
  status: '进行中',
  priority: '中',
  startDate: '',
  endDate: ''
})

const form = reactive(defaultForm())

const rules = {
  name: [
    { required: true, message: '请输入项目名称', trigger: 'blur' },
    { max: 100, message: '项目名称最长 100 字', trigger: 'blur' }
  ],
  description: [{ max: 500, message: '项目说明最长 500 字', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
  priority: [{ required: true, message: '请选择优先级', trigger: 'change' }],
  startDate: [],
  endDate: [
    {
      validator: (_rule, value, callback) => {
        if (form.startDate && value && value < form.startDate) {
          callback(new Error('结束日期不能早于开始日期'))
        } else {
          callback()
        }
      },
      trigger: 'change'
    }
  ]
}

// 监听 modelValue 和 project 变化
watch(
  () => props.modelValue,
  (val) => {
    visible.value = val
    if (val) {
      if (props.project) {
        isEdit.value = true
        // 深拷贝，避免直接修改原始对象
        Object.assign(form, JSON.parse(JSON.stringify(props.project)))
      } else {
        isEdit.value = false
        Object.assign(form, defaultForm())
      }
      // 清除上一次校验
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
  isEdit.value = false
}

const handleSubmit = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    const data = { ...form }
    if (isEdit.value && props.project) {
      data.id = props.project.id
    }
    emit('submit', data)
  } finally {
    submitting.value = false
  }
}
</script>
