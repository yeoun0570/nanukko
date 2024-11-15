<!-- components/BasicInfoForm.vue -->
<script setup>
const props = defineProps({
  productInfo: {
    type: Object,
    required: true
  }
});

const emit = defineEmits(['update:info']);

const handleInput = (field, event) => {
  emit('update:info', field, event.target.value);
};
</script>

<template>
  <div>
    <div class="form-group">
      <label>상품명</label>
      <input 
        :value="productInfo.productName"
        @input="handleInput('productName', $event)"
        required 
      />
    </div>

    <div class="form-group">
      <label>썸네일</label>
      <input 
        :value="productInfo.thumbnailImage"
        @input="handleInput('thumbnailImage', $event)"
        required 
      />
    </div>

    <div class="form-group">
      <label>이미지1</label>
      <input 
        :value="productInfo.image1"
        @input="handleInput('image1', $event)"
        required 
      />
    </div>

    <div class="form-group">
      <label>가격</label>
      <input
        type="number"
        :value="productInfo.price"
        @input="handleInput('price', $event)"
        required
        min="0"
      />
    </div>

    <div class="form-group">
      <label>상품 상태</label>
      <select 
        :value="productInfo.condition"
        @change="handleInput('condition', $event)"
        required
      >
        <option value="NEW">새상품</option>
        <option value="LIKE_NEW">거의 새것</option>
        <option value="USED">사용감 있음</option>
        <option value="HEAVILY_USED">많이 사용함</option>
      </select>
    </div>

    <div class="form-group">
      <label>배송 상태</label>
      <select 
        :value="productInfo.status"
        @change="handleInput('status', $event)"
        required
      >
        <option value="SELLING">판매중</option>
        <option value="RESERVED">예약중</option>
        <option value="SOLD_OUT">판매 완료</option>
      </select>
    </div>

    <div class="form-group">
      <label>상품 설명</label>
      <textarea 
        :value="productInfo.content"
        @input="handleInput('content', $event)"
        required
      ></textarea>
    </div>
  </div>
</template>
