<!-- components/chat/ChatRoomHeader.vue -->
<!-- components/chat/ChatRoomHeader.vue -->
<template>
  <header class="chat-room-header">
    <!-- 상품 정보 영역 -->
    <div class="product-info">
      <!-- 상품 이미지 -->
      <div class="product-image">
        <img 
          v-if="product?.thumbnailImage" 
          :src="product.thumbnailImage" 
          :alt="product.productName"
          class="product-thumbnail"
        />
        <div v-else class="image-placeholder">
          <i class="fas fa-image"></i>
        </div>
      </div>

      <!-- 상품 정보 -->
      <div class="product-details">
        <h3 class="product-name">{{ product?.productName }}</h3>
        <div class="price-condition">
          <span class="product-price">{{ formatPrice(product?.price) }}원</span>
          <span class="product-condition" :class="conditionClass">
            {{ formatCondition(product?.condition) }}
          </span>
        </div>
        <div class="product-status" :class="statusClass">
          {{ formatStatus(product?.status) }}
        </div>
      </div>
    </div>

     <!-- 우측 메뉴 영역 -->
     <div class="header-actions">
      <div class="connection-info">
        <span v-if="!connected" class="connection-status">
          <i class="fas fa-sync fa-spin"></i> 연결 중...
        </span>
      </div>

      <!-- 햄버거 메뉴 -->
      <button class="menu-button" @click="isMenuOpen = !isMenuOpen">
        <i class="fas fa-ellipsis-v"></i>
      </button>

      <!-- 메뉴 드롭다운 -->
      <div v-if="isMenuOpen" class="menu-dropdown">
        <!-- 채팅창 닫기 -->
        <button class="menu-item" @click="handleCloseChat">
          <i class="fas fa-times"></i>
          채팅창 닫기
        </button>
        <!-- 채팅방 나가기 -->
        <button class="menu-item leave-button" @click="handleLeaveRoom">
          <i class="fas fa-sign-out-alt"></i>
          채팅방 나가기
        </button>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref } from 'vue';
import { computed } from 'vue';

// Props 정의 및 검증
const props = defineProps({
  product: {
    type: Object,
    required: true,
    validator(value) {
      const requiredFields = ['productName', 'price', 'status', 'condition', 'thumbnailImage']
      return requiredFields.every(field => field in value)
    }
  },
  connected: Boolean,
  roomId: {
    type: [String, Number],
    required: true
  },
  userId: {
    type: String,
    required: true
  }
});

// Emits 정의
const emit = defineEmits(['close-chat', 'leave-room']);

// 상태 관리
const isMenuOpen = ref(false);

// 채팅창 닫기 핸들러 (STOMP 연결 유지, UI만 닫기)
const handleCloseChat = () => {
  if (confirm('채팅창을 닫으시겠습니까?')) {
    isMenuOpen.value = false;
    emit('close-chat');
  }
};

// 채팅방 나가기 핸들러 (실제 나가기 처리)
const handleLeaveRoom = () => {
  if (confirm('채팅방을 나가시겠습니까? 나가기 후에는 채팅 내역이 삭제됩니다.')) {
    isMenuOpen.value = false;
    emit('leave-room', {
      chatRoomId: props.roomId,
      userId: props.userId
    });
  }
};

// 가격 포맷팅
const formatPrice = (price) => {
  if (!price && price !== 0) return '가격 정보 없음'
  return new Intl.NumberFormat('ko-KR').format(price)
}

// 상품 상태 포맷팅
const formatStatus = (status) => {
  if (!status) return ''
  const statusMap = {
    'SELLING': '판매중',
    'RESERVED': '예약중',
    'SOLD_OUT': '판매완료',
    'REMOVED': '삭제됨'
  }
  return statusMap[status] || status
}

// 상품 컨디션 포맷팅
const formatCondition = (condition) => {
  if (!condition) return ''
  const conditionMap = {
    'NEW': '새상품',
    'LIKE_NEW': '거의 새것',
    'USED': '중고',
    'HEAVILY_USED': '많이 사용됨'
  }
  return conditionMap[condition] || condition
}

// 상태에 따른 CSS 클래스
const statusClass = computed(() => {
  const status = props.product?.status?.toLowerCase()
  switch (status) {
    case 'selling': return 'status-selling'
    case 'reserved': return 'status-reserved'
    case 'sold_out': return 'status-sold'
    case 'removed': return 'status-removed'
    default: return ''
  }
})

// 컨디션에 따른 CSS 클래스
const conditionClass = computed(() => {
  const condition = props.product?.condition?.toLowerCase()
  switch (condition) {
    case 'new': return 'condition-new'
    case 'like_new': return 'condition-like-new'
    case 'used': return 'condition-used'
    case 'heavily_used': return 'condition-heavily-used'
    default: return ''
  }
})
</script>

<style scoped>
.chat-room-header {
  padding: 1rem;
  border-bottom: 1px solid #e0e0e0;
  background: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  position: relative;
}

.product-info {
  display: flex;
  align-items: center;
  gap: 1rem;
  flex: 1;
}

.product-image {
  width: 48px;
  height: 48px;
  flex-shrink: 0;
  border-radius: 4px;
  overflow: hidden;
  background-color: #f5f5f5;
}

.product-thumbnail {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.product-details {
  flex: 1;
  min-width: 0;
}

.product-name {
  font-size: 1rem;
  font-weight: 500;
  margin-bottom: 0.5rem;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.price-condition {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-bottom: 0.5rem;
}

.product-price {
  font-weight: 600;
  color: #1a1a1a;
}

.product-condition {
  font-size: 0.75rem;
  padding: 0.125rem 0.375rem;
  border-radius: 4px;
}

.condition-new {
  background-color: #e6ffe6;
  color: #00b300;
}

.condition-like-new {
  background-color: #e6f7ff;
  color: #0077cc;
}

.condition-used {
  background-color: #fff7e6;
  color: #b37700;
}

.condition-heavily-used {
  background-color: #fff1f0;
  color: #cf1322;
}

.product-status {
  display: inline-block;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  font-size: 0.875rem;
}

.status-selling {
  background-color: #e6f4ff;
  color: #0077ff;
}

.status-reserved {
  background-color: #fff7e6;
  color: #ff9500;
}

.status-sold {
  background-color: #f5f5f5;
  color: #666;
}

.status-removed {
  background-color: #ffd6d6;
  color: #ff4d4f;
}

.connection-status {
  font-size: 0.875rem;
  color: #666;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.connection-status i {
  font-size: 0.75rem;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
  color: #999;
}

/* 헤더 우측 영역 스타일 */
.header-actions {
  display: flex;
  align-items: center;
  gap: 1rem;
  position: relative;
}

/* 메뉴 버튼 스타일 */
.menu-button {
  padding: 0.5rem;
  border: none;
  background: none;
  color: #666;
  cursor: pointer;
  transition: color 0.2s;
}

.menu-button:hover {
  color: #333;
}

/* 드롭다운 메뉴 스타일 */
.menu-dropdown {
  position: absolute;
  top: 100%;
  right: 0;
  margin-top: 0.5rem;
  background: white;
  border: 1px solid #e0e0e0;
  border-radius: 0.5rem;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
  z-index: 100;
}

.menu-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  width: 100%;
  border: none;
  background: none;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.menu-item:hover {
  background: #f5f5f5;
  color: #ff4d4f;
}

.menu-item i {
  font-size: 0.875rem;
}

.leave-button {
  color: #ff4d4f;
}

.leave-button:hover {
  background: #fff1f0;
}
</style>