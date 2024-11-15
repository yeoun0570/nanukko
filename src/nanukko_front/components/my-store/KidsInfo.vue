<script setup>
const props = defineProps({
  kids: {
    type: Array,
    default: () => [],
  },
  isEditing: Boolean,
});

const emit = defineEmits(["update:kids"]);

const updateKid = (index, field, value) => {
  const updatedKids = [...props.kids];
  updatedKids[index] = { ...updatedKids[index], [field]: value };
  emit("update:kids", updatedKids);
};
</script>

<template>
  <div v-if="kids.length > 0" class="kids-info">
    <div v-for="(kid, index) in kids" :key="kid.kidId" class="kid-item">
      <div class="info-item">
        <label>생년월일:</label>
        <span v-if="!isEditing">{{ kid.kidBirth }}</span>
        <input
          v-else
          :value="kid.kidBirth"
          @input="updateKid(index, 'kidBirth', $event.target.value)"
          type="date"
          class="edit-input"
        />
      </div>
      <div class="info-item">
        <label>성별:</label>
        <span v-if="!isEditing">{{ kid.kidGender ? "남자" : "여자" }}</span>
        <select
          v-else
          :value="kid.kidGender"
          @change="
            updateKid(index, 'kidGender', $event.target.value === 'true')
          "
          class="edit-input"
        >
          <option :value="true">남자</option>
          <option :value="false">여자</option>
        </select>
      </div>
    </div>
  </div>
</template>
