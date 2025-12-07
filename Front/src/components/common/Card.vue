<template>
  <article class="card" :class="{ 'card--clickable': clickable }" @click="handleClick">
    <header v-if="$slots.header" class="card__header">
      <slot name="header" />
    </header>
    <div class="card__body">
      <slot />
    </div>
    <footer v-if="$slots.footer" class="card__footer">
      <slot name="footer" />
    </footer>
  </article>
</template>

<script setup>
const props = defineProps({
  clickable: { type: Boolean, default: false },
})

const emit = defineEmits(['click'])

const handleClick = (event) => {
  if (!props.clickable) return
  emit('click', event)
}
</script>

<style scoped>
.card {
  background-color: #ffffff;
  border-radius: 10px;
  border: 1px solid #e5e7eb;
  padding: 12px;
  box-shadow: 0 1px 2px rgba(148, 163, 184, 0.12);
}

.card--clickable {
  cursor: pointer;
  transition: transform 0.12s ease, box-shadow 0.12s ease;
}

.card--clickable:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 18px rgba(148, 163, 184, 0.35);
}

.card__header {
  margin-bottom: 8px;
}

.card__body {
  font-size: 14px;
  color: #374151;
}

.card__footer {
  margin-top: 10px;
  font-size: 12px;
  color: #6b7280;
}
</style>

