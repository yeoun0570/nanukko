<script setup>
import { ref } from 'vue';
import { useApi } from "@/composables/useApi";
import { useRouter } from 'vue-router';
import { useToast } from 'vue-toastification';

const props = defineProps({
    productId: {
        type: String,
        required: true
    },
    modelValue: {
        type: Boolean,
        required: true
    }
});

const emit = defineEmits(['update:isWished', 'wish-updated']);
const api = useApi();
const router = useRouter();
const toast = useToast();

const localIsWished = ref(props.isWished);
const isWished = ref(props.initialIsWished);
const isProcessing = ref(false);

watch(() => props.modelValue, (newValue) => {
    localIsWished.value = newValue;
});

const handleWishClick = async () => {
    try {
        const response = await api.post(`/wishlist/${props.productId}`);

        if (response.data) {
            localIsWished.value = response.data.isWished;
            emit('update:modelValue', response.data.isWished);
            emit('wish-updated', {
                isWished: response.data.isWished,
                productId: props.productId
            });
            toast.success(response.data.message);
        }
    } catch (error) {
        if (error.response?.status === 401) {
            toast.warning('로그인이 필요한 서비스입니다.');
            router.push('/auth/login');
        } else {
            console.error('찜하기 처리 중 오류 발생:', error);
            toast.error('처리 중 오류가 발생했습니다.');
        }
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
        <button class="action-button like" @click="handleWishClick">
            <v-icon>{{ localIsWished ? 'mdi-heart' : 'mdi-heart-outline' }}</v-icon>
            <span>찜</span>
        </button>
        <button class="action-button chat" @click="handleChatClick">
            <v-icon>mdi-chat</v-icon>
            <span>채팅하기</span>
        </button>
        <button class="action-button buy" @click="handleBuyClick">
            <v-icon>mdi-gift-open</v-icon>
            <span>즉시결제</span>
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