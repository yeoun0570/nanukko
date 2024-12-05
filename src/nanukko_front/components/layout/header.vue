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
        <li class="notification-cotainer"><Notification /></li>
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
}

.logo {
  display: flex;
  align-items: center;
}

.logo img {
  vertical-align: middle;
}

.link-style {
  text-decoration: none;
  color: #000000;
}

/* 헤더 전체 레이아웃 */
.header {
  display: flex;
  /* 가로로 정렬 */
  width: 100%;
  /* 헤더를 창 너비에 맞춤 */
  flex-direction: row;
  /* 기본 행 방향 설정 */
  align-items: center;
  /* 수직 중앙 정렬 */
  justify-content: center;
  /* 수평 중앙 정렬 */
  background-color: #ffffff;
  /* 헤더 배경색 설정 */
  padding: 0.5rem 1rem;
  /* 상하 0.5rem, 좌우 1rem 패딩 */
  box-sizing: border-box;
  /* 패딩 포함 크기 계산 */
}

.header-container {
  background-color: white;
  display: flex;
  /* 가로로 정렬 */
  align-items: center;
  /* 수직 중앙 정렬 */
  justify-content: space-between;
  width: 100%;
  /* 컨테이너 너비를 100%로 설정 */
  max-width: 1050px;
  /* 최대 너비를 제한 */
  margin: 0 auto;
  /* 좌우 중앙 정렬 */
}

/* 반응형 디자인 */
@media (max-width: 768px) {
  .header-container {
    padding-left: 10px;
    /* 좁은 화면에서는 패딩을 줄임 */
  }
}

/* 로고 스타일 */
.logo {
  flex: 0 0 auto;
  /* 고정 크기 */
  display: block;
  /* 블록 요소로 설정 */
}

/* 검색창 컨테이너 스타일 */
.search-bar {
  min-width: 400px;
  /* 검색창 최소 너비 */
  max-width: 400px;
  /* 검색창 최대 너비 */
  height: 40px;
  /* 검색창 높이 */
  flex: 1;
  /* 남는 공간을 차지하도록 설정 */
  display: flex;
  /* 가로로 정렬 */
  align-items: center;
  /* 수직 중앙 정렬 */
  justify-content: space-between;
  /* 입력 필드와 버튼 간격 조정 */
  border-radius: 15px;
  /* 둥근 모서리 */
  border: 1px solid #333;
  /* 테두리를 진한 회색으로 설정 */
}

/* 검색창 입력 필드 스타일 */
.search-bar input {
  width: 100%;
  /* 입력 필드 너비를 검색창에 맞춤 */
  max-width: 400px;
  /* 최대 너비 제한 */
  padding: 0.4rem 1rem;
  /* 상하 0.5rem, 좌우 1rem 패딩 */
  border: none;
  /* 테두리 제거 */
  border-radius: 20px;
  /* 둥근 모서리 */
  outline: none;
  /* 포커스 시 파란 테두리 제거 */
  font-size: 1rem;
  /* 글자 크기 */
  color: #333;
  /* 글자 색을 진한 회색으로 설정 */
  background-color: white;
  /* 배경색 흰색 */
}

/* 입력 필드의 플레이스홀더 스타일 */
.search-bar input::placeholder {
  color: #aaa;
  /* 플레이스홀더 색상 연한 회색 */
}

/* 검색 버튼 스타일 */
.search-bar button {
  margin-left: 0.5rem;
  /* 왼쪽 여백 */
  margin-right: 0.5rem;
  /* 오른쪽 여백 */
  padding: 0.3rem 0.5rem 0.4rem 0.5rem;
  border: none;
  /* 테두리 제거 */
  background-color: white;
  /* 배경 흰색 */
  cursor: pointer;
  /* 포인터 커서 표시 */
  transition: color 0.3s ease;
  /* 색상 전환 효과 */
}

/* 검색 버튼 호버 효과 */
.search-bar button:hover {
  color: #4c6ef5;
  /* 파란색으로 변경 */
}

/* 헤더 액션 버튼들 스타일 */
.header-actions {
  display: flex;
  /* 가로로 정렬 */
  flex-direction: row;
  /* 기본 행 방향 설정 */
  width: 400px;
  /* 너비 */
  justify-content: space-around;
  /* 항목 간 간격 균등하게 */
}

/* 액션 버튼 리스트 스타일 */
.header-actions li {
  list-style: none;
  /* 기본 리스트 스타일 제거 */
}

/* 액션 버튼 스타일 */
.header-actions button {
  /* webkit-box는 webkit 엔진에서 사용하는 css 속성으로 block으로 처리돼서 한줄로 표시 */
  display: -webkit-box;
  -webkit-line-clamp: 1;
  /* 줄 수를 설정할 수 있으며 넘치는 텍스트는 ...로 대체 */
  -webkit-box-orient: vertical;
  /* 다중으로 줄이 설정될 때 세로로 표시하도록 설정 */
  overflow: visible;
  /* 넘치는 내용들을 숨김처리 */
  margin-right: 0.5rem;
  /* 오른쪽 여백 */
  padding: 0.5rem 0.5rem;
  /* 상하 0.5rem, 좌우 1rem 패딩 */
  border: none;
  /* 테두리 제거 */
  background-color: #ffffff;
  /* 파란색 배경 */
  color: #000000;
  /* 글자 색을 흰색으로 설정 */
  font-size: 1rem;
  border-radius: 10px;
  /* 둥근 모서리 */
  cursor: pointer;
  /* 포인터 커서 표시 */
  white-space: nowrap;
}

/* 판매하기 버튼 */
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
  /* 어두운 파란색 */
  color: #ffffff;
}

/* 알림 섹션 스타일 */
.notifications {
  display: flex;
  /* 가로로 정렬 */
  align-items: center;
  /* 수직 중앙 정렬 */
  gap: 0.5rem;
  /* 아이콘과 텍스트 간 간격 */
  font-size: 1rem;
  /* 글자 크기 */
  color: #4c6ef5;
  /* 파란색 */
}

/* 알림 뱃지 스타일 */
.notifications .badge {
  display: inline-flex;
  /* 인라인 플렉스 */
  justify-content: center;
  /* 가운데 정렬 */
  align-items: center;
  /* 수직 중앙 정렬 */
  width: 20px;
  /* 너비 */
  height: 20px;
  /* 높이 */
  background-color: #ffa500;
  /* 오렌지색 배경 */
  color: white;
  /* 글자 색 흰색 */
  font-size: 0.75rem;
  /* 글자 크기 */
  font-weight: bold;
  /* 글자 굵게 */
  border-radius: 50%;
  /* 원형 모양 */
}

/* 알림 아이콘이 포함된 컨테이너 */
.notification-container {
  position: relative;
  /* 상대적 위치 */
  display: flex;
  /* 가로로 정렬 */
  align-items: center;
  /* 수직 중앙 정렬 */
}

/* 알림 아이콘이 있는 li 태그 스타일 */
.header-actions li.notification-container {
  padding: 0;
  /* 패딩 제거 */
  margin: 0 1.2rem;
  /* 좌우 여백 */
  white-space: nowrap;
}
</style>
