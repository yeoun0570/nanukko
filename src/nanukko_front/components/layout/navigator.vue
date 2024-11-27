<template>
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
        <button class="sell-button" @click="goToNewProduct">판매하기</button>
    </nav>
</template>

<script setup>
const { $auth } = useNuxtApp(); // 인증 관련 플러그인이 있다고 가정
const router = useRouter();

const goToNewProduct = async () => {
    // 로그인 상태 확인
    // if (!$auth.loggedIn) {  // 실제 구현에서는 사용하는 인증 시스템에 맞게 수정 필요
    //     // 로그인되지 않은 경우
    //     if (confirm('로그인이 필요한 서비스입니다. 로그인 페이지로 이동하시겠습니까?')) {
    //         // 현재 페이지 URL을 쿼리 파라미터로 전달하여 로그인 후 복귀할 수 있도록 함
    //         await navigateTo({
    //             path: '/login',
    //             query: {
    //                 redirect: '/products/new'
    //             }
    //         });
    //     }
    //     return;
    // }

    // 로그인된 경우 상품 등록 페이지로 이동
    await navigateTo('/products/new');
}
</script>

<style scoped>
.navbar {
    display: flex;
    flex-direction: row;
    justify-content: space-between;
    background-color: white;
    padding: 0.5rem;
    margin: 0px 17rem;
}

.navbar ul {
    display: flex;
    list-style: none;
    justify-content: center;
    margin: 0;
    padding: 0;
}

.navbar li {
    margin: 0 1.2rem;
}

.navbar a {
    color: black;
    text-decoration: none;
}

.navbar a:hover {
    text-decoration: underline;
}

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
    background-color: #3b5bdb;
}

.text {
    text-decoration: none;
}
</style>