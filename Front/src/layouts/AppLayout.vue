<template>
  <div class="app-layout">
    <HeaderBar />
    <div class="app-body">
      <Sidebar />
      <main class="app-main">
        <Container>
          <slot />
        </Container>
      </main>
    </div>
  </div>
</template>

<script setup>
import HeaderBar from '../components/common/Header.vue'
import Container from '../components/common/Container.vue'
import Sidebar from '../components/common/Sidebar.vue'
</script>

<style scoped>
.app-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.app-body {
  flex: 1;
  display: flex;
}

.app-main {
  flex: 1;
  padding: 16px 0;
}
</style>
