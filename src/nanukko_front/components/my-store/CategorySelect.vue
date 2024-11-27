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
  majorId: "",
  middleId: "",
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

const handleMajorChange = async (event) => {
  selectedCategories.value.majorId = event.target.value;
  selectedCategories.value.middleId = "";
  emit("update:category", "majorId", event.target.value);

  if (selectedCategories.value.majorId) {
    await loadMiddleCategories(selectedCategories.value.majorId);
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

const handleMiddleChange = (event) => {
  emit("update:category", "middleId", event.target.value);
};

onMounted(() => {
  loadCategories();
});
</script>

<template>
  <div class="form-group">
    <label>카테고리</label>
    <div class="category-select">
      <select :value="selectedCategories.majorId" @change="handleMajorChange" required>
        <option value="">대분류 선택</option>
        <option v-for="major in categories.majorCategories" :key="major.majorId" :value="major.majorId">
          {{ major.majorName }}
        </option>
      </select>

      <select :value="productInfo.middleId" @change="handleMiddleChange" required
        :disabled="!selectedCategories.majorId">
        <option value="">중분류 선택</option>
        <option v-for="middle in categories.middleCategories" :key="middle.middleId" :value="middle.middleId">
          {{ middle.middleName }}
        </option>
      </select>
    </div>
  </div>
</template>
<style scoped>
.category-select {
  display: flex;
  gap: 1rem;
}

.category-select select {
  flex: 1;
  padding: 0.75rem;
  border: 1px solid #dee2e6;
  border-radius: 0.25rem;
  font-size: 1rem;
}

.category-select select:focus {
  outline: none;
  border-color: #007bff;
}
</style>
