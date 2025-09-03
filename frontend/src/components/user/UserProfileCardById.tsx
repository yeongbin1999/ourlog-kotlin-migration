'use client';

import React, { useEffect, useState } from 'react';
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
  isFollowRequestReceived?: boolean; // 상대가 나에게 팔로우 요청 보낸 상태
  isFollowRequestSent?: boolean;      // 내가 상대에게 팔로우 요청 보낸 상태
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
          isFollowRequestReceived: data.isFollowRequestReceived ?? false,
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

  // 팔로우 / 취소 / 언팔로우 상태 토글
  const handleFollow = async () => {
    if (!profile) return;
    setLoading(true);
    try {
      if (profile.isFollowing) {
        // 언팔로우
        await axiosInstance.delete(`/api/v1/follows/${profile.userId}`);
        setProfile(prev => prev ? { ...prev, isFollowing: false, followId: undefined } : prev);
      } else if (profile.isFollowRequestSent) {
        // 팔로우 요청 취소
        if (profile.followId) {
          await axiosInstance.delete(`/api/v1/follows/${profile.followId}`);
          setProfile(prev => prev ? { ...prev, isFollowRequestSent: false, followId: undefined } : prev);
        }
      } else {
        // 팔로우 요청
        const res = await axiosInstance.post(`/api/v1/follows/${profile.userId}`);
        const data = res.data.data ?? {};
        setProfile(prev => prev ? {
          ...prev,
          isFollowRequestSent: true,
          followId: data.followId ?? prev.followId,
        } : prev);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  // 팔로우 요청 수락
  const handleAccept = async () => {
    if (!profile || !profile.followId) return;
    setLoading(true);
    try {
      await axiosInstance.post(`/api/v1/follows/${profile.followId}/accept`);
      setProfile(prev => prev ? {
        ...prev,
        isFollowing: true,
        isFollowRequestReceived: false
      } : prev);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  // 팔로우 요청 거절
  const handleReject = async () => {
    if (!profile || !profile.followId) return;
    setLoading(true);
    try {
      await axiosInstance.post(`/api/v1/follows/${profile.followId}/reject`);
      setProfile(prev => prev ? {
        ...prev,
        isFollowRequestReceived: false,
        followId: undefined
      } : prev);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  // 버튼 렌더링
  const renderButton = () => {
    if (loading) return <button className="mt-4 px-4 py-2 rounded border">처리중...</button>;

    if (profile.isFollowRequestReceived) {
      return (
        <div className="flex justify-center gap-2 mt-4">
          <button
            onClick={e => { e.stopPropagation(); handleAccept(); }}
            className="px-4 py-2 rounded bg-green-500 text-white"
          >수락</button>
          <button
            onClick={e => { e.stopPropagation(); handleReject(); }}
            className="px-4 py-2 rounded bg-red-500 text-white"
          >거절</button>
        </div>
      );
    }

    if (profile.isFollowing) {
      return (
        <button
          onClick={e => { e.stopPropagation(); handleFollow(); }}
          className="mt-4 px-4 py-2 rounded border"
        >언팔로우</button>
      );
    }

    if (profile.isFollowRequestSent) {
      return (
        <button
          onClick={e => { e.stopPropagation(); handleFollow(); }}
          className="mt-4 px-4 py-2 rounded bg-gray-300"
        >요청 취소</button>
      );
    }

    return (
      <button
        onClick={e => { e.stopPropagation(); handleFollow(); }}
        className="mt-4 px-4 py-2 rounded bg-blue-500 text-white"
      >팔로우</button>
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