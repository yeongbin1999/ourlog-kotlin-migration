'use client';

import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { axiosInstance } from '@/lib/api-client';
import UserCard from './UserCard';

type Following = {
  userId: number;
  followId: number | null;
};

type Props = {
  myUserId: number;
  onActionCompleted?: () => void;
};

export default function FollowingList({ myUserId, onActionCompleted }: Props) {
  const { data: followings = [], refetch } = useQuery<Following[]>({
    queryKey: ['follows', 'followings', myUserId],
    queryFn: async () => {
      const res = await axiosInstance.get('/api/v1/follows/followings', { params: { userId: myUserId } });
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
      {followings.map(f => (
        <UserCard
          key={f.userId}
          userId={f.userId}
          userType="following"
          initialFollowId={f.followId}
          onActionCompleted={handleActionCompleted}
        />
      ))}
    </div>
  );
}