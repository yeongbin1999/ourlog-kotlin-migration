import { useState, useEffect, useCallback } from 'react';
import { useLocalStorage } from './useLocalStorage';
import { useApi } from './useApi';

interface User {
  id: string;
  email: string;
  nickname: string;
  profileImageUrl?: string;
}

interface UseAuthReturn {
  user: User | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (credentials: { email: string; password: string }) => Promise<{ success: boolean; error?: string }>;
  logout: () => void;
  refreshAccessToken: () => Promise<boolean>;
  updateUser: (userData: Partial<User>) => void;
}

const ACCESS_TOKEN_KEY = 'accessToken';
const USER_KEY = 'user';

export function useAuth(): UseAuthReturn {
  const [accessToken, setAccessToken, removeAccessToken] = useLocalStorage<string | null>(ACCESS_TOKEN_KEY, null);
  const [user, setUser, removeUser] = useLocalStorage<User | null>(USER_KEY, null);
  const [isLoading, setIsLoading] = useState(true);
  
  // API 호출을 위한 훅들
  const loginApi = useApi<{ accessToken: string; user: User }>();
  const verifyApi = useApi();
  const refreshApi = useApi<{ accessToken: string }>();
  const logoutApi = useApi();

  // 로그인 함수
  const login = useCallback(async (credentials: { email: string; password: string }) => {
    const data = await loginApi.execute({
      url: '/api/v1/auth/login',
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      data: credentials,
      withCredentials: true, // httpOnly 쿠키 포함
    });

    if (data) {
      setAccessToken(data.accessToken);
      setUser(data.user);
      
      // 다른 탭에 로그인 이벤트 알림
      window.localStorage.setItem('authEvent', JSON.stringify({
        type: 'login',
        timestamp: Date.now()
      }));
      
      return { success: true };
    } else {
      return { success: false, error: loginApi.error || '로그인에 실패했습니다.' };
    }
  }, [loginApi, setAccessToken, setUser]);

  // 로그아웃 함수
  const logout = useCallback(async () => {
    removeAccessToken();
    removeUser();
    
    // 백엔드에 로그아웃 요청 (httpOnly 쿠키 삭제를 위해)
    await logoutApi.execute({
      url: '/api/v1/auth/logout',
      method: 'POST',
      withCredentials: true, // 쿠키 포함
    });
    
    // 다른 탭에 로그아웃 이벤트 알림
    window.localStorage.setItem('authEvent', JSON.stringify({
      type: 'logout',
      timestamp: Date.now()
    }));
  }, [removeAccessToken, removeUser, logoutApi]);

  // 액세스 토큰 갱신 함수
  const refreshAccessToken = useCallback(async (): Promise<boolean> => {
    const data = await refreshApi.execute({
      url: '/api/v1/auth/refresh',
      method: 'POST',
      withCredentials: true, // httpOnly 쿠키 자동 포함
    });

    if (data) {
      setAccessToken(data.accessToken);
      return true;
    } else {
      logout();
      return false;
    }
  }, [refreshApi, setAccessToken, logout]);

  // 사용자 정보 업데이트 함수
  const updateUser = useCallback((userData: Partial<User>) => {
    if (user) {
      setUser({ ...user, ...userData });
    }
  }, [user, setUser]);

  // 초기 인증 상태 확인
  useEffect(() => {
    const initializeAuth = async () => {
      const token = accessToken;

      if (!token) {
        // 액세스 토큰이 없으면 리프레시 토큰으로 갱신 시도
        const refreshed = await refreshAccessToken();
        if (!refreshed) {
          setIsLoading(false);
          return;
        }
      } else {
        // 액세스 토큰이 있으면 유효성 검사
        const verifyResult = await verifyApi.execute({
          url: '/api/v1/auth/verify',
          headers: {
            'Authorization': `Bearer ${token}`,
          },
          withCredentials: true,
        });

        if (!verifyResult) {
          // 토큰이 유효하지 않으면 리프레시 시도
          const refreshed = await refreshAccessToken();
          if (!refreshed) {
            logout();
          }
        }
      }

      setIsLoading(false);
    };

    initializeAuth();
  }, [accessToken, refreshAccessToken, logout, verifyApi]);

  // 탭 간 동기화를 위한 이벤트 리스너
  useEffect(() => {
    const handleStorageChange = (e: StorageEvent) => {
      if (e.key === 'authEvent' && e.newValue) {
        try {
          const event = JSON.parse(e.newValue);
          
          // 같은 이벤트는 무시 (중복 처리 방지)
          if (event.timestamp === window.localStorage.getItem('lastAuthEventTimestamp')) {
            return;
          }
          
          window.localStorage.setItem('lastAuthEventTimestamp', event.timestamp.toString());

          if (event.type === 'logout') {
            // 다른 탭에서 로그아웃했으면 현재 탭도 로그아웃
            removeAccessToken();
            removeUser();
          } else if (event.type === 'login') {
            // 다른 탭에서 로그인했으면 현재 탭의 상태를 동기화
            const currentToken = accessToken;
            const currentUser = user;
            if (!currentToken || !currentUser) {
              // 현재 탭에 인증 정보가 없으면 새로고침
              window.location.reload();
            }
          }
        } catch (error) {
          console.error('Failed to parse auth event:', error);
        }
      }
    };

    window.addEventListener('storage', handleStorageChange);
    return () => window.removeEventListener('storage', handleStorageChange);
  }, [accessToken, user, removeAccessToken, removeUser]);

  return {
    user,
    isAuthenticated: !!accessToken && !!user,
    isLoading,
    login,
    logout,
    refreshAccessToken,
    updateUser,
  };
} 