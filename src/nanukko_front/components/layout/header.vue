<template>
  <header class="header">
    <!-- 로고, 검색창, action 을 포함하는 Flex 컨테이너 -->
    <div class="header-container">
      <!-- 로고 -->
      <div class="logo">
        <img src="../../public/image/나누고_Logo_blue.png" alt="nanukko Logo" width="150" height="80" />
      </div>
      <!-- 검색창 -->
      <div class="search-bar">
        <input type="text" placeholder="검색어를 입력해주세요" v-model="searchQuery" @keyup.enter="onSearch" />
        <button @click="onSearch">🔍</button>
      </div>

      <!-- actions 채팅, 알림, 로그인, 마이페이지 -->
      <ul class="header-actions">
        <li>
          <NuxtLink to="/chatting">채팅</NuxtLink>
        </li>
        <li class="notification-cotainer">
          <Notification />
        </li>
        <li>
          <NuxtLink to="/Login">로그인</NuxtLink>
        </li>
        <li>
          <NuxtLink to="/mypage">마이페이지</NuxtLink>
        </li>
      </ul>
    </div>
  </header>
</template>

<script setup>
import { ref } from "vue";
import { useRouter } from 'vue-router';
import Notification from "../notification/Notification.vue";

const router = useRouter();

/* 검색창의 입력값을 관리하기 위한 상태 */
const searchQuery = ref("");

/* 검색 동작 */
const onSearch = () => {
  if (!searchQuery.value.trim()) return;

  // 검색 결과 페이지로 이동
  router.push({
    path: '/search',
    query: { q: searchQuery.value.trim() }
  });

  // 검색 후 검색어 초기화 (선택사항)
  // searchQuery.value = "";
};
</script>

<style scoped>
/* 헤더 전체 레이아웃 */
.header {
  display: flex;
  flex-direction: row;
  align-items: center;
  padding: 0.5rem 1rem;
  background-color: white;
  /* 흰색 배경 */
  border-bottom: 1px solid #e0e0e0;
  /*하단 경계선 */
  /* box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1); 약간의 그림자 효과 */
}

.header-container {
  padding-left: 30px;
  display: flex;
  align-items: center;
  width: 100%;
  max-width: fit-content;
  margin: 0px 15rem;
}

/* 로고 */
.logo {
  flex: 0 0 auto;
  /* 너비 고정 */
  display: block;
}

/* 검색창 스타일 */
.search-bar {
  min-width: 400px;
  max-width: 400px;
  height: 40px;
  flex: 1;
  display: flex;
  /* justify-content: center; */
  align-items: center;
  justify-content: space-between;
  border-radius: 15px;
  border: 1px solid #333;
}

.search-bar input {
  width: 100%;
  max-width: 400px;
  /*검색창 최대 너비*/
  padding: 0.5rem 1rem;
  border: none;
  /* 회색 테두리 */
  border-radius: 20px;
  /* 둥근 모서리 */
  outline: none;
  font-size: 1rem;
  color: #333;
  background-color: white;
  /* 연한 배경 */
}

.search-bar input::placeholder {
  color: #aaa;
  /* 플레이스홀더 색상 */
}

.search-bar button {
  margin-left: 0.5rem;
  margin-right: 0.5rem;
  padding: 0.5rem 1rem;
  border: none;
  background-color: white;
  cursor: pointer;
  transition: color 0.3s ease;
}

.search-bar button:hover {
  color: #4c6ef5;
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
  margin-right: 0.5rem;
  padding: 0.5rem 1rem;
  border: none;
  background-color: #4c6ef5;
  color: white;
  border-radius: 20px;
  cursor: pointer;
}

/* 알림 섹션 */
.notifications {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-size: 1rem;
  color: #4c6ef5;
  /* 파란색 */
}

.notifications .badge {
  display: inline-flex;
  justify-content: center;
  align-items: center;
  width: 20px;
  height: 20px;
  background-color: #ffa500;
  /* 오렌지색 */
  color: white;
  font-size: 0.75rem;
  font-weight: bold;
  border-radius: 50%;
}

/* 이 아래 윤여운이 추가함 */
.notification-container {
  position: relative;
  display: flex;
  align-items: center;
}

/* 알림 아이콘이 있는 li 태그에 대한 스타일 */
.header-actions li.notification-container {
  padding: 0;
  margin: 0 1.2rem;
}
</style>
