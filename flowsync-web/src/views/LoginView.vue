<template>
  <div class="login-page">
    <div class="login-container">
      <!-- 左侧品牌区域 -->
      <div class="login-brand">
        <div class="brand-content">
          <div class="brand-logo">
            <el-icon :size="56" color="#fff"><Connection /></el-icon>
          </div>
          <h1 class="brand-title">FlowSync</h1>
          <p class="brand-subtitle">小组任务协同管理系统</p>
          <p class="brand-desc">
            高效协作 · 智能拆解 · 进度透明 · 轻松总结
          </p>
          <div class="brand-features">
            <div class="feature-item">
              <el-icon><Checked /></el-icon>
              <span>AI 智能任务拆解</span>
            </div>
            <div class="feature-item">
              <el-icon><Checked /></el-icon>
              <span>可视化进度跟踪</span>
            </div>
            <div class="feature-item">
              <el-icon><Checked /></el-icon>
              <span>角色权限管理</span>
            </div>
            <div class="feature-item">
              <el-icon><Checked /></el-icon>
              <span>阶段与最终总结</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧登录区域 -->
      <div class="login-panel">
        <div class="login-card">
          <h2 class="login-title">欢迎登录</h2>
          <p class="login-desc">请输入用户名和密码</p>
          <el-form
            ref="formRef"
            :model="form"
            :rules="rules"
            class="login-form"
            @keyup.enter="handleLogin"
          >
            <el-form-item prop="username">
              <el-input
                v-model="form.username"
                placeholder="请输入用户名"
                prefix-icon="User"
                size="large"
              />
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="form.password"
                type="password"
                placeholder="请输入密码"
                prefix-icon="Lock"
                size="large"
                show-password
              />
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                size="large"
                :loading="loading"
                class="login-btn"
                @click="handleLogin"
              >
                登 录
              </el-button>
            </el-form-item>
          </el-form>
          <div class="test-account">
            <p>测试账号：leader / 123456 &nbsp;|&nbsp; member1 / 123456</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login } from '@/api/auth'
import { setCurrentUser, setToken } from '@/utils/auth'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  try {
    const { user, token } = await login(form.username, form.password)
    setCurrentUser(user)
    setToken(token)
    ElMessage.success('登录成功')
    router.push('/home/overview')
  } catch (error) {
    // 错误已在 request 拦截器中处理，保留用户名
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.login-container {
  display: flex;
  width: 900px;
  height: 520px;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

/* 左侧品牌区域 */
.login-brand {
  flex: 1;
  background: linear-gradient(135deg, #4a6cf7 0%, #6366f1 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 48px;
}

.brand-content {
  text-align: center;
  color: #fff;
}

.brand-logo {
  margin-bottom: 16px;
}

.brand-title {
  font-size: 36px;
  font-weight: 700;
  margin-bottom: 8px;
  letter-spacing: 2px;
}

.brand-subtitle {
  font-size: 16px;
  opacity: 0.9;
  margin-bottom: 8px;
}

.brand-desc {
  font-size: 13px;
  opacity: 0.75;
  margin-bottom: 32px;
}

.brand-features {
  text-align: left;
  display: inline-block;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  opacity: 0.85;
  margin-bottom: 10px;
}

/* 右侧登录区域 */
.login-panel {
  flex: 1;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.login-card {
  width: 320px;
}

.login-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
  text-align: center;
}

.login-desc {
  font-size: 14px;
  color: #909399;
  margin-bottom: 32px;
  text-align: center;
}

.login-form {
  width: 100%;
}

.login-btn {
  width: 100%;
}

.test-account {
  margin-top: 24px;
  text-align: center;
  font-size: 12px;
  color: #c0c4cc;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

/* 响应式 */
@media (max-width: 960px) {
  .login-container {
    width: 400px;
    height: auto;
    flex-direction: column;
  }
  .login-brand {
    display: none;
  }
  .login-panel {
    padding: 40px 20px;
  }
}
</style>
