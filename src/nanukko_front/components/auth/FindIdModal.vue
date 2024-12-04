<script setup>
import { useApi } from '@/composables/useApi';

const api = useApi();

const props = defineProps({
  isOpen: {
    type: Boolean,
    required: true,
  },
  onClose: {
    type: Function,
    required: true,
  },
});

const email = ref("");
const isLoading = ref(false);

const findId = async () => {
  if (!email.value) return;

  try {
    isLoading.value = true;
    await api.post(`/user/find-id`, null, {
      params: {
        email: email.value,
      },
    });
    alert("이메일이 성공적으로 전송되었습니다.");
    email.value = ""; // 입력 필드 초기화
    props.onClose();
  } catch (error) {
    console.error("아이디 찾기 실패:", error);
    alert("이메일 전송에 실패했습니다. 다시 시도해주세요.");
  } finally {
    isLoading.value = false;
  }
};

const handleOverlayClick = (event) => {
  if (event.target === event.currentTarget) {
    props.onClose();
  }
};
</script>

<template>
  <div v-if="isOpen" class="modal-overlay" @click="handleOverlayClick">
    <div class="modal-content" @click.stop>
      <div class="modal-header">
        <h2>아이디 찾기</h2>
        <button class="close-button" @click="onClose">&times;</button>
      </div>

      <div class="modal-body">
        <div class="form-group">
          <label for="email">이메일</label>
          <input
            id="email"
            type="email"
            v-model="email"
            placeholder="가입시 등록한 이메일을 입력해주세요"
            required
          />
        </div>

        <p class="info-text">* 입력하신 이메일로 아이디 정보가 발송됩니다.</p>

        <div class="button-group">
          <button
            class="submit-button"
            @click="findId"
            :disabled="!email || isLoading"
          >
            {{ isLoading ? "전송 중..." : "이메일 전송" }}
          </button>
          <button class="cancel-button" @click="onClose">취소</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background: white;
  border-radius: 8px;
  padding: 20px;
  width: 90%;
  max-width: 400px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.modal-header h2 {
  margin: 0;
  font-size: 1.5rem;
}

.close-button {
  background: none;
  border: none;
  font-size: 1.5rem;
  cursor: pointer;
  padding: 0;
  color: #666;
}

.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-weight: 500;
}

.form-group input {
  width: 100%;
  padding: 10px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 1rem;
}

.info-text {
  color: #666;
  font-size: 0.9rem;
  margin-bottom: 20px;
}

.button-group {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}

.submit-button, 
.cancel-button {
  padding: 10px 20px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1rem;
  transition: background-color 0.2s;
}

.submit-button {
  background-color: #007bff;
  color: white;
}

.submit-button:disabled {
  background-color: #ccc;
  cursor: not-allowed;
}

.submit-button:hover:not(:disabled) {
  background-color: #0056b3;
}

.cancel-button {
  background-color: #6c757d;
  color: white;
}

.cancel-button:hover {
  background-color: #5a6268;
}
</style>