<script setup>
import { defineProps, defineEmits, computed } from 'vue';

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

const ageGroups = [
    { value: '10s', label: '10대' },
    { value: '20s', label: '20대' },
    { value: '30s', label: '30대' },
    { value: '40s', label: '40대' },
    { value: '50s', label: '50대' },
    { value: '60s', label: '60대 이상' }
];

const selectedAgeLabel = computed(() => {
    if (!props.productInfo.ageGroup) return '동행인 또는 대리인의 나이';
    const selectedAge = ageGroups.find(group => group.value === props.productInfo.ageGroup);
    return selectedAge ? `선택된 연령대: ${selectedAge.label}` : '동행인 또는 대리인의 나이';
});
</script>

<template>
    <div class="trade-options">
        <div class="options-grid">
            <!-- 왼쪽 컬럼: 동행인/대리인 선택 -->
            <div class="left-column">
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
                            <input type="radio" :checked="productInfo.deputy === true"
                                @change="handleInput('deputy', true)" name="deputy" />
                            <span class="radio-text">예</span>
                        </label>
                        <label class="radio-label">
                            <input type="radio" :checked="productInfo.deputy === false"
                                @change="handleInput('deputy', false)" name="deputy" />
                            <span class="radio-text">아니오</span>
                        </label>
                    </div>
                </div>
            </div>

            <!-- 오른쪽 컬럼: 성별/나이 선택 -->
            <div class="right-column" v-if="productInfo.companion || productInfo.deputy">
                <div class="option-group">
                    <label class="option-label">동행인 또는 대리인의 성별</label>
                    <div class="radio-group">
                        <label class="radio-label">
                            <input type="radio" :checked="productInfo.gender === 'male'"
                                @change="handleInput('gender', 'male')" name="gender" />
                            <span class="radio-text">남성</span>
                        </label>
                        <label class="radio-label">
                            <input type="radio" :checked="productInfo.gender === 'female'"
                                @change="handleInput('gender', 'female')" name="gender" />
                            <span class="radio-text">여성</span>
                        </label>
                    </div>
                </div>

                <div class="option-group">
                    <label class="option-label" :class="{ 'selected': productInfo.ageGroup }">
                        {{ selectedAgeLabel }}
                    </label>
                    <div class="select-wrapper">
                        <select class="age-select" :value="productInfo.ageGroup"
                            @change="handleInput('ageGroup', $event.target.value)">
                            <option value="">연령대 선택</option>
                            <option v-for="group in ageGroups" :key="group.value" :value="group.value">
                                {{ group.label }}
                            </option>
                        </select>
                    </div>
                </div>
            </div>
        </div>
    </div>
</template>

<style scoped>
.trade-options {
    width: 100%;
}

.options-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 2rem;
    align-items: start;
}

.left-column,
.right-column {
    display: flex;
    flex-direction: column;
    gap: 1rem;
}

.option-group {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
}

.option-label {
    font-weight: 500;
    color: #333;
    transition: color 0.3s ease;
}

.option-label.selected {
    color: #007bff;
}

.radio-group {
    display: flex;
    gap: 1rem;
}

.radio-label {
    display: flex;
    align-items: center;
    gap: 0.25rem;
    cursor: pointer;
}

.radio-text {
    font-size: 0.875rem;
}

.select-wrapper {
    width: 100%;
    position: relative;
}

.age-select {
    width: 100%;
    padding: 0.5rem;
    border: 1px solid #dee2e6;
    border-radius: 0.25rem;
    font-size: 0.875rem;
    background-color: white;
    cursor: pointer;
}

.age-select:focus {
    outline: none;
    border-color: #007bff;
}

.age-select:hover {
    border-color: #007bff;
}

/* 반응형 스타일 */
@media (max-width: 768px) {
    .options-grid {
        grid-template-columns: 1fr;
        gap: 1rem;
    }
}
</style>