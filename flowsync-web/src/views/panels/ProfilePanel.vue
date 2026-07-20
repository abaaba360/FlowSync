<template>
  <div class="page-container">
    <PageHeader title="个人信息" />

    <div class="content-card" style="max-width: 600px">
      <!-- 用户信息展示 -->
      <el-descriptions title="基本信息" :column="2" border>
        <el-descriptions-item label="用户名">
          {{ currentUser.username || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="真实姓名">
          {{ currentUser.realName || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="角色">
          <StatusTag :status="currentUser.role" type="role" />
        </el-descriptions-item>
        <el-descriptions-item label="手机号">
          {{ currentUser.phone || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="邮箱">
          {{ currentUser.email || '-' }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ formatDateTime(currentUser.createTime) }}
        </el-descriptions-item>
      </el-descriptions>

      <!-- 修改密码 -->
      <div class="password-section">
        <h3 class="section-title">修改密码</h3>
        <el-form
          ref="formRef"
          :model="passwordForm"
          :rules="rules"
          label-width="100px"
          label-position="right"
          style="max-width: 420px"
        >
          <el-form-item label="旧密码" prop="oldPassword">
            <el-input
              v-model="passwordForm.oldPassword"
              type="password"
              placeholder="请输入旧密码"
              show-password
            />
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input
              v-model="passwordForm.newPassword"
              type="password"
              placeholder="请输入新密码（不少于6位）"
              show-password
            />
          </el-form-item>
          <el-form-item label="确认新密码" prop="confirmPassword">
            <el-input
              v-model="passwordForm.confirmPassword"
              type="password"
              placeholder="请再次输入新密码"
              show-password
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="submitting" @click="handleUpdatePassword">
              修改密码
            </el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import PageHeader from '@/components/common/PageHeader.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import { updatePassword } from '@/api/user'
import { getCurrentUser, clearCurrentUser } from '@/utils/auth'
import { formatDateTime } from '@/utils/format'

const router = useRouter()
const formRef = ref(null)
const submitting = ref(false)

const currentUser = computed(() => {
  return getCurrentUser() || {}
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const validateConfirmPassword = (_rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次新密码不一致'))
  } else {
    callback()
  }
}

const validateNewPassword = (_rule, value, callback) => {
  if (value === passwordForm.oldPassword) {
    callback(new Error('新密码不能与旧密码相同'))
  } else {
    callback()
  }
}

const rules = {
  oldPassword: [{ required: true, message: '请输入旧密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '新密码不少于 6 位', trigger: 'blur' },
    { validator: validateNewPassword, trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleUpdatePassword = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await updatePassword(
      currentUser.value.id,
      passwordForm.oldPassword,
      passwordForm.newPassword
    )
    ElMessage.success('密码修改成功，请使用新密码重新登录')
    clearCurrentUser()
    router.push('/login')
  } catch {
    // 错误已在拦截器中提示
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.password-section {
  margin-top: 32px;
  padding-top: 24px;
  border-top: 1px solid #ebeef5;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 20px;
}
</style>
