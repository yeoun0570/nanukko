<script setup>
import { defineProps, defineEmits } from 'vue';

const props = defineProps({
    productInfo: {
        type: Object,
        required: true,
    },
});

const emit = defineEmits(["update:options"]);

const handleInput = (field, value) => {
    emit("update:options", field, value);
};
</script>

<template>
    <div class="trade-options">
        <div class="option-group">
            <label class="option-label">동행인 동반</label>
            <div class="radio-group">
                <label class="radio-label">
                    <input type="radio" :checked="productInfo.companion === true"
                        @change="handleInput('companion', true)" name="companion" />
                    <span class="radio-text">예</span>
                </label>
                <label class="radio-label">
                    <input type="radio" :checked="productInfo.companion === false"
                        @change="handleInput('companion', false)" name="companion" />
                    <span class="radio-text">아니오</span>
                </label>
            </div>
        </div>

        <div class="option-group">
            <label class="option-label">대리인 거래</label>
            <div class="radio-group">
                <label class="radio-label">
                    <input type="radio" :checked="productInfo.deputy === true" @change="handleInput('deputy', true)"
                        name="deputy" />
                    <span class="radio-text">예</span>
                </label>
                <label class="radio-label">
                    <input type="radio" :checked="productInfo.deputy === false" @change="handleInput('deputy', false)"
                        name="deputy" />
                    <span class="radio-text">아니오</span>
                </label>
            </div>
        </div>

        <div class="option-group" v-if="productInfo.companion || productInfo.deputy">
            <label class="option-label">성별</label>
            <div class="radio-group">
                <label class="radio-label">
                    <input type="radio" :checked="productInfo.gender === 'male'" @change="handleInput('gender', 'male')"
                        name="gender" />
                    <span class="radio-text">남성</span>
                </label>
                <label class="radio-label">
                    <input type="radio" :checked="productInfo.gender === 'female'"
                        @change="handleInput('gender', 'female')" name="gender" />
                    <span class="radio-text">여성</span>
                </label>
            </div>
        </div>
    </div>
</template>

<style scoped>
.trade-options {
    background-color: #f8f9fa;
    border-radius: 0.5rem;
    padding: 1.5rem;
    margin-top: 1rem;
}

.option-group {
    margin-bottom: 1.25rem;
}

.option-group:last-child {
    margin-bottom: 0;
}

.option-label {
    display: block;
    font-weight: 600;
    color: #495057;
    margin-bottom: 0.5rem;
}

.radio-group {
    display: flex;
    gap: 2rem;
}

.radio-label {
    display: flex;
    align-items: center;
    cursor: pointer;
    user-select: none;
}

.radio-label input[type="radio"] {
    appearance: none;
    width: 1.25rem;
    height: 1.25rem;
    border: 2px solid #007bff;
    border-radius: 50%;
    margin-right: 0.5rem;
    position: relative;
    cursor: pointer;
}

.radio-label input[type="radio"]:checked {
    background-color: #007bff;
    border-color: #007bff;
}

.radio-label input[type="radio"]:checked::after {
    content: "";
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    width: 0.5rem;
    height: 0.5rem;
    background-color: white;
    border-radius: 50%;
}

.radio-text {
    font-size: 0.95rem;
    color: #495057;
}

.notice {
    margin-top: 1rem;
    padding: 1rem;
    background-color: #fff3cd;
    border-radius: 0.375rem;
    border: 1px solid #ffeeba;
}

.notice p {
    margin: 0;
    color: #856404;
    display: flex;
    align-items: center;
    font-size: 0.9rem;
}

.notice p+p {
    margin-top: 0.5rem;
}

.notice-icon {
    margin-right: 0.5rem;
    font-size: 1.25rem;
}

/* 호버 효과 */
.radio-label:hover input[type="radio"]:not(:checked) {
    background-color: rgba(0, 123, 255, 0.1);
}

/* 포커스 효과 */
.radio-label input[type="radio"]:focus {
    outline: 2px solid rgba(0, 123, 255, 0.25);
    outline-offset: 2px;
}
</style>