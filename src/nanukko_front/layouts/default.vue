<script setup>
import Notification from "~/components/notification/Notification.vue";
</script>

<!-- layouts/default.vue -->
<template>
  <div>
    <header>
      <nav>
        <Notification />
        <!-- 다른 네비게이션 요소들 -->
      </nav>
    </header>
    <slot />
    <!-- 페이지 컨텐츠 -->
  </div>
</template>
