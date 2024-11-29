<script setup>
const props = defineProps({
  deliveryData: {
    type: Object,
    required: true,
  },
});

const isEditing = ref(false); //수정 모드

// 원본 데이터 저장용 ref 추가
const originalUserInfo = ref(null);

//수정모드 진입
const toggleEdit = () => {
  if (!isEditing.value) {
    // 수정 모드 진입 시 현재 데이터 복사
    originalUserInfo.value = JSON.parse(JSON.stringify(props.deliveryData));
  }
  isEditing.value = !isEditing.value;
};

// 취소 버튼 클릭 시 원본 데이터로 복구
const cancelEdit = () => {
  userInfo.value = JSON.parse(JSON.stringify(originalUserInfo.value));
  isEditing.value = false;
};

// 주소 검색 팝업 열기
const openAddressSearch = () => {
  console.log("window.daum:", window.daum);
  new window.daum.Postcode({
    oncomplete: async (data) => {
      const selectedAddress = data.roadAddress || data.jibunAddress;

      // 주소 정보 전달
      emit("update:addrMain", selectedAddress); // 메인 주소 업데이트
      emit("update:addrZipcode", data.zonecode); // 우편번호 업데이트
    },
  }).open();
};

const handleAddrDetailChange = (event) => {
  localAddrDetail.value = event.target.value;
  emit("update:addrDetail", event.target.value);
};
</script>

<template>
  <div class="delivery-container">
    <div class="header">
      <h2>배송지</h2>
      <button class="edit-button" @click="toggleEdit">변경</button>
    </div>
    <div class="delivery-section">
      <div class="delivery-info">
        <div class="name-tag">
          <span class="name">{{ deliveryData.nickname }}</span>
          <span class="badge">최근</span>
        </div>
        <span class="address">
          ({{ deliveryData.addrZipcode }}) {{ deliveryData.addrMain }}
        </span>
        <button
          v-if="isEditing"
          @click="openAddressSearch"
          class="search-address-btn"
        >
          주소 검색
        </button>
        <span class="address" v-if="!isEditing">{{ deliveryData.addrDetail }}</span>
        <input
          v-if="isEditing"
          :placeholder="props.deliveryData.addrDetail"
          @input="handleAddrDetailChange"
          placeholder="상세 주소를 입력하세요."
          class="detail-input"
        />
        <p class="phone">{{ deliveryData.mobile }}</p>
      </div>
    </div>
  </div>
</template>
