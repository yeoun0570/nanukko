import axios from 'axios';

export const useURL = () => {
  const baseURL = "http://localhost:8080/api";
  
  const axiosInstance = axios.create({
    baseURL,
    withCredentials: true
  });

  axiosInstance.interceptors.request.use(
    (config) => {
      const token = localStorage.getItem('access_token');
      if (token) {
        // Bearer 접두사 확인 추가
        config.headers.Authorization = token.startsWith('Bearer ') 
          ? token 
          : `Bearer ${token}`;
      }
      console.log('Request Config:', config); // 디버깅용
      return config;
    },
    (error) => {
      console.error('Request Interceptor Error:', error);
      return Promise.reject(error);
    }
  );

  // 응답 인터셉터 추가
  axiosInstance.interceptors.response.use(
    (response) => response,
    (error) => {
      console.error('Response Error:', error);
      if (error.response?.status === 403) {
        console.error('Authorization Failed:', error.response);
      }
      return Promise.reject(error);
    }
  );

  return {
    baseURL,
    axiosInstance
  };
};