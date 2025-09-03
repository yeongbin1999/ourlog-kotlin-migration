'use client';

import React, { useState, useEffect } from 'react';
import { useRouter } from 'next/navigation';
import Link from 'next/link';
import { Input } from '@/components/ui/input';
import { Button } from '@/components/ui/button';
import { toast } from 'sonner';
import { useSignup } from '@/generated/api/api';
import { Eye, EyeOff, Mail, Lock, User, Check, X } from 'lucide-react';
import { AxiosError } from 'axios';
import { useAuthStore } from '@/stores/authStore';

export default function SignUpPage() {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [nickname, setNickname] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [isLoading, setIsLoading] = useState(false);
  const router = useRouter();

  const { mutateAsync: signupMutation } = useSignup();
  const { isAuthenticated } = useAuthStore();

  useEffect(() => {
    if (isAuthenticated) {
      router.push('/');
    }
  }, [isAuthenticated, router]);

  // 비밀번호 검증
  const isPasswordValid = password.length >= 6;
  const isPasswordMatch = password === confirmPassword && confirmPassword.length > 0;
  const isFormValid = email && nickname && isPasswordValid && isPasswordMatch;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (password !== confirmPassword) {
      toast.error('비밀번호가 일치하지 않습니다.', { duration: 5000 });
      return;
    }

    setIsLoading(true);
    try {
      await signupMutation({
        data: { email, password, nickname },
      });
      toast.success('회원가입이 완료되었습니다!', { duration: 5000 });
      router.push('/login');
    } catch (error) {
      const axiosError = error as AxiosError<{ message?: string }>;
      toast.error(axiosError.response?.data?.message || '회원가입 중 오류가 발생했습니다.', { duration: 5000 });
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
              <h1 className="text-2xl font-bold text-white mb-2">SignUp</h1>
              <p className="text-gray-300 text-sm">새로운 계정을 만들어 시작하세요</p>
            </div>
          </div>

          {/* 폼 영역 */}
          <div className="px-8 py-6">
            <form onSubmit={handleSubmit} className="space-y-6">
              {/* 이메일 입력 */}
              <div className="space-y-3">
                <label htmlFor="email" className="text-base font-semibold text-gray-700 flex items-center gap-2">
                  <Mail className="w-5 h-5" />
                  이메일 <span className="text-red-500">*</span>
                </label>
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

              {/* 닉네임 입력 */}
              <div className="space-y-3">
                <label htmlFor="nickname" className="text-base font-semibold text-gray-700 flex items-center gap-2">
                  <User className="w-5 h-5" />
                  닉네임 <span className="text-red-500">*</span>
                </label>
                <Input
                  id="nickname"
                  name="nickname"
                  type="text"
                  autoComplete="nickname"
                  required
                  value={nickname}
                  onChange={(e) => setNickname(e.target.value)}
                  className="pl-5 pr-5 py-6 text-base rounded-xl border-gray-200 focus:border-gray-900 focus:ring-1 focus:ring-gray-900 transition-colors"
                  placeholder="사용할 닉네임을 입력하세요"
                />
              </div>

              {/* 비밀번호 입력 */}
              <div className="space-y-3">
                <label htmlFor="password" className="text-base font-semibold text-gray-700 flex items-center gap-2">
                  <Lock className="w-5 h-5" />
                  비밀번호 <span className="text-red-500">*</span>
                </label>
                <div className="relative">
                  <Input
                    id="password"
                    name="password"
                    type={showPassword ? 'text' : 'password'}
                    autoComplete="new-password"
                    required
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    className={`pl-5 pr-12 py-6 text-base rounded-xl transition-colors ${
                      password.length > 0 
                        ? isPasswordValid 
                          ? 'border-green-300 focus:border-green-500' 
                          : 'border-red-300 focus:border-red-500'
                        : 'border-gray-200 focus:border-gray-900'
                    } focus:ring-1 focus:ring-gray-900`}
                    placeholder="최소 6자 이상"
                  />
                  <button
                    type="button"
                    onClick={() => setShowPassword(!showPassword)}
                    className="absolute inset-y-0 right-0 flex items-center pr-5 text-gray-400 hover:text-gray-600 transition-colors"
                  >
                    {showPassword ? <EyeOff className="h-6 w-6" /> : <Eye className="h-6 w-6" />}
                  </button>
                </div>
                {password.length > 0 && (
                  <div className="flex items-center gap-2 text-sm">
                    {isPasswordValid ? (
                      <><Check className="w-4 h-4 text-green-500" /><span className="text-green-600">비밀번호가 유효합니다</span></>
                    ) : (
                      <><X className="w-4 h-4 text-red-500" /><span className="text-red-600">최소 6자 이상 입력해주세요</span></>
                    )}
                  </div>
                )}
              </div>

              {/* 비밀번호 확인 */}
              <div className="space-y-3">
                <label htmlFor="confirmPassword" className="text-base font-semibold text-gray-700 flex items-center gap-2">
                  <Lock className="w-5 h-5" />
                  비밀번호 확인 <span className="text-red-500">*</span>
                </label>
                <div className="relative">
                  <Input
                    id="confirmPassword"
                    name="confirmPassword"
                    type={showConfirmPassword ? 'text' : 'password'}
                    autoComplete="new-password"
                    required
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    className={`pl-5 pr-12 py-6 text-base rounded-xl transition-colors ${
                      confirmPassword.length > 0 
                        ? isPasswordMatch 
                          ? 'border-green-300 focus:border-green-500' 
                          : 'border-red-300 focus:border-red-500'
                        : 'border-gray-200 focus:border-gray-900'
                    } focus:ring-1 focus:ring-gray-900`}
                    placeholder="비밀번호를 다시 입력하세요"
                  />
                  <button
                    type="button"
                    onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                    className="absolute inset-y-0 right-0 flex items-center pr-5 text-gray-400 hover:text-gray-600 transition-colors"
                  >
                    {showConfirmPassword ? <EyeOff className="h-6 w-6" /> : <Eye className="h-6 w-6" />}
                  </button>
                </div>
                {confirmPassword.length > 0 && (
                  <div className="flex items-center gap-2 text-sm">
                    {isPasswordMatch ? (
                      <><Check className="w-4 h-4 text-green-500" /><span className="text-green-600">비밀번호가 일치합니다</span></>
                    ) : (
                      <><X className="w-4 h-4 text-red-500" /><span className="text-red-600">비밀번호가 일치하지 않습니다</span></>
                    )}
                  </div>
                )}
              </div>

              {/* 회원가입 버튼 */}
              <Button 
                type="submit" 
                disabled={!isFormValid || isLoading}
                className={`w-full py-6 text-lg font-semibold rounded-xl transition-all duration-200 transform ${
                  isFormValid && !isLoading
                    ? 'bg-gray-900 hover:bg-gray-800 text-white hover:scale-[1.02]' 
                    : 'bg-gray-300 text-gray-500 cursor-not-allowed'
                }`}
              >
                {isLoading ? (
                  <div className="flex items-center justify-center gap-3">
                    <div className="w-5 h-5 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                    계정 생성 중...
                  </div>
                ) : (
                  '회원가입'
                )}
              </Button>
            </form>

            {/* 로그인 링크 */}
            <div className="mt-6 text-center">
              <p className="text-sm text-gray-600">
                이미 계정이 있으신가요?{' '}
                <Link 
                  href="/login" 
                  className="font-semibold text-gray-900 hover:text-gray-700 no-underline hover:underline underline-offset-2 decoration-2"
                >
                  로그인하기
                </Link>
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}