'use client';

import React, { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { axiosInstance } from '@/lib/api-client';
import { useAuthStore } from '@/stores/authStore';

type Props = {
  userId: string;
  userType?: 'profile' | 'received' | 'sent' | 'following' | 'followers';
  followId?: number;
  isFollowing?: boolean;
  onActionCompleted?: () => void;
};

type UserProfile = {
  email: string;
  nickname: string;
  profileImageUrl: string;
  bio: string;
};

export default function UserProfileCard({
  userId,
  userType = 'profile',
  followId,
  isFollowing: isFollowingProp,
  onActionCompleted,
}: Props) {
  const router = useRouter();
  const { user: myUser } = useAuthStore();
  const myUserId = myUser?.id ? String(myUser.id) : null;

  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [isFollowing, setIsFollowing] = useState<boolean>(!!isFollowingProp);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  // 프로필 데이터 로드
  useEffect(() => {
    let ignore = false;
    const fetchProfile = async () => {
      try {
        const res = await axiosInstance.get(`/api/v1/users/${userId}`);
        const data = res.data?.data ?? res.data;
        if (!ignore) {
          setProfile({
            email: data.email,
            nickname: data.nickname,
            bio: data.bio ?? '',
            profileImageUrl: data.profileImageUrl ?? '',
          });
        }
      } catch {
        if (!ignore) setError('사용자 정보를 불러올 수 없습니다.');
      }
    };
    fetchProfile();
    return () => { ignore = true; };
  }, [userId]);

  // 팔로우 상태 확인
  useEffect(() => {
    if (typeof isFollowingProp === 'boolean') return;
    if (!myUserId) return;

    const fetchFollowStatus = async () => {
      setLoading(true);
      try {
        const followingsRes = await axiosInstance.get(`/api/v1/follows/followings?userId=${myUserId}`);
        const followingList = Array.isArray(followingsRes.data) ? followingsRes.data : [];
        setIsFollowing(followingList.some((u: any) => String(u.userId) === userId));
      } catch (err) {
        console.error('팔로우 상태 확인 실패', err);
      } finally {
        setLoading(false);
      }
    };

    fetchFollowStatus();
  }, [userId, myUserId, isFollowingProp]);

  // 팔로우 / 언팔로우 / 요청 취소
  const toggleFollow = async () => {
    if (!myUserId) return;
    setLoading(true);
    try {
      if (isFollowing || userType === 'sent') {
        await axiosInstance.delete(`/api/v1/follows/${userId}`);
        setIsFollowing(false);
      } else {
        await axiosInstance.post(`/api/v1/follows/${userId}`, {});
        setIsFollowing(true);
      }
      onActionCompleted?.();
    } catch (err) {
      console.error(err);
      alert('요청 처리 실패');
    } finally {
      setLoading(false);
    }
  };

  // 수락 / 거절
  const acceptFollow = async () => {
    if (!followId) return;
    setLoading(true);
    try {
      await axiosInstance.post(`/api/v1/follows/${followId}/accept`);
      onActionCompleted?.();
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const rejectFollow = async () => {
    if (!followId) return;
    setLoading(true);
    try {
      await axiosInstance.delete(`/api/v1/follows/${followId}/reject`);
      onActionCompleted?.();
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const renderActionButton = () => {
    if (!myUserId || myUserId === userId) return null; // 본인 프로필은 버튼 안 나옴
    if (loading) return <button disabled className="mt-6 px-4 py-2 bg-gray-200 rounded">처리 중...</button>;

    switch (userType) {
      case 'received':
        if (!followId) return null;
        return (
          <div className="flex gap-2 mt-6">
            <button onClick={acceptFollow} className="px-4 py-2 bg-green-500 text-white rounded">수락</button>
            <button onClick={rejectFollow} className="px-4 py-2 bg-red-500 text-white rounded">거절</button>
          </div>
        );
      case 'sent':
        return <button onClick={toggleFollow} className="mt-6 px-4 py-2 bg-red-500 text-white rounded">요청 취소</button>;
      case 'following':
        return <button onClick={toggleFollow} className="mt-6 px-4 py-2 border rounded-md">언팔로우</button>;
      case 'followers':
      case 'profile':
        return <button onClick={toggleFollow} className="mt-6 px-4 py-2 border rounded-md">{isFollowing ? '언팔로우' : '팔로우'}</button>;
      default:
        return null;
    }
  };

  if (error) return <div className="text-center text-red-500">{error}</div>;
  if (!profile) return <div className="text-center text-gray-500">⏳ 로딩 중...</div>;

  return (
    <div
      className="w-full bg-white p-6 rounded-3xl shadow-md border border-black flex flex-col items-center text-center cursor-pointer"
      onClick={() => router.push(`/profile/${userId}`)}
    >
      <div
        className="w-24 h-24 md:w-36 md:h-36 mb-4 rounded-full bg-center bg-cover flex-shrink-0"
        style={{ backgroundImage: `url(${profile.profileImageUrl || '/images/no-image.png'})` }}
      />
      <h2 className="text-2xl md:text-3xl font-semibold text-gray-800 mb-1">{profile.nickname}</h2>
      <p className="text-sm text-gray-600 mb-2">{profile.bio || '소개글이 없습니다.'}</p>
      <hr className="my-4 w-full" />
      {renderActionButton()}
    </div>
  );
}