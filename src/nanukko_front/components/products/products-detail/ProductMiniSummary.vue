<script setup>
defineProps({
    condition: {
        type: String,
        required: true
    },
    isPerson: {
        type: Boolean,
        required: true
    },
    isShipping: {
        type: Boolean,
        required: true
    },
    shippingFee: {
        type: Number,
        default: 0
    },
    freeShipping: {
        type: Boolean,
        required: true
    },
    isCompanion: {
        type: Boolean,
        default: false
    },
    isDeputy: {
        type: Boolean,
        default: false
    },
    gender: {
        type: Boolean,
        default: true  // true = 남성, false = 여성
    },
    ageGroup: {
        type: String,
        default: ''
    },
    address: {
        type: String,
        default: ''
    }
});

const conditionLabels = {
    NEW: '미사용',
    LIKE_NEW: '사용감 없음',
    USED: '사용감 적음',
    HEAVILY_USED: '사용감 많음'
};

const ageGroupLabels = {
    '10S': '10대',
    '20S': '20대',
    '30S': '30대',
    '40S': '40대',
    '50S': '50대',
    '60S': '60대 이상'
};

const getConditionLabel = (condition) => conditionLabels[condition] || condition;
const getAgeGroupLabel = (ageGroup) => ageGroupLabels[ageGroup] || ageGroup;
const getTradeTypeLabel = (isPerson, isShipping) => {
    const types = [];
    if (isShipping) types.push('택배거래');
    if (isPerson) types.push('직거래');
    return types.join(', ');
};
const getGenderLabel = (gender) => gender ? '남성' : '여성';
const formatPrice = (price) => {
    return price.toLocaleString() + '원';
};
const getCompanionDeputyLabel = (isCompanion, isDeputy) => {
    if (isCompanion && isDeputy) return '동행인/대리인';
    if (isCompanion) return '동행인';
    return '대리인';
};
</script>

<template>
    <div class="mini-summary-wrapper">
        <!-- 거래 방식 및 상품 상태 -->
        <div class="summary-row">
            <div class="label">거래 방식</div>
            <div class="value">{{ getTradeTypeLabel(isPerson, isShipping) }}</div>
            <div class="label">상품 상태</div>
            <div class="value">{{ getConditionLabel(condition) }}</div>
        </div>

        <!-- 배송 관련 정보 -->
        <div v-if="isShipping" class="summary-row">
            <div class="label">배송비</div>
            <div class="value">
                {{ freeShipping ? '배송비 포함' : formatPrice(shippingFee) }}
            </div>
        </div>

        <!-- 직거래 장소 -->
        <div v-if="isPerson" class="summary-row">
            <div class="label">거래 장소</div>
            <div class="value">{{ address }}</div>
        </div>

        <!-- 동행인/대리인 정보 -->
        <template v-if="isCompanion || isDeputy">
            <div class="summary-row">
                <div class="label">{{ getCompanionDeputyLabel(isCompanion, isDeputy) }}</div>
                <div class="value">{{ getGenderLabel(gender) }}</div>
                <div v-if="ageGroup" class="label">연령대</div>
                <div v-if="ageGroup" class="value">{{ getAgeGroupLabel(ageGroup) }}</div>
            </div>
        </template>
    </div>
</template>

<style scoped>
.mini-summary-wrapper {
    width: 100%;
    max-width: 100%;
    box-sizing: border-box;
    padding: 38px 20px;
    border-top: 1px solid #eee;
    border-bottom: 1px solid #eee;
    font-size: 1.05rem;
    min-width: 480px;
    margin-top: 10px;
}

.summary-row {
    display: grid;
    grid-template-columns: 15% 35% 15% 35%;
    min-height: 2.35rem;
    align-items: center;
    position: relative;
}

.label {
    color: #666;
    font-weight: 500;
}

.value {
    color: #333;
}

/* @media (max-width: 768px) {
    .summary-row {
        grid-template-columns: 25% 75%;
    }

    .summary-row>div:nth-child(3),
    .summary-row>div:nth-child(4) {
        margin-top: 0.5rem;
    }
} */
</style>