'use client';

import Image from "next/image";

import React, { useMemo } from 'react';
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
  isFollowRequestSent?: boolean;
  followersCount?: number;
  followingsCount?: number;
};

const QUERY_KEY = (userId: number) => ['users', userId, 'profile'];

type Props = {
  userId: number;
  onChanged?: () => void;
};

export default function PublicProfileHeader({ userId, onChanged }: Props) {
  const { user: me, isAuthenticated } = useAuthStore();
  const queryClient = useQueryClient();

  const isMe = useMemo(() => Number(me?.id) === userId, [me?.id, userId]);

  const { data: user, isLoading, error } = useQuery<PublicUser>({
    queryKey: QUERY_KEY(userId),
    queryFn: async () => {
      const res = await axiosInstance.get(`/api/v1/users/${userId}`);
      const d = res.data?.data ?? res.data;
      return { ...d, bio: d.bio ?? '', profileImageUrl: d.profileImageUrl ?? '' };
    },
    enabled: isAuthenticated !== null,
  });

  const useFollowMutation = (mutationFn: () => Promise<unknown>, updateFn: (user: PublicUser) => PublicUser) => {
    return useMutation({
      mutationFn,
      onMutate: async () => {
        await queryClient.cancelQueries({ queryKey: QUERY_KEY(userId) });
        const previousUser = queryClient.getQueryData<PublicUser>(QUERY_KEY(userId));
        if (previousUser) {
          queryClient.setQueryData<PublicUser>(QUERY_KEY(userId), updateFn(previousUser));
        }
        return { previousUser };
      },
      onError: (err, variables, context) => {
        if (context?.previousUser) {
          queryClient.setQueryData<PublicUser>(QUERY_KEY(userId), context.previousUser);
        }
      },
      onSettled: () => {
        // queryClient.invalidateQueries({ queryKey: QUERY_KEY(userId) }); // NOTE: Temporarily disabled for backend data consistency diagnosis
        onChanged?.();
      },
    });
  };

  const followMutation = useFollowMutation(
    () => axiosInstance.post(`/api/v1/follows/${userId}`),
    (u) => ({ ...u, isFollowRequestSent: true })
  );

  const unfollowMutation = useFollowMutation(
    () => axiosInstance.delete(`/api/v1/follows/${userId}`),
    (u) => ({ ...u, isFollowing: false, followersCount: Math.max(0, (u.followersCount || 1) - 1) })
  );

  const cancelFollowRequestMutation = useFollowMutation(
    () => axiosInstance.delete(`/api/v1/follows/${userId}/cancel`),
    (u) => ({ ...u, isFollowRequestSent: false })
  );

  if (isLoading || !user) return <div className="p-4 text-center">로딩중...</div>;
  if (error) return <div className="p-4 text-center text-gray-500">사용자를 찾을 수 없습니다.</div>;

  const renderFollowButton = () => {
    if (isMe) return null;

    const isProcessing = followMutation.isPending || unfollowMutation.isPending || cancelFollowRequestMutation.isPending;

    if (user.isFollowing) {
      return (
        <button
          onClick={() => unfollowMutation.mutate()}
          disabled={isProcessing}
          className="px-4 py-1.5 text-sm rounded bg-gray-200 text-gray-800 hover:bg-gray-300 transition-colors disabled:bg-gray-100 disabled:text-gray-400"
        >
          {unfollowMutation.isPending ? '처리 중...' : '언팔로우'}
        </button>
      );
    }

    if (user.isFollowRequestSent) {
      return (
        <button
          onClick={() => cancelFollowRequestMutation.mutate()}
          disabled={isProcessing}
          className="px-4 py-1.5 text-sm rounded bg-gray-300 text-gray-800 hover:bg-gray-400 transition-colors disabled:bg-gray-100 disabled:text-gray-400"
        >
          {cancelFollowRequestMutation.isPending ? '처리 중...' : '요청 취소'}
        </button>
      );
    }

    return (
      <button
        onClick={() => followMutation.mutate()}
        disabled={isProcessing}
        className="px-4 py-1.5 text-sm rounded bg-sky-500 text-white hover:opacity-90 transition-opacity disabled:bg-sky-300"
      >
        {followMutation.isPending ? '처리 중...' : '팔로우'}
      </button>
    );
  };

  return (
    <header className="flex items-center gap-8 md:gap-16">
      <div className="relative flex-shrink-0 w-24 h-24 md:w-36 md:h-36">
        <Image
          src={user.profileImageUrl || '/images/no-image.png'}
          alt="프로필"
          fill
          className="rounded-full object-cover ring-2 ring-offset-2 ring-gray-100"
        />
      </div>

      <div className="flex-grow space-y-3">
        <div className="flex items-center space-x-4">
          <h1 className="text-2xl md:text-3xl font-semibold text-gray-800">{user.nickname}</h1>
          {renderFollowButton()}
        </div>

        <div className="flex space-x-6">
          <div>
            <span className="font-semibold text-gray-800">{user.followersCount ?? 0}</span>
            <span className="text-gray-600 ml-1">팔로워</span>
          </div>
          <div>
            <span className="font-semibold text-gray-800">{user.followingsCount ?? 0}</span>
            <span className="text-gray-600 ml-1">팔로잉</span>
          </div>
        </div>

        {user.email && <p className="text-sm text-gray-500 font-mono">{user.email}</p>}
        <p className="text-gray-700 text-sm whitespace-pre-line pt-1">{user.bio || '소개글이 없습니다.'}</p>
      </div>
    </header>
  );
}
