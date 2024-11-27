<script setup>
import { ref } from 'vue';
import axios from 'axios';

const props = defineProps({
    productId: {
        type: String,
        required: true
    },
    initialIsWished: {
        type: Boolean,
        default: false
    }
});

const emit = defineEmits(['action-click']);

const isWished = ref(props.initialIsWished);
const isProcessing = ref(false);

const handleWishClick = async () => {
    try {
        isProcessing.value = true;

        const response = await axios.post(`http://localhost:8080/api/my-store/wishlist/${props.productId}`, null, {
            withCredentials: true  // 쿠키 기반 인증을 사용하는 경우
        });

        if (response.status === 200) {
            isWished.value = !isWished.value;
            // 성공 메시지 표시
            alert(isWished.value ? '찜 목록에 추가되었습니다.' : '찜 목록에서 제거되었습니다.');
        }
    } catch (error) {
        if (error.response?.status === 401) {
            alert('로그인이 필요한 서비스입니다.');
            // 로그인 페이지로 리다이렉션
            navigateTo('/login');
        } else {
            console.error('찜하기 처리 중 오류 발생:', error);
            alert('처리 중 오류가 발생했습니다.');
        }
    } finally {
        isProcessing.value = false;
    }
};

const handleChatClick = () => {
    emit('action-click', 'chat');
};

const handleBuyClick = () => {
    emit('action-click', 'buy');
};
</script>

<template>
    <div class="product-actions">
        <button class="action-button like" :class="{ active: isWished, disabled: isProcessing }"
            @click="handleWishClick" :disabled="isProcessing">
            <v-icon>{{ isWished ? 'mdi-heart' : 'mdi-heart-outline' }}</v-icon>
            <span>찜{{ isWished ? '됨' : '' }}</span>
        </button>
        <button class="action-button chat" @click="handleChatClick">
            <v-icon>mdi-chat</v-icon>
            <span>톡하기</span>
        </button>
        <button class="action-button buy" @click="handleBuyClick">
            <v-icon>mdi-gift-open</v-icon>
            <span>구매하기</span>
        </button>
    </div>
</template>

<style scoped>
.product-actions {
    display: flex;
    justify-content: center;
    gap: 1rem;
    padding: 1.25rem 0;
    width: 100%;
}

.action-button {
    display: flex;
    align-items: center;
    gap: 0.3125rem;
    padding: 0.75rem 1.5rem;
    border: none;
    border-radius: 0.3125rem;
    color: white;
    font-size: 1rem;
    cursor: pointer;
    transition: all 0.2s ease;
}

.action-button.disabled {
    opacity: 0.7;
    cursor: not-allowed;
}

.like {
    background-color: #007bff;
}

.like.active {
    background-color: #dc3545;
}

.chat {
    background-color: #ffc107;
}

.buy {
    background-color: #dc3545;
}

.action-button:not(.disabled):hover {
    opacity: 0.9;
    transform: translateY(-1px);
}

.action-button:not(.disabled):active {
    transform: translateY(0);
}
</style>