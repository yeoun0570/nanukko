<script setup>
import { useApi } from "@/composables/useApi";

const api = useApi();
const { $showToast } = useNuxtApp();

const deliveryInfo = ref({
  trackingNumber: "",
  status: "",
});

const updateDeliveryStatus = async () => {
  try {
    const response = await api.post(
      "/delivery/update-status",
      deliveryInfo.value
    );
    console.log("응답 데이터: ", response);
    $showToast("배송 상태 수정이 완료되었습니다.");
  } catch (error) {
    console.error("배송 상태 수정 실패: ", error);
  }
};

const handleTrackingNumberChange = (event) => {
  deliveryInfo.value.trackingNumber = event.target.value;
};

const handleStatusInTransit = () => {
  deliveryInfo.value.status = "IN_TRANSIT";
};

const handleStatusDelivered = () => {
  deliveryInfo.value.status = "DELIVERED";
};
</script>

<template>
  <div class="delivery-container">
    <div class="input-section">
      <input @input="handleTrackingNumberChange" placeholder="운송장 번호를 입력하세요" />
    </div>

    <div class="button-group">
      <button @click="handleStatusInTransit">배송 시작</button>
      <button @click="handleStatusDelivered">배송 완료</button>
    </div>

    <div class="status-display">
      <span>운송장 번호: {{ deliveryInfo.trackingNumber || '미입력' }}</span>
      <span>배송 상태: {{ deliveryInfo.status || '미지정' }}</span>
    </div>

    <button class="update-button" @click="updateDeliveryStatus">상태 변경</button>
  </div>
</template>

<style scoped>
.delivery-container {
  max-width: 600px;
  margin: 20px auto;
  padding: 20px;
  background-color: #ffffff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.input-section input {
  width: 100%;
  padding: 10px;
  margin-bottom: 15px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
}

.input-section input:focus {
  outline: none;
  border-color: #4a90e2;
  box-shadow: 0 0 0 2px rgba(74, 144, 226, 0.2);
}

.button-group {
  display: flex;
  gap: 10px;
  margin-bottom: 15px;
}

.button-group button {
  padding: 8px 16px;
  background-color: #4a90e2;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.button-group button:hover {
  background-color: #357abd;
}

.status-display {
  background-color: #f5f5f5;
  padding: 15px;
  border-radius: 4px;
  margin-bottom: 15px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.status-display span {
  font-size: 14px;
  color: #333;
}

.update-button {
  width: 100%;
  padding: 10px;
  background-color: #5c6bc0;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.update-button:hover {
  background-color: #3f51b5;
}
</style>