<!-- components/TransactionForm.vue -->
<script setup>
const props = defineProps({
  productInfo: {
    type: Object,
    required: true,
  },
});

const emit = defineEmits(["update:transaction"]);

const handleInput = (field, value) => {
  emit("update:transaction", field, value);
};
</script>

<template>
  <div>
    <div class="form-group">
      <label>직거래 여부</label>
      <div class="radio-group">
        <label class="radio-label">
          <input
            type="radio"
            :checked="productInfo.person === true"
            @change="handleInput('person', true)"
            name="person"
          />
          <span>예</span>
        </label>
        <label class="radio-label">
          <input
            type="radio"
            :checked="productInfo.person === false"
            @change="handleInput('person', false)"
            name="person"
          />
          <span>아니요</span>
        </label>
      </div>
    </div>

    <template v-if="productInfo.person">
      <div class="form-group">
        <label>동행인 여부</label>
        <div class="radio-group">
          <label class="radio-label">
            <input
              type="radio"
              :checked="productInfo.companion === true"
              @change="handleInput('companion', true)"
              name="companion"
            />
            <span>예</span>
          </label>
          <label class="radio-label">
            <input
              type="radio"
              :checked="productInfo.companion === false"
              @change="handleInput('companion', false)"
              name="companion"
            />
            <span>아니요</span>
          </label>
        </div>
      </div>

      <div class="form-group" v-if="productInfo.companion">
        <label>동행인 성별</label>
        <div class="radio-group">
          <label class="radio-label">
            <input
              type="radio"
              :checked="productInfo.companionGender === true"
              @change="handleInput('companionGender', true)"
              name="companionGender"
            />
            <span>남자</span>
          </label>
          <label class="radio-label">
            <input
              type="radio"
              :checked="productInfo.companionGender === false"
              @change="handleInput('companionGender', false)"
              name="companionGender"
            />
            <span>여자</span>
          </label>
        </div>
      </div>

      <div class="form-group">
        <label>대리인 여부</label>
        <div class="radio-group">
          <label class="radio-label">
            <input
              type="radio"
              :checked="productInfo.deputy === true"
              @change="handleInput('deputy', true)"
              name="deputy"
            />
            <span>예</span>
          </label>
          <label class="radio-label">
            <input
              type="radio"
              :checked="productInfo.deputy === false"
              @change="handleInput('deputy', false)"
              name="deputy"
            />
            <span>아니요</span>
          </label>
        </div>
      </div>

      <div class="form-group" v-if="productInfo.deputy">
        <label>대리인 성별</label>
        <div class="radio-group">
          <label class="radio-label">
            <input
              type="radio"
              :checked="productInfo.deputyGender === true"
              @change="handleInput('deputyGender', true)"
              name="deputyGender"
            />
            <span>남자</span>
          </label>
          <label class="radio-label">
            <input
              type="radio"
              :checked="productInfo.deputyGender === false"
              @change="handleInput('deputyGender', false)"
              name="deputyGender"
            />
            <span>여자</span>
          </label>
        </div>
      </div>
    </template>
  </div>
</template>
