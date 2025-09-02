'use client';

import React, { useEffect, useState } from 'react';
import UserProfileCard from './UserProfileCard';
import { axiosInstance } from '@/lib/api-client';
import { unwrapList } from '@/lib/unwrap';

type Props = {
  myUserId: number;
  onActionCompleted?: () => void;
};

type SentUserResponse = {
  userId: number;
  nickname: string;
  email: string;
  bio: string;
  profileImageUrl: string;
};

export default function SentRequestList({ myUserId, onActionCompleted }: Props) {
  const [sentRequests, setSentRequests] = useState<SentUserResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const fetchSentRequests = async () => {
    try {
      setLoading(true);
      setError(null);
      const res = await axiosInstance.get(`/api/v1/follows/sent-requests?userId=${myUserId}`);
      setSentRequests(unwrapList<SentUserResponse>(res.data));
    } catch (err) {
      console.error('보낸 요청 불러오기 실패', err);
      setError('보낸 요청을 불러오지 못했습니다.');
      setSentRequests([]);
    } finally {
      setLoading(false);
    }
  };

  const handleCancelRequest = async (targetUserId: number) => {
    try {
      await axiosInstance.delete(`/api/v1/follows/${targetUserId}?myUserId=${myUserId}`);
      alert('요청이 취소되었습니다.');
      fetchSentRequests(); // 리스트 갱신
      onActionCompleted?.(); // 상위 카운트 갱신
    } catch (err) {
      console.error('요청 취소 실패', err);
      alert('요청 취소 중 오류가 발생했습니다.');
    }
  };

  useEffect(() => {
    if (myUserId) fetchSentRequests();
  }, [myUserId]);

  if (loading) return <div className="text-center mt-10">로딩 중...</div>;
  if (error) return <div className="text-center mt-10 text-red-600">{error}</div>;
  if (!sentRequests.length) return <div className="text-center mt-10">보낸 요청이 없습니다.</div>;

  return (
    <div className="space-y-6">
      {sentRequests.map((user) => (
        <UserProfileCard
          key={user.userId}
          userId={String(user.userId)}
          userType="sent"
          onActionCompleted={() => handleCancelRequest(user.userId)}
        />
      ))}
    </div>
  );
}
