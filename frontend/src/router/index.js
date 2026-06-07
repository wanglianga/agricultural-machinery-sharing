import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/dashboard'
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/Dashboard.vue')
  },
  {
    path: '/fields',
    name: 'Fields',
    component: () => import('@/views/Fields.vue')
  },
  {
    path: '/machines',
    name: 'Machines',
    component: () => import('@/views/Machines.vue')
  },
  {
    path: '/appointments',
    name: 'Appointments',
    component: () => import('@/views/Appointments.vue')
  },
  {
    path: '/work-orders',
    name: 'WorkOrders',
    component: () => import('@/views/WorkOrders.vue')
  },
  {
    path: '/fuel-tickets',
    name: 'FuelTickets',
    component: () => import('@/views/FuelTickets.vue')
  },
  {
    path: '/settlements',
    name: 'Settlements',
    component: () => import('@/views/Settlements.vue')
  },
  {
    path: '/subsidy-policies',
    name: 'SubsidyPolicies',
    component: () => import('@/views/SubsidyPolicies.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
