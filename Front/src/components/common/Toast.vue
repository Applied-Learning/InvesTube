<template>
  <div class="toast-container">
    <transition-group name="toast">
      <div v-for="toast in toasts" :key="toast.id" class="toast" :class="toast.type">
        <span class="message">{{ toast.message }}</span>
        <button class="close-btn" @click="remove(toast.id)">×</button>
      </div>
    </transition-group>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useToastStore } from '@/stores/toast'

const toastStore = useToastStore()
const toasts = computed(() => toastStore.toasts)
const remove = (id) => toastStore.remove(id)
</script>

<style scoped>
.toast-container {
  position: fixed;
  right: 20px;
  bottom: 20px;
  display: flex;
  flex-direction: column;
  gap: 8px;
  z-index: 2000;
}

.toast {
  min-width: 280px;
  max-width: 420px;
  padding: 12px 14px;
  border-radius: 10px;
  color: #fff;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.18);
  backdrop-filter: blur(4px);
}

.toast.info {
  background: linear-gradient(135deg, #2563eb, #1d4ed8);
}

.toast.error {
  background: linear-gradient(135deg, #ef4444, #b91c1c);
}

.toast.success {
  background: linear-gradient(135deg, #10b981, #059669);
}

.message {
  flex: 1;
  margin-right: 8px;
  font-size: 14px;
  line-height: 1.4;
}

.close-btn {
  background: transparent;
  border: none;
  color: #fff;
  font-size: 16px;
  cursor: pointer;
  padding: 0 4px;
}

.toast-enter-active,
.toast-leave-active {
  transition: all 0.2s ease;
}

.toast-enter-from,
.toast-leave-to {
  opacity: 0;
  transform: translateY(10px);
}
</style>
