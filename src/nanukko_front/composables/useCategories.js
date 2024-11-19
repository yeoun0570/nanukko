import { ref } from "vue";
import axios from "axios";

export function useCategories() {
	const categories = ref({
		main: [],
		sub: {},
	});
	const isLoading = ref(false);
	const error = ref(null);

	// 대분류 카테고리 로드
	const loadMajorCategories = async () => {
		try {
			isLoading.value = true;
			error.value = null;
			const response = await axios.get(
				"http://localhost:8080/api/categories/major"
			);
			categories.value.main = response.data;
			return response.data;
		} catch (err) {
			console.error("대분류 카테고리 로드 실패:", err);
			error.value = "카테고리 정보를 불러오는데 실패했습니다.";
			throw err;
		} finally {
			isLoading.value = false;
		}
	};

	// 중분류 카테고리 로드
	const loadMiddleCategories = async (majorId) => {
		try {
			isLoading.value = true;
			error.value = null;
			const response = await axios.get(
				`http://localhost:8080/api/categories/middle/${majorId}`
			);
			categories.value.sub[majorId] = response.data;
			return response.data;
		} catch (err) {
			console.error("중분류 카테고리 로드 실패:", err);
			error.value = "하위 카테고리 정보를 불러오는데 실패했습니다.";
			throw err;
		} finally {
			isLoading.value = false;
		}
	};

	// 특정 대분류의 중분류 카테고리 가져오기
	const getMiddleCategories = (majorId) => {
		return categories.value.sub[majorId] || [];
	};

	// 카테고리 이름 가져오기
	const getCategoryName = (majorId, middleId) => {
		const major = categories.value.main.find((cat) => cat.majorId === majorId);
		if (!middleId) return major?.majorName || "";

		const middle = categories.value.sub[majorId]?.find(
			(cat) => cat.middleId === middleId
		);
		return middle?.middleName || "";
	};

	// 전체 경로 텍스트 가져오기
	const getCategoryPath = (majorId, middleId) => {
		const majorName = getCategoryName(majorId);
		const middleName = getCategoryName(majorId, middleId);

		if (middleId && majorName && middleName) {
			return `${majorName} > ${middleName}`;
		}
		return majorName;
	};

	return {
		categories,
		isLoading,
		error,
		loadMajorCategories,
		loadMiddleCategories,
		getMiddleCategories,
		getCategoryName,
		getCategoryPath,
	};
}
