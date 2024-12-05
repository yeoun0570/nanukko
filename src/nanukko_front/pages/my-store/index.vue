<script setup>
import AddressInfo from "~/components/my-store/userInfo/AddressInfo.vue";
import BasicInfo from "~/components/my-store/userInfo/BasicInfo.vue";
import KidsInfo from "~/components/my-store/userInfo/KidsInfo.vue";
import LoginInfo from "~/components/my-store/userInfo/LoginInfo.vue";
import ProfileImage from "~/components/my-store/userInfo/ProfileImage.vue";
import { useApi } from "@/composables/useApi";
import ClovaChatbot from "~/components/chatbot/ClovaChatbot.vue";

const api = useApi();
const { $showToast } = useNuxtApp();

//mystore 레이아웃에서 받은 함수
const refreshUserProfile = inject("refreshUserProfile");

definePageMeta({
  layout: "mystore",
});

const userInfo = ref({});
const loading = ref(true);
const error = ref(null);
const isEditing = ref(false); //수정 모드

// 원본 데이터 저장용 ref 추가
const originalUserInfo = ref(null);

//수정모드 진입
const toggleEdit = () => {
  if (!isEditing.value) {
    // 수정 모드 진입 시 현재 데이터 복사
    originalUserInfo.value = JSON.parse(JSON.stringify(userInfo.value));
  }
  isEditing.value = !isEditing.value;
};

// 취소 버튼 클릭 시 원본 데이터로 복구
const cancelEdit = () => {
  userInfo.value = JSON.parse(JSON.stringify(originalUserInfo.value));
  isEditing.value = false;
};

const updateUserInfo = async () => {
  try {
    loading.value = true;
    error.value = null;

    const updateData = {
      password: userInfo.value.password,
      nickname: userInfo.value.nickname,
      mobile: userInfo.value.mobile,
      email: userInfo.value.email,
      userBirth: userInfo.value.userBirth,
      addrMain: userInfo.value.addrMain,
      addrDetail: userInfo.value.addrDetail,
      addrZipcode: userInfo.value.addrZipcode,
      profile: userInfo.value.profile,
      kids: userInfo.value.kids?.map((kid) => ({
        kidId: kid.kidId,
        kidBirth: kid.kidBirth,
        kidGender: kid.kidGender,
      })),
    };

    console.log("수정된 정보: ", updateData);

    await api.post(`/my-store/modify`, updateData);
    $showToast("정보가 성공적으로 수정되었습니다.");
    isEditing.value = false; // 수정 모드 종료
    loadUserInfo(); // 새로운 정보 로드
    await refreshUserProfile();
  } catch (err) {
    error.value = "정보 수정 중 오류가 발생했습니다.";
    console.error("수정 실패:", err.response?.data);
  } finally {
    loading.value = false;
  }
};

//사용자 정보 페이지 로딩할 메서드 작성
const loadUserInfo = async () => {
  try {
    loading.value = true;
    error.value = null;
    const response = await api.get(`/my-store/info`);

    console.log("받아온 데이터:", response); // 데이터 확인
    userInfo.value = response;
    originalUserInfo.value = { ...response };
  } catch (err) {
    error.value = "사용자 정보를 불러오는데 실패했습니다.";
    console.error("Error loading user info:", err);
  } finally {
    loading.value = false;
  }
};

//프로필 정보 업데이트 받아서 수정하기 위한 메서드
const handleProfileUpdate = (newProfileUrl) => {
  userInfo.value = {
    ...userInfo.value,
    profile: newProfileUrl,
  };
  console.log("프로필 업데이트 후 사용자 정보: ", userInfo.value);
  // 필요한 경우 여기서 추가적인 처리
};

onMounted(() => {
  loadUserInfo();
});
</script>

<template>
  <div v-if="userInfo" class="user-info">
    <h2>내 정보</h2>
    <br />
    <hr color="black" />
    <br />
    <div class="info-container">
      <ProfileImage v-model:profile="userInfo.profile" v-model:isEditing="isEditing"
        @update:profile="handleProfileUpdate"></ProfileImage>
      <div class="section-header">
        <p class="section-title">우리 부모님은요</p>
        <button v-if="!isEditing" @click="toggleEdit" class="edit-button">
          수정
        </button>
      </div>
      <LoginInfo :userId="userInfo.userId" :password="userInfo.password"></LoginInfo>
      <BasicInfo v-model:nickname="userInfo.nickname" v-model:email="userInfo.email" v-model:mobile="userInfo.mobile"
        v-model:userBirth="userInfo.userBirth" v-model:isEditing="isEditing"></BasicInfo>
      <!-- 주소 수정은 따로 지도 API로 구현 -->
      <AddressInfo v-model:addrMain="userInfo.addrMain" v-model:addrDetail="userInfo.addrDetail"
        v-model:addrZipcode="userInfo.addrZipcode" v-model:isEditing="isEditing"></AddressInfo>
      <div class="section-header">
        <p class="section-title">우리 아이는요</p>
      </div>
      <KidsInfo v-model:kids="userInfo.kids" :isEditing="isEditing"></KidsInfo>
    </div>

    <div class="button-container" v-if="isEditing">
      <button @click="updateUserInfo" :disabled="loading" class="save-button">
        {{ loading ? "저장 중..." : "저장" }}
      </button>
      <button @click="cancelEdit" class="cancel-button">취소</button>
    </div>
    <br />

    <NuxtLink v-if="!isEditing" :to="`/my-store/remove?userId=${userInfo.userId}`" class="remove-user">탈퇴하기</NuxtLink>
  </div>
  <div v-else>로딩중...</div>

  <ClovaChatbot />
</template>

<style>
@import url("../../assets/my-store/UserInfo.css");
</style>
