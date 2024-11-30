import { ref, onMounted } from "vue";
import { jwtDecode } from "jwt-decode";

export function useAuth() {
	const isAuthenticated = ref(false);
	const userId = ref(null);
	const nickname = ref(null);
	const token = ref(null);

	// 토큰 디코딩 및 사용자 정보 설정
	const decodeAndSetUserInfo = (accessToken) => {
		try {
			const decoded = jwtDecode(accessToken);
			userId.value = decoded.userId;
			nickname.value = decoded.nickname;
			isAuthenticated.value = true;
		} catch (error) {
			console.error("토큰 디코딩 실패:", error);
			clearAuth();
		}
	};

	// 토큰 설정
	const setToken = (accessToken) => {
		if (!accessToken) return;

		localStorage.setItem("access_token", accessToken);
		token.value = accessToken;
		decodeAndSetUserInfo(accessToken);
	};

	// 토큰 가져오기
	const getToken = () => {
		if (token.value) {
			return token.value;
		}

		const storedToken = localStorage.getItem("access_token");
		if (storedToken) {
			token.value = storedToken;
			decodeAndSetUserInfo(storedToken);
		}
		return storedToken;
	};

	// 인증 정보 초기화
	const clearAuth = () => {
		localStorage.removeItem("access_token");
		token.value = null;
		userId.value = null;
		nickname.value = null;
		isAuthenticated.value = false;
	};

	// 로그아웃
	const logout = async () => {
		try {
			const response = await fetch("/api/logout", {
				method: "POST",
				credentials: "include",
			});

			if (!response.ok) {
				throw new Error("로그아웃 처리중 오류가 발생했습니다.");
			}

			clearAuth();
		} catch (error) {
			console.error("로그아웃 에러:", error);
			throw error;
		}
	};

	// 초기화 함수
	const initialize = () => {
		const storedToken = localStorage.getItem("access_token");
		if (storedToken) {
			setToken(storedToken);
		}
	};

	// 컴포저블이 생성될 때 초기화 실행
	// 클라이언트 사이드에서만 실행되도록 처리
	if (typeof window !== "undefined") {
		initialize();
	}

	return {
		isAuthenticated,
		userId,
		nickname,
		token,
		setToken,
		getToken,
		logout,
		clearAuth,
	};
}
