<script setup>
import { ref } from "vue";
import Notification from "../notification/Notification.vue";
import { useAuth } from "~/composables/auth/useAuth";
import { useRouter } from "vue-router";
import { useAuthStore } from "~/stores/authStore";
import ChatNotification from "../chat/ChatNotification.vue";

const router = useRouter();
const { isAuthenticated, logout } = useAuth();
const { $showToast } = useNuxtApp();
const authStore = useAuthStore();



const navigateToChat = () => {
  router.push("/chat");
};
const navigateToProducts = () => {
  router.push('/products/new')
};

const shouldRerender = ref(0); //리렌더링을 트리거하기 위한 키

// 로그아웃
const doLogout = () => {
  authStore.logout();
  $showToast('로그아웃 되었습니다!');
  router.push("/auth/login");
};

/* 검색창의 입력값을 관리하기 위한 상태 */
const searchQuery = ref("");

/* 검색 동작 */
const onSearch = () => {
  if (searchQuery.value.trim()) {
    router.push({
      path: '/products/search',
      query: { q: searchQuery.value.trim() }
    });
  }
};

// 컴포넌트 마운트 시 상태 초기화
onMounted(async () => {
  if (process.client) {
    await nextTick()
    authStore.initialize()
  }
})

// 인증 상태 변화 감시
watch(() => authStore.isAuthenticated, (newValue) => {
  shouldRerender.value++; //값을 변경하여 리렌더링
  console.log('Authentication state changed:', newValue);
}, { immediate: true });

</script>

<template>
  <header class="header" :key="shouldRerender">
    <!-- 로고, 검색창, 액션 항목을 포함하는 컨테이너 -->
    <div class="header-container">
      <!-- 로고 섹션 -->
      <div class="logo">
        <NuxtLink to="/">
          <img src="../../public/image/나누고_Logo_blue.png" alt="nanukko Logo" width="150" height="80" />
        </NuxtLink>
      </div>

      <!-- 검색창 섹션 -->
      <div class="search-bar">
        <!-- 검색어 입력 필드 -->
        <input type="text" placeholder="검색어를 입력해주세요" v-model="searchQuery" @keyup.enter="onSearch" />
        <!-- 검색 버튼 -->
        <button @click="onSearch" class="search-button"><i class="fi fi-rr-search"></i></button>
        <!-- 클릭 시 검색 실행 -->
      </div>

      <!-- 액션 섹션 (채팅, 알림, 로그인, 마이페이지 링크) -->
      <ul class="header-actions">
        <li>
          <ChatNotification @click="navigateToChat" />
        </li>
        <li class="notification-cotainer">
          <Notification />
        </li>
        <button v-if="!authStore.isAuthenticated">
          <NuxtLink to="/auth/login">로그인</NuxtLink>
        </button>
        <button v-if="authStore.isAuthenticated">
          <NuxtLink to="/my-store">마이페이지</NuxtLink>
        </button>
        <button v-if="authStore.isAuthenticated" @click="doLogout">로그아웃</button>
        <!-- 판매 글 작성을 위한 페이지로 이동하는 링크 -->
        <button v-if="authStore.isAuthenticated" @click="navigateToProducts" class="sell-button">판매하기</button>
      </ul>
    </div>
  </header>
</template>

<style>
.fi-rr-search {
  display: flex;
  align-items: center;
  color: #333;
}

.fi-rr-search:hover {
  color: #FFAD30;
}

.logo {
  flex: 1;
  display: flex;
  align-items: center;
  margin: 0;
  padding: 0;
  min-width: 150px;
}

.logo img {
  vertical-align: middle;
}

.link-style {
  text-decoration: none;
  color: #000000;
}

.header {
  display: flex;
  width: 100%;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  background-color: #ffffff;
  padding: 0;
  box-sizing: border-box;
  margin-top: 0.5rem;
}

.header-container {
  background-color: white;
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  max-width: 1200px;
  margin: 0 auto;
}

@media (max-width: 768px) {
  .header-container {
    padding-left: 10px;
  }
}

/* .search-bar {
  min-width: 500px;
  max-width: 500px;
  height: 43px;
  flex: 2;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-radius: 30px;
  border: 1px solid #333;
  margin-right: 5rem;
} */

.search-bar {
  min-width: 500px;
  max-width: 500px;
  height: 43px;
  flex: 2;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-radius: 30px;
  border: none;
  margin-right: 5rem;
  box-shadow: 0 1px 8px rgba(0, 0, 0, 0.15);
  transition: box-shadow 0.3s ease;
}

.search-bar:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.search-bar input {
  width: 100%;
  max-width: 450px;
  padding: 0.4rem 1rem;
  border: none;
  border-radius: 30px;
  outline: none;
  font-size: 1rem;
  color: #333;
  background-color: white;
}

.search-bar input::placeholder {
  color: #aaa;
}

.search-bar button {
  margin-left: 0.5rem;
  margin-right: 0.5rem;
  padding: 0.3rem 0.5rem 0.4rem 0.5rem;
  border: none;
  background-color: white;
  cursor: pointer;
  transition: color 0.3s ease;
}

.header-actions {
  display: flex;
  flex-direction: row;
  flex: 1;
  min-width: 400px;
  justify-content: flex-end;
  align-items: center;
  gap: 1.7rem;
}

.header-actions li {
  list-style: none;
  display: flex;
  align-items: center;
  height: 100%;
}

.header-actions button {
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: visible;
  padding: 0.5rem 0.5rem;
  border: none;
  background-color: #ffffff;
  color: #000000;
  font-size: 1rem;
  border-radius: 10px;
  cursor: pointer;
  white-space: nowrap;
}

.header-actions button a {
  text-decoration: none;
  color: #000000;
}


.sell-button {
  padding: 0.8rem 1.6rem;
  border: none;
  color: #000000;
  border-radius: 10px;
  font-size: 1rem;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.sell-button:hover {
  background-color: #618EFF;
  color: #ffffff;
}

.notifications {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1rem;
  color: #4c6ef5;
}

.notifications .badge {
  display: inline-flex;
  justify-content: center;
  align-items: center;
  width: 20px;
  height: 20px;
  background-color: #ffa500;
  color: white;
  font-size: 0.75rem;
  font-weight: bold;
  border-radius: 50%;
}

.notification-container {
  position: relative;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.header-actions li.notification-container {
  padding: 0;
  margin: 0 1.2rem;
  white-space: nowrap;
}
</style>