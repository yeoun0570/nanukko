<script setup>
import { ref, computed } from 'vue';
import CategorySelect from '~/components/my-store/CategorySelect.vue';
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

// CategorySelect 컴포넌트를 위한 프로퍼티
const categoryInfo = computed(() => ({
    majorId: props.modelValue.majorId || '',
    middleId: props.modelValue.middleId || ''
}));

// TradeOptions을 위한 프로퍼티
const tradeOptions = computed(() => ({
    companion: props.modelValue.companion || false,
    deputy: props.modelValue.deputy || false
}));

// 카테고리 업데이트 핸들러
const handleCategoryUpdate = (field, value) => {
    updateField(field, value);
};

// 거래 옵션 업데이트 핸들러
const handleTradeOptionUpdate = (field, value) => {
    updateField(field, value);
};

// 배송 방식 변경 핸들러
const handleDeliveryTypeChange = (type) => {
    const updatedProduct = {
        ...props.modelValue,
        deliveryType: type
    };

    if (type === 'delivery') {
        updatedProduct.dealLocation = { zipCode: '', address: '', detailAddress: '' };
        updatedProduct.companion = false;
        updatedProduct.deputy = false;
    } else {
        updatedProduct.deliveryFee = 0;
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

        <div class="category-select">
            <CategorySelect :productInfo="categoryInfo" @update:category="handleCategoryUpdate" />
        </div>

        <div class="form-group">
            <label for="price">가격</label>
            <input type="number" id="price" :value="modelValue.price" @input="updateField('price', $event.target.value)"
                required>
        </div>

        <div class="form-group">
            <label>배송 방식</label>
            <div class="delivery-type">
                <button type="button" :class="['delivery-button', { active: modelValue.deliveryType === 'delivery' }]"
                    @click="handleDeliveryTypeChange('delivery')">
                    택배
                </button>
                <button type="button" :class="['delivery-button', { active: modelValue.deliveryType === 'direct' }]"
                    @click="handleDeliveryTypeChange('direct')">
                    직거래
                </button>
            </div>
        </div>

        <template v-if="modelValue.deliveryType === 'delivery'">
            <div class="form-group">
                <label for="deliveryFee">배송비</label>
                <input type="number" id="deliveryFee" :value="modelValue.deliveryFee"
                    @input="updateField('deliveryFee', $event.target.value)" required>
            </div>
        </template>

        <template v-if="modelValue.deliveryType === 'direct'">
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

            <!-- TradeOptions 컴포넌트로 변경 -->
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

.delivery-button:hover:not(.active) {
    background-color: #f8f9fa;
}

textarea {
    resize: vertical;
    min-height: 100px;
}
</style>