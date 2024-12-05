<script setup>
import { ref, onMounted } from 'vue';

const { $showToast } = useNuxtApp();
const props = defineProps({
    initialAddress: {
        type: String,
        default: ''
    }
});

const emit = defineEmits(['update:address']);

const address = ref(props.initialAddress);
const detailAddress = ref('');
const postcode = ref('');
const coordinates = ref({ lat: null, lon: null });

// 주소로 좌표 검색
const getCoordinates = (addr) => {
    return new Promise((resolve, reject) => {
        const tryServiceInit = () => {
            if (window.kakao?.maps?.services) {
                const geocoder = new window.kakao.maps.services.Geocoder();

                geocoder.addressSearch(addr, (result, status) => {
                    if (status === window.kakao.maps.services.Status.OK) {
                        resolve({
                            lat: Number(result[0].y),
                            lon: Number(result[0].x)
                        });
                    } else {
                        reject('주소를 찾을 수 없습니다.');
                    }
                });
            } else {
                // 아직 초기화되지 않았다면 잠시 후 다시 시도
                setTimeout(tryServiceInit, 500);
            }
        };

        // 초기 시도
        tryServiceInit();

        // 10초 후에도 초기화되지 않으면 타임아웃
        setTimeout(() => {
            reject('지도 서비스 초기화 시간이 초과되었습니다.');
        }, 10000);
    });
};

// 주소 검색 팝업 열기
const openAddressSearch = () => {
    if (!window.daum?.Postcode) {
        $showToast('주소 검색 서비스를 불러오는 중입니다. 잠시 후 다시 시도해주세요.');
        return;
    }

    new window.daum.Postcode({
        oncomplete: async (data) => {
            const selectedAddress = data.roadAddress || data.jibunAddress;

            // 우선 기본 주소 정보 업데이트
            address.value = selectedAddress;
            postcode.value = data.zonecode;

            try {
                // 좌표 검색 시도
                const coords = await getCoordinates(selectedAddress);
                coordinates.value = coords;

                // 전체 정보 업데이트
                emit('update:address', {
                    zipCode: data.zonecode,
                    address: selectedAddress,
                    detailAddress: detailAddress.value,
                    latitude: coords.lat,
                    longitude: coords.lon
                });
            } catch (error) {
                console.error('좌표 변환 실패:', error);

                // 좌표 없이 주소 정보만 전달
                emit('update:address', {
                    zipCode: data.zonecode,
                    address: selectedAddress,
                    detailAddress: detailAddress.value,
                    latitude: null,
                    longitude: null
                });

                $showToast(typeof error === 'string' ? error : '주소의 좌표를 찾을 수 없습니다.');
            }
        }
    }).open();
};

// 상세 주소 변경 핸들러
const handleDetailAddressChange = (e) => {
    detailAddress.value = e.target.value;
    emit('update:address', {
        zipCode: postcode.value,
        address: address.value,
        detailAddress: detailAddress.value,
        latitude: coordinates.value?.lat || null,
        longitude: coordinates.value?.lon || null
    });
};

// 주소 초기화
const resetAddress = () => {
    address.value = '';
    detailAddress.value = '';
    postcode.value = '';
    coordinates.value = { lat: null, lon: null };
    emit('update:address', {
        zipCode: '',
        address: '',
        detailAddress: '',
        latitude: null,
        longitude: null
    });
};

// 다음 주소검색 API 로드
onMounted(() => {
    const script = document.createElement('script');
    script.src = '//t1.daumcdn.net/mapjsapi/bundle/postcode/prod/postcode.v2.js';
    script.async = true;
    document.head.appendChild(script);
});
</script>

<template>
    <div class="address-search-container">
        <!-- 우편번호 입력란 -->
        <div class="postcode-input">
            <input type="text" v-model="postcode" placeholder="우편번호" readonly class="postcode" />
            <button type="button" @click="openAddressSearch" class="search-button">
                주소 검색
            </button>
        </div>

        <!-- 기본 주소 입력란 -->
        <input type="text" v-model="address" placeholder="주소" readonly class="address-input" />

        <!-- 상세 주소 입력란 -->
        <div class="detail-address-input">
            <input type="text" v-model="detailAddress" placeholder="상세 주소를 입력하세요" @input="handleDetailAddressChange"
                class="detail-input" />
            <button type="button" @click="resetAddress" class="reset-button" v-if="address">
                <v-icon>mdi-close</v-icon>
            </button>
        </div>
    </div>
</template>

<style scoped>
.address-search-container {
    display: flex;
    flex-direction: column;
    gap: 0.5rem;
    width: 100%;
}

.postcode-input {
    display: flex;
    gap: 0.5rem;
}

.postcode {
    width: 150px;
    padding: 0.75rem;
    border: 1px solid #dee2e6;
    border-radius: 0.25rem;
    background-color: #f8f9fa;
    cursor: default;
}

.address-input {
    width: 100%;
    padding: 0.75rem;
    border: 1px solid #dee2e6;
    border-radius: 0.25rem;
    background-color: #f8f9fa;
    cursor: default;
}

.detail-address-input {
    position: relative;
    width: 100%;
}

.detail-input {
    width: 100%;
    padding: 0.75rem;
    border: 1px solid #dee2e6;
    border-radius: 0.25rem;
}

.search-button {
    padding: 0.75rem 1rem;
    background-color: #007bff;
    color: white;
    border: none;
    border-radius: 0.25rem;
    cursor: pointer;
    white-space: nowrap;
    font-weight: 500;
    min-width: 100px;
}

.search-button:hover {
    background-color: #0056b3;
}

.reset-button {
    position: absolute;
    right: 0.75rem;
    top: 50%;
    transform: translateY(-50%);
    background: none;
    border: none;
    color: #6c757d;
    cursor: pointer;
    padding: 0.25rem;
}

.reset-button:hover {
    color: #343a40;
}

input {
    font-size: 0.875rem;
}

input:focus {
    outline: none;
    border-color: #007bff;
}

/* 읽기 전용 입력란 스타일 */
input[readonly] {
    background-color: #f8f9fa;
    cursor: default;
}

/* 읽기 전용 입력란 호버 시 스타일 */
input[readonly]:hover {
    border-color: #dee2e6;
}

/* 모바일 반응형 스타일 */
@media (max-width: 768px) {
    .postcode {
        width: 120px;
    }

    .search-button {
        min-width: 80px;
        padding: 0.75rem 0.5rem;
    }
}
</style>