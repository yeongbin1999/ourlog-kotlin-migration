'use client';

import React, { useEffect, useState } from 'react';
import { axiosInstance } from '@/lib/api-client';
import UserProfileCard from './UserProfileCard';
import { unwrapList } from '@/lib/unwrap';

type Props = {
  myUserId: number;
  onActionCompleted?: () => void;
};

type FollowingUserResponse = {
  userId: number;
  nickname: string;
  email: string;
  bio: string;
  profileImageUrl: string;
  isFollowing: boolean;
  followId: number;
};

export default function FollowingList({ myUserId, onActionCompleted }: Props) {
  const [followings, setFollowings] = useState<FollowingUserResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchFollowings = async () => {
    try {
      setLoading(true);
      setError(null);
      const res = await axiosInstance.get(`/api/v1/follows/followings?userId=${myUserId}`);
      setFollowings(unwrapList<FollowingUserResponse>(res.data));
    } catch (err) {
      console.error('팔로잉 목록 불러오기 실패', err);
      setError('팔로잉 목록을 불러오지 못했습니다.');
      setFollowings([]);
    } finally {
      setLoading(false);
    }
  };

  const handleRefresh = () => {
    onActionCompleted?.();
    fetchFollowings();
  };

  useEffect(() => {
    if (myUserId) fetchFollowings();
  }, [myUserId]);

  if (loading) return <div className="text-center mt-10">로딩 중...</div>;
  if (error) return <div className="text-center mt-10 text-red-600">{error}</div>;
  if (!followings.length) return <div className="text-center mt-10">아직 팔로잉한 유저가 없습니다.</div>;

  return (
    <div className="space-y-6">
      {followings.map((user) => (
        <UserProfileCard
          key={user.userId}
          userId={String(user.userId)}
          userType="following"
          followId={user.followId}
          isFollowing={user.isFollowing}
          onActionCompleted={handleRefresh}
        />
      ))}
    </div>
  );
}
