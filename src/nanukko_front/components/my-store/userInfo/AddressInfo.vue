<script setup>
const props = defineProps({
  addrMain: String,
  addrZipcode: String,
  addrDetail: String,
  isEditing: Boolean,
});

const emit = defineEmits([
  "update:addrMain",
  "update:addrZipcode",
  "update:addrDetail",
]);

// 로컬 상태로 관리
const localAddrDetail = ref(props.addrDetail);

// props 변경 감지하여 로컬 상태 업데이트
watch(
  () => props.addrDetail,
  (newVal) => {
    localAddrDetail.value = newVal;
  }
);

const handleAddrDetailChange = (event) => {
  localAddrDetail.value = event.target.value;
  emit("update:addrDetail", event.target.value);
};

// 주소 검색 팝업 열기
const openAddressSearch = () => {
  console.log("openAddressSearch called");

  // if (!window.daum?.Postcode) {
  //   alert("주소 검색 서비스를 불러오는 중입니다. 잠시 후 다시 시도해주세요.");
  //   return;
  // }

  console.log("Daum Postcode API is available. Opening search popup...");
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
</script>

<template>
  <div class="basic-info">
    <div class="info-item">
      <label>주소:</label>
      <span>({{ addrZipcode }}) {{ addrMain }}</span>
      <button
        v-if="isEditing"
        @click="openAddressSearch"
        class="search-address-btn"
      >
        주소 검색
      </button>
    </div>
    <div class="info-item">
      <label>상세 주소:</label>
      <input
        v-if="isEditing"
        v-model="localAddrDetail"
        @input="handleAddrDetailChange"
        placeholder="상세 주소를 입력하세요."
        class="detail-input"
      />
      <span v-else class="detail-text">{{ addrDetail }}</span>
    </div>
  </div>
</template>
