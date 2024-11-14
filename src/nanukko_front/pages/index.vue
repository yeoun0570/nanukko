<template>
<div>아무것도 안 뜨네</div>
{{ userData?.userId }}
<hr>
{{ userData2?.email }}

</template>

<script setup>
import axios, { Axios } from 'axios';

const userData = ref(null)
const userData2 = ref(null);
const message = ref('')

// 방법 1: useFetch 사용 (권장)
const { data, error } = await useFetch('http://localhost:8080/api/my-store/info', {
  query: {
    userId: 'seller1'
  }
});

if(data.value){
    userData.value = data.value;
}else {
    userData.value = "연결  실패ㅠㅠ";
    console.error("에러메시지 : ",error.value);
}

// 방법 2: axios 사용
try {
  const response = await axios.get('http://localhost:8080/api/my-store/info', {
    params: {
      userId: 'buyer1'
    }
  })
  userData2.value = response.data
} catch (error) {
  console.error('Error:', error)
  message.value = '연결 실패ㅠㅠ'
}

</script>