<script setup>
import { ref, onMounted, watch } from 'vue';

const props = defineProps({
    lat: {
        type: Number,
        required: true
    },
    lon: {
        type: Number,
        required: true
    }
});

const map = ref(null);
const marker = ref(null);

// 카카오맵 초기화 대기 함수
const waitForKakaoMap = () => {
    return new Promise((resolve) => {
        const checkKakao = setInterval(() => {
            if (window.kakao?.maps) {
                clearInterval(checkKakao);
                resolve(true);
            }
        }, 100);
    });
};

// 지도 초기화 함수
const initializeMap = async () => {
    try {
        await waitForKakaoMap();

        const mapContainer = document.getElementById('map');
        const mapOption = {
            center: new window.kakao.maps.LatLng(props.lat, props.lon),
            level: 3
        };

        map.value = new window.kakao.maps.Map(mapContainer, mapOption);

        // 마커 생성
        const markerPosition = new window.kakao.maps.LatLng(props.lat, props.lon);
        marker.value = new window.kakao.maps.Marker({
            position: markerPosition
        });

        marker.value.setMap(map.value);
    } catch (error) {
        console.error('지도 초기화 실패:', error);
    }
};

// 좌표 변경 시 지도 업데이트
const updateMapPosition = () => {
    if (map.value && marker.value) {
        const newPosition = new window.kakao.maps.LatLng(props.lat, props.lon);
        map.value.setCenter(newPosition);
        marker.value.setPosition(newPosition);
    }
};

// props 변경 감지
watch([() => props.lat, () => props.lon], () => {
    updateMapPosition();
});

onMounted(async () => {
    await initializeMap();
});
</script>

<template>
    <div id="map" class="map-container"></div>
</template>

<style scoped>
.map-container {
    width: 100%;
    height: 400px;
    margin: 20px 0;
    border-radius: 8px;
    overflow: hidden;
    border: 1px solid #dee2e6;
}
</style>