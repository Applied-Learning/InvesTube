<template>
  <button
    :type="type"
    class="btn"
    :class="[`btn--${variant}`, { 'btn--full': fullWidth }]"
    :disabled="disabled || loading"
    @click="onClick"
  >
    <span v-if="loading" class="spinner" />
    <span><slot /></span>
  </button>
</template>

<script setup>
const props = defineProps({
  type: { type: String, default: 'button' },
  variant: { type: String, default: 'primary' },
  fullWidth: { type: Boolean, default: false },
  disabled: { type: Boolean, default: false },
  loading: { type: Boolean, default: false },
})

const emit = defineEmits(['click'])

const onClick = (event) => {
  if (props.disabled || props.loading) return
  emit('click', event)
}
</script>

<style scoped>
.btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 8px 16px;
  border-radius: 6px;
  border: 1px solid transparent;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background-color 0.15s ease, color 0.15s ease, border-color 0.15s ease;
}

.btn--primary {
  background-color: #2563eb;
  color: #ffffff;
}

.btn--primary:hover {
  background-color: #1d4ed8;
}

.btn--secondary {
  background-color: #e5e7eb;
  color: #111827;
}

.btn--ghost {
  background-color: transparent;
  color: #111827;
  border-color: #d1d5db;
}

.btn--full {
  width: 100%;
}

.btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.spinner {
  width: 14px;
  height: 14px;
  border-radius: 999px;
  border: 2px solid rgba(255, 255, 255, 0.6);
  border-top-color: transparent;
  margin-right: 6px;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>

