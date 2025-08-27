// FollowRequestList.tsx
'use client';

import React, { useEffect, useState } from 'react';
import UserProfileCard from './UserProfileCard'; // 상대 프로필 카드 (공용 컴포넌트)
import { axiosInstance } from '@/lib/api-client';

type Props = {
  myUserId: number;
  onActionCompleted?: () => void;
};

type FollowUserResponse = {
  userId: number;
  nickname: string;
  email: string;
  bio: string;
  profileImageUrl: string;
  followId: number; // 수락/거절용 ID..
};

// 나에게 팔로우 요청한 사람들..
export default function FollowRequestList({ myUserId, onActionCompleted }: Props) {
  const [requests, setRequests] = useState<FollowUserResponse[]>([]);
  const [loading, setLoading] = useState(true);

  const fetchRequests = async () => {
    console.log("Fetching follow requests...");
    try {
      const res = await axiosInstance.get(`/api/v1/follows/requests?userId=${myUserId}`);
      console.log("Follow Requests API Response:", res.data);
      setRequests(res.data);
    } catch (err) {
      console.error('받은 요청 불러오기 실패', err);
    } finally {
      setLoading(false);
    }
  };

  const handleRefresh = () => {
    fetchRequests();
    onActionCompleted?.(); // 액션 완료 후 콜백 호출
  };

  useEffect(() => {
    fetchRequests();
  }, []);

  if (loading) return <div className="text-center mt-10">로딩 중...</div>;
  if (requests.length === 0) return <div className="text-center mt-10"> 받은 요청이 없습니다.</div>;

  return (
    <div className="space-y-6">
      {requests.map((user) => (
        <UserProfileCard
          key={user.userId}
          userId={String(user.userId)}
          userType="received"
          followId={user.followId}
          onActionCompleted={handleRefresh}
        />
      ))}
    </div>
  );
}
