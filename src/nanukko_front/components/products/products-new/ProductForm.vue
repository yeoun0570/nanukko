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

const updateField = (field, value) => {
    emit('update:modelValue', {
        ...props.modelValue,
        [field]: value
    });
};

// 카테고리 모달 표시 여부
const showCategoryModal = ref(false);

// 선택된 카테고리 표시 텍스트
const selectedCategoryText = computed(() => {
    if (props.modelValue.majorName && props.modelValue.middleName) {
        return `${props.modelValue.majorName} > ${props.modelValue.middleName}`;
    } else if (props.modelValue.majorName) {
        return props.modelValue.majorName;
    }
    return '';
});

// TradeOptions을 위한 프로퍼티
const tradeOptions = computed(() => ({
    companion: props.modelValue.companion || false,
    deputy: props.modelValue.deputy || false
}));

// 카테고리 업데이트 핸들러
const handleCategorySelect = (category) => {
    // 카테고리 정보 업데이트
    updateField('majorId', category.majorId);
    updateField('majorName', category.majorName);
    updateField('middleId', category.middleId);
    updateField('middleName', category.middleName);

    console.log('선택된 카테고리:', {
        대분류: `${category.majorId} - ${category.majorName}`,
        중분류: `${category.middleId} - ${category.middleName}`
    });
};

// 거래 옵션 업데이트 핸들러
const handleTradeOptionUpdate = (field, value) => {
    updateField(field, value);
};

// 배송 방식 변경 핸들러
const handleDeliveryTypeChange = (type) => {
    // 현재 배송 방식 배열 가져오기
    const currentTypes = Array.isArray(props.modelValue.deliveryType)
        ? [...props.modelValue.deliveryType]
        : [];

    // 선택한 타입이 이미 있으면 제거, 없으면 추가
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

    // 배송 방식별 관련 필드 업데이트
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

// 주소 업데이트 핸들러
const handleAddressUpdate = (addressInfo) => {
    console.log("주소 변경 확인");
    updateField('dealLocation', addressInfo);
};
</script>

<template>
    <div class="product-form">
        <div class="form-group">
            <label for="title">상품명</label>
            <input type="text" id="title" :value="modelValue.title" @input="updateField('title', $event.target.value)"
                required>
        </div>

        <!-- 카테고리 선택 부분 -->
        <div class="form-group">
            <label>카테고리</label>
            <div class="category-select">
                <button type="button" class="category-button" @click="showCategoryModal = true">
                    {{ selectedCategoryText || '카테고리를 선택해주세요' }}
                </button>
            </div>
        </div>

        <CategoryModal v-model="showCategoryModal" @select="handleCategorySelect" />

        <div class="form-group">
            <label for="price">가격</label>
            <input type="number" id="price" :value="modelValue.price" @input="updateField('price', $event.target.value)"
                required>
        </div>

        <div class="form-group">
            <label>배송 방식 (다중 선택 가능)</label>
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

        <!-- 택배 선택 시 표시되는 부분 -->
        <template v-if="Array.isArray(modelValue.deliveryType) && modelValue.deliveryType.includes('delivery')">
            <div class="form-group">
                <label for="deliveryFee">배송비</label>
                <input type="number" id="deliveryFee" :value="modelValue.deliveryFee"
                    @input="updateField('deliveryFee', $event.target.value)" required>
            </div>
        </template>

        <!-- 직거래 선택 시 표시되는 부분 -->
        <template v-if="Array.isArray(modelValue.deliveryType) && modelValue.deliveryType.includes('direct')">
            <div class="form-group">
                <label>거래 장소</label>
                <AddressSearch :initial-address="modelValue.dealLocation?.address"
                    @update:address="handleAddressUpdate" />

                <!-- 좌표가 있을 때만 지도 표시 -->
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
    </div>
</template>

<style scoped>
.product-form {
    display: flex;
    flex-direction: column;
    gap: 1.5rem;
}

.form-group {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
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

.category-button:hover {
    background-color: #f8f9fa;
    border-color: #ced4da;
}

/* 카테고리가 선택되지 않은 경우의 스타일 */
.category-button:empty::before {
    content: '카테고리를 선택해주세요';
    color: #6c757d;
}

/* 유효성 검사 스타일 */
.category-button:invalid {
    border-color: #dc3545;
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
    position: relative;
    width: 50%;
    /* margin: 0px 10px 0px 0px; */
}

.delivery-button.active {
    background-color: #007bff;
    color: white;
    border-color: #007bff;
}

.delivery-button.active::after {
    position: absolute;
    top: 4px;
    right: 4px;
    font-size: 12px;
}

.delivery-button:hover:not(.active) {
    background-color: #f8f9fa;
}

textarea {
    resize: vertical;
    min-height: 100px;
}
</style>