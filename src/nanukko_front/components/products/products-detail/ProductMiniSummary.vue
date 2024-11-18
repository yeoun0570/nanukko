<script setup>
defineProps({
    categoryId: String,
    offlineType: String,
    gender: String,
    condition: String,
    shippingFee: Number,
    isOffline: Boolean,
    location: String
});

const formattedShippingFee = computed(() => {
    return props.shippingFee.toLocaleString();
});
</script>

<template>
    <div class="mini-summary-wrapper">
        <div class="summary-row">
            <div class="label">카테고리</div>
            <div class="value">{{ categoryId }}</div>
            <div class="label">{{ offlineType }}</div>
            <div class="value">{{ gender }}</div>
        </div>
        <div class="summary-row">
            <div class="label">상품상태</div>
            <div class="value">{{ condition }}</div>
            <div class="label">배송비</div>
            <div class="value">{{ formattedShippingFee }}원</div>
        </div>
        <div class="summary-row" v-if="isOffline">
            <div class="label">직거래 장소</div>
            <div class="value">{{ location }}</div>
        </div>
    </div>
</template>

<style scoped>
.mini-summary-wrapper {
    width: 85%;
    padding: 1.25rem 0;
    margin: 0 auto;
    font-size: 0.875rem;
}

.summary-row {
    display: grid;
    grid-template-columns: 15% 35% 15% 35%;
    height: 1.5625rem;
    align-items: center;
}

.label {
    color: #666;
}

.value {
    color: #333;
}
</style>
