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
      <input
        v-else
        :value="userBirth"
        @input="updateField('userBirth', $event.target.value)"
        type="date"
        class="edit-input"
      />
    </div>
  </div>
</template>
