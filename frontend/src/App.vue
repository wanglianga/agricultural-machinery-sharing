<template>
  <el-container class="app-container">
    <el-header class="app-header">
      <div class="header-left">
        <el-icon size="32" color="#409EFF"><Tools /></el-icon>
        <h1 class="app-title">农机共享平台</h1>
      </div>
      <div class="header-right">
        <el-dropdown @command="handleRoleChange">
          <span class="role-switch">
            <el-icon><User /></el-icon>
            {{ currentRoleName }}
            <el-icon><CaretBottom /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="LARGE_GROWER">种植大户</el-dropdown-item>
              <el-dropdown-item command="COOPERATIVE">服务合作社</el-dropdown-item>
              <el-dropdown-item command="MACHINE_OPERATOR">农机手</el-dropdown-item>
              <el-dropdown-item command="TOWN_SERVICE_CENTER">乡镇农服中心</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>
    
    <el-container>
      <el-aside width="220px" class="app-aside">
        <el-menu
          :default-active="activeMenu"
          router
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
        >
          <el-menu-item v-for="item in menuItems" :key="item.path" :index="item.path">
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.title }}</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      
      <el-main class="app-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute } from 'vue-router'
import { useAppStore } from '@/store/app'
import { Tools, User, CaretBottom } from '@element-plus/icons-vue'

const route = useRoute()
const appStore = useAppStore()

const activeMenu = computed(() => route.path)

const currentRoleName = computed(() => {
  const roleMap = {
    LARGE_GROWER: '种植大户',
    COOPERATIVE: '服务合作社',
    MACHINE_OPERATOR: '农机手',
    TOWN_SERVICE_CENTER: '乡镇农服中心'
  }
  return roleMap[appStore.currentRole] || '种植大户'
})

const menuItems = computed(() => {
  const role = appStore.currentRole
  const menus = [
    { path: '/dashboard', title: '数据概览', icon: 'DataAnalysis' },
    { path: '/fields', title: '地块管理', icon: 'Location' },
    { path: '/machines', title: '机具管理', icon: 'Tools' },
    { path: '/appointments', title: '预约管理', icon: 'Calendar' },
    { path: '/work-orders', title: '作业单', icon: 'List' },
    { path: '/fuel-tickets', title: '油票管理', icon: 'Tickets' },
    { path: '/settlements', title: '补贴核算', icon: 'Money' },
    { path: '/subsidy-policies', title: '补贴政策', icon: 'Document' }
  ]
  
  if (role === 'LARGE_GROWER') {
    return menus.filter(m => ['/dashboard', '/fields', '/appointments', '/work-orders', '/settlements'].includes(m.path))
  } else if (role === 'COOPERATIVE') {
    return menus.filter(m => ['/dashboard', '/machines', '/appointments', '/work-orders', '/fuel-tickets', '/settlements'].includes(m.path))
  } else if (role === 'MACHINE_OPERATOR') {
    return menus.filter(m => ['/dashboard', '/work-orders', '/fuel-tickets'].includes(m.path))
  } else if (role === 'TOWN_SERVICE_CENTER') {
    return menus
  }
  return menus
})

const handleRoleChange = (role) => {
  appStore.setCurrentRole(role)
}
</script>

<style scoped>
.app-container {
  height: 100vh;
}

.app-header {
  background: linear-gradient(90deg, #2b5876 0%, #4e4376 100%);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 24px;
  color: white;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.app-title {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
}

.role-switch {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  font-size: 14px;
}

.app-aside {
  background-color: #304156;
}

.app-main {
  background-color: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}

:deep(.el-menu) {
  border-right: none;
}
</style>
