<script setup>
import { ref } from 'vue';

const props = defineProps({
    images: {
        type: Array,
        required: true
    }
});

const activeIndex = ref(0);

const nextSlide = () => {
    activeIndex.value = (activeIndex.value + 1) % props.images.length;
};

const prevSlide = () => {
    activeIndex.value = (activeIndex.value - 1 + props.images.length) % props.images.length;
};

const goToSlide = (index) => {
    activeIndex.value = index;
};
</script>

<template>
    <div class="image-slider">
        <div v-for="(img, i) in images" :key="i" :class="['image-slide', { active: i === activeIndex }]">
            <img :src="img" alt="상품 이미지" />
        </div>

        <div class="slider-navigation">
            <span v-for="(_, index) in images" :key="index" :class="['nav-dot', { active: index === activeIndex }]"
                @click="goToSlide(index)"></span>
        </div>

        <button class="slider-button prev" @click="prevSlide">
            <v-icon>mdi-chevron-left</v-icon>
        </button>
        <button class="slider-button next" @click="nextSlide">
            <v-icon>mdi-chevron-right</v-icon>
        </button>
    </div>
</template>

<style scoped>
.image-slider {
    position: relative;
    width: 500px;
    height: 500px;
    overflow: hidden;
}

.image-slide {
    display: none;
    width: 500px;
    height: 500px;
}

.image-slide.active {
    display: block;
}

.image-slide img {
    width: 500px;
    height: 500px;
    object-fit: contain;
}

.slider-navigation {
    position: absolute;
    bottom: 1rem;
    left: 50%;
    transform: translateX(-50%);
    display: flex;
    gap: 0.5rem;
}

.nav-dot {
    width: 0.625rem;
    height: 0.625rem;
    background-color: rgba(0, 0, 0, 0.5);
    border-radius: 50%;
    cursor: pointer;
}

.nav-dot.active {
    background-color: white;
}

.slider-button {
    position: absolute;
    top: 50%;
    transform: translateY(-50%);
    border: none;
    padding: 0.625rem;
    cursor: pointer;
    color: rgba(0, 0, 0, 0.5);
    font-size: 1.5em;
}

.slider-button.prev {
    left: 0.625rem;
}

.slider-button.next {
    right: 0.625rem;
}
</style>