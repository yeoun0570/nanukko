<script setup>
import { useApi } from "@/composables/useApi";

const api = useApi();

const props = defineProps({
  deliveryData: {
    type: Object,
    required: true,
  },
});

const userAddrInfo = ref({});

const isEditing = ref(false); //수정 모드

// 로컬 상태로 관리
const localAddrDetail = ref(props.deliveryData.addrDetail);

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
  props.deliveryData = JSON.parse(JSON.stringify(originalUserInfo.value));
  isEditing.value = false;
};

// 주소 검색 팝업 열기
const openAddressSearch = () => {
  console.log("window.daum:", window.daum);
  new window.daum.Postcode({
    oncomplete: async (data) => {
      const selectedAddress = data.roadAddress || data.jibunAddress;

      // 주소 정보 전달
      props.deliveryData.addrMain = selectedAddress;
      props.deliveryData.addrZipcode = data.zonecode;
    },
  }).open();
};

const handleAddrDetailChange = (event) => {
  localAddrDetail.value = event.target.value;
  props.deliveryData.addrDetail = event.target.value
};

const updateUserAddr = async () => {
  const updateData = {
    addrMain: props.deliveryData.addrMain,
    addrDetail: props.deliveryData.addrDetail,
    addrZipcode: props.deliveryData.addrZipcode,
  };

  try {
    const response = await api.post("/payments/modify-address", updateData);
    isEditing.value = false;
    alert("주소가 변경되었습니다.")
  } catch (error) {
    console.error("사용자 배송지 정보 수정 실패: ", error);
  }
};
</script>

<template>
  <div class="delivery-container">
    <h2>배송지</h2>
    <div class="delivery-section">
      <div class="delivery-info">
        <div class="name-tag">
          <span class="name">{{ deliveryData.nickname }}</span>
          <span class="badge">최근</span>
          <button class="edit-button" @click="toggleEdit" v-if="!isEditing">
            변경
          </button>
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
        <span class="address" v-if="!isEditing">{{
          deliveryData.addrDetail
        }}</span>
        <input
          v-if="isEditing"
          v-model="localAddrDetail"
          @input="handleAddrDetailChange"
          placeholder="상세 주소를 입력하세요."
          class="detail-input"
        />
        <p class="phone">{{ deliveryData.mobile }}</p>
      </div>
    </div>
  </div>
  <div class="delivery-btn-container">
    <button class="save-button" v-if="isEditing" @click="updateUserAddr">
      저장
    </button>

    <button class="cancel-button" @click="cancelEdit" v-if="isEditing">
      취소
    </button>
  </div>
</template>
