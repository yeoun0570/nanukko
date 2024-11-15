<script setup>
import axios from "axios";
import AddressInfo from "~/components/my-store/AddressInfo.vue";
import BasicInfo from "~/components/my-store/BasicInfo.vue";
import KidsInfo from "~/components/my-store/KidsInfo.vue";
import LoginInfo from "~/components/my-store/LoginInfo.vue";
import ProfileImage from "~/components/my-store/ProfileImage.vue";

const userInfo = ref({});
const loading = ref(true);
const error = ref(null);
const userId = "buyer1"; //테스트를 위한 사용자 아이디 임의로 설정
const isEditing = ref(false); //수정 모드

const toggleEdit = () => {
  isEditing.value = !isEditing.value;
};

const updateUserInfo = async () => {
  try {
    loading.value = true;
    error.value = null;

    const updateData = {
      userId: userId,
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

    await axios.post(
      `http://localhost:8080/api/my-store/modify`,
      updateData
    );
    alert("정보가 성공적으로 수정되었습니다.");
    isEditing.value = false; // 수정 모드 종료
    loadUserInfo(); // 새로운 정보 로드
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
    const response = await axios.get(
      `http://localhost:8080/api/my-store/info`,
      {
        params: {
          userId: userId,
        },
      }
    );
    console.log("받아온 데이터:", response.data); // 데이터 확인
    userInfo.value = response.data;
  } catch (err) {
    error.value = "사용자 정보를 불러오는데 실패했습니다.";
    console.error("Error loading user info:", err);
  } finally {
    loading.value = false;
  }
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
      <ProfileImage :profile="userInfo.profile"></ProfileImage>
      <div class="section-header">
        <p class="section-title">우리 부모님은요</p>
        <button v-if="!isEditing" @click="toggleEdit" class="edit-button">
          수정
        </button>
      </div>
      <LoginInfo :userId="userId" :password="password"></LoginInfo>
      <BasicInfo
        v-model:nickname="userInfo.nickname"
        v-model:email="userInfo.email"
        v-model:mobile="userInfo.mobile"
        v-model:userBirth="userInfo.userBirth"
        v-model:isEditing="isEditing"
      ></BasicInfo>
      <!-- 주소 수정은 따로 지도 API로 구현 -->
      <AddressInfo
        v-model:addrMain="userInfo.addrMain"
        v-model:addrDetail="userInfo.addrDetail"
        v-model:addrZipcode="userInfo.addrZipcode"
      ></AddressInfo>
      <div class="section-header">
        <p class="section-title">우리 아이는요</p>
      </div>
      <KidsInfo v-model:kids="userInfo.kids" :isEditing="isEditing"></KidsInfo>
    </div>

    <div class="button-container" v-if="isEditing">
      <button @click="updateUserInfo" :disabled="loading" class="save-button">
        {{ loading ? "저장 중..." : "저장" }}
      </button>
      <button @click="toggleEdit" class="cancel-button">취소</button>
    </div>
    <br />

    <NuxtLink v-if="!isEditing" to="/my-store/remove" class="remove-user"
      >탈퇴하기</NuxtLink
    >
  </div>
  <div v-else>로딩중...</div>
</template>

<style>
@import url("../../assets/my-store/UserInfo.css");
</style>
