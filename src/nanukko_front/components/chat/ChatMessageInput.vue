<template>
  <div class="chat-input-container">
    <div class="input-wrapper">
      <!-- 메뉴 토글 버튼 -->
      <button 
        class="menu-button"
        @click="isMenuOpen = !isMenuOpen"
      >
        <i class="fas" :class="isMenuOpen ? 'fa-times' : 'fa-plus'"></i>
      </button>

      <!-- 추가 기능 메뉴 -->
      <div v-if="isMenuOpen" class="additional-menu">
        <!-- 이미지 업로드 -->
        <div class="image-upload-container">
          <input
            ref="fileInput"
            type="file"
            accept="image/jpeg,image/png,image/webp"
            class="hidden"
            @change="handleFileSelect"
          />
          <button 
            class="menu-item"
            @click="triggerFileInput"
          >
            <i class="fas fa-image"></i>
          </button>
          <!-- 이미지 미리보기 -->
          <div v-if="previewUrl" class="image-preview">
            <img :src="previewUrl" alt="선택된 이미지" />
            <button class="remove-preview" @click="removeSelectedImage">
              <i class="fas fa-times"></i>
            </button>
          </div>
        </div>

        <!-- 위치 공유 -->
        <button 
          class="menu-item"
          @click="handleLocationShare"
        >
          <i class="fas fa-map-marker-alt"></i>
        </button>
      </div>

      <!-- 메시지 입력창 -->
      <input
        v-model="message"
        type="text"
        class="message-input"
        placeholder="메시지를 입력하세요..."
        :disabled="!connected || isLoading"
        @keyup.enter="handleSendMessage"
      />

      <!-- 전송 버튼 -->
      <button
        class="send-button"
        :disabled="!canSend"
        @click="handleSendMessage"
      >
        전송
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue';
import { useURL } from '~/composables/useURL';
import { useAuth } from '~/composables/auth/useAuth';

const { axiosInstance } = useURL();
const { getToken } = useAuth();

const props = defineProps({
  connected: Boolean,
  isLoading: Boolean,
  roomId: {
    type: [String, Number],
    required: true
  }
});

const emit = defineEmits(['send-message']);

const message = ref('');
const isMenuOpen = ref(false);
const fileInput = ref(null);
const previewUrl = ref(null);
const selectedFile = ref(null);
const isUploading = ref(false);

// 전송 가능 상태 확인
const canSend = computed(() => {
  return props.connected && !props.isLoading && !isUploading.value && 
        (message.value.trim() || selectedFile.value);
});

// 파일 입력 트리거
const triggerFileInput = () => {
  fileInput.value?.click();
};

// 파일 선택 처리
const handleFileSelect = async (event) => {
  const file = event.target.files?.[0];
  if (!file) return;

  // 파일 유효성 검사
  const allowedTypes = ['image/jpeg', 'image/png', 'image/webp'];
  if (!allowedTypes.includes(file.type)) {
    alert('지원하지 않는 파일 형식입니다. JPG, PNG, WebP 형식만 업로드 가능합니다.');
    event.target.value = '';
    return;
  }

  // 파일 크기 제한 (10MB)
  if (file.size > 10 * 1024 * 1024) {
    alert('파일 크기는 10MB를 초과할 수 없습니다.');
    event.target.value = '';
    return;
  }

  // 미리보기 생성
  selectedFile.value = file;
  previewUrl.value = URL.createObjectURL(file);
};

// 선택된 이미지 제거
const removeSelectedImage = () => {
  if (previewUrl.value) {
    URL.revokeObjectURL(previewUrl.value);
  }
  previewUrl.value = null;
  selectedFile.value = null;
  if (fileInput.value) {
    fileInput.value.value = '';
  }
};

// 이미지 메시지 전송
const handleImageUpload = async () => {
  if (!selectedFile.value) return null;

  try {
    isUploading.value = true;
    const formData = new FormData();
    formData.append('files', selectedFile.value);

    const response = await axiosInstance.post('/files/chat/multiple', formData, {
      headers: {
        'Authorization': `Bearer ${getToken()}`,
        'Content-Type': 'multipart/form-data'
      }
    });

    return response.data[0].uploadFileUrl;
  } catch (error) {
    console.error('이미지 업로드 실패:', error);
    throw error;
  } finally {
    isUploading.value = false;
  }
};

// 메시지 전송 처리
const handleSendMessage = async () => {
  if (!canSend.value) return;

  try {
    let messageData = {
      messageType: 'CHAT',  // 기본 타입은 CHAT
      chatMessage: message.value.trim(),
      image: null,
      createdAt: new Date().toISOString()
    };

    // 이미지가 있는 경우
    if (selectedFile.value) {
      try {
        const imageUrl = await handleImageUpload();
        if (imageUrl) {
          messageData = {
            ...messageData,
            messageType: 'IMAGE', 
            image: imageUrl,
            chatMessage: message.value.trim() || ''
          };
        }
      } catch (error) {
        console.error('이미지 업로드 실패:', error);
        alert('이미지 업로드에 실패했습니다.');
        return;
      }
    }

    emit('send-message', messageData);
    message.value = '';
    removeSelectedImage();
    isMenuOpen.value = false;

  } catch (error) {
    console.error('메시지 전송 실패:', error);
    alert('메시지 전송에 실패했습니다. 다시 시도해주세요.');
  }
};

// 위치 공유 처리
const handleLocationShare = () => {
  if (!navigator.geolocation) {
    alert('위치 정보를 사용할 수 없습니다.');
    return;
  }

  navigator.geolocation.getCurrentPosition(
    (position) => {
      const messageData = {
        type: 'LOCATION',
        latitude: position.coords.latitude,
        longitude: position.coords.longitude,
        chatMessage: '위치를 공유했습니다.',
        createdAt: new Date().toISOString()
      };
      
      emit('send-message', messageData);
      isMenuOpen.value = false;
    },
    () => {
      alert('위치 정보를 가져오는데 실패했습니다.');
    }
  );
};
</script>

<style scoped>
@import '~/assets/chat/chat-chatMessageInput.css';
</style>