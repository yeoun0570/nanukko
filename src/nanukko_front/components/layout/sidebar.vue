<script setup>

const isOpen = ref({
  store: false,
  traslation: false,
});

const handleStore = () => {
  if (isOpen.value.store === false) {
    isOpen.value.store = true;
  } else {
    isOpen.value.store = false;
  }
};

const handleTranslation = () => {
  if (isOpen.value.traslation === false) {
    isOpen.value.traslation = true;
  } else {
    isOpen.value.traslation = false;
  }
};
</script>

<template>
  <div class="sidebar">
    <aside class="sidebar-container">
      <!-- 내정보 -->
      <div class="userInfo">
        <h2>내정보</h2>
      </div>

      <!--내상점-->
      <div class="mystore">
        <div class="dropdown-header" @click="Dropdown('store')">
          <h2 class="arrow" @click="handleStore">내상점</h2>
          <!-- <span class="arrow" :class="{'arrow-open': isOpen.store}" >▶</span> -->
        </div>
        <!-- <ul class ="dropdown-content" v-show="isOpen('store')"> -->
        <ul class="dropdown-content" v-if="isOpen.store">
          <li>판매상품</li>
          <li>구매상품</li>
          <li>찜상품</li>
          <li>나의 후기</li>
        </ul>
      </div>

      <!--거래내역-->
      <div class="transaction-history">
        <div class="dropdown-header" @click="Dropdown('translation')">
          <h2  class="arrow" @click="handleTranslation">거래내역</h2>
        </div>
        <!-- <ul class ="dropdown-content" v-show="isOpen('transaction-history')"> -->
        <ul class="dropdown-content" v-if="isOpen.traslation">
          <li>판매완료</li>
          <li>구매완료</li>
        </ul>
      </div>
    </aside>
  </div>
</template>

<style scoped>
.sidebar {
  width: 250px;
  background-color: #f8f9fa;
  min-height: 100vh;
}

.sidebar-container {
  padding: 20px;
}

.mystore {
  margin-bottom: 20px;
}

.dropdown-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  cursor: pointer;
  padding: 5px 0;
}

.dropdown-header h2 {
  margin: 0;
  font-size: 1.2rem;
}

.arrow {
  transition: transform 0.3s ease;
}

.arrow-down {
  transform: rotate(90deg);
}

.dropdown-content {
  list-style: none;
  padding-left: 20px;
  margin: 10px 0;
}

.dropdown-content li {
  padding: 8px 0;
  cursor: pointer;
  color: #666;
}

.dropdown-content li:hover {
  color: #000;
}
</style>
