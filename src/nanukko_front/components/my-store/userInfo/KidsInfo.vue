<script setup>
const props = defineProps({
  kids: {
    type: Array,
    default: () => [],
  },
  isEditing: Boolean,
});

const emit = defineEmits(["update:kids"]);

// 현재 추가중인 새로운 자녀들을 추적하기 위한 Set
const newKidIndices = new Set();

// 년/월/일 선택을 위한 데이터
const years = computed(() => {
  const currentYear = new Date().getFullYear();
  return Array.from({ length: 3 }, (_, i) => currentYear - 2 + i);
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

// 생년월일 파싱 메서드 - 기본값을 현재 날짜로 설정
const parseBirthDate = (birthDate) => {
  if (!birthDate) {
    const today = new Date();
    return {
      year: today.getFullYear(),
      month: today.getMonth() + 1,
      day: today.getDate(),
    };
  }
  const [year, month, day] = birthDate.split("-").map(Number);
  return { year, month, day };
};

// addKid 메서드
const addKid = () => {
  const today = new Date();
  const defaultBirthDate = `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, "0")}-${String(today.getDate()).padStart(2, "0")}`;

  const updatedKids = [...props.kids];
  updatedKids.push({
    kidBirth: defaultBirthDate,
    kidGender: true,
  });
  // 새로 추가된 자녀의 인덱스를 newKidIndices에 추가
  newKidIndices.add(updatedKids.length - 1);
  emit("update:kids", updatedKids);
};

// 새로 추가된 자녀 정보 취소
const cancelNewKid = (index) => {
  if (newKidIndices.has(index)) {
    const updatedKids = props.kids.filter((_, i) => i !== index);
    newKidIndices.delete(index);
    // index보다 큰 인덱스들을 모두 1씩 감소
    const newIndices = new Set(
      Array.from(newKidIndices).map((i) => (i > index ? i - 1 : i))
    );
    newKidIndices.clear();
    newIndices.forEach((i) => newKidIndices.add(i));
    emit("update:kids", updatedKids);
  }
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
            <!-- 새로 추가된 자녀에 대해서만 취소 버튼 표시 -->
            <button
              v-if="newKidIndices.has(index)"
              @click="cancelNewKid(index)"
              class="cancel-kid-btn"
            >
              <i class="fas fa-times"></i>
            </button>
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
