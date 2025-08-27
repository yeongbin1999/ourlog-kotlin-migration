'use client';

import React, { useState } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { toast } from 'sonner';
import { useAuthStore } from '@/stores/authStore';

export default function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const router = useRouter();

  const { login: authStoreLogin } = useAuthStore();

  // 환경변수 읽기
  const GOOGLE_CLIENT_ID = process.env.NEXT_PUBLIC_GOOGLE_CLIENT_ID || '';
  const NAVER_CLIENT_ID = process.env.NEXT_PUBLIC_NAVER_CLIENT_ID || '';
  const KAKAO_CLIENT_ID = process.env.NEXT_PUBLIC_KAKAO_CLIENT_ID || '';

  const FRONTEND_REDIRECT_URI = process.env.NEXT_PUBLIC_FRONTEND_REDIRECT_URI || 'http://localhost:3000/oauth/callback';

  const getOAuthUrl = (provider: string) => {
    switch (provider) {
      case 'google':
        return `https://accounts.google.com/o/oauth2/v2/auth?client_id=${GOOGLE_CLIENT_ID}&redirect_uri=${encodeURIComponent(FRONTEND_REDIRECT_URI + '/google')}&response_type=code&scope=email profile`;
      case 'naver':
        return `https://nid.naver.com/oauth2.0/authorize?client_id=${NAVER_CLIENT_ID}&redirect_uri=${encodeURIComponent(FRONTEND_REDIRECT_URI + '/naver')}&response_type=code&scope=name,email`;
      case 'kakao':
        return `https://kauth.kakao.com/oauth/authorize?client_id=${KAKAO_CLIENT_ID}&redirect_uri=${encodeURIComponent(FRONTEND_REDIRECT_URI + '/kakao')}&response_type=code&scope=profile_nickname,profile_image`;
      default:
        return '';
    }
  };

  const handleOAuthLogin = (provider: string) => {
    const oauthUrl = getOAuthUrl(provider);
    if (oauthUrl) {
      // 외부 OAuth URL 이동 시 window.location.href 사용 권장
      window.location.href = oauthUrl;
    } else {
      toast.error('지원하지 않는 OAuth 제공자입니다.');
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    const result = await authStoreLogin({ email, password });

    if (result.success) {
      toast.success('로그인 성공!', { duration: 5000 });
      router.push('/');
    } else {
      toast.error(result.error || '로그인 중 오류가 발생했습니다.', {
        duration: 5000,
      });
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-gray-100">
      <div className="w-full max-w-md rounded-lg bg-white p-8 shadow-md">
        <h2 className="mb-6 text-center text-3xl font-bold text-gray-900">로그인</h2>
        <form onSubmit={handleSubmit} className="space-y-6">
          <div>
            <label htmlFor="email" className="block text-sm font-medium text-gray-700">
              이메일
            </label>
            <Input
              id="email"
              name="email"
              type="email"
              autoComplete="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="mt-1 block w-full"
            />
          </div>
          <div>
            <label htmlFor="password" className="block text-sm font-medium text-gray-700">
              비밀번호
            </label>
            <Input
              id="password"
              name="password"
              type="password"
              autoComplete="current-password"
              required
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              className="mt-1 block w-full"
            />
          </div>
          <Button type="submit" className="w-full" variant="black">
            로그인
          </Button>
        </form>

        <p className="mt-6 text-center text-sm">
          계정이 없으신가요?{' '}
          <Link href="/signup" className="font-medium text-blue-500 hover:text-blue-700 underline underline-offset-2">
            회원가입
          </Link>
        </p>

        <div className="mt-6 space-y-3">
          <Button
            className="w-full bg-red-500 hover:bg-red-600 text-white"
            onClick={() => handleOAuthLogin('google')}
          >
            Google 로그인
          </Button>
          <Button
            className="w-full bg-green-500 hover:bg-green-600 text-white"
            onClick={() => handleOAuthLogin('naver')}
          >
            Naver 로그인
          </Button>
          <Button
            className="w-full bg-yellow-500 hover:bg-yellow-600 text-white"
            onClick={() => handleOAuthLogin('kakao')}
          >
            Kakao 로그인
          </Button>
        </div>
      </div>
    </div>
  );
}