<template>
  <div class="reset-container">
    <div class="reset-card">
      <div class="card-header">
        <h1>비밀번호 재설정</h1>
      </div>

      <!-- 토큰이 없는 경우 -->
      <div v-if="!token" class="error-state">
        <i class="fas fa-exclamation-circle"></i>
        <h2>유효하지 않은 접근입니다</h2>
        <p>비밀번호 재설정 링크가 필요합니다.</p>
        <button @click="navigateToLogin" class="retry-button">
          비밀번호 찾기 다시 시도
        </button>
      </div>

      <!-- 비밀번호 재설정 폼 -->
      <form v-else class="reset-form" @submit.prevent="handleSubmit">
        <div class="form-group">
          <label for="newPassword">새 비밀번호</label>
          <div class="password-input">
            <input :type="showPassword ? 'text' : 'password'" id="newPassword" v-model="newPassword"
              :class="{ 'error': passwordError }" placeholder="새로운 비밀번호를 입력하세요" required />
            <button type="button" class="toggle-password" @click="() => togglePasswordVisibility('password')">
              <i :class="showPassword ? 'fas fa-eye-slash' : 'fas fa-eye'"></i>
            </button>
          </div>
        </div>

        <div class="form-group">
          <label for="confirmPassword">비밀번호 확인</label>
          <div class="password-input">
            <input :type="showConfirmPassword ? 'text' : 'password'" id="confirmPassword" v-model="confirmPassword"
              :class="{ 'error': confirmPasswordError }" placeholder="비밀번호를 다시 입력하세요" required />
            <button type="button" class="toggle-password" @click="() => togglePasswordVisibility('confirm')">
              <i :class="showConfirmPassword ? 'fas fa-eye-slash' : 'fas fa-eye'"></i>
            </button>
          </div>
        </div>

        <div class="password-rules">
          <p>비밀번호 요구사항:</p>
          <ul>
            <li :class="{ 'valid': passwordLength }">8자 이상 16자 이하</li>
            <li :class="{ 'valid': hasUpperCase }">대문자 포함</li>
            <li :class="{ 'valid': hasLowerCase }">소문자 포함</li>
            <li :class="{ 'valid': hasNumber }">숫자 포함</li>
          </ul>
        </div>

        <button type="submit" class="submit-button" :disabled="!isFormValid || isSubmitting">
          {{ isSubmitting ? "처리 중..." : "비밀번호 변경" }}
        </button>
      </form>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { usePasswordReset } from '~/composables/auth/usePasswordReset';

const route = useRoute();
const router = useRouter();
const token = ref(route.query.token);
const { $showToast } = useNuxtApp();

const {
  newPassword,
  confirmPassword,
  passwordError,
  confirmPasswordError,
  isSubmitting,
  showPassword,
  showConfirmPassword,
  passwordLength,
  hasUpperCase,
  hasLowerCase,
  hasNumber,
  isFormValid,
  togglePasswordVisibility,
  resetPassword
} = usePasswordReset();

const handleSubmit = async () => {
  try {
    const success = await resetPassword(token.value);
    if (success) {
      $showToast('비밀번호가 성공적으로 변경되었습니다. 새 비밀번호로 로그인해주세요.');
      router.push('/auth/login');
    }
  } catch (error) {
    $showToast('비밀번호 변경에 실패했습니다. 링크가 만료되었거나 유효하지 않을 수 있습니다.');
  }
};

const navigateToLogin = () => {
  router.push('/auth/login');
};
</script>

<style>
@import '~/assets/auth/reset-password.css';
</style>