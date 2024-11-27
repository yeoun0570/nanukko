<script setup>
import axios from "axios";
import { useApi } from "~/composables/useApi";

const { baseURL } = useApi();

const route = useRoute();

const userId = route.query.userId;

// userId가 없는 경우 처리
if (!userId) {
  navigateTo("/");
}

const userInfo = ref({ canceled: false }); // 초기값 설정

const removeUser = async () => {
  if (!confirm("정말 탈퇴하시겠습니까?")) {
    return;
  }

  try {
    const response = await axios.post(
      `${baseURL}/my-store/remove`,
      null,
      {
        params: {
          userId: userId,
        },
      }
    );
    userInfo.value = response.data;

    console.log("Response:", response.data);
    console.log("userInfo: ", userInfo.value);
    if (response.data.canceled) {
      alert("회원 탈퇴가 완료되었습니다.");
      await navigateTo("/login");
    }
  } catch (error) {
    console.log("탈퇴 실패: ", error);
    console.error("에러 응답:", error.response?.data); // 에러 응답 확인용 로그

    if (error.response?.data) {
      // 백엔드의 예외 메시지를 그대로 표시
      if (error.response?.data?.message) {
        alert(error.response.data.message);
      } else {
        alert("회원 탈퇴 처리 중 오류가 발생했습니다.");
      }
    }
  }
};
</script>

<template>
  <div class="remove-page">
    <div class="remove-container">
      <div class="header">
        <h2>회원 탈퇴</h2>
        <div class="warning-icon">!</div>
      </div>

      <hr />

      <div class="content">
        <div class="warning-box">
          <h3>탈퇴 전 꼭 확인해주세요!</h3>
          <div class="warning-items">
            <div class="warning-item">
              <span class="bullet"></span>
              <p>계정을 삭제하시면 아래 정보가 모두 삭제됩니다:</p>
            </div>
            <ul class="delete-list">
              <li>스킨</li>
              <li>포인트</li>
              <li>거래내역</li>
              <li>게시글</li>
              <li>찜</li>
              <li>채팅</li>
            </ul>
            <div class="warning-item">
              <span class="bullet"></span>
              <p>
                계정 삭제 후, <strong>7일 간 다시 가입이 제한</strong>됩니다.
              </p>
            </div>
          </div>
        </div>
      </div>

      <div class="button-container">
        <NuxtLink to="/my-store" class="cancel-btn">취소</NuxtLink>
        <button @click="removeUser" class="remove-btn">탈퇴하기</button>
      </div>
    </div>
  </div>
</template>

<style scoped>
@import url(../../assets/my-store/UserRemove.css);
</style>
