'use client';

import React, { useMemo, useState } from 'react'; // Added useState
import { useRouter } from 'next/navigation';
import { axiosInstance } from '@/lib/api-client';
import { useAuthStore } from '@/stores/authStore';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';

type PublicUser = {
  userId: number;
  email?: string;
  nickname: string;
  bio?: string;
  profileImageUrl?: string | null;
  isFollowing?: boolean;
  followId?: number;
  followersCount?: number;
  followingsCount?: number;
};

type Props = {
  userId: number;
  onChanged?: () => void;
  relationType?: 'profile' | 'follower' | 'following' | 'received-request' | 'sent-request';
  followId?: number;
};

export default function PublicProfileHeader({
  userId,
  onChanged,
  relationType = 'profile',
}: Props) {
  const { user: me, isAuthenticated } = useAuthStore();
  const router = useRouter();
  const queryClient = useQueryClient();
  const [isRequestPending, setIsRequestPending] = useState(false); // New state

  const isMe = useMemo(() => {
    if (!me || typeof me.id === 'undefined') return false;
    return Number(me.id) === Number(userId);
  }, [me?.id, userId]);

  const { data: user, isLoading: userLoading, error } = useQuery<PublicUser>({
    queryKey: ['users', userId, 'profile'],
    queryFn: async () => {
      const res = await axiosInstance.get(`/api/v1/users/${userId}`);
      const d = res.data?.data ?? res.data;
      return {
        ...d,
        bio: d.bio ?? '',
        profileImageUrl: d.profileImageUrl ?? '',
      };
    },
    enabled: isAuthenticated !== null,
  });

  const { data: diaryCount = 0, isLoading: diaryCountLoading } = useQuery<number>({
    queryKey: ['diaries', 'users', userId, 'count'],
    queryFn: async () => {
      const res = await axiosInstance.get(`/api/v1/diaries/users/${userId}?size=1`);
      return res.data?.data?.totalElements ?? 0;
    },
    enabled: isAuthenticated !== null,
  });

  const followMutation = useMutation({
    mutationFn: () => {
      console.log('followMutation: Starting mutationFn');
      setIsRequestPending(true); // Set pending state
      return axiosInstance.post(`/api/v1/follows/${userId}`, {});
    },
    onSuccess: () => {
      console.log('followMutation: onSuccess triggered');
      setIsRequestPending(false); // Reset pending state
      queryClient.invalidateQueries({ queryKey: ['users', userId, 'profile'] });
      queryClient.invalidateQueries({ queryKey: ['users', 'me', 'profile'] });
      queryClient.invalidateQueries({ queryKey: ['follows', 'requests'] });
      queryClient.invalidateQueries({ queryKey: ['follows', 'sent-requests'] });
      queryClient.invalidateQueries({ queryKey: ['follows', 'followings'] });
      queryClient.invalidateQueries({ queryKey: ['follows', 'followers'] });
      // Optimistically update the user's profile to reflect the new following status
      queryClient.setQueryData(['users', userId, 'profile'], (oldData: PublicUser | undefined) => {
        if (oldData) {
          return { ...oldData, isFollowing: true };
        }
        return oldData;
      });
      onChanged?.();
    },
    onError: (err) => {
      console.log('followMutation: onError triggered', err);
      setIsRequestPending(false); // Reset pending state
      console.error('Follow error:', err);
      alert('팔로우 처리 중 오류가 발생했습니다.');
    },
  });

  const unfollowMutation = useMutation({
    mutationFn: () => axiosInstance.delete(`/api/v1/follows/${userId}`),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users', userId, 'profile'] });
      queryClient.invalidateQueries({ queryKey: ['users', 'me', 'profile'] });
      queryClient.invalidateQueries({ queryKey: ['follows', 'requests'] });
      queryClient.invalidateQueries({ queryKey: ['follows', 'sent-requests'] });
      queryClient.invalidateQueries({ queryKey: ['follows', 'followings'] });
      queryClient.invalidateQueries({ queryKey: ['follows', 'followers'] });
      onChanged?.();
    },
    onError: (err) => {
      console.error('Unfollow error:', err);
      alert('언팔로우 처리 중 오류가 발생했습니다.');
    },
  });

  const acceptFollowMutation = useMutation({
    mutationFn: (id: number) => axiosInstance.post(`/api/v1/follows/${id}/accept`),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users', userId, 'profile'] });
      queryClient.invalidateQueries({ queryKey: ['follows', 'requests'] });
      queryClient.invalidateQueries({ queryKey: ['follows', 'followers'] });
      onChanged?.();
    },
    onError: (err) => {
      console.error('Accept follow error:', err);
      alert('수락 처리 중 오류가 발생했습니다.');
    },
  });

  const rejectFollowMutation = useMutation({
    mutationFn: (id: number) => axiosInstance.delete(`/api/v1/follows/${id}/reject`),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['users', userId, 'profile'] });
      queryClient.invalidateQueries({ queryKey: ['follows', 'requests'] });
      queryClient.invalidateQueries({ queryKey: ['follows', 'followers'] });
      onChanged?.();
    },
    onError: (err) => {
      console.error('Reject follow error:', err);
      alert('거절 처리 중 오류가 발생했습니다.');
    },
  });

  const actionLoading = followMutation.isPending || unfollowMutation.isPending || isRequestPending || acceptFollowMutation.isPending || rejectFollowMutation.isPending; // Include new state

  if (userLoading || isAuthenticated === null) {
    return (
      <div className="flex items-center gap-8 md:gap-16 animate-pulse">
        <div className="w-24 h-24 md:w-36 md:h-36 rounded-full bg-gray-200 flex-shrink-0" />
        <div className="flex-grow space-y-4">
          <div className="h-6 bg-gray-200 rounded w-1/2" />
          <div className="flex space-x-6">
            <div className="h-5 bg-gray-200 rounded w-16" />
            <div className="h-5 bg-gray-200 rounded w-16" />
            <div className="h-5 bg-gray-200 rounded w-16" />
          </div>
          <div className="h-4 bg-gray-200 rounded w-3/4" />
          <div className="h-4 bg-gray-200 rounded w-1/2" />
        </div>
      </div>
    );
  }

  if (error || !user) {
    return <div className="p-8 text-center text-gray-500">사용자를 찾을 수 없습니다.</div>;
  }
  
  const stats = [
    { label: '게시물', value: diaryCount ?? 0 },
    { label: '팔로워', value: user.followersCount ?? 0 },
    { label: '팔로잉', value: user.followingsCount ?? 0 },
  ];

  return (
    <header className="flex items-center gap-8 md:gap-16">
      <div className="flex-shrink-0 w-24 h-24 md:w-36 md:h-36">
        <img
          src={user.profileImageUrl || '/images/no-image.png'}
          alt="프로필"
          className="w-full h-full rounded-full object-cover ring-2 ring-offset-2 ring-gray-100"
        />
      </div>
      <div className="flex-grow space-y-3">
        <div className="flex items-center space-x-4">
          <h1 className="text-2xl md:text-3xl font-semibold text-gray-800">{user.nickname}</h1>
          {!isMe && isAuthenticated && (
            isRequestPending ? ( // Prioritize local pending state
              <button disabled className="px-4 py-1.5 text-sm font-semibold rounded-md bg-gray-200 text-gray-500 cursor-not-allowed">
                요청 보냄
              </button>
            ) : user.isFollowing ? (
              <button onClick={() => unfollowMutation.mutate()} disabled={actionLoading} className="px-4 py-1.5 text-sm font-semibold rounded-md bg-gray-200 text-gray-800 hover:bg-gray-300 transition-colors disabled:opacity-50">
                {actionLoading ? '...' : '팔로잉'}
              </button>
            ) : (
              <button
                onClick={() => followMutation.mutate()}
                disabled={actionLoading}
                className="px-4 py-1.5 text-sm font-semibold rounded-md bg-sky-500 text-white hover:opacity-90 transition-opacity disabled:opacity-50"
              >
                {actionLoading ? '...' : '팔로우'}
              </button>
            )
          )}
        </div>
        <div className="flex space-x-6">
          {stats.map((s) => (
            <div key={s.label}>
              <span className="font-semibold text-gray-800">{s.value}</span>
              <span className="text-gray-600 ml-1">{s.label}</span>
            </div>
          ))}
        </div>
        {user.email && <p className="text-sm text-gray-500 font-mono">{user.email}</p>}
        <p className="text-gray-700 text-sm whitespace-pre-line pt-1">{user.bio || '소개글이 없습니다.'}</p>
      </div>
    </header>
  );
}