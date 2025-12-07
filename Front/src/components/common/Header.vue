<template>
  <header class="header">
    <Container>
      <div class="header-inner">
        <RouterLink to="/" class="logo-link">
          <h1 class="logo">Investube</h1>
        </RouterLink>
        <nav class="nav">
          <button type="button" class="nav-item nav-item--ghost">
            알림
          </button>
          <RouterLink to="/login" class="nav-item nav-item--primary">
            로그인
          </RouterLink>
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

.logo-link {
  display: inline-flex;
  align-items: center;
  text-decoration: none;
  color: inherit;
}

.logo {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
}

.nav {
  display: flex;
  align-items: center;
  gap: 12px;
}

.nav-item {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 6px 12px;
  border-radius: 999px;
  font-size: 14px;
  cursor: pointer;
  border: 1px solid transparent;
  text-decoration: none;
}

.nav-item--ghost {
  background-color: transparent;
  color: #e5e7eb;
  border-color: #4b5563;
}

.nav-item--primary {
  background-color: #2563eb;
  color: #ffffff;
}

.nav-item--primary:hover {
  background-color: #1d4ed8;
}
</style>

