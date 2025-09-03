'use client';

import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { axiosInstance } from '@/lib/api-client';
import UserCard from './UserCard';

type Follower = {
  userId: number;
  followId: number | null;
};

type Props = {
  myUserId: number;
  onActionCompleted?: () => void;
};

export default function FollowerList({ myUserId, onActionCompleted }: Props) {
  const { data: followers = [], refetch } = useQuery<Follower[]>({
    queryKey: ['follows', 'followers', myUserId],
    queryFn: async () => {
      const res = await axiosInstance.get('/api/v1/follows/followers', { params: { userId: myUserId } });
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
      {followers.map(f => (
        <UserCard
          key={f.userId}
          userId={f.userId}
          userType="followers"
          initialFollowId={f.followId}
          onActionCompleted={handleActionCompleted}
        />
      ))}
    </div>
  );
}