'use client';

import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { toast } from 'sonner';
import { useAuthStore } from '@/stores/authStore';
import { Eye, EyeOff, Mail, Lock } from 'lucide-react';

export default function LoginPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const router = useRouter();

  const { login: authStoreLogin, isAuthenticated } = useAuthStore();

  useEffect(() => {
    if (isAuthenticated) {
      router.push('/');
    }
  }, [isAuthenticated, router]);

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
      window.location.href = oauthUrl;
    } else {
      toast.error('지원하지 않는 OAuth 제공자입니다.');
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);

    const result = await authStoreLogin({ email, password });

    if (result.success) {
      toast.success('로그인 성공!', { duration: 5000 });
      router.push('/');
    } else {
      toast.error(result.error || '로그인 중 오류가 발생했습니다.', {
        duration: 5000,
      });
    }
    setIsLoading(false);
  };

  return (
    <div className="min-h-screen bg-white flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        <div className="bg-white rounded-3xl shadow-xl border border-gray-100 overflow-hidden">
          {/* 헤더 */}
          <div className="bg-black px-8 py-6">
            <div className="text-center">
              <h1 className="text-2xl font-bold text-white mb-2">LogIn</h1>
              <p className="text-gray-300 text-sm">로그인하여 계속 진행하세요</p>
            </div>
          </div>

          {/* 폼 영역 */}
          <div className="px-8 py-6">
            <form onSubmit={handleSubmit} className="space-y-6">
              {/* 이메일 입력 */}
              <div className="space-y-3">
                <label htmlFor="email" className="text-base font-semibold text-gray-700 flex items-center gap-2">
                  <Mail className="w-5 h-5" />
                  이메일
                </label>
                <div className="relative">
                  <Input
                    id="email"
                    name="email"
                    type="email"
                    autoComplete="email"
                    required
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    className="pl-5 pr-5 py-6 text-base rounded-xl border-gray-200 focus:border-gray-900 focus:ring-1 focus:ring-gray-900 transition-colors"
                    placeholder="your@email.com"
                  />
                </div>
              </div>

              {/* 비밀번호 입력 */}
              <div className="space-y-3">
                <label htmlFor="password" className="text-base font-semibold text-gray-700 flex items-center gap-2">
                  <Lock className="w-5 h-5" />
                  비밀번호
                </label>
                <div className="relative">
                  <Input
                    id="password"
                    name="password"
                    type={showPassword ? 'text' : 'password'}
                    autoComplete="current-password"
                    required
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    className="pl-5 pr-12 py-6 text-base rounded-xl border-gray-200 focus:border-gray-900 focus:ring-1 focus:ring-gray-900 transition-colors"
                    placeholder="••••••••"
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="absolute inset-y-0 right-0 flex items-center pr-5 text-gray-400 hover:text-gray-600 transition-colors"
                  >
                    {showPassword ? <EyeOff className="h-6 w-6" /> : <Eye className="h-6 w-6" />}
                  </button>
                </div>
              </div>

              {/* 로그인 버튼 */}
              <Button 
                type="submit" 
                disabled={isLoading}
                className="w-full py-6 text-lg bg-gray-900 hover:bg-gray-800 text-white font-semibold rounded-xl transition-all duration-200 transform hover:scale-[1.02] disabled:opacity-50 disabled:scale-100"
              >
                {isLoading ? (
                  <div className="flex items-center justify-center gap-3">
                    <div className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                    로그인 중...
                  </div>
                ) : (
                  '로그인'
                )}
              </Button>
            </form>

            {/* 구분선 */}
            <div className="relative my-6">
              <div className="absolute inset-0 flex items-center">
                <div className="w-full border-t border-gray-200"></div>
              </div>
              <div className="relative flex justify-center text-sm">
                <span className="px-4 bg-white text-gray-500 font-medium">또는</span>
              </div>
            </div>

            {/* 소셜 로그인 */}
            <div className="space-y-3">
              <button
                type="button"
                onClick={() => handleOAuthLogin('google')}
                className="w-full flex items-center justify-center gap-3 px-4 py-3 border border-gray-200 rounded-xl hover:bg-gray-50 transition-colors font-medium text-gray-700"
              >
                <svg className="w-5 h-5" viewBox="0 0 24 24">
                  <path fill="#4285f4" d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92c-.26 1.37-1.04 2.53-2.21 3.31v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.09z"/>
                  <path fill="#34a853" d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"/>
                  <path fill="#fbbc05" d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"/>
                  <path fill="#ea4335" d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"/>
                </svg>
                Google로 로그인
              </button>

              <button
                type="button"
                onClick={() => handleOAuthLogin('naver')}
                className="w-full flex items-center justify-center gap-3 px-4 py-3 bg-[#03C75A] hover:bg-[#02B551] text-white rounded-xl transition-colors font-medium"
              >
                <div className="w-5 h-5 bg-white rounded flex items-center justify-center">
                  <span className="text-[#03C75A] font-bold text-sm">N</span>
                </div>
                네이버로 로그인
              </button>

              <button
                type="button"
                onClick={() => handleOAuthLogin('kakao')}
                className="w-full flex items-center justify-center gap-3 px-4 py-3 bg-[#FEE500] hover:bg-[#FDD800] text-black rounded-xl transition-colors font-medium"
              >
                <svg className="w-5 h-5" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M12 3c5.799 0 10.5 3.664 10.5 8.185 0 4.52-4.701 8.184-10.5 8.184a13.5 13.5 0 0 1-1.727-.11l-4.408 2.883c-.501.265-.678.236-.472-.413l.892-3.678c-2.88-1.46-4.785-3.99-4.785-6.866C1.5 6.665 6.201 3 12 3z"/>
                </svg>
                카카오로 로그인
              </button>
            </div>

            {/* 회원가입 링크 */}
            <div className="mt-6 text-center">
              <p className="text-sm text-gray-600">
                아직 계정이 없으신가요?{' '}
                <Link 
                  href="/signup" 
                  className="font-semibold text-gray-900 hover:text-gray-700 no-underline hover:underline underline-offset-2 decoration-2"
                >
                  회원가입하기
                </Link>
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}