<template>
  <div class="app-layout">
    <HeaderBar />
    <main class="app-main">
      <Container>
        <slot />
      </Container>
    </main>
  </div>
</template>

<script setup>
import HeaderBar from '../components/common/Header.vue'
import Container from '../components/common/Container.vue'
</script>

<style scoped>
.app-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.app-main {
  flex: 1;
  padding: 16px 0;
}
</style>

