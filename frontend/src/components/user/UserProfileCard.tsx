'use client';

import { useRouter } from 'next/navigation';
import React from 'react';

export type UserData = {
  userId: number;
  nickname: string;
  email: string;
  bio: string;
  profileImageUrl: string;
  isFollowing?: boolean;
  followId?: number;
};

export type ActionType = 'follow' | 'unfollow' | 'cancel' | 'accept_reject' | 'none';

type Props = {
  user: UserData;
  actionType: ActionType;
  onAction?: (action: 'follow' | 'unfollow' | 'cancel' | 'accept' | 'reject', targetUserId: number, followId?: number) => void;
  isLoading?: boolean;
};

const actionDetails = {
  follow: { text: '팔로우', className: 'bg-white text-black border border-gray-300 hover:bg-gray-100' },
  unfollow: { text: '언팔로우', className: 'bg-gray-100 text-gray-600 hover:bg-gray-200' },
  cancel: { text: '요청 취소', className: 'bg-red-50 text-red-600 hover:bg-red-100' },
};

export default function UserProfileCard({ user, actionType, onAction, isLoading = false }: Props) {
  const router = useRouter();

  const handleCardClick = (e: React.MouseEvent<HTMLDivElement>) => {
    if ((e.target as HTMLElement).closest('button')) return;
    router.push(`/profile/${user.userId}`);
  };

  const renderButtons = () => {
    if (actionType === 'none') return null;

    if (actionType === 'accept_reject') {
      return (
        <div className="flex items-center gap-2">
          <button
            onClick={() => onAction?.('accept', user.userId, user.followId)}
            disabled={isLoading}
            className="px-4 py-1.5 text-sm font-semibold bg-sky-500 text-white rounded-md hover:bg-sky-600 disabled:bg-sky-300"
          >
            {isLoading ? '...' : '수락'}
          </button>
          <button
            onClick={() => onAction?.('reject', user.userId, user.followId)}
            disabled={isLoading}
            className="px-4 py-1.5 text-sm font-semibold bg-gray-100 text-gray-700 rounded-md hover:bg-gray-200 disabled:bg-gray-300"
          >
            {isLoading ? '...' : '거절'}
          </button>
        </div>
      );
    }

    const detail = actionDetails[actionType as keyof typeof actionDetails];
    if (!detail) return null;

    return (
      <button
        onClick={() => onAction?.(actionType, user.userId, user.followId)}
        disabled={isLoading}
        className={`px-4 py-1.5 text-sm font-semibold rounded-md transition-colors duration-200 disabled:opacity-50 ${detail.className}`}
      >
        {isLoading ? '처리 중...' : detail.text}
      </button>
    );
  };

  return (
    <div
      className="flex items-center gap-4 p-4 bg-white rounded-xl border border-gray-100 hover:border-gray-300 transition-all duration-200 cursor-pointer"
      onClick={handleCardClick}
    >
      <img
        src={user.profileImageUrl || '/images/no-image.png'}
        alt={`${user.nickname} 프로필`}
        className="w-14 h-14 rounded-full object-cover border-2 border-gray-100"
      />
      <div className="flex-1 min-w-0">
        <p className="font-bold text-base text-gray-900 truncate">{user.nickname}</p>
        <p className="text-sm text-gray-500 truncate">{user.bio || '소개글이 없습니다.'}</p>
      </div>
      <div className="flex-shrink-0">{renderButtons()}</div>
    </div>
  );
}
