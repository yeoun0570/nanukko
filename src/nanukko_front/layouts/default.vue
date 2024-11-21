<script setup>
import Header from "~/components/layout/header.vue";
import Footer from "~/components/layout/footer.vue";
import navigator from "~/components/layout/navigator.vue";
import sidebar from "~/components/layout/sidebar.vue";
</script>

<template>
  <div class="layout">
    <!-- Header 컴포넌트 -->
    <Header />
    <navigator />

    <!-- 메인 콘텐츠 영역 -->
    <main>
    <sidebar />
      <NuxtPage />
    </main>

    <!-- Footer 컴포넌트 -->
    <Footer />
  </div>
</template>

<style scoped>
.layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
}

main {
  flex: 1;
}
</style>
