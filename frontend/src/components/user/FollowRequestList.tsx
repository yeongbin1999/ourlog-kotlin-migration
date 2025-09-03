'use client';

import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { axiosInstance } from '@/lib/api-client';
import UserCard from './UserCard';

type FollowRequest = {
  userId: number;
  followId: number;
};

type Props = {
  myUserId: number;
  onActionCompleted?: () => void;
};

export default function FollowRequestList({ myUserId, onActionCompleted }: Props) {
  const { data: requests = [], refetch } = useQuery<FollowRequest[]>({
    queryKey: ['follows', 'requests', myUserId],
    queryFn: async () => {
      const res = await axiosInstance.get(`/api/v1/follows/requests`, { params: { userId: myUserId } });
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
      {requests.map(r => (
        <UserCard
          key={r.followId}
          userId={r.userId}
          userType="received"
          initialFollowId={r.followId}
          onActionCompleted={handleActionCompleted}
        />
      ))}
    </div>
  );
}