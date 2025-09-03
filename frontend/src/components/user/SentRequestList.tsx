'use client';

import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { axiosInstance } from '@/lib/api-client';
import UserCard from './UserCard';

type SentRequest = {
  userId: number;
  followId: number;
};

type Props = {
  myUserId: number;
  onActionCompleted?: () => void;
};

export default function SentRequestList({ myUserId, onActionCompleted }: Props) {
  const { data: sentRequests = [], refetch } = useQuery<SentRequest[]>({
    queryKey: ['follows', 'sent-requests', myUserId],
    queryFn: async () => {
      const res = await axiosInstance.get('/api/v1/follows/sent-requests', { params: { userId: myUserId } });
      return res.data.data ?? [];
    },
    enabled: !!myUserId,
  });

  const handleActionCompleted = () => {
    refetch();
    onActionCompleted?.();
  };

  return (
    <div className="space-y-4">
      {sentRequests.map(s => (
        <UserCard
          key={s.followId}
          userId={s.userId}
          userType="sent"
          initialFollowId={s.followId}
          onActionCompleted={handleActionCompleted}
        />
      ))}
    </div>
  );
}