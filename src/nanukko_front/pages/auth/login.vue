<template>
  <div class="login-container">
    <div class="login-card">
      <form class="login-form" @submit.prevent="handleLogin">
        <!--아이디 입력-->
        <div class="form-group">
          <label for="userId">아이디</label>
          <input
            id="userId"
            v-model="userId"
            type="text"
            required
            placeholder="아이디를 입력하세요"
            :class="{ error: error }"
          /><!--isError가 true라면 <input>에 error 클래스가 추가-->
        </div>
        <!-- 비밀번호 입력-->
        <div class="form-group">
          <label for="password">비밀번호</label>
          <input
            type="password"
            id="password"
            v-model="password"
            required
            placeholder="비밀번호를 입력하세요"
            :class="{ error: error }"
          />
        </div>

        <!-- 에러 메시지 출력-->
        <div v-if="error" class="error-message">
          {{ error }}
        </div>

        <!--로그인 버튼-->
        <button
          type="submit"
          :disabled="isLoading"
          class="login-button"
          @click="handleLogin"
        >
          <span v-if="isLoading">로그인 중...</span>
          <span v-else>로그인</span>
        </button>
      </form>

      <!--아아디/비밀번호 찾기 & 회원가입 링크-->
      <div class="auth-links">
        <div class="find-links">
          <button class="/auth/find-id" @click="showFindIdModal = true">
            아이디 찾기
          </button>
          <span class="divider">|</span>
          <button class="/auth/find-id" @click="showPasswordModal = true">
            비밀번호 찾기
          </button>
        </div>
        <NuxtLink to="/auth/register" class="register-link">회원가입</NuxtLink>
      </div>
    </div>
  </div>

  <FindIdModal :is-open="showFindIdModal" :on-close="handleCloseModal" />
  <ResetPasswordRequestModal :isOpen="showPasswordModal" :onClose="handleClosePasswordModal"/>
</template>

<script setup>
import { ref } from "vue";
import { useAuth } from "~/composables/auth/useAuth";
import { useApi } from "~/composables/useApi";
import { useRouter } from "vue-router";
import FindIdModal from "~/components/auth/FindIdModal.vue";
import ResetPasswordRequestModal from "~/components/auth/ResetPasswordRequestModal.vue";
import { useAuthStore } from "~/stores/authStore";

// 필요한 composables 초기화
const router = useRouter();
const { setToken } = useAuth();
const api = useApi();
const authStore = useAuthStore();

// 로컬 상태 관리
const userId = ref("");
const password = ref("");
const error = ref("");
const isLoading = ref(false); // 로그인 진행 중 여부

//아이디 찾기 모달창 관리
const showFindIdModal = ref(false);

// 모달 닫기 함수
const handleCloseModal = () => {
  showFindIdModal.value = false;
};

//비밀번호 찾기 모달 닫기 상태
const showPasswordModal = ref(false);
const handleClosePasswordModal = () =>{
  showPasswordModal.value = false
}

/**
 * 로그인 폼 제출 핸들러
 * - 사용자 입력값을 검증하고 API 호출
 * - 성공시 토큰 저장 및 페이지 이동
 * - 실패시 에러 메시지 표시
 */
const handleLogin = async () => {
  // 이미 로그인 진행 중이면 중복 실행 방지
  if (isLoading.value) return;

  try {
    isLoading.value = true; // 로딩 상태 활성화
    error.value = ""; // 이전 에러 메시지 초기화

    const loginData = {
      userId: userId.value,
      password: password.value,
    };

    //const response = await api.post('/login', loginData);
    // API 호출 시 raw response를 받도록 수정
    const response = await api.post("/login", loginData, {
      rawResponse: true, // useApi에 이 옵션을 추가해야 함
    });

    // 응답 검증
    if (!response.ok) {
      // 에러 응답의 경우 JSON으로 파싱 시도
      const errorData = await response.text();
      throw new Error(errorData || "로그인에 실패했습니다.");
    }

    // 응답 헤더에서 토큰 추출
    const token = response.headers.get("Authorization")?.replace("Bearer ", "");
    if (!token) {
      throw new Error("인증 토큰을 받지 못했습니다.");
    }

    // 로그인 성공 처리
    setToken(token); // 토큰 저장

    authStore.setAuthenticated(true, loginData);
   

    // 페이지 이동 (완료될 때까지 대기)
    await router.push("/");
    //window.location.reload();

    console.log("로그인 완료 후 메인페이지 이동");
  } catch (err) {
    // 에러 처리
    error.value = err.message;
    console.error("로그인 처리 중 에러 발생:", err);
  } finally {
    // 상태 정리
    isLoading.value = false; // 로딩 상태 비활성화
  }
};
</script>

<style scoped>
@import "~/assets/auth/login.css";
</style>
