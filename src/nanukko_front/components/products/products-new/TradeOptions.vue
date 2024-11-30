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

const handleGenderChange = (value) => {
    // male/female 선택에 따라 true/false/null 전달
    const genderValue = value === 'male' ? true : (value === 'female' ? false : null);
    emit("update:options", "gender", genderValue);
};

const ageGroups = [
    { value: '10S', label: '10대' },
    { value: '20S', label: '20대' },
    { value: '30S', label: '30대' },
    { value: '40S', label: '40대' },
    { value: '50S', label: '50대' },
    { value: '60S', label: '60대 이상' }
];
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
                            <input type="radio" :checked="productInfo.isCompanion === true"
                                @change="handleInput('isCompanion', true)" name="companion" />
                            <span class="radio-text">예</span>
                        </label>
                        <label class="radio-label">
                            <input type="radio" :checked="productInfo.isCompanion === false"
                                @change="handleInput('isCompanion', false)" name="companion" />
                            <span class="radio-text">아니오</span>
                        </label>
                    </div>
                </div>

                <div class="option-group">
                    <label class="option-label">대리인 거래</label>
                    <div class="radio-group">
                        <label class="radio-label">
                            <input type="radio" :checked="productInfo.isDeputy === true"
                                @change="handleInput('isDeputy', true)" name="deputy" />
                            <span class="radio-text">예</span>
                        </label>
                        <label class="radio-label">
                            <input type="radio" :checked="productInfo.isDeputy === false"
                                @change="handleInput('isDeputy', false)" name="deputy" />
                            <span class="radio-text">아니오</span>
                        </label>
                    </div>
                </div>
            </div>

            <!-- 오른쪽 컬럼: 성별 선택 -->
            <div class="right-column" v-if="productInfo.isCompanion || productInfo.isDeputy">
                <div class="option-group">
                    <label class="option-label">동행인 또는 대리인의 성별</label>
                    <div class="radio-group">
                        <label class="radio-label">
                            <input type="radio" :checked="productInfo.gender === true"
                                @change="handleGenderChange('male')" name="gender" />
                            <span class="radio-text">남성</span>
                        </label>
                        <label class="radio-label">
                            <input type="radio" :checked="productInfo.gender === false"
                                @change="handleGenderChange('female')" name="gender" />
                            <span class="radio-text">여성</span>
                        </label>
                    </div>
                </div>

                <div class="option-group">
                    <label class="option-label">동행인 또는 대리인의 연령대</label>
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

@media (max-width: 768px) {
    .options-grid {
        grid-template-columns: 1fr;
        gap: 1rem;
    }
}
</style>