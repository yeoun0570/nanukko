<script setup>
import Header from '~/components/layout/header.vue'
import Footer from '~/components/layout/footer.vue'
</script>

<template>
  <div class="layout">
    <!-- Header 컴포넌트 -->
    <Header />

    <!-- 메인 콘텐츠 영역 -->
    <main>
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
