<template>
  <teleport to="body">
    <div v-if="modelValue" class="backdrop" @click.self="close">
      <div class="modal">
        <header v-if="$slots.header" class="modal__header">
          <slot name="header" />
        </header>
        <section class="modal__body">
          <slot />
        </section>
        <footer v-if="$slots.footer" class="modal__footer">
          <slot name="footer" />
        </footer>
      </div>
    </div>
  </teleport>
</template>

<script setup>
const props = defineProps({
  modelValue: { type: Boolean, default: false },
})

const emit = defineEmits(['update:modelValue'])

const close = () => {
  emit('update:modelValue', false)
}
</script>

<style scoped>
.backdrop {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 50;
}

.modal {
  background: #ffffff;
  border-radius: 10px;
  min-width: 320px;
  max-width: 480px;
  padding: 16px 20px;
  box-shadow: 0 10px 40px rgba(15, 23, 42, 0.25);
}

.modal__header {
  margin-bottom: 8px;
  font-weight: 600;
}

.modal__body {
  font-size: 14px;
  color: #374151;
}

.modal__footer {
  margin-top: 16px;
  text-align: right;
}
</style>
