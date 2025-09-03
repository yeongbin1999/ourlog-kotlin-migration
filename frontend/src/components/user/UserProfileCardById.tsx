'use client';

import React, { useEffect, useState, useMemo } from 'react';
import { useRouter } from 'next/navigation';
import { axiosInstance } from '@/lib/api-client';
import { useAuthStore } from '@/stores/authStore';

type Props = { userId: number };

type UserProfileData = {
  userId: number;
  nickname: string;
  bio?: string;
  profileImageUrl?: string;
  isFollowing: boolean;           // 내가 팔로우하고 있는 상태
  followId?: number;              // 팔로우 관계 ID
  isFollowRequestSent?: boolean;  // 내가 상대에게 팔로우 요청 보낸 상태
};

export default function UserProfileCardById({ userId }: Props) {
  const router = useRouter();
  const { user: myUser } = useAuthStore();
  const myUserId = myUser?.id;

  const [profile, setProfile] = useState<UserProfileData | null>(null);
  const [loading, setLoading] = useState(false);

  // 서버에서 유저 정보 가져오기
  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const res = await axiosInstance.get(`/api/v1/users/${userId}`);
        const data = res.data.data ?? res.data;
        setProfile({
          userId: data.userId,
          nickname: data.nickname,
          bio: data.bio,
          profileImageUrl: data.profileImageUrl,
          isFollowing: data.isFollowing ?? false,
          followId: data.followId,
          isFollowRequestSent: data.isFollowRequestSent ?? false,
        });
      } catch (err) {
        console.error(err);
      }
    };
    fetchProfile();
  }, [userId]);

  if (!profile) return <div>로딩중...</div>;

  const isMe = Number(myUserId) === profile.userId;

  // 팔로우 / 요청 취소 / 언팔로우 처리
  const handleFollow = async () => {
    if (!profile || loading) return;

    setLoading(true);
    const prevProfile = { ...profile };

    try {
      if (profile.isFollowing) {
        // 언팔로우
        setProfile({ ...profile, isFollowing: false, followId: undefined });
        if (profile.followId) await axiosInstance.delete(`/api/v1/follows/${profile.followId}`);
      } else if (profile.isFollowRequestSent) {
        // 팔로우 요청 취소
        setProfile({ ...profile, isFollowRequestSent: false, followId: undefined });
        if (profile.followId) await axiosInstance.delete(`/api/v1/follows/${profile.followId}`);
      } else {
        // 팔로우 요청 보내기
        setProfile({ ...profile, isFollowRequestSent: true });
        const res = await axiosInstance.post(`/api/v1/follows/${profile.userId}`);
        const data = res.data.data ?? {};
        setProfile(prev => ({ ...prev!, followId: data.followId ?? prev!.followId }));
      }
    } catch (err) {
      console.error(err);
      setProfile(prevProfile);
      alert('요청 처리 중 오류가 발생했습니다.');
    } finally {
      setLoading(false);
    }
  };

  // 버튼 렌더링
  const renderButton = () => {
    if (isMe) return null;

    if (loading) return <button className="mt-4 px-4 py-2 rounded border">처리중...</button>;

    if (profile.isFollowing) {
      return (
        <button
          onClick={e => { e.stopPropagation(); handleFollow(); }}
          className="mt-4 px-4 py-2 rounded border"
        >
          언팔로우
        </button>
      );
    }

    if (profile.isFollowRequestSent) {
      return (
        <button
          onClick={e => { e.stopPropagation(); handleFollow(); }}
          className="mt-4 px-4 py-2 rounded bg-gray-300"
        >
          요청 취소
        </button>
      );
    }

    return (
      <button
        onClick={e => { e.stopPropagation(); handleFollow(); }}
        className="mt-4 px-4 py-2 rounded bg-blue-500 text-white"
      >
        팔로우
      </button>
    );
  };

  return (
    <div
      className="w-full p-6 bg-white rounded-xl shadow-md text-center cursor-pointer"
      onClick={() => router.push(`/profile/${profile.userId}`)}
    >
      <div
        className="w-24 h-24 mb-4 rounded-full bg-center bg-cover mx-auto"
        style={{ backgroundImage: `url(${profile.profileImageUrl || '/images/no-image.png'})` }}
      />
      <h2 className="text-xl font-semibold">{profile.nickname}</h2>
      <p className="text-gray-500">{profile.bio || '소개글 없음'}</p>

      {!isMe && renderButton()}
    </div>
  );
}