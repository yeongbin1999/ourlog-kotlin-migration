'use client';

import React, { useEffect, useState } from 'react';
import { axiosInstance } from '@/lib/api-client';
import UserProfileCard from './UserProfileCard';

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

  const fetchFollowings = async () => {
    console.log("Fetching followings...");
    try {
      const res = await axiosInstance.get(`/api/v1/follows/followings?userId=${myUserId}`);
      const data = Array.isArray(res.data) ? res.data : res.data ?? [];
      setFollowings(data);
    } catch (err) {
      console.error('팔로잉 목록 불러오기 실패', err);
    } finally {
      setLoading(false);
    }
  };

  const handleRefresh = () => {
    fetchFollowings();
    onActionCompleted?.(); // 액션 완료 후 콜백 호출
  };

  useEffect(() => {
    if (myUserId) {
      fetchFollowings();
    }
  }, [myUserId]);

  if (loading) return <div className="text-center mt-10">로딩 중...</div>;
  if (followings.length === 0) return <div className="text-center mt-10">아직 팔로잉한 유저가 없습니다.</div>;

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