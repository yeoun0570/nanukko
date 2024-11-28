<script setup>
import axios from "axios";
import { useURL } from "~/composables/useURL";
import { useAuth } from "~/composables/auth/useAuth";

const { baseURL } = useURL();
const auth = useAuth();

// token을 ref로 선언
const token = ref(null);

// 토큰을 사용한 axios 인스턴스 생성
const axiosInstance = axios.create({
  baseURL: baseURL,
});

// 컴포넌트가 마운트된 후에 token 가져오기
// 요청 인터셉터 설정
onMounted(() => {
  axiosInstance.interceptors.request.use((config) => {
    const token = auth.getToken();
    console.log("Current token:", token); // 토큰 확인용 로그
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    console.log("Request headers:", config.headers); // 헤더 확인용 로그
    return config;
  });
});

const props = defineProps({
  profile: String,
  isEditing: Boolean,
});

const emit = defineEmits(["update:profile"]);
const fileInput = ref(null);
// profile prop이 변경될 때마다 previewUrl 업데이트
const previewUrl = ref(props.profile);
const isHoverd = ref(false);

// profile prop 변경 감지
watch(
  () => props.profile,
  (newValue) => {
    previewUrl.value = newValue;
  }
);

// 이미지 클릭 시 파일 input 트리거
const triggerFileInput = () => {
  if (props.isEditing) {
    fileInput.value.click();
  }
};

// 파일이 선택되었을 때 실행되는 핸들러 함수
const handleFileSelect = async (event) => {
  // 선택된 파일 가져오기
  const file = event.target.files[0];
  // 파일이 없으면 함수 종료
  if (!file) return;

  //파일 미리보기 생성
  // FileReader를 사용하여 선택된 파일의 미리보기 생성
  const reader = new FileReader();
  // 파일 읽기가 완료되면 실행될 콜백
  reader.onload = () => {
    // 미리보기 URL 업데이트
    previewUrl.value = reader.result;
  };
  // 파일을 Data URL로 읽기 시작
  reader.readAsDataURL(file);

  //파일 업로드 처리
  try {
    // 서버로 전송할 FormData 객체 생성
    const formData = new FormData();
    formData.append("file", file);

    const response = await axiosInstance.post(
      `${baseURL}/files/profile`,
      formData,
      {
        headers: {
          "Content-Type": "multipart/form-data",
          "x-amz-acl": "public-read", // 공개 읽기 권한으로 수정
        },
      }
    );
    console.log("response.data.uploadFileUrl: ", response.data.uploadFileUrl);

    //업로드 성공시 부모 컴포넌트에 URL 전달
    // uploadFileUrl은 NCP Object Storage의 전체 URL
    if (response.data?.uploadFileUrl) {
      // URL 업데이트 후 사용자 정보도 함께 업데이트
      previewUrl.value = response.data.uploadFileUrl;
      console.log("previewUrl", previewUrl.value);
      emit("update:profile", response.data.uploadFileUrl);
    }
  } catch (error) {
    console.error("프로필 이미지 업로드 실패: ", error);
    alert("이미지 업로드에 실패했습니다.");
  }
};
</script>

<template>
  <div class="profile-container">
    <div
      class="profile-image"
      @mouseenter="isHovered = true"
      @mouseleave="isHovered = false"
      @click="triggerFileInput"
    >
      <img :src="previewUrl" alt="프로필 이미지" />

      <div v-if="isEditing && isHovered" class="image-overlay">
        <i class="fas fa-camera"></i>
      </div>

      <input
        ref="fileInput"
        type="file"
        class="hidden-input"
        accept="image/*"
        @change="handleFileSelect"
      />
    </div>
  </div>
</template>
