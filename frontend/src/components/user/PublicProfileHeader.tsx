'use client';

import React, { useEffect, useMemo, useState } from 'react';
import { useRouter } from 'next/navigation';
import { axiosInstance } from '@/lib/api-client';
import { useAuthStore } from '@/stores/authStore';

type PublicUser = {
  userId: number;
  email?: string;
  nickname: string;
  bio?: string;
  profileImageUrl?: string | null;
  isFollowing?: boolean;
  followId?: number;
  followersCount?: number;
  followingsCount?: number;
};

type Props = {
  userId: number;
  onChanged?: () => void;
};

export default function PublicProfileHeader({ userId, onChanged }: Props) {
  const [user, setUser] = useState<PublicUser | null>(null);
  const [loading, setLoading] = useState(true);
  const [diaryCount, setDiaryCount] = useState<number | null>(null);
  const [actionLoading, setActionLoading] = useState(false);
  const { user: me, isAuthenticated } = useAuthStore();

  const router = useRouter();

  const isMe = useMemo(() => {
    if (!me || typeof me.id === 'undefined') return false;
    return Number(me.id) === Number(userId);
  }, [me?.id, userId]);

  useEffect(() => {
    if (isAuthenticated === null) {
      return; 
    }

    let ignore = false;
    const fetchAll = async () => {
      setLoading(true);
      try {
        const res = await axiosInstance.get(`/api/v1/users/${userId}`);
        const d = res.data?.data ?? res.data;
        if (!ignore) setUser({ ...d, bio: d.bio ?? '', profileImageUrl: d.profileImageUrl ?? '' });

        const r2 = await axiosInstance.get(`/api/v1/diaries/users/${userId}?size=1`);
        if (!ignore) setDiaryCount(r2.data?.data?.totalElements ?? 0);
      } finally {
        if (!ignore) setLoading(false);
      }
    };

    fetchAll();
    return () => { ignore = true; };
  }, [userId, isAuthenticated]);


  const doFollow = async () => {
    if (!isAuthenticated) {
      alert('로그인이 필요합니다.');
      router.push('/login');
      return;
    }
    setActionLoading(true);
    try {
      await axiosInstance.post(`/api/v1/follows/${userId}`);
      setUser((prev) => prev ? {
        ...prev,
        isFollowing: true,
        followersCount: (prev.followersCount ?? 0) + 1
      } : prev);
      onChanged?.();
    } catch (error) {
      console.error('Follow error:', error);
      alert('팔로우 처리 중 오류가 발생했습니다.');
    } finally {
      setActionLoading(false);
    }
  };

  const doUnfollow = async () => {
    setActionLoading(true);
    try {
      await axiosInstance.delete(`/api/v1/follows/${userId}`);
      setUser((prev) => prev ? {
        ...prev,
        isFollowing: false,
        followersCount: Math.max(0, (prev.followersCount ?? 0) - 1)
      } : prev);
      onChanged?.();
    } catch (error) {
      console.error('Unfollow error:', error);
      alert('언팔로우 처리 중 오류가 발생했습니다.');
    } finally {
      setActionLoading(false);
    }
  };

  if (loading || isAuthenticated === null) {
    return (
      <div className="flex items-center gap-8 md:gap-16 animate-pulse">
        <div className="w-24 h-24 md:w-36 md:h-36 rounded-full bg-gray-200 flex-shrink-0" />
        <div className="flex-grow space-y-4">
          <div className="h-6 bg-gray-200 rounded w-1/2" />
          <div className="flex space-x-6">
            <div className="h-5 bg-gray-200 rounded w-16" />
            <div className="h-5 bg-gray-200 rounded w-16" />
            <div className="h-5 bg-gray-200 rounded w-16" />
          </div>
          <div className="h-4 bg-gray-200 rounded w-3/4" />
          <div className="h-4 bg-gray-200 rounded w-1/2" />
        </div>
      </div>
    );
  }

  if (!user) {
    return <div className="p-8 text-center text-gray-500">사용자를 찾을 수 없습니다.</div>;
  }
  
  const stats = [
    { label: '게시물', value: diaryCount ?? 0 },
    { label: '팔로워', value: user.followersCount ?? 0 },
    { label: '팔로잉', value: user.followingsCount ?? 0 },
  ];

  return (
    <header className="flex items-center gap-8 md:gap-16">
      <div className="flex-shrink-0 w-24 h-24 md:w-36 md:h-36">
        <img
          src={user.profileImageUrl || '/images/no-image.png'}
          alt="프로필"
          className="w-full h-full rounded-full object-cover ring-2 ring-offset-2 ring-gray-100"
        />
      </div>
      <div className="flex-grow space-y-3">
        <div className="flex items-center space-x-4">
          <h1 className="text-2xl md:text-3xl font-semibold text-gray-800">{user.nickname}</h1>
          {!isMe && isAuthenticated && (
            user.isFollowing ? (
              <button onClick={doUnfollow} disabled={actionLoading} className="px-4 py-1.5 text-sm font-semibold rounded-md bg-gray-200 text-gray-800 hover:bg-gray-300 transition-colors disabled:opacity-50">
                {actionLoading ? '...' : '팔로잉'}
              </button>
            ) : (
              <button
                onClick={doFollow}
                disabled={actionLoading}
                className="px-4 py-1.5 text-sm font-semibold rounded-md bg-sky-500 text-white hover:opacity-90 transition-opacity disabled:opacity-50"
              >
                {actionLoading ? '...' : '팔로우'}
              </button>
            )
          )}
        </div>
        <div className="flex space-x-6">
          {stats.map((s) => (
            <div key={s.label}>
              <span className="font-semibold text-gray-800">{s.value}</span>
              <span className="text-gray-600 ml-1">{s.label}</span>
            </div>
          ))}
        </div>
        {user.email && <p className="text-sm text-gray-500 font-mono">{user.email}</p>}
        <p className="text-gray-700 text-sm whitespace-pre-line pt-1">{user.bio || '소개글이 없습니다.'}</p>
      </div>
    </header>
  );
}