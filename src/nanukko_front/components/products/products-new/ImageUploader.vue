<script setup>
import { ref, watch } from 'vue';

const props = defineProps({
    images: {
        type: Array,
        default: () => []
    }
});

const emit = defineEmits(['update:images']);

const handleImageUpload = (event) => {
    const files = Array.from(event.target.files);

    if (files.length + props.images.length > 5) {
        alert("최대 5장까지 이미지를 업로드할 수 있습니다.");
        return;
    }

    const newImages = files.map(file => ({
        url: URL.createObjectURL(file),
        file: file
    }));

    emit('update:images', [...props.images, ...newImages]);
};

const removeImage = (index) => {
    const newImages = [...props.images];
    URL.revokeObjectURL(newImages[index].url);
    newImages.splice(index, 1);
    emit('update:images', newImages);
};
</script>

<template>
    <div class="image-uploader">
        <div class="image-container">
            <div v-for="(image, index) in images" :key="index" class="image-item">
                <div v-if="index === 0" class="badge">대표이미지</div>
                <img :src="image.url" alt="상품 이미지">
                <button type="button" class="delete-button" @click="removeImage(index)">
                    <v-icon>mdi-close</v-icon>
                </button>
            </div>

            <label v-if="images.length < 5" class="upload-label">
                <v-icon class="camera-icon">mdi-camera</v-icon>
                <span>이미지 등록</span>
                <input type="file" class="file-input" multiple @change="handleImageUpload" accept="image/*">
            </label>
        </div>

        <p class="help-text">최대 5장까지 이미지를 업로드할 수 있습니다.</p>
    </div>
</template>

<style scoped>
.image-uploader {
    margin-bottom: 2rem;
}

.image-container {
    display: flex;
    gap: 0.5rem;
    flex-wrap: nowrap;
    overflow-x: auto;
    padding: 0.5rem 0;
}

.image-item {
    position: relative;
    width: 150px;
    height: 150px;
    flex-shrink: 0;
    border-radius: 0.25rem;
    overflow: hidden;
}

.image-item img {
    width: 100%;
    height: 100%;
    object-fit: cover;
}

.upload-label {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    width: 150px;
    height: 150px;
    background-color: #f8f9fa;
    border: 2px dashed #dee2e6;
    border-radius: 0.25rem;
    cursor: pointer;
    flex-shrink: 0;
}

.camera-icon {
    color: #6c757d;
    font-size: 1.5rem;
    margin-bottom: 0.5rem;
}

.file-input {
    display: none;
}

.help-text {
    font-size: 0.875rem;
    color: #6c757d;
    margin-top: 0.5rem;
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
    z-index: 1;
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
    z-index: 1;
}
</style>