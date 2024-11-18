<script setup>
defineProps({
    name: String,
    price: Number,
    viewCount: Number,
    timestamp: String
});

const formattedPrice = computed(() => {
    return props.price.toLocaleString();
});
</script>

<template>
    <div class="product-info">
        <p class="product-name">{{ name }}</p>
        <p class="product-price">
            {{ formattedPrice }}<span class="price-unit">원</span>
        </p>
        <hr>
        <div class="product-stats">
            <div class="stat-item">
                <v-icon>mdi-heart</v-icon>
                <span>3</span>
            </div>
            <div class="stat-item">
                <v-icon>mdi-eye-outline</v-icon>
                <span>{{ viewCount }}</span>
            </div>
            <div class="stat-item">
                <v-icon>mdi-clock-outline</v-icon>
                <span>{{ timestamp }}</span>
            </div>
        </div>
    </div>
</template>

<style scoped>
.product-info {
    margin-bottom: 1.5rem;
}

.product-name {
    font-size: 1.125rem;
    margin-bottom: 0.5rem;
}

.product-price {
    font-size: 1.5rem;
    font-weight: bold;
}

.price-unit {
    font-size: 1rem;
    margin-left: 0.3125rem;
}

.product-stats {
    display: flex;
    gap: 1rem;
    color: #ccc;
    font-size: 0.875rem;
}

.stat-item {
    display: flex;
    align-items: center;
    gap: 0.3125rem;
}
</style>