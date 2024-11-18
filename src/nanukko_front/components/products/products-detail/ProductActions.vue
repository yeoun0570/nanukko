<script setup>
const emit = defineEmits(['action-click']);
</script>

<template>
    <div class="product-actions">
        <button class="action-button like" @click="$emit('action-click')">
            <v-icon>mdi-heart</v-icon>
            <span>찜</span>
        </button>
        <button class="action-button chat" @click="$emit('action-click')">
            <v-icon>mdi-chat</v-icon>
            <span>톡하기</span>
        </button>
        <button class="action-button buy" @click="$emit('action-click')">
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

.action-button:hover {
    opacity: 0.9;
}
</style>