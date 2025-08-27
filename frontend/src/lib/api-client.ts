import axios, { AxiosError, InternalAxiosRequestConfig, AxiosInstance, AxiosRequestConfig } from 'axios';
import { useAuthStore } from '@/stores/authStore';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || '/';

/**
 * Axios 인스턴스
 *
 * 모든 API 요청은 이 인스턴스를 통해 이루어집니다.
 * `withCredentials: true` 옵션을 통해 요청 시 쿠키를 포함하도록 설정합니다.
 */
const axiosInstance = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true,
});

// Axios 인스턴스를 customInstance라는 이름으로 내보냅니다.
// orval로 생성된 코드가 이 이름으로 인스턴스를 가져오기 때문입니다.
export { axiosInstance };

export const customInstance = <T>(config: AxiosRequestConfig, options?: { request?: AxiosRequestConfig }): Promise<T> => {
  const mergedConfig = { ...config, ...options?.request };
  return axiosInstance(mergedConfig);
};

/**
 * 요청 인터셉터 (Request Interceptor)
 *
 * API 요청을 보내기 전에 특정 작업을 수행합니다.
 * 여기서는 Zustand 스토어에서 액세스 토큰을 가져와 Authorization 헤더에 추가합니다.
 */
axiosInstance.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const { accessToken } = useAuthStore.getState();

    if (accessToken) {
      config.headers.Authorization = `Bearer ${accessToken}`;
    }
    return config;
  },
  (error: AxiosError) => {
    return Promise.reject(error);
  },
);

/**
 * 응답 인터셉터 (Response Interceptor)
 *
 * API 응답을 받은 후 특정 작업을 수행합니다.
 * 여기서는 401 (Unauthorized) 에러가 발생했을 때, 토큰 갱신을 시도합니다.
 */
axiosInstance.interceptors.response.use(
  (response) => {
    // 정상적인 응답은 그대로 반환합니다.
    return response;
  },
  async (error: AxiosError) => {
    const originalRequest = error.config as InternalAxiosRequestConfig & { _retry?: boolean };

    // 401 에러이고, 아직 재시도되지 않은 요청인 경우
    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true; // 재시도 플래그를 설정하여 무한 재시도를 방지합니다.

      const { refreshAccessToken, logout } = useAuthStore.getState();
      interface BackendErrorResponse {
        status: number;
        code: string;
        message: string;
      }
      const errorCode = (error.response.data as BackendErrorResponse)?.code;

      // 로그아웃 요청이거나, 토큰 만료가 아닌 다른 401 에러인 경우 즉시 로그아웃 처리
      if (originalRequest.url === '/api/v1/auth/logout' || errorCode !== 'AUTH_002') {
        await logout(true);
        return Promise.reject(error);
      }

      // 토큰 만료 (AUTH_002)인 경우에만 토큰 갱신 시도
      try {
        const refreshed = await refreshAccessToken();

        if (refreshed) {
          return axiosInstance(originalRequest);
        } else {
          await logout(true);
        }
      } catch (refreshError) {
        await logout(true);
        return Promise.reject(refreshError);
      }
    }

    // 그 외의 에러는 그대로 반환합니다.
    return Promise.reject(error);
  },
);