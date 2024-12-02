<script setup>
import { ref } from "vue";
import Notification from "../notification/Notification.vue";
import { useAuth } from "~/composables/auth/useAuth";
import { useRouter } from "vue-router";
import { useToast } from "vue-toastification";

const router = useRouter();
const { userId, nickname, isAuthenticated, logout } = useAuth();
const searchQuery = ref('')
const toast = useToast();

const handleSearch = () => {
  if (searchQuery.value.length < 2) {
    alert('검색어는 2글자 이상 입력해주세요.')
    return
  }

  navigateTo({
    path: '/products/search',
    query: {
      q: searchQuery.value,
      page: '0'
    }
  })
}

const showLoginAlert = () => {
  alert("채팅을 이용하려면 로그인이 필요합니다.");
  router.push("/auth/login");
};

const navigateToChat = () => {
  router.push("/chat");
};


// 로그아웃
const doLogout = () => {

  logout();
  //알림 팝업
  toast.info("로그아웃되었습니다.", {
    timeout: 3000, // 3초 동안 유지
    position: "bottom-center", // 화면 중앙 하단
    icon: "🔒", // 커스텀 아이콘
    hideProgressBar: true, // 진행 바 숨기기
  });

  router.push("/auth/login");
};

</script>

<template>
  <header class="header">
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
        <input type="text" placeholder="검색어를 입력해주세요" v-model="searchQuery" @keyup.enter="handleSearch" min="2" />
        <button @click="handleSearch"><i class="fi fi-rr-search"></i></button>
      </div>

      <!-- 액션 섹션 (채팅, 알림, 로그인, 마이페이지 링크) -->
      <ul class="header-actions">
        <li>
          <button v-if="isAuthenticated" @click="navigateToChat">채팅</button>
          <button v-else @click="showLoginAlert">채팅</button>
        </li>
        <li class="notification-cotainer">
          <Notification />
        </li>
        <button v-if="!isAuthenticated">
          <NuxtLink to="/auth/login">로그인</NuxtLink>
        </button>
        <button v-if="isAuthenticated">
          <NuxtLink to="/my-store">마이페이지</NuxtLink>
        </button>
        <button v-if="isAuthenticated" @click="doLogout">로그아웃</button>
        <!-- 판매 글 작성을 위한 페이지로 이동하는 링크 -->
        <button v-if="isAuthenticated" class="sell-button">판매하기</button>
      </ul>
    </div>
  </header>
</template>

<style scoped>
.header {
  display: flex;
  width: 100%;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  background-color: #ffffff;
  padding: 0.5rem 1rem;
  box-sizing: border-box;
}

.header-container {
  background-color: white;
  display: flex;
  align-items: center;
  justify-content: space-between;
  width: 100%;
  max-width: 1050px;
  margin: 0 auto;
}

@media (max-width: 768px) {
  .header-container {
    padding-left: 10px;
  }
}

.logo {
  flex: 0 0 auto;
  display: block;
}

.search-bar {
  min-width: 400px;
  max-width: 400px;
  height: 40px;
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-radius: 15px;
  border: 1px solid #333;
}

.search-bar input {
  width: 100%;
  max-width: 400px;
  padding: 0.4rem 1rem;
  border: none;
  border-radius: 20px;
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

.search-bar button:hover {
  color: #4c6ef5;
}

.suggestions {
  position: absolute;
  top: 100%;
  left: 0;
  right: 0;
  background: white;
  border: 1px solid #ddd;
  border-radius: 4px;
  margin-top: 4px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  z-index: 1000;
}

.suggestions ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.suggestions li {
  padding: 10px 20px;
  cursor: pointer;
}

.suggestions li.active,
.suggestions li:hover {
  background-color: #f8f9fa;
}

.suggestions li strong {
  color: #007bff;
}

.header-actions {
  display: flex;
  flex-direction: row;
  width: 400px;
  justify-content: space-around;
}

.header-actions li {
  list-style: none;
}

.header-actions button {
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-right: 0.5rem;
  padding: 0.5rem 0.5rem;
  border: none;
  background-color: #ffffff;
  color: #000000;
  font-size: 1rem;
  border-radius: 10px;
  cursor: pointer;
  white-space: nowrap;
}

.sell-button {
  padding: 0.8rem 1.6rem;
  border: none;
  background-color: #ffffff;
  color: #003798;
  border-radius: 10px;
  font-size: 1rem;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.sell-button:hover {
  background-color: #4c6ef5;
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
}

.header-actions li.notification-container {
  padding: 0;
  margin: 0 1.2rem;
  white-space: nowrap;
}
</style>