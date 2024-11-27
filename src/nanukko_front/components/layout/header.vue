// components/layout/header.vue
<template>
    <header class="header">
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
                <li class="notification-container">
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
const searchQuery = ref("");

// 검색 실행
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
    position: relative;
    display: flex;
    flex-direction: row;
    align-items: center;
    padding: 0.5rem 1rem;
    background-color: white;
    border-bottom: 1px solid #e0e0e0;
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
    display: block;
}

/* 검색창 스타일 */
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
    position: relative;
}

.search-bar input {
    width: 100%;
    padding: 0.5rem 1rem;
    border: none;
    border-radius: 15px;
    outline: none;
    font-size: 1rem;
    color: #333;
    background-color: white;
}

.search-bar input::placeholder {
    color: #aaa;
}

.search-bar button {
    margin: 0 0.5rem;
    padding: 0.5rem;
    border: none;
    background: none;
    cursor: pointer;
    transition: opacity 0.2s;
}

.search-bar button:hover {
    opacity: 0.7;
}

/* 헤더 액션 버튼 */
.header-actions {
    display: flex;
    flex-direction: row;
    width: 400px;
    justify-content: space-around;
}

.header-actions li {
    list-style: none;
}

.header-actions a {
    text-decoration: none;
    color: #333;
    font-size: 0.9rem;
    padding: 0.5rem;
    transition: color 0.2s;
}

.header-actions a:hover {
    color: #4c6ef5;
}

/* 알림 컨테이너 */
.notification-container {
    position: relative;
    display: flex;
    align-items: center;
}

/* 반응형 스타일 */
@media (max-width: 1200px) {
    .header-container {
        margin: 0px 5rem;
    }
}

@media (max-width: 900px) {
    .header-container {
        margin: 0px 2rem;
    }

    .search-bar {
        min-width: 300px;
    }

    .header-actions {
        width: 300px;
    }
}

@media (max-width: 768px) {
    .header-container {
        flex-wrap: wrap;
        justify-content: center;
        gap: 1rem;
        padding: 1rem;
    }

    .search-bar {
        order: 2;
        min-width: 100%;
    }

    .header-actions {
        order: 3;
        width: 100%;
        justify-content: space-evenly;
    }
}
</style>