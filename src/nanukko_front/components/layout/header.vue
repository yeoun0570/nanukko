<template>
    <header class="header">
      <!-- 로고와 검색창을 포함하는 Flex 컨테이너 -->
      <div class="header-container">
        <!-- 로고 -->
        <div class="logo">
            <img src="../../public/image/나누고_Logo_blue.png" alt="nanukko Logo" width="50" height="50" />
        </div>
        <!-- 검색창 -->
        <div class="search-bar">
          <input
            type="text"
            placeholder="Search"
            v-model="searchQuery"
            @keyup.enter="onSearch"
          />
          <button @click="onSearch"><i class="fi fi-br-search"></i></button>
        </div>
      </div>
    </header>
  </template>

<script setup>
import { ref } from 'vue'

/* 검색창의 입력값을 관리하기 위한 상태 */
const searchQuery = ref('')

/* 검색 동작 */
const onSearch = () => {
  if (searchQuery.value.trim()) {
    console.log('검색어:', searchQuery.value) // 실제로는 API 호출 또는 페이지 이동 처리
    alert(`You searched for: ${searchQuery.value}`) // 임시 알림 처리
  }
}
</script>
