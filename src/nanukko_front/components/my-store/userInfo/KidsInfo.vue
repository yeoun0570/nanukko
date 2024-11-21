<script setup>
const props = defineProps({
  kids: {
    type: Array,
    default: () => [],
  },
  isEditing: Boolean,
});

const emit = defineEmits(["update:kids"]);

// 년/월/일 선택을 위한 데이터
const years = computed(() => {
  const currentYear = new Date().getFullYear();
  return Array.from({ length: 10 }, (_, i) => currentYear - 5 + i);
});

const months = Array.from({ length: 12 }, (_, i) => i + 1);
const days = Array.from({ length: 31 }, (_, i) => i + 1);

const updateKid = (index, field, value) => {
  const updatedKids = [...props.kids];
  if (field === "birthDate") {
    const [year, month, day] = value.split("-");
    updatedKids[index] = {
      ...updatedKids[index],
      kidBirth: `${year}-${month.padStart(2, "0")}-${day.padStart(2, "0")}`,
    };
  } else {
    updatedKids[index] = { ...updatedKids[index], [field]: value };
  }
  emit("update:kids", updatedKids);
};

const addKid = () => {
  const updatedKids = [...props.kids];
  updatedKids.push({
    kidBirth: null,
    kidGender: true,
  });
  emit("update:kids", updatedKids);
};

// 생년월일 파싱 함수
const parseBirthDate = (birthDate) => {
  if (!birthDate) return { year: 2020, month: 1, day: 1 };
  const [year, month, day] = birthDate.split("-").map(Number);
  return { year, month, day };
};
</script>

<template>
  <div>
    <div v-if="kids.length > 0" class="kids-info">
      <div
        v-for="(kid, index) in kids"
        :key="kid.kidId || index"
        :class="['kid-item', kid.kidGender ? 'boy' : 'girl']"
      >
        <div class="kid-info-row">
          <template v-if="isEditing">
            <div class="birth-date-selectors">
              <select
                :value="parseBirthDate(kid.kidBirth).year"
                @change="
                  updateKid(
                    index,
                    'birthDate',
                    `${$event.target.value}-${parseBirthDate(kid.kidBirth).month}-${
                      parseBirthDate(kid.kidBirth).day
                    }`
                  )
                "
              >
                <option v-for="year in years" :key="year" :value="year">
                  {{ year }}년
                </option>
              </select>
              <select
                :value="parseBirthDate(kid.kidBirth).month"
                @change="
                  updateKid(
                    index,
                    'birthDate',
                    `${parseBirthDate(kid.kidBirth).year}-${
                      $event.target.value
                    }-${parseBirthDate(kid.kidBirth).day}`
                  )
                "
              >
                <option v-for="month in months" :key="month" :value="month">
                  {{ month }}월
                </option>
              </select>
              <select
                :value="parseBirthDate(kid.kidBirth).day"
                @change="
                  updateKid(
                    index,
                    'birthDate',
                    `${parseBirthDate(kid.kidBirth).year}-${
                      parseBirthDate(kid.kidBirth).month
                    }-${$event.target.value}`
                  )
                "
              >
                <option v-for="day in days" :key="day" :value="day">
                  {{ day }}일
                </option>
              </select>
            </div>
            <select
              :value="kid.kidGender"
              @change="
                updateKid(index, 'kidGender', $event.target.value === 'true')
              "
              class="gender-select"
            >
              <option :value="true">아들</option>
              <option :value="false">딸</option>
            </select>
          </template>
          <template v-else>
            <span class="birth-date">{{ kid.kidBirth }}</span>
            <span :class="['gender-indicator', kid.kidGender ? 'boy' : 'girl']">
              {{ kid.kidGender ? "아들" : "딸" }}
            </span>
          </template>
        </div>
      </div>
    </div>
    <button v-if="isEditing" @click="addKid" class="add-kid-btn">
      자녀 추가
    </button>
  </div>
</template>
