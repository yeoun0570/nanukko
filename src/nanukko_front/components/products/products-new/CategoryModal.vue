<script setup>
import { ref, computed, onMounted } from 'vue';
import { useCategories } from '~/composables/useCategories';

const props = defineProps({
    show: {
        type: Boolean,
        default: false
    }
});

const emit = defineEmits(['update:show', 'select']);

// 상태 관리를 위한 refs
const currentDepth = ref(0);           // 현재 depth (0: 대분류, 1: 중분류)
const selectedPath = ref([]);          // 선택된 카테고리 경로
const slideDirection = ref('next');    // 슬라이드 방향

// 카테고리 관련 컴포저블 사용
const {
    categories,
    isLoading,
    error,
    loadMajorCategories,
    loadMiddleCategories
} = useCategories();

// 현재 표시할 카테고리 목록 계산
const currentCategories = computed(() => {
    if (currentDepth.value === 0) return categories.value.main;
    const majorId = selectedPath.value[0]?.majorId;
    return categories.value.sub[majorId] || [];
});

// 현재 경로 텍스트
const pathText = computed(() => {
    return selectedPath.value
        .map(cat => cat.majorName || cat.middleName)
        .filter(Boolean)
        .join(' > ');
});

// 카테고리 선택 처리
const selectCategory = async (category) => {
    slideDirection.value = 'next';
    selectedPath.value = selectedPath.value.slice(0, currentDepth.value);
    selectedPath.value.push(category);

    if (currentDepth.value < 1) {
        // 대분류 선택 시
        try {
            await loadMiddleCategories(category.majorId);
            currentDepth.value++;
        } catch (err) {
            return;
        }
    } else {
        // 중분류 선택 시 - 선택 완료
        emit('select', {
            majorId: selectedPath.value[0].majorId,
            majorName: selectedPath.value[0].majorName,
            middleId: category.middleId,
            middleName: category.middleName
        });
        closeModal();
    }
};

// 뒤로가기
const goBack = () => {
    if (currentDepth.value > 0) {
        slideDirection.value = 'prev';
        currentDepth.value--;
        selectedPath.value.pop();
    } else {
        closeModal();
    }
};

// 모달 닫기
const closeModal = () => {
    emit('update:show', false);
    currentDepth.value = 0;
    selectedPath.value = [];
};

// 컴포넌트 마운트 시 대분류 카테고리 로드
onMounted(async () => {
    await loadMajorCategories();
});
</script>

<template>
    <Transition name="modal">
        <div v-if="show" class="category-modal">
            <!-- 헤더 -->
            <div class="modal-header">
                <button class="back-button" @click="goBack">
                    <v-icon>mdi-arrow-left</v-icon>
                </button>
                <h2>카테고리 선택</h2>
                <button class="close-button" @click="closeModal">
                    <v-icon>mdi-close</v-icon>
                </button>
            </div>

            <!-- 선택된 경로 표시 -->
            <div class="selected-path" v-if="selectedPath.length > 0">
                {{ pathText }}
            </div>

            <!-- 카테고리 목록 컨테이너 -->
            <div class="categories-container">
                <!-- 로딩 상태 -->
                <div v-if="isLoading" class="loading-indicator">
                    <v-progress-circular indeterminate color="primary" />
                </div>

                <!-- 에러 상태 -->
                <div v-else-if="error" class="error-message">
                    {{ error }}
                    <button @click="loadMajorCategories" class="retry-button">
                        다시 시도
                    </button>
                </div>

                <!-- 카테고리 목록 -->
                <TransitionGroup v-else :name="slideDirection === 'next' ? 'slide-next' : 'slide-prev'" tag="div"
                    class="categories-list">
                    <div v-for="category in currentCategories" :key="category.majorId || category.middleId"
                        class="category-item" @click="selectCategory(category)">
                        {{ category.majorName || category.middleName }}
                        <v-icon v-if="currentDepth === 0">mdi-chevron-right</v-icon>
                    </div>
                </TransitionGroup>
            </div>
        </div>
    </Transition>
</template>

<style scoped>
.category-modal {
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background: white;
    z-index: 1000;
    display: flex;
    flex-direction: column;
}

.modal-header {
    display: flex;
    align-items: center;
    padding: 1rem;
    border-bottom: 1px solid #eee;
}

.back-button,
.close-button {
    padding: 0.5rem;
    background: none;
    border: none;
    cursor: pointer;
    color: #666;
}

.modal-header h2 {
    flex: 1;
    text-align: center;
    margin: 0;
    font-size: 1.2rem;
    font-weight: 600;
}

.selected-path {
    padding: 0.75rem 1rem;
    color: #666;
    font-size: 0.9rem;
    background-color: #f8f9fa;
    border-bottom: 1px solid #eee;
}

.categories-container {
    flex: 1;
    overflow: hidden;
    position: relative;
}

.categories-list {
    height: 100%;
    overflow-y: auto;
}

.category-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 1rem;
    border-bottom: 1px solid #eee;
    cursor: pointer;
    transition: background-color 0.2s;
}

.category-item:hover {
    background-color: #f8f9fa;
}

/* 로딩 인디케이터 */
.loading-indicator {
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
}

/* 에러 메시지 */
.error-message {
    text-align: center;
    padding: 2rem;
    color: #dc3545;
}

.retry-button {
    margin-top: 1rem;
    padding: 0.5rem 1rem;
    background-color: #dc3545;
    color: white;
    border: none;
    border-radius: 0.25rem;
    cursor: pointer;
}

.retry-button:hover {
    background-color: #c82333;
}

/* 슬라이드 애니메이션 */
.slide-next-enter-active,
.slide-next-leave-active,
.slide-prev-enter-active,
.slide-prev-leave-active {
    transition: transform 0.3s ease;
}

.slide-next-enter-from {
    transform: translateX(100%);
}

.slide-next-leave-to {
    transform: translateX(-100%);
}

.slide-prev-enter-from {
    transform: translateX(-100%);
}

.slide-prev-leave-to {
    transform: translateX(100%);
}

/* 모달 애니메이션 */
.modal-enter-active,
.modal-leave-active {
    transition: all 0.3s ease;
}

.modal-enter-from,
.modal-leave-to {
    opacity: 0;
    transform: translateY(10px);
}
</style>