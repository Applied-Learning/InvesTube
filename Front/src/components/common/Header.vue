<template>
  <header class="header">
    <Container>
      <div class="header-inner">
        <h1 class="logo">Investube</h1>
        <nav class="nav">
          <RouterLink to="/">홈</RouterLink>
        </nav>
      </div>
    </Container>
  </header>
</template>

<script setup>
import { RouterLink } from 'vue-router'
import Container from './Container.vue'
</script>

<style scoped>
.header {
  background-color: #111827;
  color: #f9fafb;
}

.header-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 56px;
}

.logo {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
}

.nav a {
  color: #e5e7eb;
  text-decoration: none;
  margin-left: 16px;
  font-size: 14px;
}

.nav a.router-link-active {
  color: #ffffff;
  font-weight: 600;
}
</style>

