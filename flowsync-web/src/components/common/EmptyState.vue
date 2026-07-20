<template>
  <div class="empty-state">
    <el-empty :description="description">
      <slot />
    </el-empty>
  </div>
</template>

<script setup>
defineProps({
  description: {
    type: String,
    default: '暂无数据'
  }
})
</script>

<style scoped>
.empty-state {
  padding: 60px 0;
}
</style>
