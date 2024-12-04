// composables/useApi.js

/**
 * JWT 토큰 검증이 필요없는 공개 API 경로 목록
 */
const PUBLIC_PATHS = ["/login", "/register", "/reissue", "/logout", "/user/find-id"];

/**
 * @param{string} path - 검사할 API 경로
 * @returns {boolean} - 공개 API 여부
 */

const isPublicPath = (path) => {
  return PUBLIC_PATHS.some((publicPath) => path.startsWith(publicPath));
};

/**
 * 공통 Fetch 로직
 * - 공개 API와 인증 필요 API에 대한 다른 헤더 설정
 * - 에러 처리 통합
 */
export const useApi = () => {
  // baseURL을 runtimeConfig에서 가져옴
  const {
    public: { baseURL },
  } = useRuntimeConfig();

  // 기본 옵션
  const commonFetch = async (url, options = {}) => {
    try {
      const baseOptions = {
        credentials: "include",
        headers: {
          "Content-Type": "application/json",
        },
      };

      // 공개 API가 아닌 경우 토큰 추가
      if (!isPublicPath(url)) {
        const token = localStorage.getItem("access_token");
        if (token) {
          baseOptions.headers.Authorization = `Bearer ${token}`;
        }
      }

      // URL 파라미터 처리 확인
      const queryParams = options.params
        ? "?" + new URLSearchParams(options.params).toString()
        : "";
      const fullUrl = url.startsWith("http")
        ? `${url}${queryParams}`
        : `${baseURL}${url}${queryParams}`;

      console.log("Request URL:", fullUrl); // 실제 요청 URL 확인

      const finalOptions = {
        ...baseOptions,
        ...options,
        headers: {
          ...baseOptions.headers,
          ...options.headers,
        },
      };

      const response = await fetch(fullUrl, finalOptions);

      // 여기에 상태 코드 체크를 추가
      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(
          errorData.message || "요청 처리 중 오류가 발생했습니다"
        );
      }

      // rawResponse 옵션이 있으면 response 객체를 직접 반환
      if (options.rawResponse) {
        return response;
      }

      // 응답이 있는 경우에만 JSON 파싱 시도
      const contentType = response.headers.get("content-type");
      if (contentType && contentType.includes("application/json")) {
        return await response.json();
      }

      // JSON이 아닌 경우 response 객체 반환
      return response;
    } catch (error) {
      console.error("API 요청 실패:", error);
      throw error;
    }
  };

  /**
   * GET 요청 메서드
   * @param {string} url - 요청 URL
   * @param {object} options - 추가 옵션
   */
  const get = (url, options = {}) =>
    commonFetch(url, {
      method: "GET",
      ...options,
    });

  /**
   * POST 요청 메서드
   * @param {string} url - 요청 URL
   * @param {object} data - 요청 본문 데이터
   * @param {object} options - 추가 옵션
   */
  const post = (url, data, options = {}) => {
    // 로그인 요청의 경우 특별 처리
    if (url === "/login") {
      const formData = new URLSearchParams();
      formData.append("username", data.userId); // userId로 키 변경
      formData.append("password", data.password);

      return commonFetch(url, {
        method: "POST",
        headers: {
          "Content-Type": "application/x-www-form-urlencoded",
        },
        body: formData,
        ...options,
      });
    }

    // 아이디 찾기 요청의 경우
    if (url === "/find-id") {
      return commonFetch(url, {
        method: "POST",
        ...options,
      });
    }

    // 일반 POST 요청
    return commonFetch(url, {
      method: "POST",
      body: JSON.stringify(data),
      ...options,
    });
  };

  /**
   * PUT 요청 메서드
   * @param {string} url - 요청 URL
   * @param {object} data - 요청 본문 데이터
   * @param {object} options - 추가 옵션
   */
  const put = (url, data, options = {}) =>
    commonFetch(url, {
      method: "PUT",
      body: JSON.stringify(data),
      ...options,
    });

  /**
   * DELETE 요청 메서드
   * @param {string} url - 요청 URL
   * @param {object} options - 추가 옵션
   */
  const del = (url, options = {}) =>
    commonFetch(url, {
      method: "DELETE",
      ...options,
    });

  return {
    get,
    post,
    put,
    delete: del,
  };
};
