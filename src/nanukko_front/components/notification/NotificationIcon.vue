<script setup>
defineProps({
    unreadCount: {
        type: Number,
        default: 0,
    },
});

defineEmits(['toggle']);
</script>

<template>
<div class="notification-icon" @click="$emit('toggle')">
    <i class="fas fa-bell">
        <span v-if="unreadCount > 0" class="notification-badge">
            {{  unreadCount }}
        </span>
    </i>
</div>

</template>