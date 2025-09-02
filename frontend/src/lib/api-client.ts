import axios from "axios";
import type {
  AxiosError,
  InternalAxiosRequestConfig,
  AxiosInstance,
  AxiosRequestConfig,
  AxiosResponse,
} from "axios";
import { useAuthStore } from "@/stores/authStore";

const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || "/";

/**
 * Axios 인스턴스
 *
 * 모든 API 요청은 이 인스턴스를 통해 이루어집니다.
 * `withCredentials: true` 옵션을 통해 요청 시 쿠키를 포함하도록 설정합니다.
 */
export const axiosInstance: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true,
});

/**
 * orval용 customInstance
 * ⚠️ orval은 `Promise<AxiosResponse<T>>`를 기대합니다.
 *    여기서 data만 반환하면(= Promise<T>) 생성 코드가 깨집니다.
 */
export const customInstance = <T>(
  config: AxiosRequestConfig,
  options?: { request?: AxiosRequestConfig }
): Promise<AxiosResponse<T>> => {
  const mergedConfig: AxiosRequestConfig = { ...config, ...options?.request };
  // 절대 .then(res => res.data)로 바꾸지 마세요.
  return axiosInstance.request<T>(mergedConfig);
};

/**
 * 요청 인터셉터 (Request Interceptor)
 * - Zustand 스토어에서 액세스 토큰을 가져와 Authorization 헤더에 추가
 */
axiosInstance.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const { accessToken } = useAuthStore.getState();
    if (accessToken) {
      config.headers.Authorization = `Bearer ${accessToken}`;
    }
    return config;
  },
  (error: AxiosError) => Promise.reject(error)
);

/**
 * 응답 인터셉터 (Response Interceptor)
 * - 401 발생 시 토큰 만료 코드(AUTH_002)면 갱신 시도, 그 외는 즉시 로그아웃
 */
axiosInstance.interceptors.response.use(
  (response: AxiosResponse) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as (InternalAxiosRequestConfig & {
      _retry?: boolean;
    }) | undefined;

    if (!originalRequest) {
      return Promise.reject(error);
    }

    if (error.response?.status === 401 && !originalRequest._retry) {
      originalRequest._retry = true;

      const { refreshAccessToken, logout } = useAuthStore.getState();
      interface BackendErrorResponse {
        status: number;
        code: string;
        message: string;
      }
      const errorCode = (error.response.data as BackendErrorResponse)?.code;

      if (
        originalRequest.url === "/api/v1/auth/logout" ||
        errorCode !== "AUTH_002"
      ) {
        await logout(true);
        return Promise.reject(error);
      }

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

    return Promise.reject(error);
  }
);
