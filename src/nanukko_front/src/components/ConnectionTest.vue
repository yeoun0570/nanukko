<script>
import axios from "axios";
import { ref, onMounted } from "vue";

export default {
  setup() {
    const data = ref("로딩 중...");

    onMounted(async () => {
      try {
        const response = await axios.get("http://localhost:8080/api/test"); // 8080 포트로 요청
        data.value = response.data;
      } catch (error) {
        console.error("Error fetching data:", error); // 에러 로그 확인
      }
    });

    return { data };
  },
};
</script>

<template>
  <div id="app">
    <h1>백엔드에서 받은 데이터:</h1>
    <pre>{{ data }}</pre>
  </div>
</template>
