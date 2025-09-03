'use client';

import { useRouter } from "next/navigation";

/* eslint-disable @typescript-eslint/no-explicit-any */

import React, { useEffect, useState } from 'react';
import { axiosInstance } from '@/lib/api-client';
import { useAuthStore } from '@/stores/authStore';

type FollowUser = {
  userId: number;
};

type Props = {
  userId: string;
  userType?: 'sent' | 'received' | 'profile' | 'followers' | 'following';
  followId?: number;
  onActionCompleted?: () => void;
  isFollowing?: boolean;
};

type UserProfile = {
  email: string;
  nickname: string;
  profileImageUrl: string;
  bio: string;
  followersCount: number;
  followingsCount: number;
  diaryCount: number;
};

export default function UserProfileCard({
  userId,
  userType = 'profile',
  followId,
  onActionCompleted,
  isFollowing: isFollowingProp,
}: Props) {
  const router = useRouter();
  const [profile, setProfile] = useState<UserProfile | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [isFollowing, setIsFollowing] = useState<boolean>(
    typeof isFollowingProp === 'boolean' ? isFollowingProp : false
  );
  const [hasSentPendingRequest, setHasSentPendingRequest] = useState(false);

  const [loading, setLoading] = useState(false);
  const { user: myUser } = useAuthStore();
  const myUserId = myUser?.id ? Number(myUser.id) : null;

  // 프로필 데이터 로드..
  useEffect(() => {
    const fetchProfileData = async () => {
      try {
        const userRes = await axiosInstance.get(`/api/v1/users/${userId}`);
        const userData = userRes.data.data;

        const diaryCountRes = await axiosInstance.get(`/api/v1/diaries/users/${userId}?size=1`);
        const diaryCountData = diaryCountRes.data?.data?.totalElements ?? 0;

        setProfile({
          ...userData,
          diaryCount: diaryCountData,
        });
        console.log('UserProfileCard profile:', userData);
        console.log('UserProfileCard profileImageUrl:', userData.profileImageUrl);
      } catch (err) {
        setError('존재하지 않는 사용자입니다.');
      }
    };
    fetchProfileData();
  }, [userId]);

  // ..팔로우 상태 확인
  const fetchFollowStatus = async () => {
    if (!myUserId) return;
    setLoading(true);

    try {
      const followingsRes = await axiosInstance.get(`/api/v1/follows/followings?userId=${myUserId}`);
      const followingList = Array.isArray(followingsRes.data) ? followingsRes.data : [];
      const isMeFollowing = followingList.some(
        (user: FollowUser) => user.userId === Number(userId)
      );
      setIsFollowing(isMeFollowing);

      if (userType === 'profile') {
        const sentRequestsRes = await axiosInstance.get(`/api/v1/follows/sent-requests`);
        const sentRequestsList = Array.isArray(sentRequestsRes.data) ? sentRequestsRes.data : [];
        const hasPending = sentRequestsList.some(
          (user: FollowUser) => user.userId === Number(userId)
        );
        console.log('fetchFollowStatus: API response for sent-requests:', sentRequestsRes.data);
        console.log('fetchFollowStatus: Calculated hasPending:', hasPending);
        setHasSentPendingRequest(hasPending);
      }
    } catch (err) {
      console.error('팔로우 상태 불러오기 실패', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (typeof isFollowingProp === 'boolean') {
      setIsFollowing(isFollowingProp);
    } else if (['profile', 'followers', 'following'].includes(userType)) {
      fetchFollowStatus();
    }
  }, [isFollowingProp, userType, myUserId, userId]);

  // 팔로우 / 언팔로우
  const toggleFollow = async () => {
    if (!myUserId) return;
    setLoading(true);

    try {
      if (hasSentPendingRequest) {
        await axiosInstance.delete(`/api/v1/follows/${userId}`);
        alert('팔로우 요청 취소 완료!');
        setHasSentPendingRequest(false);
      } else if (isFollowing) {
        await axiosInstance.delete(`/api/v1/follows/${userId}`);
        alert('언팔로우 완료!');
        setIsFollowing(false);
      } else {
        await axiosInstance.post(`/api/v1/follows/${userId}`, {});
        alert('팔로우 요청 완료!');
        setHasSentPendingRequest(true);
      }
      onActionCompleted?.();
      fetchFollowStatus();
    } catch (err) {
      console.error('요청 처리 실패', err);
      alert('요청 처리 중 오류가 발생했습니다.');
    } finally {
      setLoading(false);
    }
  };

  // 수락 / 거절..
  const acceptFollow = async () => {
    console.log('[디버깅] followId: ', followId);
    if (!followId) return;

    setLoading(true);
    try {
      const res = await axiosInstance.post(`/api/v1/follows/${followId}/accept`);

      if (res.status !== 200) throw new Error('서버 오류');

      alert('수락 완료!');
      onActionCompleted?.();
      console.log("acceptFollow action completed");
    } catch (err) {
      console.error('수락 실패', err);
    } finally {
      setLoading(false);
    }
  };

  const rejectFollow = async () => {
    if (!followId) return;

    setLoading(true);
    try {
      const res = await axiosInstance.delete(`/api/v1/follows/${followId}/reject`);

      if (res.status !== 200) throw new Error('서버 오류');

      alert('거절 완료!');
      onActionCompleted?.();
      console.log("rejectFollow action completed");
    } finally {
      setLoading(false);
    }
  };

  // 버튼 렌더링..
  const renderActionButton = () => {
    console.log({ myUserId, userId, userType, hasSentPendingRequest, isFollowing });
    if (!myUserId || String(myUserId) === userId) return null;
    if (loading) {
      return (
        <button disabled className="mt-6 px-4 py-2 bg-gray-200 rounded">
          처리 중...
        </button>
      );
    }

    switch (userType) {
      case 'received':
        return (
          <div className="flex gap-2 mt-6">
                <button onClick={acceptFollow} className="px-4 py-2 bg-green-500 text-white rounded">
                  수락
                </button>
            <button onClick={rejectFollow} className="px-4 py-2 bg-red-500 text-white rounded">
              거절
            </button>
          </div>
        );
      case 'sent':
        return (
          <button
            onClick={(e) => {
              e.stopPropagation();
              onActionCompleted?.();
            }}
            className="mt-6 px-4 py-2 bg-red-500 text-white rounded"
          >
            요청 취소
          </button>
        );
      case 'followers':
        return null;
      default:
        if (hasSentPendingRequest) {
          return (
            <button
              onClick={toggleFollow}
              className="mt-6 px-4 py-2 bg-gray-200 text-black rounded"
            >
              요청 취소
            </button>
          );
        }
        return (
          <button
            onClick={toggleFollow}
            className={`mt-6 px-4 py-2 border rounded-md transition ${
              isFollowing
                ? 'bg-gray-200 text-black hover:bg-gray-300'
                : 'border-black hover:bg-black hover:text-white'
            }`}
          >
            {isFollowing ? '언팔로우' : '팔로우'}
          </button>
        );
    }
  };

  if (error) return <div className="text-center text-black text-lg mt-10">{error}</div>;
  if (!profile) return <div className="text-center">⏳ 로딩 중...</div>;

  return (
    <div
      className="w-full bg-white p-6 rounded-3xl shadow-md border border-black flex flex-col items-center text-center cursor-pointer"
      onClick={() => router.push(`/profile/${userId}`)}
    >
      <div
        className="w-24 h-24 md:w-36 md:h-36 mb-4 rounded-full bg-center bg-cover flex-shrink-0"
        style={{ backgroundImage: `url(${profile.profileImageUrl || '/images/no-image.png'})` }}
      />
      <h2 className="text-2xl md:text-3xl font-semibold text-gray-800 mb-1">{profile.nickname}</h2>
      <p className="text-sm text-gray-600 mb-2">{profile.bio || '소개글이 없습니다.'}</p>
      <div className="flex space-x-6 mb-4">
        <div>
          <span className="font-semibold text-gray-800">{profile.diaryCount}</span>
          <span className="text-gray-600 ml-1">게시물</span>
        </div>
        <div>
          <span className="font-semibold text-gray-800">{profile.followersCount}</span>
          <span className="text-gray-600 ml-1">팔로워</span>
        </div>
        <div>
          <span className="font-semibold text-gray-800">{profile.followingsCount}</span>
          <span className="text-gray-600 ml-1">팔로잉</span>
        </div>
      </div>
      <hr className="my-4 w-full" />
      <ul className="space-y-2 text-sm text-gray-600 w-full text-left pl-4 ml-28">
        <li>Email: {profile.email}</li>
      </ul>
      {renderActionButton()}
    </div>
  );
}
}
