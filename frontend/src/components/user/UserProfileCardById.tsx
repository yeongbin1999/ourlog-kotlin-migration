'use client';

import React, { useEffect, useState } from 'react';
import UserProfileCard from './UserProfileCard';
import { axiosInstance } from '@/lib/api-client';

type UserData = {
  userId: number;
  nickname: string;
  email: string;
  bio: string;
  profileImageUrl: string;
  isFollowing: boolean;
  followId?: number;
};

type ActionType = 'none' | 'accept_reject' | 'cancel' | 'unfollow';

type Props = {
  userId: number;
  actionType?: ActionType;
};

export default function UserProfileCardById({ userId, actionType = 'none' }: Props) {
  const [user, setUser] = useState<UserData | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let ignore = false;
    setLoading(true);
    axiosInstance.get(`/api/v1/users/${userId}`)
      .then((res) => {
        const d = res.data?.data ?? res.data;
        if (!ignore) {
          setUser({
            userId: d.userId,
            nickname: d.nickname,
            email: d.email,
            bio: d.bio ?? '',
            profileImageUrl: d.profileImageUrl ?? '',
            isFollowing: d.isFollowing,
            followId: d.followId,
          });
        }
      })
      .finally(() => !ignore && setLoading(false));
    return () => { ignore = true; };
  }, [userId]);

  if (loading) return <div className="p-6 text-center text-gray-500">프로필 불러오는 중...</div>;
  if (!user)   return <div className="p-6 text-center text-gray-500">사용자 정보를 찾을 수 없습니다.</div>;
  const mappedUserType = (() => {
    switch (actionType) {
      case 'accept_reject':
        return 'received';
      case 'cancel':
        return 'sent';
      case 'unfollow':
        return 'following';
      case 'none':
        return 'followers';
      default:
        return 'profile'; // Default or handle other cases
    }
  })();

  return (
    <UserProfileCard
      userId={String(user.userId)}
      userType={mappedUserType}
      followId={user.followId}
      isFollowing={user.isFollowing}
    />
  );
}
