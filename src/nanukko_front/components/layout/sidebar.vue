<script setup>

//드롭다운 메뉴 상태 관리
const isOpen = ref({
  store: false,
  traslation: false,
});

// 사용자 프로필 데이터 상태 관리
const userProfile = ref(null);

// 기본 프로필 이미지 경로
const defaultProfileImage = '../image/나누고_Logo_block.png';

//내상점 드롭다운//
const handleStore = () => {
  if (isOpen.value.store === false) {
    isOpen.value.store = true;
  } else {
    isOpen.value.store = false;
  }
};

//거래내역 드롭다운//
const handleTranslation = () => {
  if (isOpen.value.traslation === false) {
    isOpen.value.traslation = true;
  } else {
    isOpen.value.traslation = false;
  }
};

// 사용자 프로필 데이터 가져오기
const fetchUserProfile = async () => {
  try {
    // API 호출 예시 - 실제 엔드포인트로 수정 필요
    const response = await fetch('/api/user/profile');
    userProfile.value = await response.json();
  } catch (error) {
    console.error('프로필 정보 로드 실패:', error);
  }
};

// 평점 포맷팅 (소수점 한자리)
const formatRating = (rating) => {
  return rating ? rating.toFixed(1) : '0.0';
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
    <div class="profile-section" >
      <!--프로필 사진 영역-->
      <div class="profile-image" >
        <img src='../../public/image/나누고_Logo_blue.png' alt="프로필사진">
        <div>
          <!-- 프로필 정보 영역: 이름, 상품 수, 평점 등 -->
          <div class="profile-info">
            <!--사용자 이름-->
            <div class="profile-name">이효승</div>
          </div>
          <!-- 통계 정보 -->
          <div class="profile-stats">
              <!-- 상품 개수 -->
              <div class="stat-item">
                <span class="stat-label">상품</span>
                <span class="stat-value">{{ userProfile?.productCount }}개</span>
              </div>
              <!-- 평점 -->
              <div class="stat-item">
                <span class="stat-label">평점</span>
                <div class="rating">
                  <star-rating :rating="userProfile?.rating || 0" />
                  <span class="rating-value">{{ formatRating(userProfile?.rating) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
      <!-- 내정보 -->
      <div class="userInfo">
        <h2 class="click-title" @click="">내정보</h2>
      </div>

      <!--내상점-->
      <div class="mystore">
        <div class="dropdown-header" @click="Dropdown('store')">
          <h2 class="click-title" @click="handleStore">내상점</h2>
          <!-- <span class="arrow" :class="{'arrow-open': isOpen.store}" >▶</span> -->
        </div>
        <!-- <ul class ="dropdown-content" v-show="isOpen('store')"> -->
        <ul class="dropdown-content" v-if="isOpen.store">
          <li>판매상품</li>
          <li>구매상품</li>
          <li>찜상품</li>
          <li>나의 후기</li>
        </ul>
      </div>

      <!--거래내역-->
      <div class="transaction-history">
        <div class="dropdown-header" @click="Dropdown('translation')">
          <h2  class="click-title" @click="handleTranslation">거래내역</h2>
        </div>
        <!-- <ul class ="dropdown-content" v-show="isOpen('transaction-history')"> -->
        <ul class="dropdown-content" v-if="isOpen.traslation">
          <li>판매완료</li>
          <li>구매완료</li>
        </ul>
      </div>
    </aside>
  </div>
</template>

<style scoped>
@import url('../../assets/sidebar.css');
</style>