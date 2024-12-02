<script setup>
import Header from "~/components/layout/header.vue";
import Navigator from "~/components/layout/navigator.vue";
import Footer from "~/components/layout/footer.vue";

</script>

<template>
    <link rel='stylesheet' href='https://cdn-uicons.flaticon.com/2.6.0/uicons-regular-rounded/css/uicons-regular-rounded.css'>
  <div class="layout">
    <!-- Header 컴포넌트 -->
  <Header />
  <Navigator />

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
