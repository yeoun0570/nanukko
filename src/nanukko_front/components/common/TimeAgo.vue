<template>
    <span>{{ timeAgoText }}</span>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'

const props = defineProps({
    timestamp: {
        type: String,
        required: true
    }
})

const timeAgoText = computed(() => {
    try {
        const date = new Date(props.timestamp)
        const now = new Date()

        const seconds = Math.floor((now - date) / 1000)
        const minutes = Math.floor(seconds / 60)
        const hours = Math.floor(minutes / 60)
        const days = Math.floor(hours / 24)
        const months = Math.floor(days / 30)
        const years = Math.floor(days / 365)

        if (years > 0) return `${years}년 전`
        if (months > 0) return `${months}개월 전`
        if (days > 0) return `${days}일 전`
        if (hours > 0) return `${hours}시간 전`
        if (minutes > 0) return `${minutes}분 전`
        return '방금 전'
    } catch (error) {
        console.error('Invalid date format:', error)
        return '날짜 형식 오류'
    }
})
</script>