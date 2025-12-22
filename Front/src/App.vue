<template>
  <AppLayout>
    <router-view />
    <Toast />
  </AppLayout>
</template>

<script setup>
import AppLayout from './layouts/AppLayout.vue'
import Toast from './components/common/Toast.vue'
</script>

<style scoped>
:global(body) {
  margin: 0;
  font-family:
    system-ui,
    -apple-system,
    BlinkMacSystemFont,
    'Segoe UI',
    sans-serif;
  background-color: #f5f5f5;
}
</style>
