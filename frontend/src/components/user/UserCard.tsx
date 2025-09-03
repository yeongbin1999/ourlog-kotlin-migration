'use client';

import React, { useEffect, useState } from 'react';
import { axiosInstance } from '@/lib/api-client';
import { useAuthStore } from '@/stores/authStore';
import { useQueryClient } from '@tanstack/react-query';

type Props = {
  userId: number;
  userType: 'profile' | 'followers' | 'following' | 'sent' | 'received';
  initialFollowId?: number | null;
  initialFollowStatus?: 'none' | 'pending' | 'accepted';
  onActionCompleted?: () => void;
};

type UserProfile = {
  nickname: string;
  bio?: string;
  profileImageUrl?: string;
  email?: string;
};

type FollowStatus = 'none' | 'pending' | 'accepted';

export default function UserCard({
  userId,
  userType,
  initialFollowId = null,
  initialFollowStatus = 'none',
  onActionCompleted,
}: Props) {
  const { user: me } = useAuthStore();
  const myUserId = me?.id;
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [followStatus, setFollowStatus] = useState<FollowStatus>(initialFollowStatus);
  const [followId, setFollowId] = useState<number | null>(initialFollowId);
  const [loading, setLoading] = useState(false);
  const isMe = Number(myUserId) === Number(userId);
  const queryClient = useQueryClient();

  useEffect(() => {
    let ignore = false;
    const fetchProfile = async () => {
      try {
        const res = await axiosInstance.get(`/api/v1/users/${userId}`);
        const data = res.data?.data ?? res.data;
        if (!ignore) {
          setProfile({
            nickname: data.nickname,
            bio: data.bio ?? '',
            profileImageUrl: data.profileImageUrl ?? '',
            email: data.email,
          });
        }
      } catch (err) {
        console.error(err);
      }
    };
    fetchProfile();
    return () => { ignore = true; };
  }, [userId]);

  const handleAction = async (
    action: 'follow' | 'unfollow' | 'cancel' | 'accept' | 'reject'
  ) => {
    if (!myUserId || isMe || loading) return;
    setLoading(true);
    try {
      switch (action) {
        case 'follow': {
          const res = await axiosInstance.post(`/api/v1/follows/${userId}`);
          setFollowStatus('pending');
          setFollowId(res.data.data?.followId ?? null);
          break;
        }
        case 'unfollow': {
            // followId가 아니라 userId 사용
            await axiosInstance.delete(`/api/v1/follows/${userId}`);
            setFollowStatus('none');
            setFollowId(null);
            break;
        }
          case 'cancel': {
            // followId가 아니라 userId 사용
            await axiosInstance.delete(`/api/v1/follows/${userId}/cancel`);
            setFollowStatus('none');
            setFollowId(null);
            break;
        }
        case 'accept': {
          if (!followId) break;
          await axiosInstance.post(`/api/v1/follows/${followId}/accept`);
          setFollowStatus('accepted');
          break;
        }
        case 'reject': {
          if (!followId) break;
          await axiosInstance.delete(`/api/v1/follows/${followId}/reject`);
          setFollowStatus('none');
          setFollowId(null);
          break;
        }
      }
      queryClient.invalidateQueries({ queryKey: ['follows'] });
      onActionCompleted?.();
    } catch (err) {
      console.error(err);
      alert('요청 처리 실패');
    } finally {
      setLoading(false);
    }
  };

  const renderActionButton = () => {
    if (isMe) return null;
    if (loading) return <button disabled className="px-4 py-2 bg-gray-200 rounded">처리 중...</button>;

    switch (userType) {
      case 'received':
        return (
          <div className="flex gap-2">
            <button onClick={() => handleAction('accept')} className="px-4 py-2 bg-green-300 text-white rounded hover:bg-green-400">수락</button>
            <button onClick={() => handleAction('reject')} className="px-4 py-2 bg-red-300 text-white rounded hover:bg-red-400">거절</button>
          </div>
        );
      case 'sent':
        return (
          <button onClick={() => handleAction('cancel')} className="px-4 py-2 bg-gray-300 text-gray-800 rounded hover:bg-gray-400">요청 취소</button>
        );
      case 'following':
        return (
          <button onClick={() => handleAction('unfollow')} className="px-4 py-2 bg-gray-300 text-gray-800 rounded hover:bg-gray-400">언팔로우</button>
        );
      case 'followers':
      case 'profile':
        return (
          <button
            onClick={() => {
              if (followStatus === 'accepted') handleAction('unfollow');
              else if (followStatus === 'pending') handleAction('cancel');
              else handleAction('follow');
            }}
            className={`px-4 py-2 rounded ${
              followStatus === 'accepted' || followStatus === 'pending'
                ? 'bg-gray-300 text-gray-800 hover:bg-gray-400'
                : 'bg-sky-300 text-white hover:bg-sky-400'
            }`}
          >
            {followStatus === 'accepted' ? '언팔로우' : followStatus === 'pending' ? '요청 취소' : '팔로우'}
          </button>
        );
      default:
        return null;
    }
  };

  if (!profile) return <div className="text-center text-gray-500">⏳ 로딩 중...</div>;

  return (
    <div className="w-full p-4 rounded-xl shadow bg-white flex items-center gap-4">
      <div className="w-20 h-20 md:w-24 md:h-24 rounded-full bg-cover bg-center flex-shrink-0" style={{ backgroundImage: `url(${profile.profileImageUrl || '/images/no-image.png'})` }} />
      <div className="flex-1 flex flex-col justify-center">
        <h2 className="text-lg font-semibold">{profile.nickname}</h2>
        <p className="text-sm text-gray-600">{profile.bio || '소개글이 없습니다.'}</p>
      </div>
      <div className="flex-shrink-0">{renderActionButton()}</div>
    </div>
  );
}