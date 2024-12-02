<script setup>
import { useApi } from "@/composables/useApi";

const api = useApi();

// 사용자 프로필 데이터 상태 관리
const userProfile = ref({});

// 사용자 프로필 데이터 가져오기
const fetchUserProfile = async () => {
  try {
    const response = await api.get("/my-store/simple-info");
    userProfile.value = response;
    console.log("사이드바 사용자 정보: ", userProfile.value);
  } catch (error) {
    console.error("프로필 정보 로드 실패:", error);
  }
};

// 컴포넌트 마운트 시 프로필 정보 로드
onMounted(() => {
  fetchUserProfile();
});
</script>

<template>
  <div class="sidebar">
    <!--세 가지 모두를 포함하는 사이드바-->
    <aside class="sidebar-container">
      <!--프로필 영역-->
      <div class="profile-section">
        <!--프로필 사진 영역-->
        <div class="profile-image">
          <img
            :src="userProfile.profile || '/image/default-profile.png'"
            alt="프로필사진"
          />
          <div>
            <!-- 프로필 정보 영역: 이름, 상품 수, 평점 등 -->
            <div class="profile-info">
              <!--사용자 이름-->
              <div class="profile-name">{{ userProfile.nickname }}</div>
            </div>
            <!-- 통계 정보 -->
            <div class="profile-stats">
              <!-- 상품 개수 -->
              <div class="stat-item">
                <span class="stat-label">상품</span>
                <span class="stat-value"
                  >{{ userProfile?.countProduct }}개</span
                >
              </div>
              <!-- 평점 -->
              <div class="stat-item">
                <span class="stat-label">평점</span>
                <div class="rating">
                  <span class="rating-value">{{ userProfile.reviewRate }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div class="border-section"><br></div>
      <!-- 내정보 -->
      <!-- <div class="userInfo">
        <h2 class="click-title" @click="">내정보</h2>
      </div> -->

      <!--내상점-->
      <div class="mystore">
        <h2 class="click-title">내상점</h2>
        <ul class="dropdown-content">
          <li><NuxtLink to="/my-store">내 정보</NuxtLink></li>
          <li><NuxtLink to="/my-store/sale-products">판매상품</NuxtLink></li>
          <li><NuxtLink to="/my-store/buy-products">구매상품</NuxtLink></li>
          <li><NuxtLink to="/my-store/wishlist">찜상품</NuxtLink></li>
          <li><NuxtLink to="/my-store/reviews">나의 후기</NuxtLink></li>
        </ul>
      </div>

      <!--거래내역-->
      <!-- <div class="transaction-history">
        <h2 class="click-title">거래내역</h2>
        <ul class="dropdown-content">
          <li>판매완료</li>
          <li>구매완료</li>
        </ul>
      </div> -->
    </aside>
  </div>
</template>

<style scoped>
@import url("../../assets/sidebar.css");
</style>
