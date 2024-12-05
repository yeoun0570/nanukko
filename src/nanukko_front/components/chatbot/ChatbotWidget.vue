<template>
  <div class="chatbot-widget">
    <!-- 채팅창 -->
    <div :class="['chatbot-window', { open: isOpen }]">
      <ClovaChatbot v-if="isOpen" />
    </div>

    <!-- 채팅 아이콘 -->
    <div class="chatbot-toggle" @click="toggleChat">
      <img
        src="/image/나누고_Logo_block.png"
        alt="챗봇 열기/닫기"
        :class="['toggle-icon', { active: isOpen }]"
      />
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import ClovaChatbot from "./ClovaChatbot.vue";

const isOpen = ref(false);
const toggleChat = () => {
  isOpen.value = !isOpen.value;
};
</script>

<style scoped>
.chatbot-widget {
  position: fixed;
  bottom: 20px;
  right: 20px;
  z-index: 1000;
}

.chatbot-window {
  position: fixed;
  bottom: 100px; /* 아이콘 위에 위치 */
  right: 20px;
  visibility: hidden;
  opacity: 0;
  transform: translateY(20px);
  transition: all 0.3s ease;
}

.chatbot-window.open {
  visibility: visible;
  opacity: 1;
  transform: translateY(0);
}

.chatbot-toggle {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  background: white;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: transform 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.toggle-icon {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.toggle-icon.active {
  transform: rotate(180deg);
}

.chatbot-toggle:hover {
  transform: scale(1.1);
}
</style>
