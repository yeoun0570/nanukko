<script setup>
import { useURL } from "~/composables/useURL";
import { useAuth } from "~/composables/auth/useAuth";

const { axiosInstance } = useURL();
const auth = useAuth();

// token을 ref로 선언
const token = ref(null);

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

  // 허용되는 이미지 형식 검사
  const allowedTypes = ["image/jpeg", "image/png"];
  if (!allowedTypes.includes(file.type)) {
    alert("지원하지 않는 파일 형식입니다. JPG, PNG 형식만 업로드 가능합니다.");
    event.target.value = ""; // 파일 선택 초기화
    return;
  }

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

    const response = await axiosInstance.post("/files/profile", formData, {
      headers: {
        "Content-Type": "multipart/form-data",
      },
    });
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

// 프로필 이미지 삭제
const removeProfileImage = async () => {
  try {
    // 프로필 이미지 URL을 null로 업데이트
    previewUrl.value = null;
    emit("update:profile", null);
  } catch (error) {
    console.error("프로필 이미지 삭제 실패: ", error);
    alert("이미지 삭제에 실패했습니다.");
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
      <!-- 기본 프로필 이미지 또는 업로드된 이미지 표시 -->
      <img
        :src="previewUrl || '/image/default-profile.png'"
        alt="프로필 이미지"
      />

      <!-- 수정 모드일 때 오버레이 표시 -->
      <div v-if="isEditing && isHovered" class="image-overlay">
        <i class="fas fa-camera"></i>
      </div>

      <!-- 삭제 버튼 (수정 모드이고 프로필 이미지가 있을 때만 표시) -->
      <button
        v-if="isEditing && previewUrl"
        class="remove-button"
        @click.stop="removeProfileImage"
      >
        <i class="fas fa-times"></i>
      </button>

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
