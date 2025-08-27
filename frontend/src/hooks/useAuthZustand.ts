import { useEffect } from 'react';
import { useAuthStore } from '../stores/authStore';

// 기존 useAuth 훅과 동일한 인터페이스를 제공하는 새로운 훅
export function useAuth() {
  const {
    user,
    isAuthenticated,
    isLoading,
    error,
    login,
    logout,
    refreshAccessToken,
    updateUser,
    clearError,
    initializeAuth,
  } = useAuthStore();

  // 컴포넌트 마운트 시 인증 상태 초기화
  useEffect(() => {
    initializeAuth();
  }, [initializeAuth]);

  return {
    user,
    isAuthenticated,
    isLoading,
    error,
    login,
    logout,
    refreshAccessToken,
    updateUser,
    clearError,
  };
} 