<script setup>
import { ref, computed } from 'vue';
import TradeOptions from '~/components/products/products-new/TradeOptions.vue';
import AddressSearch from '~/components/common/AddressSearch.vue';
import Map from '~/components/products/products-detail/Map.vue';
import CategorySelect from '~/components/my-store/CategorySelect.vue';

const props = defineProps({
    modelValue: {
        type: Object,
        required: true
    }
});

const emit = defineEmits(['update:modelValue']);

// 가격과 배송비 입력 처리를 위한 유틸리티 함수들
const formatNumber = (value) => {
    if (!value) return '';
    return value.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',');
};

const extractNumber = (value) => {
    return value.replace(/[^\d]/g, '');
};

const handlePriceInput = (event, field) => {
    let value = extractNumber(event.target.value);
    value = value.replace(/^0+/, ''); // 앞자리 0 제거

    // 최대값 체크 (999,999,999)
    if (Number(value) > 999999999) {
        value = '999999999';
    }

    // 빈 값이거나 0인 경우 0으로 설정
    if (!value) {
        value = '0';
    }

    event.target.value = formatNumber(value);
    updateField(field, Number(value));
};

const handlePriceBlur = (event, field) => {
    let value = extractNumber(event.target.value);
    if (!value) {
        value = '0';
    }
    event.target.value = formatNumber(value);
    updateField(field, Number(value));
};

const updateField = (field, value) => {
    // 가격과 배송비의 경우 음수와 소수점 처리
    if (field === 'price' || field === 'shippingFee') {
        value = Math.max(0, Math.floor(Number(value)));
    }

    emit('update:modelValue', {
        ...props.modelValue,
        [field]: value
    });
};

// 거래 옵션 computed 속성
const tradeOptions = computed(() => ({
    isCompanion: props.modelValue.isCompanion || false,
    isDeputy: props.modelValue.isDeputy || false,
    gender: props.modelValue.gender ?? true,  // true = 남성, false = 여성
    ageGroup: props.modelValue.ageGroup || ''
}));

// 카테고리 업데이트 핸들러
const handleCategoryUpdate = (field, value) => {
    if (field === "middleCategory") {
        // value는 {majorId, middleId} 형태의 객체
        updateField('middleCategory', value);
    }
};

const handleTradeTypeChange = (type) => {
    const updatedProduct = { ...props.modelValue };

    if (type === 'shipping') {
        updatedProduct.isShipping = !updatedProduct.isShipping;
        // 배송 거래 해제시 관련 필드 초기화
        if (!updatedProduct.isShipping) {
            updatedProduct.freeShipping = false;
            updatedProduct.shippingFee = 0;
        }
    } else if (type === 'person') {
        updatedProduct.isPerson = !updatedProduct.isPerson;
        // 직거래 해제시 관련 필드 초기화
        if (!updatedProduct.isPerson) {
            updatedProduct.zipCode = '';
            updatedProduct.address = '';
            updatedProduct.detailAddress = '';
            updatedProduct.latitude = null;
            updatedProduct.longitude = null;
            updatedProduct.isCompanion = false;
            updatedProduct.isDeputy = false;
            updatedProduct.gender = true;
            updatedProduct.ageGroup = '';
        }
    }

    // 최소 하나의 거래 방식이 선택되어 있는지 확인
    if (!updatedProduct.isShipping && !updatedProduct.isPerson) {
        // 모든 관련 필드 초기화
        updatedProduct.freeShipping = false;
        updatedProduct.shippingFee = 0;
        updatedProduct.zipCode = '';
        updatedProduct.address = '';
        updatedProduct.detailAddress = '';
        updatedProduct.latitude = null;
        updatedProduct.longitude = null;
        updatedProduct.isCompanion = false;
        updatedProduct.isDeputy = false;
        updatedProduct.gender = true;
        updatedProduct.ageGroup = '';
    }

    emit('update:modelValue', updatedProduct);
};

// 배송비 포함 여부 변경 핸들러
const handleIncludeShippingChange = (value) => {
    const updatedProduct = { ...props.modelValue };
    updatedProduct.freeShipping = value;
    updatedProduct.shippingFee = 0;  // 배송비 포함 시 무조건 0으로 설정
    emit('update:modelValue', updatedProduct);
};

// 주소 업데이트 핸들러
const handleAddressUpdate = (addressInfo) => {
    const updatedProduct = {
        ...props.modelValue,
        zipCode: addressInfo.zipCode,
        address: addressInfo.address,
        detailAddress: addressInfo.detailAddress,
        latitude: addressInfo.latitude,
        longitude: addressInfo.longitude
    };
    emit('update:modelValue', updatedProduct);
};

// 거래 옵션 업데이트 핸들러
const handleTradeOptionUpdate = (field, value) => {
    updateField(field, value);
};

// 상품 상태 옵션
const productConditions = [
    { value: 'NEW', label: '미사용', description: '사용하지 않은 새 상품이예요' },
    { value: 'LIKE_NEW', label: '사용감 없음', description: '사용은 했지만 눈에 띄는 부분은 없어요' },
    { value: 'USED', label: '사용감 적음', description: '눈에 띄는 부분이 약간 있어요' },
    { value: 'HEAVILY_USED', label: '사용감 많음', description: '눈에 띄는 부분이 많아요' }
];
</script>

<template>
    <div class="product-form">
        <div class="form-group">
            <label for="productName">상품명</label>
            <input type="text" id="productName" :value="modelValue.productName"
                @input="updateField('productName', $event.target.value)" required>
        </div>

        <!-- 카테고리 선택 부분 -->
        <CategorySelect :productInfo="modelValue" @update:category="handleCategoryUpdate" />

        <!-- 가격 입력 부분 -->
        <div class="form-group">
            <label for="price">가격</label>
            <div class="price-input-wrapper">
                <div class="input-container">
                    <input type="text" inputmode="numeric" id="price" :value="formatNumber(modelValue.price)"
                        @input="handlePriceInput($event, 'price')" @blur="handlePriceBlur($event, 'price')"
                        maxlength="11" required>
                    <span class="currency-label">원</span>
                </div>
            </div>
        </div>

        <div class="form-group">
            <label>거래 방식 (1개 이상 선택)</label>
            <div class="delivery-type">
                <button type="button" :class="['delivery-button', { active: modelValue.isShipping }]"
                    @click="handleTradeTypeChange('shipping')">
                    택배
                </button>
                <button type="button" :class="['delivery-button', { active: modelValue.isPerson }]"
                    @click="handleTradeTypeChange('person')">
                    직거래
                </button>
            </div>
        </div>

        <!-- 택배 거래 관련 옵션 -->
        <template v-if="modelValue.isShipping">
            <!-- 배송비 포함 여부 -->
            <div class="form-group shipping-include">
                <label class="checkbox-label">
                    <input type="checkbox" :checked="modelValue.freeShipping"
                        @change="handleIncludeShippingChange($event.target.checked)">
                    <span>배송비 포함</span>
                    <div class="shipping-tooltip">가격에 배송비가 포함되요</div>
                </label>
            </div>

            <!-- 배송비 입력 (배송비 포함이 아닐 때만 표시) -->
            <div class="form-group" v-if="!modelValue.freeShipping">
                <label for="shippingFee">배송비</label>
                <div class="price-input-wrapper">
                    <div class="input-container">
                        <input type="text" inputmode="numeric" id="shippingFee"
                            :value="formatNumber(modelValue.shippingFee)"
                            @input="handlePriceInput($event, 'shippingFee')"
                            @blur="handlePriceBlur($event, 'shippingFee')" maxlength="11" required>
                        <span class="currency-label">원</span>
                    </div>
                </div>
            </div>
        </template>

        <!-- 직거래 관련 옵션 -->
        <template v-if="modelValue.isPerson">
            <div class="form-group">
                <label>거래 장소</label>
                <AddressSearch :initial-address="modelValue.address" @update:address="handleAddressUpdate" />

                <!-- Map 컴포넌트 사용 부분 수정 -->
                <div v-if="modelValue.latitude && modelValue.longitude">
                    <Map :lat="Number(modelValue.latitude)" :lon="Number(modelValue.longitude)" />
                </div>
            </div>

            <TradeOptions :productInfo="tradeOptions" @update:options="handleTradeOptionUpdate" />
        </template>

        <!-- 상품 상태 선택 -->
        <div class="form-group">
            <label>상품 상태</label>
            <div class="condition-options">
                <div v-for="condition in productConditions" :key="condition.value" class="condition-option"
                    :class="{ active: modelValue.condition === condition.value }"
                    @click="updateField('condition', condition.value)">
                    <span class="condition-label">{{ condition.label }}</span>
                    <div class="condition-tooltip">{{ condition.description }}</div>
                </div>
            </div>
        </div>

        <!-- 상품 설명 -->
        <div class="form-group">
            <label for="content">상품 설명</label>
            <textarea id="content" :value="modelValue.content" @input="updateField('content', $event.target.value)"
                rows="4" required></textarea>
        </div>
    </div>
</template>

<style scoped>
div.product-form {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
    padding: 0;
}

.form-group {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
}

.form-group input[type="text"],
.form-group input[type="number"],
.form-group textarea {
    width: 100%;
    box-sizing: border-box;
    /* padding이 width에 포함되도록 설정 */
    padding: 0.75rem;
    border: 1px solid #dee2e6;
    border-radius: 0.25rem;
    font-size: 1rem;
}

label {
    font-weight: 500;
    color: #333;
}

input,
select,
textarea {
    padding: 0.75rem;
    border: 1px solid #dee2e6;
    border-radius: 0.25rem;
    font-size: 1rem;
}

input:focus,
select:focus,
textarea:focus {
    outline: none;
    border-color: #007bff;
}

input:disabled {
    background-color: #e9ecef;
    cursor: not-allowed;
}

.category-select {
    width: 100%;
}

.category-button {
    width: 100%;
    padding: 0.75rem;
    text-align: left;
    background-color: #fff;
    border: 1px solid #dee2e6;
    border-radius: 0.25rem;
    cursor: pointer;
    transition: all 0.2s;
}

.shipping-include {
    position: relative;
    margin: -0.5rem 0;
}

.shipping-include .checkbox-label {
    display: inline-flex;
    align-items: center;
    gap: 0.5rem;
    cursor: pointer;
    padding: 0.25rem 0;
}

.shipping-include input[type="checkbox"] {
    margin: 0;
    width: 1rem;
    height: 1rem;
}

.shipping-include span {
    font-size: 0.95rem;
    color: #495057;
}

.shipping-tooltip {
    position: absolute;
    bottom: 100%;
    left: 0;
    transform: translateY(-4px);
    background-color: rgba(0, 0, 0, 0.8);
    color: white;
    padding: 0.5rem;
    border-radius: 0.25rem;
    font-size: 0.875rem;
    white-space: nowrap;
    opacity: 0;
    visibility: hidden;
    transition: all 0.2s ease;
    z-index: 1000;
}

.checkbox-label:hover .shipping-tooltip {
    opacity: 1;
    visibility: visible;
}

.shipping-tooltip::after {
    content: '';
    position: absolute;
    top: 100%;
    left: 0.75rem;
    border-width: 5px;
    border-style: solid;
    border-color: rgba(0, 0, 0, 0.8) transparent transparent transparent;
}

.delivery-options {
    display: flex;
    flex-direction: column;
    gap: 0.75rem;
}

.delivery-type {
    display: flex;
    gap: 1rem;
}

.delivery-button {
    flex: 1;
    padding: 0.75rem;
    border: 1px solid #dee2e6;
    border-radius: 0.25rem;
    background: none;
    cursor: pointer;
    transition: all 0.2s ease;
}

.delivery-button.active {
    background-color: #007bff;
    color: white;
    border-color: #007bff;
}

.condition-options {
    display: flex;
    gap: 1rem;
    flex-wrap: wrap;
}

.condition-option {
    flex: 1;
    min-width: 120px;
    padding: 0.75rem;
    border: 1px solid #dee2e6;
    border-radius: 0.25rem;
    cursor: pointer;
    text-align: center;
    position: relative;
    transition: all 0.2s ease;
}

.condition-option:hover .condition-tooltip {
    opacity: 1;
    visibility: visible;
}

.condition-option.active {
    background-color: #007bff;
    color: white;
    border-color: #007bff;
}

.condition-tooltip {
    position: absolute;
    bottom: 100%;
    left: 50%;
    transform: translateX(-50%);
    background-color: rgba(0, 0, 0, 0.8);
    color: white;
    padding: 0.5rem;
    border-radius: 0.25rem;
    font-size: 0.875rem;
    white-space: nowrap;
    opacity: 0;
    visibility: hidden;
    transition: all 0.2s ease;
    z-index: 1000;
    margin-bottom: 0.5rem;
}

.condition-tooltip::after {
    content: '';
    position: absolute;
    top: 100%;
    left: 50%;
    transform: translateX(-50%);
    border-width: 5px;
    border-style: solid;
    border-color: rgba(0, 0, 0, 0.8) transparent transparent transparent;
}

.category-button:hover {
    border-color: #007bff;
}

.delivery-button:hover {
    background-color: #f8f9fa;
}

.condition-option:hover {
    border-color: #007bff;
}

textarea {
    resize: vertical;
    min-height: 100px;
}

.form-group textarea {
    width: 100%;
    padding: 0.75rem;
    border: 1px solid #dee2e6;
    border-radius: 0.25rem;
    font-size: 1rem;
    font-family: inherit;
    /* 부모 요소의 font-family 상속 */
    line-height: 1.5;
    resize: vertical;
    min-height: 100px;
}

/* 포커스 시 스타일 */
.form-group textarea:focus {
    outline: none;
    border-color: #007bff;
}

.price-input-wrapper {
    position: relative;
    width: 100%;
}

.price-input-wrapper input {
    width: 100%;
    padding: 0.75rem;
    padding-right: 4.5rem;
    /* '원' 글자를 위한 여백 */
    border: 1px solid #dee2e6;
    border-radius: 0.25rem;
    font-size: 1rem;
    min-width: 0;
}

.price-input-wrapper .currency-label {
    position: absolute;
    right: 1rem;
    top: 50%;
    transform: translateY(-50%);
    color: #495057;
    pointer-events: none;
    user-select: none;
    margin-left: 1rem;
}

/* input이 disabled 상태일 때도 동일한 스타일 유지 */
.price-input-wrapper input:disabled {
    background-color: #e9ecef;
    padding-right: 4.5rem;
}
</style>