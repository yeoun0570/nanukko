<script setup>
import Header from "~/components/layout/header.vue";
import Footer from "~/components/layout/footer.vue";
import Navigator from "~/components/layout/navigator.vue";
import Sidebar from "~/components/layout/sidebar.vue";
import { useApi } from "@/composables/useApi";

const api = useApi();
// 공유할 프로필 데이터(userProfile)와 갱신 함수(refreshUserProfile) 준비
const userProfile = ref({});

const refreshUserProfile = async () => {
  try {
    const response = await api.get("/my-store/simple-info");
    userProfile.value = response;
    // 리뷰 점수 계산 로직 추가 (기존 sidebar의 로직 이동)
    userProfile.value.reviewRate =
      Math.round((userProfile.value.reviewRate / 2) * 10) / 10;
  } catch (error) {
    console.error("프로필 정보 로드 실패:", error);
  }
};

// 프로필 데이터와 갱신 함수를 하위 컴포넌트에 제공
provide("userProfile", userProfile);
provide("refreshUserProfile", refreshUserProfile);

// 최초 마운트 시 프로필 정보 로드
onMounted(() => {
  refreshUserProfile();
});
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
    <ChatbotWidget/>
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
