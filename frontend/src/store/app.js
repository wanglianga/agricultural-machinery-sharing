import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  const currentRole = ref('LARGE_GROWER')

  const setCurrentRole = (role) => {
    currentRole.value = role
  }

  return {
    currentRole,
    setCurrentRole
  }
})
