import { useAuthStore } from '../stores';
import {
  useGetDiary,
  useWriteDiary,
  useUpdateDiary,
  useDeleteDiary,
  useGetUserProfile,
  useLogin,
  useSignup,
  useLogout,
  useSearchContents,
  LoginMutationResult,
} from '../generated/api/api';

// 사용자 관련 API 훅들
export const useUserProfile = (userId: number) => {
  return useGetUserProfile(userId, {
    query: {
      enabled: !!userId && !!useAuthStore.getState().isAuthenticated,
    },
  });
};

// 인증 관련 API 훅들
export const useLoginMutation = () => {
  return useLogin({
    mutation: {
      onSuccess: (response: LoginMutationResult) => {
        // API 응답에서 토큰 추출하여 상태 업데이트
        const accessToken = response.data?.accessToken;
        if (accessToken) {
          useAuthStore.setState({
            accessToken,
            isAuthenticated: true,
            isLoading: false,
            error: null,
          });
        }
      },
    },
  });
};

export const useRegister = () => {
  return useSignup({
    mutation: {
      onSuccess: (response) => {
        // Orval 생성 타입에 따라 response가 RsDataObject일 수 있음
        // 로그인과 동일한 응답을 기대한다면 백엔드 확인 필요
        const accessToken = (response.data as { accessToken?: string })?.accessToken;
        if (accessToken) {
          useAuthStore.setState({
            accessToken,
            isAuthenticated: true,
            isLoading: false,
            error: null,
          });
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
 