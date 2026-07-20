<template>
  <div class="page-container">
    <PageHeader title="成员列表" />

    <!-- 筛选区域 -->
    <div class="filter-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索姓名/用户名"
        clearable
        style="width: 220px"
      />
      <el-select v-model="filterRole" placeholder="角色筛选" clearable>
        <el-option
          v-for="role in roleOptions"
          :key="role"
          :label="role"
          :value="role"
        />
      </el-select>
      <el-button @click="handleReset">重置</el-button>
    </div>

    <!-- 表格 -->
    <div class="content-card" v-loading="loading">
      <el-table :data="filteredMembers" border stripe style="width: 100%" v-if="filteredMembers.length > 0">
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="realName" label="真实姓名" width="120" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.realName || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="角色" width="110" align="center">
          <template #default="{ row }">
            <StatusTag :status="row.role" type="role" />
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="140" align="center">
          <template #default="{ row }">
            {{ row.phone || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.email || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" align="center">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
      </el-table>
      <EmptyState v-else description="暂无成员数据" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import PageHeader from '@/components/common/PageHeader.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import StatusTag from '@/components/common/StatusTag.vue'
import { getUserList } from '@/api/user'
import { USER_ROLES } from '@/utils/constants'
import { formatDateTime } from '@/utils/format'

const loading = ref(false)
const memberList = ref([])
const searchKeyword = ref('')
const filterRole = ref('')

const roleOptions = [USER_ROLES.ADMIN, USER_ROLES.LEADER, USER_ROLES.MEMBER]

const filteredMembers = computed(() => {
  let list = memberList.value
  if (searchKeyword.value) {
    const kw = searchKeyword.value.toLowerCase()
    list = list.filter(
      (m) =>
        (m.username && m.username.toLowerCase().includes(kw)) ||
        (m.realName && m.realName.toLowerCase().includes(kw))
    )
  }
  if (filterRole.value) {
    list = list.filter((m) => m.role === filterRole.value)
  }
  return list
})

const fetchMembers = async () => {
  loading.value = true
  try {
    const data = await getUserList()
    // 过滤掉密码字段（如果有）
    memberList.value = (Array.isArray(data) ? data : []).map((u) => {
      const { password, ...rest } = u
      return rest
    })
  } finally {
    loading.value = false
  }
}

const handleReset = () => {
  searchKeyword.value = ''
  filterRole.value = ''
}

onMounted(() => {
  fetchMembers()
})
</script>
