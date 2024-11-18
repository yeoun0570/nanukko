<script setup>
import { computed } from 'vue';

const props = defineProps({
    timestamp: {
        type: [Number, String, Date],
        required: true
    }
});

const getTimeAgo = computed(() => {
    // timestamp가 Date 객체인 경우 그대로 사용, 아닌 경우 변환
    const updatedTimeValue = props.timestamp instanceof Date
        ? props.timestamp
        : new Date(Number(props.timestamp) * 1000);

    const now = new Date();
    const diffInSeconds = Math.floor((now - updatedTimeValue) / 1000);

    // 시간 단위별 기준값
    const MINUTE = 60;
    const HOUR = MINUTE * 60;
    const DAY = HOUR * 24;
    const WEEK = DAY * 7;
    const MONTH = DAY * 30;
    const YEAR = DAY * 365;

    if (diffInSeconds < MINUTE) {
        return `${diffInSeconds}초 전`;
    } else if (diffInSeconds < HOUR) {
        const diffInMinutes = Math.floor(diffInSeconds / MINUTE);
        return `${diffInMinutes}분 전`;
    } else if (diffInSeconds < DAY) {
        const diffInHours = Math.floor(diffInSeconds / HOUR);
        return `${diffInHours}시간 전`;
    } else if (diffInSeconds < WEEK) {
        const diffInDays = Math.floor(diffInSeconds / DAY);
        return `${diffInDays}일 전`;
    } else if (diffInSeconds < MONTH) {
        const diffInWeeks = Math.floor(diffInSeconds / WEEK);
        return `${diffInWeeks}주 전`;
    } else if (diffInSeconds < YEAR) {
        const diffInMonths = Math.floor(diffInSeconds / MONTH);
        return `${diffInMonths}개월 전`;
    } else {
        const diffInYears = Math.floor(diffInSeconds / YEAR);
        return `${diffInYears}년 전`;
    }
});
</script>

<template>
    <span class="time-ago">{{ getTimeAgo }}</span>
</template>

<style scoped>
.time-ago {
    color: inherit;
    font-size: inherit;
}
</style>