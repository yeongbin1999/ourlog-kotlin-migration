'use client';

import { useRouter } from 'next/navigation';
import React, { useEffect, useState, useCallback } from 'react';

import FollowRequestList from '@/components/user/FollowRequestList';
import SentRequestList from '@/components/user/SentRequestList';
import FollowingList from '@/components/user/FollowingList';
import FollowerList from '@/components/user/FollowerList';
import { useAuthStore } from '@/stores/authStore';
import DiaryList from '@/components/user/DiaryList'; 
import { axiosInstance } from '@/lib/api-client';

const TAB_ITEMS = [
  { key: 'received', label: '받은 요청', icon: '↓' },
  { key: 'sent', label: '보낸 요청', icon: '↑' },
  { key: 'following', label: '팔로잉', icon: '→' },
  { key: 'followers', label: '팔로워', icon: '♥' },
] as const;

type TabKey = (typeof TAB_ITEMS)[number]['key'];

/** ✅ 서버 응답을 항상 배열로 정규화 */
function unwrapList<T = any>(data: any): T[] {
  if (Array.isArray(data)) return data;
  if (Array.isArray(data?.data?.content)) return data.data.content;
  if (Array.isArray(data?.data)) return data.data;
  if (Array.isArray(data?.content)) return data.content;
  return [];
}

export default function MyProfilePage() {
  const [selectedTab, setSelectedTab] = useState<TabKey | null>('received');
  const { user } = useAuthStore();
  const router = useRouter();

  useEffect(() => {
    // user가 falsy면 로그인으로 보냄 (초기 로딩 동안은 살짝 대기)
    if (!user) {
      router.push('/login');
    }
  }, [user, router]);

  const [myUserId, setMyUserId] = useState<number | null>(null);
  const [counts, setCounts] = useState<Record<TabKey, number>>({
    received: 0,
    sent: 0,
    following: 0,
    followers: 0,
  });

  useEffect(() => {
    if (user?.id) {
      setMyUserId(Number(user.id));
    }
  }, [user]);

  /** ✅ 카운트도 정규화해서 길이만 집계 */
  const fetchCounts = useCallback(async (userId: number) => {
    try {
      const endpoints = {
        received: `/api/v1/follows/requests?userId=${userId}`,
        sent: `/api/v1/follows/sent-requests?userId=${userId}`,
        following: `/api/v1/follows/followings?userId=${userId}`,
        followers: `/api/v1/follows/followers?userId=${userId}`,
      };

      const res = await Promise.all(
        Object.values(endpoints).map((url) => axiosInstance.get(url).then((r) => r.data))
      );

      setCounts({
        received: unwrapList(res[0]).length,
        sent: unwrapList(res[1]).length,
        following: unwrapList(res[2]).length,
        followers: unwrapList(res[3]).length,
      });
    } catch (err) {
      console.error('수량 불러오기 실패', err);
      // 실패해도 UI 깨지지 않게 유지
    }
  }, []); // ✅ 의존성에 myUserId 불필요

  useEffect(() => {
    if (myUserId) {
      fetchCounts(myUserId);
    }
  }, [myUserId, fetchCounts]);

  const handleTabClick = (tabKey: TabKey) => {
    setSelectedTab((prev) => (prev === tabKey ? null : tabKey));
    if (myUserId) {
      fetchCounts(myUserId); // 탭 바꿀 때 최신화
    }
  };

  const renderTabContent = () => {
    if (!myUserId || selectedTab === null) {
      return (
        <div className="flex flex-col items-center justify-center py-20 text-gray-400">
          <div className="w-16 h-16 border-2 border-dashed border-gray-200 rounded-full flex items-center justify-center mb-4">
            <span className="text-xl text-gray-300">•••</span>
          </div>
          <p className="text-sm font-medium text-gray-500">탭을 선택하면 내용이 표시됩니다</p>
        </div>
      );
    }

    const onActionCompleted = () => fetchCounts(myUserId);

    switch (selectedTab) {
      case 'received':
        return <FollowRequestList myUserId={myUserId} onActionCompleted={onActionCompleted} />;
      case 'sent':
        return <SentRequestList myUserId={myUserId} onActionCompleted={onActionCompleted} />;
      case 'following':
        return <FollowingList myUserId={myUserId} onActionCompleted={onActionCompleted} />;
      case 'followers':
        return <FollowerList myUserId={myUserId} onActionCompleted={onActionCompleted} />;
      default:
        return null;
    }
  };

  if (!user) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="flex flex-col items-center gap-3">
          <div className="w-8 h-8 border-2 border-black border-t-transparent rounded-full animate-spin"></div>
          <p className="text-sm text-gray-600">로그인 확인 중...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* 헤더 */}
      <div className="bg-white border-b border-gray-100">
        <div className="max-w-6xl mx-auto px-6 py-8">
          <h1 className="text-2xl font-bold text-black text-center">MY PROFILE</h1>
        </div>
      </div>

      <div className="max-w-6xl mx-auto px-6 py-8">
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          {/* 왼쪽: 프로필 정보 */}
          <div className="lg:col-span-1 space-y-6">
            {/* 프로필 카드 */}
            <div className="bg-white rounded-none shadow-sm border border-gray-200 overflow-hidden">
              <div className="p-8">
                <div className="flex flex-col items-center text-center mb-6">
                  <div className="w-24 h-24 rounded-full bg-gray-100 border-2 border-gray-200 overflow-hidden mb-4">
                    <img
                      src={user.profileImageUrl || '/images/no-image.png'}
                      alt="프로필"
                      className="w-full h-full object-cover"
                    />
                  </div>
                  <h2 className="text-xl font-bold text-black mb-2">{user.nickname}</h2>
                  <p className="text-sm text-gray-600 mb-3 max-w-xs leading-relaxed">
                    {user.bio || '소개글이 없습니다.'}
                  </p>
                  <p className="text-xs text-gray-400 font-mono">{user.email}</p>
                </div>

                {/* 통계 */}
                <div className="grid grid-cols-2 gap-4 pt-6 border-t border-gray-100">
                  <div className="text-center">
                    <div className="text-lg font-bold text-black">{counts.following}</div>
                    <div className="text-xs text-gray-500 uppercase tracking-wider">Following</div>
                  </div>
                  <div className="text-center">
                    <div className="text-lg font-bold text-black">{counts.followers}</div>
                    <div className="text-xs text-gray-500 uppercase tracking-wider">Followers</div>
                  </div>
                </div>
              </div>
            </div>

            {/* 소셜 네비게이션 */}
            <div className="bg-white rounded-none shadow-sm border border-gray-200">
              <div className="p-6">
                <h3 className="text-sm font-semibold text-gray-900 uppercase tracking-wider mb-4">
                  Social Activity
                </h3>
                <div className="space-y-2">
                  {TAB_ITEMS.map((tab) => (
                    <button
                      key={tab.key}
                      onClick={() => handleTabClick(tab.key)}
                      className={`w-full flex items-center justify-between px-4 py-3 text-left transition-all duration-200 ${
                        selectedTab === tab.key
                          ? 'bg-black text-white'
                          : 'bg-white hover:bg-gray-50 text-gray-700 border border-gray-100'
                      }`}
                    >
                      <div className="flex items-center gap-3">
                        <span className="text-sm font-medium">{tab.icon}</span>
                        <span className="text-sm font-medium">{tab.label}</span>
                      </div>
                      <div className="flex items-center gap-2">
                        {counts[tab.key] > 0 && (
                          <span
                            className={`px-2 py-1 text-xs font-bold ${
                              selectedTab === tab.key ? 'bg-white text-black' : 'bg-gray-900 text-white'
                            }`}
                          >
                            {counts[tab.key]}
                          </span>
                        )}
                        <span className="text-xs">{selectedTab === tab.key ? '●' : '○'}</span>
                      </div>
                    </button>
                  ))}
                </div>
              </div>
            </div>
          </div>

          {/* 오른쪽: 콘텐츠 영역 */}
          <div className="lg:col-span-2 space-y-6">
            {/* 소셜 활동 콘텐츠 */}
            <div className="bg-white rounded-none shadow-sm border border-gray-200">
              <div className="border-b border-gray-100 px-6 py-4">
                <h3 className="text-sm font-semibold text-gray-900 uppercase tracking-wider">
                  {selectedTab ? TAB_ITEMS.find((tab) => tab.key === selectedTab)?.label : 'Social Content'}
                </h3>
              </div>
              <div className="p-6">{renderTabContent()}</div>
            </div>

            {/* 다이어리 섹션 */}
            <div className="bg-white rounded-none shadow-sm border border-gray-200">
              <div className="border-b border-gray-100 px-6 py-4">
                <h3 className="text-sm font-semibold text-gray-900 uppercase tracking-wider">My Diary</h3>
              </div>
              <div className="p-6">
                {myUserId ? (
                  <DiaryList userId={myUserId} />
                ) : (
                  <div className="flex flex-col items-center justify-center py-20 text-gray-400">
                    <div className="w-16 h-16 border-2 border-dashed border-gray-200 rounded-full flex items-center justify-center mb-4 animate-pulse">
                      <span className="text-xl text-gray-300">◐</span>
                    </div>
                    <p className="text-sm font-medium text-gray-500">다이어리를 불러오는 중...</p>
                  </div>
                )}
              </div>
            </div>
          </div>
          {/* /오른쪽 */}
        </div>
      </div>
    </div>
  );
}
