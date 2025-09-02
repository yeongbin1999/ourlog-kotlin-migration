'use client';

import React, { useEffect, useState } from 'react';
import UserProfileCard from './UserProfileCard';
import { axiosInstance } from '@/lib/api-client';
import { unwrapList } from '@/lib/unwrap';

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
  followId: number;
};

export default function FollowRequestList({ myUserId, onActionCompleted }: Props) {
  const [requests, setRequests] = useState<FollowUserResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchRequests = async () => {
    try {
      setLoading(true);
      setError(null);
      const res = await axiosInstance.get(`/api/v1/follows/requests?userId=${myUserId}`);
      setRequests(unwrapList<FollowUserResponse>(res.data)); // ✅ 항상 배열 보장
    } catch (err) {
      console.error('받은 요청 불러오기 실패', err);
      setError('요청 목록을 불러오지 못했습니다.');
      setRequests([]); // ✅ 실패 시에도 배열 유지
    } finally {
      setLoading(false);
    }
  };

  const handleRefresh = () => {
    onActionCompleted?.();
    fetchRequests();
  };

  useEffect(() => {
    if (myUserId) fetchRequests();
  }, [myUserId]);

  if (loading) return <div className="text-center mt-10">로딩 중...</div>;
  if (error) return <div className="text-center mt-10 text-red-600">{error}</div>;
  if (!requests.length) return <div className="text-center mt-10">받은 요청이 없습니다.</div>;

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
