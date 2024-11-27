<script setup>
const props = defineProps({
  nickname: String,
  email: String,
  mobile: String,
  userBirth: String,
  addrMain: String,
  addrDetail: String,
  addrZipcode: String,
  isEditing: Boolean,
});

const emit = defineEmits([
  "update:nickname",
  "update:email",
  "update:mobile",
  "update:userBirth",
  "update:addrMain",
  "update:addrDetail",
  "update:addrZipcode",
]);

const updateField = (field, value) => {
  emit(`update:${field}`, value);
};

//년/월/일 선택을 위한 데이터
const years = computed(() => {
  const currentYear = new Date().getFullYear();
  return Array.from({ length: 62 }, (_, i) => currentYear - 80 + i);
});

const months = Array.from({ length: 12 }, (_, i) => i + 1);
const days = Array.from({ length: 31 }, (_, i) => i + 1);

//생년월일 파싱
const birthDate = computed(() => {
  if (!props.userBirth) return { year: 1980, month: 1, day: 1 };
  const [year, month, day] = props.userBirth.split("-").map(Number);
  return { year, month, day };
});

// 생년월일 업데이트
const updateBirthDate = (field, value) => {
  const date = { ...birthDate.value, [field]: value };
  const formattedDate = `${date.year}-${String(date.month).padStart(2, "0")}-${String(date.day).padStart(2, "0")}`;
  updateField("userBirth", formattedDate);
};
</script>

<template>
  <div class="basic-info">
    <div class="info-item">
      <label>이름:</label>
      <span v-if="!isEditing">{{ nickname }}</span>
      <input
        v-else
        :value="nickname"
        @input="updateField('nickname', $event.target.value)"
        type="text"
        class="edit-input"
      />
    </div>
    <div class="info-item">
      <label>이메일:</label>
      <span v-if="!isEditing">{{ email }}</span>
      <input
        v-else
        :value="email"
        @input="updateField('email', $event.target.value)"
        type="email"
        class="edit-input"
      />
    </div>
    <div class="info-item">
      <label>전화번호:</label>
      <span v-if="!isEditing">{{ mobile }}</span>
      <input
        v-else
        :value="mobile"
        @input="updateField('mobile', $event.target.value)"
        type="mobile"
        class="edit-input"
      />
    </div>
    <div class="info-item">
      <label>생년월일:</label>
      <span v-if="!isEditing">{{ userBirth }}</span>
      <div v-else class="birth-date-selectors">
        <select
          :value="birthDate.year"
          @change="updateBirthDate('year', $event.target.value)"
          class="year-select"
        >
          <option v-for="year in years" :key="year" :value="year">
            {{ year }}년
          </option>
        </select>
        <select
          :value="birthDate.month"
          @change="updateBirthDate('month', $event.target.value)"
          class="month-select"
        >
          <option v-for="month in months" :key="month" :value="month">
            {{ month }}월
          </option>
        </select>
        <select
          :value="birthDate.day"
          @change="updateBirthDate('day', $event.target.value)"
          class="day-select"
        >
          <option v-for="day in days" :key="day" :value="day">
            {{ day }}일
          </option>
        </select>
      </div>
      <!-- <input
        v-else
        :value="userBirth"
        @input="updateField('userBirth', $event.target.value)"
        type="date"
        class="edit-input"
      /> -->
    </div>
  </div>
</template>
