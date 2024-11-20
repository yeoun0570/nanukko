<template>
    <!-- 네비게이션 바 -->
    <nav class="navbar">
      <ul>
        <!-- NuxtLink로 카테고리 페이지 이동  -->
        <li><NuxtLink to="/first">0~3개월</NuxtLink></li> 
        <li><NuxtLink to="/second">3~6개월</NuxtLink></li>
        <li><NuxtLink to="/">6~12개월</NuxtLink></li>
        <li><NuxtLink to="/">만 1세</NuxtLink></li>
        <li><NuxtLink to="/">만 2세</NuxtLink></li>
        <li><NuxtLink to="/">만 3세</NuxtLink></li>
        <li><NuxtLink to="/">만 4세</NuxtLink></li>
        <li><NuxtLink to="/" class="text">만 5세</NuxtLink></li>
      </ul>
      <button class="sell-button">판매하기</button>
    </nav>
    
  </template>
  
  <style scoped>
  /* 네비게이션 바 */
  .navbar {

    display: flex;
    flex-direction: row;
    justify-content: space-between;
    background-color: white;/* 배경색 */
    padding: 0.5rem; /* 상하좌우 여백 */
    margin: 0px 17rem;
  }
  
  .navbar ul {
    display: flex; /* 메뉴 항목을 가로로 정렬 */
    list-style: none; /* 기본 리스트 스타일 제거 */
    justify-content: center; /* 메뉴를 가운데 정렬 */
    margin: 0; /* 외부 여백 제거 */
    padding: 0; /* 내부 여백 제거 */
  }
  
  .navbar li {
    margin: 0 1.2rem; /* 각 메뉴 항목 사이에 여백 추가 */
  }
  
  .navbar a {
    color: black; /* 링크 텍스트를 흰색으로 설정 */
    text-decoration: none; /* 기본 밑줄 제거 */
  }
  
  .navbar a:hover {
    text-decoration: underline; /* 링크에 마우스를 올리면 밑줄 표시 */
  }

  /* 판매하기 버튼 */
.sell-button {
    padding: 0.8rem 1.5rem;
  border: none;
  background-color: #4c6ef5;
  color: white;
  border-radius: 10px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}


.sell-button:hover {
  background-color: #3b5bdb; /* 어두운 파란색 */
}
.text{
    text-decoration: none;
}
  </style>
  