'use client';

import { useRouter } from "next/navigation";

import React, { useEffect, useState, useCallback } from 'react';
import FollowRequestList from '@/components/user/FollowRequestList';
import SentRequestList from '@/components/user/SentRequestList';
import FollowingList from '@/components/user/FollowingList';
import FollowerList from '@/components/user/FollowerList';
import { useAuthStore } from '@/stores/authStore';
import DiaryList from '@/components/user/DirayList';
import { axiosInstance } from '@/lib/api-client';

const TAB_ITEMS = [
    { key: 'received', label: '받은 요청' },
    { key: 'sent', label: '보낸 요청' },
    { key: 'following', label: '팔로잉' },
    { key: 'followers', label: '팔로워' },
] as const;

type TabKey = typeof TAB_ITEMS[number]['key'];

export default function MyProfilePage() {
    const [selectedTab, setSelectedTab] = useState<TabKey | null>('received');
    const { user } = useAuthStore();
    const router = useRouter(); // useRouter 훅 사용

    useEffect(() => {
        if (!user) {
            router.push('/login'); // 로그인 페이지로 리다이렉트
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

    const fetchCounts = useCallback(async (userId: number) => {
        try {
            const endpoints = {
                received: `/api/v1/follows/requests?userId=${userId}`,
                sent: `/api/v1/follows/sent-requests?userId=${userId}`,
                following: `/api/v1/follows/followings?userId=${userId}`,
                followers: `/api/v1/follows/followers?userId=${userId}`,
            };

            const res = await Promise.all(
                Object.values(endpoints).map((url) => axiosInstance.get(url).then(r => r.data))
            );

            setCounts({
                received: Array.isArray(res[0]) ? res[0].length : 0,
                sent: Array.isArray(res[1]) ? res[1].length : 0,
                following: Array.isArray(res[2]) ? res[2].length : 0,
                followers: Array.isArray(res[3]) ? res[3].length : 0,
            });
        } catch (err) {
            console.error('수량 불러오기 실패', err);
        }
    }, [myUserId]); // myUserId를 의존성 배열에 추가

    useEffect(() => {
        if (myUserId) {
            fetchCounts(myUserId);
        }
    }, [myUserId]);

    const handleTabClick = (tabKey: TabKey) => {
        if (selectedTab === tabKey) {
            setSelectedTab(null);
        } else {
            setSelectedTab(tabKey);
            if (myUserId) {
                fetchCounts(myUserId);
            }
        }
    };

    const renderTabContent = () => {
        if (!myUserId || selectedTab === null) {
            return <div className="text-center text-gray-500">탭을 클릭하세요!..</div>;
        }

        switch (selectedTab) {
            case 'received':
                return <FollowRequestList myUserId={myUserId} onActionCompleted={() => fetchCounts(myUserId)} />;
            case 'sent':
                return <SentRequestList myUserId={myUserId} onActionCompleted={() => fetchCounts(myUserId)} />;
            case 'following':
                return <FollowingList myUserId={myUserId} onActionCompleted={() => fetchCounts(myUserId)} />;
            case 'followers':
                return <FollowerList myUserId={myUserId} onActionCompleted={() => fetchCounts(myUserId)} />;
            default:
                return null;
        }
    };

    return (
        <div className="flex">
            <div className="flex-1 px-4 py-10">
                <h1 className="text-3xl font-bold text-center mb-8">내 프로필</h1>

                {myUserId && (
                    <div className="bg-white rounded-xl shadow-lg p-6 mb-10">
                        {!user ? (
                            <div className="text-center">⏳ 프로필 로딩 중...</div>
                        ) : (
                            <div className="w-full bg-white p-6 rounded-3xl shadow-md border border-black mx-auto flex flex-row items-center gap-6">
                                <div
                                    className="w-24 h-24 rounded-full bg-center bg-cover border border-gray-300"
                                    style={{
                                        backgroundImage: `url(${user.profileImageUrl || '/images/no-image.png'})`,
                                    }}
                                />

                                <div className="flex-1">
                                    <h2 className="text-2xl font-bold mb-1">{user.nickname}</h2>
                                    <p className="text-sm text-gray-600 mb-3">
                                        {user.bio || '소개글이 없습니다.'}
                                    </p>

                                    <div className="mt-2 text-xs text-gray-500">📧 {user.email}</div>
                                </div>
                            </div>
                        )}

                        {/* 탭 버튼 */}
                        <div className="mt-6 flex flex-wrap justify-around gap-3">
                            {TAB_ITEMS.map((tab) => (
                                <button
                                    key={tab.key}
                                    onClick={() => handleTabClick(tab.key)}
                                    className={`relative px-4 py-2 rounded-md text-sm font-medium transition border ${
                                        selectedTab === tab.key
                                            ? 'bg-black text-white'
                                            : 'bg-white text-black border-gray-300 hover:bg-gray-100'
                                    }`}
                                >
                                    {tab.label}
                                    <span className="absolute -top-2 -right-2 bg-red-500 text-white text-xs font-bold px-2 py-0.5 rounded-full">
                    {counts[tab.key]}
                  </span>
                                </button>
                            ))}
                        </div>
                    </div>
                )}

                {/* 탭 콘텐츠 */}
                {renderTabContent()}
            </div>

            {/* 오른쪽: 내 다이어리 리스트 */}
            <div className="flex-1 mx-auto px-4 py-10">
                <h1 className="text-3xl font-bold text-center mb-8">내 다이어리</h1>
                {myUserId ? (
                    <DiaryList userId={myUserId} />
                ) : (
                    <div className="text-center text-gray-500">유저 정보를 불러오는 중입니다...</div>
                )}
            </div>
        </div>
    );
}