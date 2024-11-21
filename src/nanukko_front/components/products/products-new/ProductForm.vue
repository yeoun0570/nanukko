<script setup>
import { ref, computed } from 'vue';
import CategoryModal from '~/components/products/products-new/CategoryModal.vue';
import TradeOptions from '~/components/products/products-new/TradeOptions.vue';
import AddressSearch from '~/components/common/AddressSearch.vue';
import Map from '~/components/products/products-detail/Map.vue';

const props = defineProps({
    modelValue: {
        type: Object,
        required: true
    }
});

const emit = defineEmits(['update:modelValue']);
const showCategoryModal = ref(false);
const includeShipping = ref(false);

const updateField = (field, value) => {
    // 가격과 배송비의 경우 음수와 소수점 처리
    if (field === 'price' || field === 'deliveryFee') {
        value = Math.max(0, Math.floor(Number(value)));
    }

    emit('update:modelValue', {
        ...props.modelValue,
        [field]: value
    });
};

const selectedCategoryText = computed(() => {
    if (props.modelValue.majorName && props.modelValue.middleName) {
        return `${props.modelValue.majorName} > ${props.modelValue.middleName}`;
    } else if (props.modelValue.majorName) {
        return props.modelValue.majorName;
    }
    return '';
});

const tradeOptions = computed(() => ({
    companion: props.modelValue.companion || false,
    deputy: props.modelValue.deputy || false,
    gender: props.modelValue.gender || '',
    ageGroup: props.modelValue.ageGroup || ''  // 추가
}));

const handleCategorySelect = (category) => {
    updateField('majorId', category.majorId);
    updateField('majorName', category.majorName);
    updateField('middleId', category.middleId);
    updateField('middleName', category.middleName);
};

const handleTradeOptionUpdate = (field, value) => {
    updateField(field, value);
};

const handleDeliveryTypeChange = (type) => {
    const currentTypes = Array.isArray(props.modelValue.deliveryType)
        ? [...props.modelValue.deliveryType]
        : [];

    const index = currentTypes.indexOf(type);
    if (index > -1) {
        currentTypes.splice(index, 1);
    } else {
        currentTypes.push(type);
    }

    const updatedProduct = {
        ...props.modelValue,
        deliveryType: currentTypes
    };

    if (!currentTypes.includes('delivery')) {
        updatedProduct.deliveryFee = 0;
    }
    if (!currentTypes.includes('direct')) {
        updatedProduct.dealLocation = { zipCode: '', address: '', detailAddress: '' };
        updatedProduct.companion = false;
        updatedProduct.deputy = false;
    }

    emit('update:modelValue', updatedProduct);
};

const handleAddressUpdate = (addressInfo) => {
    updateField('dealLocation', addressInfo);
};

const handleIncludeShippingChange = (value) => {
    includeShipping.value = value;
    if (value && props.modelValue.deliveryType?.includes('delivery')) {
        updateField('deliveryFee', 0);
    }
};

const productConditions = [
    { value: 'NEW', label: '미사용', description: '사용하지 않은 새 상품이예요' },
    { value: 'LIKE_NEW', label: '사용감 없음', description: '사용은 했지만 눈에 띄는 부분은 없어요' },
    { value: 'USED', label: '사용감 적음', description: '눈에 띄는 부분이 약간 있어요' },
    { value: 'HEAVILY_USED', label: '사용감 많음', description: '눈에 띄는 부분이 많아요' }
];

// 숫자 포맷팅 함수
const formatNumber = (value) => {
    if (!value) return '';
    return value.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ',');
};

// 숫자만 추출하는 함수
const extractNumber = (value) => {
    return value.replace(/[^\d]/g, '');
};

// 입력값 처리 함수
const handlePriceInput = (event, field) => {
    let value = extractNumber(event.target.value);

    // 앞자리 0 제거
    value = value.replace(/^0+/, '');

    // 최대값 체크 (999,999,999)
    if (Number(value) > 999999999) {
        value = '999999999';
    }

    // 빈 값이거나 0인 경우 0으로 설정
    if (!value) {
        value = '0';
    }

    // 콤마가 있는 형태로 입력창에 표시
    event.target.value = formatNumber(value);

    // 실제 숫자값을 데이터에 저장
    updateField(field, Number(value));
};

// 포커스를 잃었을 때 처리 함수
const handlePriceBlur = (event, field) => {
    let value = extractNumber(event.target.value);

    // 빈 값이거나 0인 경우 0으로 설정
    if (!value) {
        value = '0';
    }

    // 콤마가 있는 형태로 다시 표시
    event.target.value = formatNumber(value);
    updateField(field, Number(value));
};
</script>

<template>
    <div class="product-form">
        <div class="form-group">
            <label for="title">상품명</label>
            <input type="text" id="title" :value="modelValue.title" @input="updateField('title', $event.target.value)"
                required>
        </div>

        <div class="form-group">
            <label>카테고리</label>
            <div class="category-select">
                <button type="button" class="category-button" @click="showCategoryModal = true">
                    {{ selectedCategoryText || '카테고리를 선택해주세요' }}
                </button>
            </div>
        </div>

        <CategoryModal v-model="showCategoryModal" @select="handleCategorySelect" />

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

        <!-- 배송비 포함 여부 부분 -->
        <div class="form-group shipping-include">
            <label class="checkbox-label">
                <input type="checkbox" :checked="includeShipping"
                    @change="handleIncludeShippingChange($event.target.checked)">
                <span>배송비 포함</span>
                <div class="shipping-tooltip">가격에 배송비가 포함되요</div>
            </label>
        </div>

        <div class="form-group">
            <label>배송 방식 (다중 선택 가능)</label>
            <div class="delivery-options">
                <div class="delivery-type">
                    <button type="button" :class="['delivery-button', {
                        active: Array.isArray(modelValue.deliveryType) &&
                            modelValue.deliveryType.includes('delivery')
                    }]" @click="handleDeliveryTypeChange('delivery')">
                        택배
                    </button>
                    <button type="button" :class="['delivery-button', {
                        active: Array.isArray(modelValue.deliveryType) &&
                            modelValue.deliveryType.includes('direct')
                    }]" @click="handleDeliveryTypeChange('direct')">
                        직거래
                    </button>
                </div>
            </div>
        </div>

        <!-- 배송비 입력 부분 -->
        <template v-if="Array.isArray(modelValue.deliveryType) && modelValue.deliveryType.includes('delivery')">
            <div class="form-group">
                <label for="deliveryFee">배송비</label>
                <div class="price-input-wrapper">
                    <div class="input-container">
                        <input type="text" inputmode="numeric" id="deliveryFee"
                            :value="formatNumber(modelValue.deliveryFee)"
                            @input="handlePriceInput($event, 'deliveryFee')"
                            @blur="handlePriceBlur($event, 'deliveryFee')" :disabled="includeShipping" maxlength="11"
                            required>
                        <span class="currency-label">원</span>
                    </div>
                </div>
            </div>
        </template>

        <!-- 직거래 장소 입력 부분 -->
        <template v-if="Array.isArray(modelValue.deliveryType) && modelValue.deliveryType.includes('direct')">
            <div class="form-group">
                <label>거래 장소</label>
                <AddressSearch :initial-address="modelValue.dealLocation?.address"
                    @update:address="handleAddressUpdate" />

                <div v-if="modelValue.dealLocation?.latitude && modelValue.dealLocation?.longitude">
                    <Map :lat="Number(modelValue.dealLocation.latitude)"
                        :lon="Number(modelValue.dealLocation.longitude)"></Map>
                </div>
            </div>

            <TradeOptions :productInfo="tradeOptions" @update:options="handleTradeOptionUpdate" />
        </template>

        <div class="form-group">
            <label for="description">상품 설명</label>
            <textarea id="description" :value="modelValue.description"
                @input="updateField('description', $event.target.value)" rows="4" required></textarea>
        </div>

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