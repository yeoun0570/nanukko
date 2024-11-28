<script setup>
import Header from "~/components/layout/header.vue";
import Footer from "~/components/layout/footer.vue";
import Navigator from "~/components/layout/navigator.vue";
import Sidebar from "~/components/layout/sidebar.vue";
</script>

<template>
  <div class="layout">
    <Header />
    <Navigator />
    <div class="main-container">
      <Sidebar class="sidebar" />
      <main class="content">
        <slot />
      </main>
    </div>
    <Footer />
  </div>
</template>

<style scoped>
.layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.main-container {
  display: flex;
  flex: 1;
  gap: 20px;
  max-width: 1200px;
  margin: 0 auto;
  width: 100%;
  padding: 20px;

}

.sidebar {
  width: 250px;
  flex-shrink: 0;
}

.content {
  flex: 1;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}
</style>