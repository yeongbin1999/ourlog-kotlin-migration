'use client';

import { useRouter } from "next/navigation";
import React, { useEffect, useState } from 'react';
import { axiosInstance } from '@/lib/api-client';
import { useAuthStore } from '@/stores/authStore';

type Props = {
  userId: string;
  onActionCompleted?: () => void;
};

type UserProfile = {
  email: string;
  nickname: string;
  profileImageUrl: string;
  bio: string;
};

type FollowStatus = 'none' | 'pending' | 'accepted';

export default function UserProfileCard({ userId, onActionCompleted }: Props) {
  const router = useRouter();
  const { user: me } = useAuthStore();
  const myUserId = me?.id ? Number(me.id) : null;

  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [followStatus, setFollowStatus] = useState<FollowStatus>('none');
  const [followId, setFollowId] = useState<number | null>(null);
  const [loading, setLoading] = useState(false);

  const isMe = myUserId === Number(userId);

  // 프로필 데이터 로드
  useEffect(() => {
    axiosInstance.get(`/api/v1/users/${userId}`)
      .then(res => setProfile(res.data.data))
      .catch(() => setError('존재하지 않는 사용자입니다.'));
  }, [userId]);

  // 팔로우 상태 로드
  useEffect(() => {
    const fetchFollowStatus = async () => {
      if (!myUserId || isMe) return;

      try {
        // 팔로잉 목록
        const followingRes = await axiosInstance.get(`/api/v1/follows/followings?userId=${myUserId}`);
        const followings = followingRes.data.data ?? [];

        // API 응답 구조에 맞게 필드 확인 (예: followeeId)
        if (followings.some((f: any) => f.followeeId === Number(userId))) {
          setFollowStatus('accepted');
          setFollowId(followings.find((f: any) => f.followeeId === Number(userId))?.id ?? null);
          return;
        }

        // 보낸 팔로우 요청
        const sentRes = await axiosInstance.get(`/api/v1/follows/sent-requests`);
        const sent = sentRes.data.data ?? [];
        if (sent.some((f: any) => f.followeeId === Number(userId))) {
          setFollowStatus('pending');
          setFollowId(sent.find((f: any) => f.followeeId === Number(userId))?.id ?? null);
          return;
        }

        setFollowStatus('none');
        setFollowId(null);
      } catch (err) {
        console.error('팔로우 상태 불러오기 실패', err);
      }
    };

    fetchFollowStatus();
  }, [myUserId, userId, isMe]);

  const handleAction = async () => {
    if (!myUserId || isMe || loading) return;
    setLoading(true);

    try {
      switch (followStatus) {
        case 'none': {
          // 팔로우 요청 보내기
          const res = await axiosInstance.post(`/api/v1/follows/${userId}`);
          setFollowStatus('pending');
          setFollowId(res.data.data?.id ?? null); // followId 저장
          break;
        }
        case 'pending': {
          // 요청 취소
          if (!followId) break;
          await axiosInstance.delete(`/api/v1/follows/${followId}`);
          setFollowStatus('none');
          setFollowId(null);
          break;
        }
        case 'accepted': {
          // 언팔로우
          if (!followId) break;
          await axiosInstance.delete(`/api/v1/follows/${followId}`);
          setFollowStatus('none');
          setFollowId(null);
          break;
        }
      }

      onActionCompleted?.();
    } catch (err) {
      console.error(err);
      alert('요청 처리 실패');
    } finally {
      setLoading(false);
    }
  };

  const renderButton = () => {
    if (isMe) return null;
    if (loading) return <button disabled className="px-4 py-2 bg-gray-200 rounded">처리 중...</button>;

    let text = '팔로우';
    let style = 'bg-sky-500 text-white hover:bg-sky-600';
    if (followStatus === 'pending') {
      text = '요청 취소';
      style = 'bg-gray-300 text-gray-800 hover:bg-gray-400';
    } else if (followStatus === 'accepted') {
      text = '언팔로우';
      style = 'bg-gray-300 text-gray-800 hover:bg-gray-400';
    }

    return (
      <button onClick={handleAction} className={`px-4 py-2 rounded ${style}`}>
        {text}
      </button>
    );
  };

  if (error) return <div className="text-center text-black text-lg mt-10">{error}</div>;
  if (!profile) return <div className="text-center">⏳ 로딩 중...</div>;

  return (
    <div
      className="w-full max-w-sm bg-white p-6 rounded-3xl shadow-md border border-black mx-auto flex flex-col items-center text-center"
    >
      <div
        className="w-20 h-20 mb-4 rounded-full bg-center bg-cover"
        style={{ backgroundImage: `url(${profile.profileImageUrl})` }}
      />
      <h2 className="text-2xl font-bold mb-1">{profile.nickname}</h2>
      <p className="text-sm text-gray-600 mb-2">{profile.bio}</p>
      <hr className="my-4 w-full" />
      <ul className="space-y-2 text-sm text-gray-600 w-full text-left pl-4 ml-28">
        <li>Email: {profile.email}</li>
      </ul>
      {renderButton()}
    </div>
  );
}