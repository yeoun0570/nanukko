<template>
    <!-- 네비게이션 바 -->
    <nav class="navbar">
        <ul>
            <li>
                <NuxtLink to="/products/category/100000">0~3개월</NuxtLink>
            </li>
            <li>
                <NuxtLink to="/products/category/200000">3~6개월</NuxtLink>
            </li>
            <li>
                <NuxtLink to="/products/category/300000">6~12개월</NuxtLink>
            </li>
            <li>
                <NuxtLink to="/products/category/400000">만1세</NuxtLink>
            </li>
            <li>
                <NuxtLink to="/products/category/500000">만2세</NuxtLink>
            </li>
            <li>
                <NuxtLink to="/products/category/600000">유아</NuxtLink>
            </li>
        </ul>

    </nav>
</template>

<script setup>
</script>

<style scoped>
.navbar {
    display: flex;
    width: 100%;
    /* 전체 너비 */
    flex-direction: row;
    /* 기본 행 방향 설정 */
    align-items: center;
    /* 수직 중앙 정렬 */
    justify-content: center;
    /* 수평 중앙 정렬 */
    background-color: #ffffff;
    padding: 1rem;
    /* 상하좌우 여백 */
    margin: 0px auto;
    /* 좌우 자동 마진으로 중앙 정렬 */
    border-bottom: 1px solid #e0e0e0;
    /*하단 경계선 */
    box-sizing: border-box;
    /* 패딩 포함 크기 계산 */
}

.navbar ul {
    background-color: white;
    display: flex;
    /* 메뉴 항목을 가로로 정렬 */
    list-style: none;
    /* 기본 리스트 스타일 제거 */
    align-items: center;
    /* 메뉴를 가운데 정렬 */
    justify-content: center;
    /* 메뉴 항목들도 중앙 정렬 */
    /*margin: 0 0 0 20; /* 외부 여백 제거 */
    padding: 0;
    /* 내부 여백 제거 */
    width: 100%;
    /* ul도 전체 너비 차지 */
    max-width: 1000px;
    /* 최대 너비 설정 */
}

.navbar li {
    margin: 0 1.5rem;
    /* 각 메뉴 항목 사이에 여백 추가 */
    white-space: nowrap;
}

.navbar a {
    color: black;
    /* 링크 텍스트를 검점ㅇ으로 설정 */
    text-decoration: none;
    /* 기본 밑줄 제거 */
}

.navbar a:hover {
    text-decoration: none;
    /* 링크에 마우스를 올리면 밑줄 표시 */
}
</style>