import { ref, computed } from 'vue';
import { useApi } from '../useApi';

export const usePasswordReset = () => {
  const api = useApi();
  
  // 상태 관리
  const newPassword = ref('');
  const confirmPassword = ref('');
  const passwordError = ref('');
  const confirmPasswordError = ref('');
  const isSubmitting = ref(false);
  const showPassword = ref(false);
  const showConfirmPassword = ref(false);

  // 비밀번호 유효성 검사
  const passwordLength = computed(() => newPassword.value.length >= 8 && newPassword.value.length <= 16);
  const hasUpperCase = computed(() => /[A-Z]/.test(newPassword.value));
  const hasLowerCase = computed(() => /[a-z]/.test(newPassword.value));
  const hasNumber = computed(() => /[0-9]/.test(newPassword.value));

  const isPasswordValid = computed(() => {
    return passwordLength.value && 
           hasUpperCase.value && 
           hasLowerCase.value && 
           hasNumber.value;
  });

  const isFormValid = computed(() => {
    return isPasswordValid.value && newPassword.value === confirmPassword.value;
  });

  // 비밀번호 표시/숨김 토글
  const togglePasswordVisibility = (field) => {
    if (field === 'password') {
      showPassword.value = !showPassword.value;
    } else {
      showConfirmPassword.value = !showConfirmPassword.value;
    }
  };

  // 비밀번호 변경 요청
  const resetPassword = async (token) => {
    if (!isFormValid.value) return false;
  
    try {
      isSubmitting.value = true;
  
      await api.post('/user/reset-password', {
        token,  // 토큰
        newPassword: newPassword.value  // 새 비밀번호
      });
      return true;
    } catch (error) {
      console.error('비밀번호 변경 실패:', error);
      throw error;
    } finally {
      isSubmitting.value = false;
    }
  };

  return {
    // 상태
    newPassword,
    confirmPassword,
    passwordError,
    confirmPasswordError,
    isSubmitting,
    showPassword,
    showConfirmPassword,
    
    // 유효성 검사
    passwordLength,
    hasUpperCase,
    hasLowerCase,
    hasNumber,
    isPasswordValid,
    isFormValid,
    
    // 메서드
    togglePasswordVisibility,
    resetPassword
  };
};