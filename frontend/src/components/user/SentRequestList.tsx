// src/components/user/SentRequestList.tsx
'use client';

import React, { useEffect, useState } from 'react';
import axios from 'axios';
import UserProfileCard from './UserProfileCard';
import { axiosInstance } from '@/lib/api-client';

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

// 내가 보낸 팔로우 요청..
export default function SentRequestList({ myUserId, onActionCompleted }: Props) {
  const [sentRequests, setSentRequests] = useState<SentUserResponse[]>([]);
  const [loading, setLoading] = useState(true);

  const fetchSentRequests = async () => {
    console.log("Fetching sent requests...");
    try {
      const res = await axiosInstance.get(`/api/v1/follows/sent-requests?userId=${myUserId}`);
      console.log("Sent Requests API Response:", res.data); // 추가된 로그
      const data = Array.isArray(res.data) ? res.data : res.data ?? [];
      setSentRequests(data);
    } catch (err) {
      console.error('보낸 요청 불러오기 실패', err);
    } finally {
      setLoading(false);
    }
  };

  const handleCancelRequest = async (targetUserId: number) => {
    try {
      await axiosInstance.delete(`/api/v1/follows/${targetUserId}?myUserId=${myUserId}`);
      alert('요청이 취소되었습니다.');
      fetchSentRequests(); // 리스트 갱신
      onActionCompleted?.(); // 액션 완료 후 콜백 호출
    } catch (err) {
      console.error('요청 취소 실패', err);
      alert('요청 취소 중 오류가 발생했습니다.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchSentRequests();
  }, []);

  if (loading) return <div className="text-center mt-10">로딩 중...</div>;
  if (sentRequests.length === 0) return <div className="text-center mt-10">보낸 요청이 없습니다.</div>;

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