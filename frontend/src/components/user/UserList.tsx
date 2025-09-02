'use client';

import React, { useEffect, useState, useCallback } from 'react';
import { axiosInstance } from '@/lib/api-client';
import { unwrapList } from '@/lib/unwrap';
import UserProfileCard, { UserData, ActionType } from './UserProfileCard';

type Props = {
  myUserId: number;
  endpoint: string;
  actionType: ActionType;
  onActionCompleted?: () => void;
  emptyMessage: string;
};

export default function UserList({ myUserId, endpoint, actionType, onActionCompleted, emptyMessage }: Props) {
  const [users, setUsers] = useState<UserData[]>([]);
  const [loading, setLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState<number | null>(null);
  const [error, setError] = useState<string | null>(null);

  const fetchData = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await axiosInstance.get(endpoint);
      setUsers(unwrapList<UserData>(res.data));
    } catch (err) {
      console.error('목록 불러오기 실패:', err);
      setError('목록을 불러오는 데 실패했습니다.');
      setUsers([]);
    } finally {
      setLoading(false);
    }
  }, [endpoint]);

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  const handleAction = async (
    action: 'follow' | 'unfollow' | 'cancel' | 'accept' | 'reject',
    targetUserId: number,
    followId?: number
  ) => {
    setActionLoading(targetUserId);
    try {
      switch (action) {
        case 'unfollow':
        case 'cancel':
          await axiosInstance.delete(`/api/v1/follows/${targetUserId}`);
          break;
        case 'follow':
          await axiosInstance.post(`/api/v1/follows/${targetUserId}`);
          break;
        case 'accept':
          await axiosInstance.post(`/api/v1/follows/${followId}/accept`);
          break;
        case 'reject':
          await axiosInstance.delete(`/api/v1/follows/${followId}/reject`);
          break;
      }
      onActionCompleted?.();
      fetchData();
    } catch (err) {
      console.error('액션 처리 실패:', err);
      alert('요청 처리 중 오류가 발생했습니다.');
    } finally {
      setActionLoading(null);
    }
  };

  if (loading) return <div className="text-center py-10">목록을 불러오는 중...</div>;
  if (error) return <div className="text-center py-10 text-red-500">{error}</div>;
  if (users.length === 0) return <div className="text-center py-10 text-gray-500">{emptyMessage}</div>;

  return (
    <div className="space-y-4">
      {users.map((user) => (
        <UserProfileCard
          key={user.userId}
          user={user}
          actionType={actionType}
          onAction={handleAction}
          isLoading={actionLoading === user.userId}
        />
      ))}
    </div>
  );
}