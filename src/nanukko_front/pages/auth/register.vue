<template>
  <!-- 전체 회원가입 컨테이너 -->
  <div class="register-container">
    <!-- form 태그 사용, submit 이벤트 발생시 handleSubmit 메서드 실행 -->
    <form @submit.prevent="handleSubmit" class="register-form">
      
      <!-- 1. 기본 정보 섹션 -->
      <section class="form-section" :class="{ 'error-section': hasBasicInfoErrors }">
        <h3>기본 정보</h3>
        
        <!-- 1.1 아이디 입력 그룹 -->
        <div class="form-group">
          <!-- label의 for와 input의 id를 일치시켜 연결 -->
          <label for="userId">
            아이디 <span class="required">*</span>
            <span class="hint">(5자 이상)</span>
          </label>
          <!-- 아이디 입력 필드와 중복확인 버튼을 감싸는 div -->
          <div class="input-button-group">
            <input 
              id="userId"
              v-model="formData.userId" 
              type="text"
              required
              minlength="6"
              :class="{ 'error': errors.userId || errors.isDuplicatedId }" 
            />
            <button 
              type="button"  
              class="check-button"
              @click="checkDuplicateId"
              :disabled="!formData.userId || formData.userId.length < 5"
            >
              중복확인
            </button>
          </div>
          <!-- 에러 메시지 표시 영역 -->
          <span v-if="errors.userId" class="error-text">{{ errors.userId }}</span>
          <span v-if="errors.isDuplicatedId" class="error-text">{{ errors.isDuplicatedId }}</span>
          <span v-if="idCheckPassed" class="success-text">사용 가능한 아이디입니다.</span>
        </div>
        

    <!-- 비밀번호 입력 -->
    <div class="form-group">
      <label for="password">
        비밀번호 <span class="required">*</span>
        <span class="hint">(8~16자의 영문 대/소문자, 숫자 조합)</span>
      </label>
      <input
        id="password"
        v-model="formData.password"
        type="password"
        required
        minlength="8"
        maxlength="16"
        :class="{ 'error': errors.password }"
      />
      <span v-if="errors.password" class="error-text">{{ errors.password }}</span>
    </div>

    <!-- 비밀번호 확인 입력 추가 -->
    <div class="form-group">
      <label for="passwordConfirm">
        비밀번호 확인 <span class="required">*</span>
      </label>
      <input
        id="passwordConfirm"
        v-model="formData.passwordConfirm"
        type="password"
        required
        :class="{ 'error': errors.passwordConfirm }"
      />
      <span v-if="errors.passwordConfirm" class="error-text">{{ errors.passwordConfirm }}</span>
    </div>
        <!-- 1.3 이름 입력 그룹 -->
        <div class="form-group">
          <label for="nickname">
            이름 <span class="required">*</span>
          </label>
          <input
            id="nickname"
            v-model="formData.nickname"
            type="text"
            required
          />
        </div>

        <!-- 1.4 생년월일 입력 그룹 -->
        <div class="form-group">
          <label for="userBirth">
            생년월일 <span class="required">*</span>
          </label>
          <input
            id="userBirth"
            v-model="formData.userBirth"
            type="date"
            required
            max="2099-12-31"
          />
        </div>

        <!-- 1.5 성별 선택 그룹 -->
        <div class="form-group">
          <label>성별 <span class="required">*</span></label>
          <div class="radio-group">
            <!-- 남성 선택 라디오 버튼 -->
            <label class="radio-label">
              <input
                type="radio"
                v-model="formData.gender"
                :value="true"
                required
              />
              <span>남성</span>
            </label>
            <!-- 여성 선택 라디오 버튼 -->
            <label class="radio-label">
              <input
                type="radio"
                v-model="formData.gender"
                :value="false"
                required
              />
              <span>여성</span>
            </label>
          </div>
        </div>
      </section>



      <!-- 자녀 정보 섹션 -->
<!-- 자녀 정보 섹션 -->
<section class="form-section" :class="{ 'error-section': hasChildInfoErrors }">
  <h3>자녀 정보</h3>
  
  <!-- 자녀 유무 선택 -->
  <div class="form-group">
    <label>자녀 유무 <span class="required">*</span></label>
    <div class="radio-group">
      <label class="radio-label">
        <input
          type="radio"
          v-model="hasChildren"
          :value="true"
          required
          @change="handleHasChildrenChange"
        />
        <span>있음</span>
      </label>
      <label class="radio-label">
        <input
          type="radio"
          v-model="hasChildren"
          :value="false"
          required
        />
        <span>없음</span>
      </label>
    </div>
    <span v-if="errors.hasChildren" class="error-text">{{ errors.hasChildren }}</span>
  </div>
  
  <!-- 자녀 정보 입력 영역 -->
  <div v-if="hasChildren" class="children-section">
    <div v-for="(kid, index) in formData.kids" :key="index" class="child-info">
      <div class="child-header">
        <h4>자녀 {{ index + 1 }}</h4>
        <button 
          type="button" 
          @click="removeChild(index)"
          class="remove-button"
        >
          삭제
        </button>
      </div>

      <!-- 자녀 생년월일 -->
      <div class="form-group">
        <label :for="'kidBirth' + index">
          생년월일 <span class="required">*</span>
        </label>
        <input
          :id="'kidBirth' + index"
          v-model="kid.kidBirth"
          type="date"
          required
        />
      </div>

      <!-- 자녀 성별 -->
      <div class="form-group">
        <label>성별 <span class="required">*</span></label>
        <div class="radio-group">
          <label class="radio-label">
            <input
              type="radio"
              v-model="kid.kidGender"
              :value="true"
              required
            />
            <span>남자</span>
          </label>
          <label class="radio-label">
            <input
              type="radio"
              v-model="kid.kidGender"
              :value="false"
              required
            />
            <span>여자</span>
          </label>
        </div>
      </div>
    </div>

    <!-- 자녀 추가 버튼 -->
    <button 
      type="button"
      @click="addChild"
      class="add-child-button"
    >
      + 자녀 추가
    </button>
  </div>
</section>

<!-- 2. 연락처 정보 섹션 -->
<section class="form-section" :class="{ 'error-section': hasContactInfoErrors }">
        <h3>연락처 정보</h3>
        
        <!-- 2.1 이메일 입력 그룹 -->
        <div class="form-group">
          <label for="email">
            이메일 <span class="required">*</span>
          </label>
          <input
            id="email"
            v-model="formData.email"
            type="email"
            required
            pattern="[a-z0-9._%+-]+@[a-z0-9.-]+\.[a-z]{2,}$"
            :class="{ 'error': errors.email }"
          />
          <span v-if="errors.email" class="error-text">{{ errors.email }}</span>
        </div>

        <!-- 2.2 휴대폰 번호 입력 그룹 -->
        <div class="form-group">
          <label for="mobile">
            휴대폰 번호 <span class="required">*</span>
          </label>
          <input
            id="mobile"
            v-model="formData.mobile"
            type="tel"
            required
            pattern="[0-9]{10,11}"
            placeholder="'-' 없이 숫자만 입력"
          />
        </div>

        <!-- 2.3 주소 입력 그룹 -->
        <div class="form-group">
          <label for="addrZipcode">
            우편번호 <span class="required">*</span>
          </label>
          <div class="input-button-group">
            <input
              id="addrZipcode"
              v-model="formData.addrZipcode"
              type="text"
              readonly
              required
            />
            <button type="button" @click="openAddressSearch" class="check-button">
              주소 찾기
            </button>
          </div>
        </div>
        
        <!-- 2.4 기본주소 입력 -->
        <div class="form-group">
          <label for="addrMain">
            기본주소 <span class="required">*</span>
          </label>
          <input
            id="addrMain"
            v-model="formData.addrMain"
            type="text"
            readonly
            required
          />
        </div>

        <!-- 2.5 상세주소 입력 -->
        <div class="form-group">
          <label for="addrDetail">
            상세주소 <span class="required">*</span>
          </label>
          <input
            id="addrDetail"
            v-model="formData.addrDetail"
            type="text"
            required
          />
        </div>
      </section>

      <!-- 3. 약관 동의 섹션 -->
     <!-- 약관 동의 섹션 -->
<section class="form-section" :class="{ 'error-section': hasConsentErrors }">
  <h3>약관 동의</h3>
  
  <!-- 전체 동의 -->
  <div class="consent-all">
    <label class="consent-label">
      <input
        type="checkbox"
        v-model="allConsent"
        @change="handleAllConsent"
      />
      <span class="consent-text">전체 동의</span>
    </label>
  </div>
  
  <!-- 약관 목록 -->
  <div class="consent-group">
    <!-- 1. 서비스 이용약관 -->
    <div class="consent-item">
      <div class="consent-header" @click="toggleTerms('tos')">
        <label class="consent-label">
          <input
            type="checkbox"
            v-model="formData.tosConsent"
            required
            @change="updateAllConsent"
          />
          <span class="consent-text">서비스 이용약관 동의 (필수)</span>
        </label>
        <span class="toggle-icon" :class="{ 'is-open': termsVisible.tos }">▼</span>
      </div>
      <div v-show="termsVisible.tos" class="terms-content">
        서비스 이용약관 내용...
      </div>
    </div>

    <!-- 2. 개인정보 처리방침 -->
    <div class="consent-item">
      <div class="consent-header" @click="toggleTerms('privacy')">
        <label class="consent-label">
          <input
            type="checkbox"
            v-model="formData.privacyConsent"
            required
            @change="updateAllConsent"
          />
          <span class="consent-text">개인정보 처리방침 동의 (필수)</span>
        </label>
        <span class="toggle-icon" :class="{ 'is-open': termsVisible.privacy }">▼</span>
      </div>
      <div v-show="termsVisible.privacy" class="terms-content">
        개인정보 처리방침 내용...
      </div>
    </div>

    <!-- 3. 위치기반 서비스 -->
    <div class="consent-item">
      <div class="consent-header" @click="toggleTerms('location')">
        <label class="consent-label">
          <input
            type="checkbox"
            v-model="formData.locConsent"
            @change="updateAllConsent"
          />
          <span class="consent-text">위치기반 서비스 이용약관 동의 (선택)</span>
        </label>
        <span class="toggle-icon" :class="{ 'is-open': termsVisible.location }">▼</span>
      </div>
      <div v-show="termsVisible.location" class="terms-content">
        위치기반 서비스 약관 내용...
      </div>
    </div>

    <!-- 4. 쿠키 정책 -->
    <div class="consent-item">
      <div class="consent-header" @click="toggleTerms('cookie')">
        <label class="consent-label">
          <input
            type="checkbox"
            v-model="formData.cookieConsent"
            @change="updateAllConsent"
          />
          <span class="consent-text">쿠키 정책 동의 (선택)</span>
        </label>
        <span class="toggle-icon" :class="{ 'is-open': termsVisible.cookie }">▼</span>
      </div>
      <div v-show="termsVisible.cookie" class="terms-content">
        쿠키 정책 내용...
      </div>
    </div>
  </div>

  <!-- 필수 약관 동의 관련 에러 메시지 -->
  <div v-if="errors.tosConsent || errors.privacyConsent" class="error-text">
    필수 약관에 동의해주세요.
  </div>
</section>
      

      <!-- 4. 제출 버튼 -->
      <div class="submit-section">
        <button 
          type="submit" 
          class="submit-button"
          :disabled="isSubmitting || !idCheckPassed"
        >
          {{ isSubmitting ? '처리중...' : '가입하기' }}
        </button>
      </div>
    </form>
  </div>
</template>

<script setup>
// 1. 필요한 Vue Composition API와 외부 모듈 불러오기
import { ref, reactive, computed } from 'vue'
import { useApi } from '~/composables/useApi'
import { useRouter } from 'vue-router'

// 2. API와 라우터 초기화
const router = useRouter()  // 페이지 이동을 위한 라우터
const api = useApi()        // API 호출을 위한 유틸리티

// 3. 폼 데이터 초기화
// reactive를 사용하여 객체의 모든 속성을 반응형으로 만듦
const formData = reactive({
  // 기본 정보
  userId: '',           
  password: '',         
  passwordConfirm: '',  // 비밀번호 확인
  nickname: '',         
  userBirth: '',        
  gender: null,         // 성별 (true: 남성, false: 여성)
  
  // 연락처 정보
  mobile: '',           
  email: '',           
  addrMain: '',        
  addrDetail: '',      
  addrZipcode: '',     
  
  // 자녀 정보
  kids: [],         // 자녀 정보 배열 [{childBirth, childGender}, ...]
  
  // 약관 동의
  tosConsent: false,    // 서비스 이용약관 동의(필수)
  privacyConsent: false,// 개인정보 처리방침 동의(필수)
  locConsent: false,    
  cookieConsent: false  
})

// 4. 상태 관리를 위한 ref 선언
const isSubmitting = ref(false)    // 폼 제출 진행 중 여부
const idCheckPassed = ref(false)   // 아이디 중복 확인 통과 여부
const hasChildren = ref(null)      // 자녀 유무 (true: 있음, false: 없음)
const allConsent = ref(false)      // 전체 약관 동의 여부

// 5. 약관 토글 상태 관리
const termsVisible = reactive({
  tos: false,      // 이용약관
  privacy: false,  // 개인정보처리방침
  location: false, // 위치기반서비스
  cookie: false    // 쿠키정책
})

// 6. 에러 메시지 관리
const errors = reactive({})

// 7. 유효성 검사 관련 computed 속성
// 각 섹션별 에러 상태를 확인하는 computed 속성들
const hasBasicInfoErrors = computed(() => {
  // 기본 정보 섹션의 필드들 중 하나라도 에러가 있으면 true 반환
  return ['userId', 'password', 'passwordConfirm', 'nickname', 'userBirth', 'gender']
    .some(field => errors[field])
})

const hasContactInfoErrors = computed(() => {
  // 연락처 정보 섹션의 필드들 중 하나라도 에러가 있으면 true 반환
  return ['email', 'mobile', 'addrZipcode', 'addrMain', 'addrDetail']
    .some(field => errors[field])
})

const hasChildInfoErrors = computed(() => {
  // 자녀 정보 섹션의 필드들 중 하나라도 에러가 있으면 true 반환
  return ['hasChildren', ...formData.kids.map((_, i) => `kid${i}`)]
    .some(field => errors[field])
})

const hasConsentErrors = computed(() => {
  // 필수 약관 동의 관련 에러가 있으면 true 반환
  return ['tosConsent', 'privacyConsent']
    .some(field => errors[field])
})

// 8. 비밀번호 유효성 검사 함수
const validatePassword = (password) => {
  // 비밀번호 규칙: 8~16자의 영문 대/소문자, 숫자 조합
  const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)[a-zA-Z\d]{8,16}$/
  return passwordRegex.test(password)
}

// 9. 아이디 중복 검사 함수
const checkDuplicateId = async () => {
  try {
    // 기본 유효성 검사
    if (!formData.userId || formData.userId.length < 5) {
      errors.userId = '아이디는 5자 이상이어야 합니다.'
      return
    }

    // 아이디 중복 검사 
    const response = await api.post('/register/duplicatedIdCheck', { 
      userId: formData.userId 
    })
    
    // 응답 처리
    if (response.ok) {
      const isDuplicated = await response.json()
      if (!isDuplicated) {//중복이 아니라면
        idCheckPassed.value = true
        errors.isDuplicatedId = ''
      } else {
        errors.isDuplicatedId = '이미 사용중인 아이디입니다.'
        idCheckPassed.value = false
      }
    }
  } catch (error) {
    console.error('아이디 중복 검사 실패:', error)
    errors.isDuplicatedId = '중복 검사 중 오류가 발생했습니다.'
    idCheckPassed.value = false
  }
}

// 10. 자녀 정보 관련 함수들
// 자녀 추가
const addChild = () => {
  formData.kids.push({
    kidBirth: '',
    kidGender: null
  })
}

// 자녀 삭제
const removeChild = (index) => {
  formData.kids.splice(index, 1)//배열에서 한 개 삭제
}

// 자녀 유무 변경 처리
const handleHasChildrenChange = (e) => {
  // '없음' 선택시 자녀 정보 초기화
  if (!e.target.checked) {
    formData.kids = []
  }
}

// 11. 약관 관련 함수들
// 약관 토글
const toggleTerms = (termType) => {
  termsVisible[termType] = !termsVisible[termType]
}

// 전체 동의 처리
const handleAllConsent = (e) => {
  const checked = e.target.checked
  allConsent.value = checked
  // 모든 약관 동의 상태를 한번에 변경
  formData.tosConsent = checked
  formData.privacyConsent = checked
  formData.locConsent = checked
  formData.cookieConsent = checked
}

// 개별 약관 동의 시 전체 동의 상태 업데이트
const updateAllConsent = () => {
  allConsent.value = [
    formData.tosConsent,
    formData.privacyConsent,
    formData.locConsent,
    formData.cookieConsent
  ].every(consent => consent)  // 모든 약관이 동의되었는지 확인
}

// 12. 주소 검색
const openAddressSearch = () => {
  new window.daum.Postcode({
    oncomplete: (data) => {
      formData.addrZipcode = data.zonecode
      formData.addrMain = data.address
    }
  }).open()
}

// 13. 전체 폼 유효성 검사
const validateForm = () => {
  const newErrors = {}

  // 아이디 검증
  if (!idCheckPassed.value) {
    newErrors.userId = '아이디 중복 확인이 필요합니다.'
  }

  // 비밀번호 검증
  if (!validatePassword(formData.password)) {
    newErrors.password = '비밀번호는 8~16자의 영문 대/소문자, 숫자 조합이어야 합니다.'
  }

  // 비밀번호 확인 검증
  if (formData.password !== formData.passwordConfirm) {
    newErrors.passwordConfirm = '비밀번호가 일치하지 않습니다.'
  }

  // 이메일 검증
  const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
  if (!emailRegex.test(formData.email)) {
    newErrors.email = '올바른 이메일 형식이 아닙니다.'
  }

  // 자녀 정보 검증
  if (hasChildren.value === true) {
    if (formData.kids.length === 0) {
      newErrors.hasChildren = '자녀 정보를 입력해주세요.'
    } else {
      formData.kids.forEach((kid, index) => {
        if (!kid.kidBirth || kid.kidGender === null) {
          newErrors[`kid${index}`] = '모든 자녀 정보를 입력해주세요.'
        }
      })
    }
  }

  // 필수 약관 동의 검증
  if (!formData.tosConsent) {
    newErrors.tosConsent = '서비스 이용약관 동의는 필수입니다.'
  }
  if (!formData.privacyConsent) {
    newErrors.privacyConsent = '개인정보 처리방침 동의는 필수입니다.'
  }

  // 에러 객체 업데이트
  Object.assign(errors, newErrors)//errors는 UI 에러 표시, newErrors는 새 에러로 erros를 덮어씌움
  
  // 에러가 없으면 true, 있으면 false 반환(객체의 키 값(문자열)들로 이루어진 배열 검사)
  return Object.keys(newErrors).length === 0
}

// 14. 폼 제출 처리
const handleSubmit = async () => {
  // 폼 유효성 검사
  if (!validateForm()) return
  
  try {
    // 제출 중 상태로 설정
    isSubmitting.value = true
    
    // API 요청 수행
    const response = await api.post('/register/', formData)
    
    // 응답 확인
    if (!response.ok) {
      throw new Error('회원가입 처리 중 오류가 발생했습니다.')
    }
    
    alert('회원가입이 완료되었습니다. 로그인 화면으로 이동합니다.')
    // 성공시 로그인 페이지로 이동
    await router.push('/auth/login')
    
  } catch (error) {
    console.error('회원가입 에러:', error)
    alert(error.message)
  } finally {
    // 제출 중 상태 해제
    isSubmitting.value = false
  }
}
</script>


<style scoped>
@import '~/assets/register/register.css';
</style>