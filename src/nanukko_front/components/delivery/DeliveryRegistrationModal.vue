<script setup>
import { useApi } from '../../composables/useApi'

const api = useApi();

const props = defineProps({
  productId: {
    type: Number,
    required: true,
  },
});

const emit = defineEmits(["close", "success"]);

// 배송 추적 API에 사용되는 택배사ID와 운송장번호
const formData = ref({
  carrierId: "",
  trackingNumber: "",
});

const loading = ref(false);
const error = ref(null);

//택배사 고유 ID들
const carriers = [
  { id: "kr.cjlogistics", name: "CJ대한통운" },
  { id: "kr.epost", name: "우체국택배" },
  { id: "kr.hanjin", name: "한진택배" },
  { id: "kr.lotte", name: "롯데택배" },
];

// 배송 등록시키기
const handleSubmit = async () => {
  if (loading.value) return;
  loading.value = true;
  error.value = null;

  try {
    // 요청 데이터 로깅
    console.log("배송 등록 요청 데이터:", {
      productId: props.productId,
      carrierId: formData.value.carrierId,
      trackingNumber: formData.value.trackingNumber,
    });

    const response = await api.post(
      `/delivery/register`,
      {
        productId: props.productId,
        carrierId: formData.value.carrierId,
        trackingNumber: formData.value.trackingNumber,
      }
    );

    console.log("response: ", response);
    emit("success");
  } catch (err) {
    error.value = err.response?.data?.message || "운송장 등록에 실패했습니다.";
  } finally {
    loading.value = false;
  }
};

const closeModal = () => {
  emit("close");
};
</script>

<template>
  <div class="modal-overlay" @click.self="closeModal">
    <div class="modal-content">
      <div class="modal-header">
        <h3>운송장 등록</h3>
        <button class="close-button" @click="closeModal">&times;</button>
      </div>

      <div v-if="error" class="error-message">
        {{ error }}
      </div>

      <form @submit.prevent="handleSubmit" class="delivery-form">
        <div class="form-group">
          <label for="carrier">택배사 선택</label>
          <select id="carrier" v-model="formData.carrierId" required>
            <option value="">택배사를 선택하세요</option>
            <option
              v-for="carrier in carriers"
              :key="carrier.id"
              :value="carrier.id"
            >
              {{ carrier.name }}
            </option>
          </select>
        </div>

        <div class="form-group">
          <label for="tracking">운송장 번호</label>
          <input
            id="tracking"
            type="text"
            v-model="formData.trackingNumber"
            placeholder="운송장 번호를 입력하세요"
            required
          />
        </div>

        <div class="button-group">
          <button
            type="button"
            class="cancel-btn"
            @click="closeModal"
            :disabled="loading"
          >
            취소
          </button>
          <button type="submit" class="submit-btn" :disabled="loading">
            {{ loading ? "등록 중..." : "운송장 등록" }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>

<style scoped>
@import url("../../assets/delivery/DeliveryRegistration.css");
</style>
