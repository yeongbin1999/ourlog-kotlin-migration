'use client';

import { useEffect } from 'react';
import { useRouter, useSearchParams, useParams } from 'next/navigation';
import { useAuthStore } from '@/stores/authStore';
import { toast } from 'sonner';
import { axiosInstance } from '@/lib/api-client';
import { useDeviceStore } from '@/stores/deviceStore';
import { getMe } from '@/generated/api/api';

const OAuthCallbackPage = () => {
  const router = useRouter();
  const searchParams = useSearchParams();
  const params = useParams();
  const provider = params.provider as string;
  const { setAuthInfo } = useAuthStore();
  const { deviceInfo } = useDeviceStore();

  useEffect(() => {
    const code = searchParams.get('code');
    const error = searchParams.get('error');

    if (error) {
      toast.error(`${provider} OAuth Error: ${error}`);
      router.push('/login');
      return;
    }

    if (code) {
      const handleOAuthLogin = async () => {
        try {
          // 환경변수 사용, NEXT_PUBLIC_ 접두어가 있어야 클라이언트에서 접근 가능
          const FRONTEND_REDIRECT_URI = `${process.env.NEXT_PUBLIC_FRONTEND_REDIRECT_URI}/${provider}`;

          const response = await axiosInstance.post(
            `/api/v1/auth/oauth/callback/${provider}`,
            { code, redirectUri: FRONTEND_REDIRECT_URI },
            {
              headers: {
                'X-Device-Id': deviceInfo.deviceId,
              },
            },
          );

          if (response.data.isSuccess) {
            const { accessToken } = response.data.data;
            console.log('Before first setAuthInfo. isAuthenticated:', true, 'accessToken:', accessToken);
            setAuthInfo({ accessToken, user: null, isAuthenticated: true });
            console.log('After first setAuthInfo. isAuthenticated:', useAuthStore.getState().isAuthenticated);

            try {
              const meResponse = await getMe({
                request: {
                  headers: { Authorization: `Bearer ${accessToken}` },
                },
              });
              if (meResponse.data) {
                const fetchedUser = {
                  id: meResponse.data.userId?.toString() || '',
                  email: meResponse.data.email || '',
                  nickname: meResponse.data.nickname || '',
                  profileImageUrl: meResponse.data.profileImageUrl,
                  bio: meResponse.data.bio,
                  followingsCount: meResponse.data.followingsCount,
                  followersCount: meResponse.data.followersCount,
                };
                setAuthInfo({ accessToken, user: fetchedUser, isAuthenticated: true });
                console.log('After second setAuthInfo. isAuthenticated:', useAuthStore.getState().isAuthenticated, 'user:', useAuthStore.getState().user);
              }
            } catch (meError) {
              console.error('Failed to fetch user profile after OAuth login:', meError);
              toast.error('사용자 프로필을 불러오는 데 실패했습니다.');
            }

            toast.success(`${provider} 로그인 성공!`);
            console.log('Attempting to redirect to /');
            router.push('/');
          } else {
            toast.error(response.data.msg || `${provider} 로그인 중 오류가 발생했습니다.`);
            router.push('/login');
          }
        } catch (err) {
          console.error(`Error during ${provider} OAuth login:`, err);
          toast.error(`${provider} 로그인 중 오류가 발생했습니다.`);
          router.push('/login');
        }
      };
      handleOAuthLogin();
    } else {
      toast.error(`${provider} OAuth 인증 코드를 받지 못했습니다.`);
      router.push('/login');
    }
  }, [searchParams, router, provider, setAuthInfo, deviceInfo.deviceId]);

  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-100">
      <p>{provider} 로그인 처리 중...</p>
    </div>
  );
};

export default OAuthCallbackPage;
