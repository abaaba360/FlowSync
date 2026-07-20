<template>
  <div class="home-layout">
    <AppSidebar />
    <div class="main-container">
      <AppHeader />
      <div class="main-content">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup>
import AppHeader from '@/components/layout/AppHeader.vue'
import AppSidebar from '@/components/layout/AppSidebar.vue'
</script>

<style scoped>
.home-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.main-content {
  flex: 1;
  overflow-y: auto;
  background: #f0f2f5;
}
</style>
