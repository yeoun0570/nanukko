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
const { $auth } = useNuxtApp(); // 인증 관련 플러그인이 있다고 가정
const router = useRouter();

// const goToNewProduct = async () => {
//     // 로그인 상태 확인
//     if (!$auth.loggedIn) {  // 실제 구현에서는 사용하는 인증 시스템에 맞게 수정 필요
//         // 로그인되지 않은 경우
//         if (confirm('로그인이 필요한 서비스입니다. 로그인 페이지로 이동하시겠습니까?')) {
//             // 현재 페이지 URL을 쿼리 파라미터로 전달하여 로그인 후 복귀할 수 있도록 함
//             await navigateTo({
//                 path: '/login',
//                 query: {
//                     redirect: '/products/new'
//                 }
//             });
//         }
//         return;
//     }

//     // 인증 상태 확인 (예: JWT 또는 토큰 기반 인증)
//     const token = $auth.getToken(); // 사용하는 인증 모듈에 맞게 변경 필요
//     if (!token) {
//         // 인증되지 않은 경우 로그인 페이지로 이동
//         if (confirm('로그인이 필요한 서비스입니다. 로그인 페이지로 이동하시겠습니까?')) {
//             // 로그인 후 복귀할 수 있도록 현재 페이지 정보 전달
//             await navigateTo({
//                 path: '/login',
//                 query: {
//                     redirect: '/products/new',
//                 },
//             });
//         }
//         return;
//     }


//     const token = localStorage.getItem('authToken');


//     // 인증된 경우 상품 등록 페이지로 이동
//     await navigateTo({
//         path: '/products/new',
//         query: { authToken: token },
//     });
// }
</script>

<style scoped>
.navbar {
    display: flex;
    width: 100%; /* 전체 너비 */
    flex-direction: row; /* 기본 행 방향 설정 */
    align-items: center; /* 수직 중앙 정렬 */
    justify-content: center; /* 수평 중앙 정렬 */
    background-color: #ffffff;
    padding: 1rem; /* 상하좌우 여백 */
    margin: 0px auto; /* 좌우 자동 마진으로 중앙 정렬 */
    border-bottom: 1px solid #e0e0e0; /*하단 경계선 */
    box-sizing: border-box; /* 패딩 포함 크기 계산 */
  }

  .navbar ul {
    background-color: white;
    display: flex; /* 메뉴 항목을 가로로 정렬 */
    list-style: none; /* 기본 리스트 스타일 제거 */
    align-items: center; /* 메뉴를 가운데 정렬 */
    justify-content: center; /* 메뉴 항목들도 중앙 정렬 */
    /*margin: 0 0 0 20; /* 외부 여백 제거 */
    padding: 0; /* 내부 여백 제거 */
    width: 100%; /* ul도 전체 너비 차지 */
    max-width: 1000px; /* 최대 너비 설정 */
  }

  .navbar li {
    margin: 0 1.5rem; /* 각 메뉴 항목 사이에 여백 추가 */
    white-space: nowrap;
  }

  .navbar a {
    color: black; /* 링크 텍스트를 검점ㅇ으로 설정 */
    text-decoration: none; /* 기본 밑줄 제거 */
  }

  .navbar a:hover {
    text-decoration: none; /* 링크에 마우스를 올리면 밑줄 표시 */
  }

  </style>
  