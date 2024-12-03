<script setup>
import { ref, onMounted } from 'vue';
import { useApi } from '@/composables/useApi';

const api = useApi();

const props = defineProps({
  productInfo: {
    type: Object,
    required: true,
  },
});

const emit = defineEmits(["update:category"]);

const categories = ref({
  majorCategories: [],
  middleCategories: []
});

const selectedCategories = ref({
  majorId: null,
  middleId: null
});

const isLoading = ref(true);

const loadCategories = async () => {
  try {
    const majorCategories = await api.get("/categories/major");
    categories.value.majorCategories = majorCategories || [];  // response.data 제거
    console.log("설정된 majorCategories:", categories.value.majorCategories);

    if (props.productInfo.majorId) {
      selectedCategories.value.majorId = props.productInfo.majorId;
      await loadMiddleCategories(props.productInfo.majorId);
      if (props.productInfo.middleId) {
        selectedCategories.value.middleId = props.productInfo.middleId;
      }
    }
  } catch (error) {
    console.error("대분류 카테고리 로드 실패:", error);
    categories.value.majorCategories = [];
  } finally {
    isLoading.value = false;
  }
};

const loadMiddleCategories = async (majorId) => {
  if (!majorId) return;

  try {
    const middleCategories = await api.get(`/categories/middle/${majorId}`);
    categories.value.middleCategories = middleCategories || [];  // response.data 제거
  } catch (error) {
    console.error("중분류 카테고리 로드 실패:", error);
    categories.value.middleCategories = [];
  }
};

const handleMajorSelect = async (majorId) => {
  selectedCategories.value.majorId = majorId;
  selectedCategories.value.middleId = null;
  emit("update:category", "majorId", majorId);
  await loadMiddleCategories(majorId);
};

const handleMiddleSelect = (middleId) => {
  selectedCategories.value.middleId = middleId;
  emit("update:category", "middleCategory", {
    majorId: selectedCategories.value.majorId,
    middleId
  });
};

onMounted(() => {
  loadCategories();
});
</script>

<template>
  <div class="form-group">
    <label>카테고리</label>
    <div v-if="isLoading" class="loading-state">
      로딩중...
    </div>
    <div v-else class="category-container">
      <!-- 대분류 카테고리 -->
      <div class="category-column">
        <h3 class="category-title">대분류</h3>
        <div class="category-list">
          <template v-if="categories.majorCategories.length > 0">
            <button type="button" v-for="major in categories.majorCategories" :key="major.majorId" :class="[
              'category-item',
              { active: selectedCategories.majorId === major.majorId }
            ]" @click="handleMajorSelect(major.majorId)">
              {{ major.majorName }}
            </button>
          </template>
          <div v-else class="empty-state">
            카테고리가 없습니다
          </div>
        </div>
      </div>

      <!-- 중분류 카테고리 -->
      <div class="category-column">
        <h3 class="category-title">중분류</h3>
        <div class="category-list" :class="{ disabled: !selectedCategories.majorId }">
          <template v-if="selectedCategories.majorId">
            <template v-if="categories.middleCategories.length > 0">
              <button type="button" v-for="middle in categories.middleCategories" :key="middle.middleId" :class="[
                'category-item',
                { active: selectedCategories.middleId === middle.middleId }
              ]" @click="handleMiddleSelect(middle.middleId)">
                {{ middle.middleName }}
              </button>
            </template>
            <div v-else class="empty-state">
              중분류가 없습니다
            </div>
          </template>
          <div v-else class="empty-state">
            대분류를 선택해주세요
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.loading-state {
  text-align: center;
  padding: 1rem;
  color: #666;
}

.category-container {
  display: flex;
  gap: 1rem;
}

.category-column {
  flex: 1;
}

.category-title {
  margin-top: 0.5rem;
  margin-bottom: 0.5rem;
  font-size: 0.9rem;
  color: #666;
  font-weight: normal;
}

.category-list {
  display: flex;
  flex-direction: column;
  border: 1px solid #ddd;
  border-radius: 4px;
  overflow: hidden;
}

.category-item {
  padding: 0.7rem 1rem;
  background: white;
  border: none;
  border-bottom: 1px solid #ddd;
  cursor: pointer;
  text-align: left;
  transition: all 0.2s;
}

.category-item:last-child {
  border-bottom: none;
}

.category-item:hover {
  background: #f8f9fa;
}

.category-item.active {
  background: #007bff;
  color: white;
}

.category-item.active:hover {
  background: #0056b3;
}

.empty-state {
  padding: 1rem;
  text-align: center;
  color: #666;
  background: #f8f9fa;
}

.disabled {
  opacity: 0.7;
  pointer-events: none;
}
</style>