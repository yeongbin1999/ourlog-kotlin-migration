import { useAuthStore } from '../stores';
import {
  useGetDiary,
  useCreateDiary,
  useUpdateDiary,
  useDeleteDiary,
  useGetUserProfile,
  useLogin,
  useSignup,
  useLogout,
  useSearchContents,
  LoginMutationResult,
} from '../generated/api/api';

/** ===== 공통 유틸: RsData<LoginResponseDTO>에서 accessToken 안전 추출 ===== */
type RsData<T> = {
  resultCode: string;
  msg?: string | null;
  data?: T | null;
};

type LoginResponseDTO = {
  accessToken: string;
};

const isRecord = (v: unknown): v is Record<string, unknown> =>
  v !== null && typeof v === 'object';

const getAccessTokenFromRsData = (payload: unknown): string | undefined => {
  // payload가 RsData<LoginResponseDTO>라면 payload.data.accessToken 경로에 존재
  if (!isRecord(payload)) return undefined;
  const data = payload['data'];
  if (isRecord(data) && typeof data['accessToken'] === 'string') {
    return data['accessToken'];
  }
  // 혹시 백엔드가 평평한 형태로 내려주는 경우까지 방어
  if (typeof payload['accessToken'] === 'string') {
    return payload['accessToken'] as string;
  }
  return undefined;
};

/** ===== 사용자 관련 API 훅들 ===== */
export const useUserProfile = (userId: number) => {
  return useGetUserProfile(userId, {
    query: {
      enabled: !!userId && !!useAuthStore.getState().isAuthenticated,
    },
  });
};

/** ===== 인증 관련 API 훅들 ===== */
export const useLoginMutation = () => {
  return useLogin({
    mutation: {
      onSuccess: (response: LoginMutationResult) => {
        // ✅ 핵심: RsData<LoginResponseDTO> 구조 대응
        const accessToken = getAccessTokenFromRsData(response.data);
        if (accessToken) {
          useAuthStore.setState({
            accessToken,
            isAuthenticated: true,
            isLoading: false,
            error: null,
          });
        } else {
          console.warn('로그인 응답에 accessToken이 없습니다.', response.data);
        }
      },
    },
  });
};

export const useRegister = () => {
  return useSignup({
    mutation: {
      onSuccess: (response) => {
        // ✅ 회원가입 응답도 RsData<LoginResponseDTO> 혹은 평평한 형태 모두 대응
        const accessToken = getAccessTokenFromRsData(response.data);
        if (accessToken) {
          useAuthStore.setState({
            accessToken,
            isAuthenticated: true,
            isLoading: false,
            error: null,
          });
        } else {
          console.warn('회원가입 응답에 accessToken이 없습니다.', response.data);
        }
      },
    },
  });
};

export const useLogoutMutation = () => {
  return useLogout({
    mutation: {
      onSuccess: () => {
        useAuthStore.getState().logout();
      },
    },
  });
};
