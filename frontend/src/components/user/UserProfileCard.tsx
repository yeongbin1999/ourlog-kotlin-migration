'use client';

import { useRouter } from 'next/navigation';
import React, { useEffect, useState } from 'react';
import { axiosInstance } from '@/lib/api-client';
import { useAuthStore } from '@/stores/authStore';
import { unwrapList } from '@/lib/unwrap';

type FollowUser = { userId: number };

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

  // 프로필 데이터 로드
  useEffect(() => {
    axiosInstance
      .get(`/api/v1/users/${userId}`)
      .then((res) => setProfile(res.data.data))
      .catch(() => setError('존재하지 않는 사용자입니다.'));
  }, [userId]);

  const fetchFollowStatus = async () => {
    if (!myUserId) return;
    setLoading(true);
    try {
      // 내가 팔로잉 중인지
      const followingsRes = await axiosInstance.get(`/api/v1/follows/followings?userId=${myUserId}`);
      const followingList = unwrapList<FollowUser>(followingsRes.data);
      setIsFollowing(followingList.some((u) => u.userId === Number(userId)));

      // 보낸 요청 존재 여부 (profile 화면에서만 체크)
      if (userType === 'profile') {
        const sentReqRes = await axiosInstance.get(`/api/v1/follows/sent-requests?userId=${myUserId}`);
        const sentList = unwrapList<FollowUser>(sentReqRes.data);
        setHasSentPendingRequest(sentList.some((u) => u.userId === Number(userId)));
      }
    } catch (err) {
      console.error('팔로우 상태 불러오기 실패', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (typeof isFollowingProp === 'boolean') {
      setIsFollowing(isFollowingProp); // props 우선
    } else if (['profile', 'followers', 'following'].includes(userType)) {
      fetchFollowStatus(); // API로 확인
    }
  }, [isFollowingProp, userType, myUserId, userId]);

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
        await axiosInstance.post(`/api/v1/follows/${userId}`);
        alert('팔로우 요청 완료!');
        setHasSentPendingRequest(true);
      }
      onActionCompleted?.();
    } catch (err) {
      console.error('요청 처리 실패', err);
      alert('요청 처리 중 오류가 발생했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const acceptFollow = async () => {
    if (!followId) return;
    setLoading(true);
    try {
      const res = await axiosInstance.post(`/api/v1/follows/${followId}/accept`);
      if (res.status !== 200) throw new Error('서버 오류');
      alert('수락 완료!');
      onActionCompleted?.();
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
    } catch (err) {
      console.error('거절 실패', err);
    } finally {
      setLoading(false);
    }
  };

  const renderActionButton = () => {
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
          <button onClick={onActionCompleted} className="mt-6 px-4 py-2 bg-red-500 text-white rounded">
            요청 취소
          </button>
        );
      case 'followers':
        return null;
      default:
        if (hasSentPendingRequest) {
          return (
            <button onClick={toggleFollow} className="mt-6 px-4 py-2 bg-gray-200 text-black rounded">
              요청 취소
            </button>
          );
        }
        return (
          <button
            onClick={toggleFollow}
            className={`mt-6 px-4 py-2 border rounded-md transition ${
              isFollowing ? 'bg-gray-200 text-black hover:bg-gray-300' : 'border-black hover:bg-black hover:text-white'
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
      className="w-full max-w-sm bg-white p-6 rounded-3xl shadow-md border border-black mx-auto flex flex-col items-center text-center cursor-pointer"
      onClick={() => router.push(`/profile/${userId}`)}
    >
      <div
        className="w-20 h-20 mb-4 rounded-full bg-center bg-cover"
        style={{ backgroundImage: `url(${profile.profileImageUrl})` }}
      />
      <h2 className="text-2xl font-bold mb-1">{profile.nickname}</h2>
      <p className="text-sm text-gray-600 mb-2">{profile.bio}</p>
      <hr className="my-4 w-full" />
      <ul className="space-y-2 text-sm text-gray-600 w-full text-left pl-4 ml-28">
        <li>Email: {profile.email}</li>
      </ul>
      {renderActionButton()}
    </div>
  );
}
