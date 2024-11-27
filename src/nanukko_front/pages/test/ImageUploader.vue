<script setup>
import { ref, watch, onUnmounted } from 'vue';
import { v4 as uuidv4 } from 'uuid';

const props = defineProps({
    images: {
        type: Array,
        default: () => []
    }
});

const emit = defineEmits(['update:images']);

const imageUrls = ref([]);

const handleImageUpload = (event) => {
    const files = Array.from(event.target.files);

    if (files.length + props.images.length > 5) {
        alert("최대 5장까지 이미지를 업로드할 수 있습니다.");
        return;
    }

    const newImages = files.map(file => ({
        file: file,
        url: URL.createObjectURL(file),
        uuid: uuidv4()
    }));

    console.log('새로 업로드된 이미지 파일들:', newImages);

    imageUrls.value = [...imageUrls.value, ...newImages.map(image => image.url)];
    emit('update:images', [...props.images, ...newImages]);
};

const removeImage = (index) => {
    const newImages = [...props.images];
    newImages.splice(index, 1);
    emit('update:images', newImages);

    URL.revokeObjectURL(imageUrls.value[index]);
    imageUrls.value.splice(index, 1);
};

onUnmounted(() => {
    imageUrls.value.forEach(url => URL.revokeObjectURL(url));
});
</script>

<template>
    <div class="image-uploader">
        <label class="upload-label">
            <v-icon class="camera-icon">mdi-camera</v-icon>
            <span>이미지 등록</span>
            <input type="file" class="file-input" multiple @change="handleImageUpload" accept="image/*">
        </label>

        <p class="help-text">최대 5장까지 이미지를 업로드할 수 있습니다.</p>

        <ul class="image-list">
            <li v-for="(image, index) in images" :key="image.uuid" class="image-item">
                <div v-if="index === 0" class="badge">대표이미지</div>
                <img :src="image.url" alt="상품 이미지">
                <button type="button" class="delete-button" @click="removeImage(index)">
                    <v-icon>mdi-close</v-icon>
                </button>
            </li>
        </ul>
    </div>
</template>

<style scoped>
.image-uploader {
    margin-bottom: 2rem;
}

.upload-label {
    display: inline-flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.75rem 1.5rem;
    background-color: #f8f9fa;
    border: 1px dashed #dee2e6;
    border-radius: 0.25rem;
    cursor: pointer;
}

.camera-icon {
    color: #6c757d;
}

.file-input {
    display: none;
}

.help-text {
    font-size: 0.875rem;
    color: #6c757d;
    margin-top: 0.5rem;
}

.image-list {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
    gap: 1rem;
    padding: 0;
    margin-top: 1rem;
    list-style: none;
}

.image-item {
    position: relative;
    aspect-ratio: 1;
    border-radius: 0.25rem;
    overflow: hidden;
}

.image-item img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.badge {
    position: absolute;
    top: 0.5rem;
    left: 0.5rem;
    padding: 0.25rem 0.5rem;
    background-color: rgba(0, 0, 0, 0.6);
    color: white;
    font-size: 0.75rem;
    border-radius: 0.25rem;
}

.delete-button {
    position: absolute;
    top: 0.5rem;
    right: 0.5rem;
    padding: 0.25rem;
    background-color: rgba(0, 0, 0, 0.6);
    color: white;
    border: none;
    border-radius: 50%;
    cursor: pointer;
}
</style>