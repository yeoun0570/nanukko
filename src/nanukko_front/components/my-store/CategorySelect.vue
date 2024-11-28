<script setup>
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
  middleCategories: [],
});

const selectedCategories = ref({
  majorId: props.productInfo.majorId || "",
  middleId: props.productInfo.middleId || "",
});

const loadCategories = async () => {
  try {
    const response = await api.get(
      `/categories/major`
    );
    categories.value.majorCategories = response.data;
    if (props.productInfo.majorId) {
      await loadMiddleCategories(props.productInfo.majorId);
    }
  } catch (error) {
    console.error("카테고리 로드 실패:", error);
  }
};

const handleMajorSelect = async (majorId) => {
  selectedCategories.value.majorId = majorId;
  selectedCategories.value.middleId = "";
  emit("update:category", "majorId", majorId);

  if (majorId) {
    await loadMiddleCategories(majorId);
  } else {
    categories.value.middleCategories = [];
  }
};

const loadMiddleCategories = async (majorId) => {
  try {
    const response = await axios.get(
      `/categories/middle/${majorId}`
    );
    categories.value.middleCategories = response.data;
  } catch (error) {
    console.error("중분류 로드 실패:", error);
    categories.value.middleCategories = [];
  }
};

const handleMiddleSelect = (middleId) => {
  selectedCategories.value.middleId = middleId;
  // 대분류와 중분류 ID를 모두 emit
  emit("update:category", "middleCategory", {
    majorId: selectedCategories.value.majorId,
    middleId: middleId
  });
};

onMounted(() => {
  loadCategories();
});
</script>

<template>
  <div class="form-group">
    <label>카테고리</label>
    <div class="category-container">
      <!-- 대분류 카테고리 -->
      <div class="category-column">
        <h3 class="category-title">대분류</h3>
        <div class="category-list">
          <button v-for="major in categories.majorCategories" :key="major.majorId"
            :class="['category-item', { active: selectedCategories.majorId === major.majorId }]"
            @click="handleMajorSelect(major.majorId)">
            {{ major.majorName }}
          </button>
        </div>
      </div>

      <!-- 중분류 카테고리 -->
      <div class="category-column">
        <h3 class="category-title">중분류</h3>
        <div class="category-list" :class="{ disabled: !selectedCategories.majorId }">
          <div v-if="!selectedCategories.majorId" class="empty-state">
            대분류를 선택해주세요
          </div>
          <button v-else v-for="middle in categories.middleCategories" :key="middle.middleId"
            :class="['category-item', { active: selectedCategories.middleId === middle.middleId }]"
            @click="handleMiddleSelect(middle.middleId)">
            {{ middle.middleName }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.category-container {
  display: flex;
  gap: 1rem;
  margin-top: 0.5rem;
}

.category-column {
  flex: 1;
  min-width: 0;
}

.category-title {
  font-size: 0.875rem;
  color: #666;
  margin-bottom: 0.5rem;
  padding: 0.5rem;
  background-color: #f8f9fa;
  border-radius: 0.25rem;
}

.category-list {
  display: flex;
  flex-direction: column;
  gap: 0.25rem;
  max-height: 300px;
  overflow-y: auto;
  border: 1px solid #dee2e6;
  border-radius: 0.25rem;
  padding: 0.5rem;
}

.category-list.disabled {
  background-color: #f8f9fa;
  opacity: 0.75;
}

.category-item {
  text-align: left;
  padding: 0.75rem;
  border: none;
  border-radius: 0.25rem;
  background: none;
  cursor: pointer;
  transition: all 0.2s;
  color: #495057;
  font-size: 0.875rem;
}

.category-item:hover {
  background-color: #f8f9fa;
}

.category-item.active {
  background-color: #e7f5ff;
  color: #007bff;
  font-weight: 500;
}

.empty-state {
  padding: 2rem 1rem;
  text-align: center;
  color: #adb5bd;
  font-size: 0.875rem;
}

/* 스크롤바 스타일링 */
.category-list::-webkit-scrollbar {
  width: 6px;
}

.category-list::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 3px;
}

.category-list::-webkit-scrollbar-thumb {
  background: #ccc;
  border-radius: 3px;
}

.category-list::-webkit-scrollbar-thumb:hover {
  background: #999;
}
</style>