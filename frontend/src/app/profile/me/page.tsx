"use client";

import { useRouter } from "next/navigation";
import React, { useEffect, useState, useCallback } from "react";
import Image from "next/image";
import { useAuthStore } from "@/stores/authStore";
import DiaryList from "@/components/user/DiaryList";

import FollowRequestList from "@/components/user/FollowRequestList";
import SentRequestList from "@/components/user/SentRequestList";
import FollowerList from "@/components/user/FollowerList";
import FollowingList from "@/components/user/FollowingList";
import { axiosInstance } from "@/lib/api-client";
import { unwrapList } from "@/lib/unwrap";

const TAB_ITEMS = [
  { key: "diary", label: "마이 다이어리", type: "diary" },
  { key: "received", label: "받은 요청", type: "user_list", endpoint: () => `/api/v1/follows/requests`, actionType: "accept_reject", empty: "받은 팔로우 요청이 없습니다." },
  { key: "sent", label: "보낸 요청", type: "user_list", endpoint: () => `/api/v1/follows/sent-requests`, actionType: "cancel", empty: "보낸 팔로우 요청이 없습니다." },
  { key: "following", label: "팔로잉", type: "user_list", endpoint: () => `/api/v1/follows/followings`, actionType: "unfollow", empty: "팔로우하는 사용자가 없습니다." },
  { key: "followers", label: "팔로워", type: "user_list", endpoint: () => `/api/v1/follows/followers`, actionType: "none", empty: "아직 팔로워가 없습니다." },
] as const;

type TabKey = (typeof TAB_ITEMS)[number]["key"];

export default function MyProfilePage() {
  const [selectedTabKey, setSelectedTabKey] = useState<TabKey>("diary");
  const { user, isAuthenticated, isLoading, initializeAuth } = useAuthStore();
  const router = useRouter();

  const [counts, setCounts] = useState({ diary: 0, received: 0, sent: 0, following: 0, followers: 0 });

  const fetchAllCounts = useCallback(async (userId: number) => {
    try {
      const diaryPromise = axiosInstance
        .get(`/api/v1/diaries/users/${userId}`, { params: { page: 0, size: 1 } }) // ✅ 대괄호 X
        .then((r) => r.data.data?.totalElements ?? 0);

      const socialPromises = TAB_ITEMS
        .filter((tab) => tab.type === "user_list")
        .map((tab) => axiosInstance.get(tab.endpoint!()).then((r) => unwrapList(r.data).length));

      const [diaryCount, ...socialCounts] = await Promise.all([diaryPromise, ...socialPromises]);

      setCounts({
        diary: diaryCount,
        received: socialCounts[0],
        sent: socialCounts[1],
        following: socialCounts[2],
        followers: socialCounts[3],
      });
    } catch (error) {
      // eslint-disable-next-line no-console
      console.error("Failed to fetch counts", error);
    }
  }, []);

  useEffect(() => {
    initializeAuth();
  }, [initializeAuth]);

  useEffect(() => {
    if (!isLoading) { // Only proceed after loading is complete
      if (!isAuthenticated) {
        router.push("/login");
      } else if (user?.id) {
        fetchAllCounts(Number(user.id));
      }
    }
  }, [user, isAuthenticated, isLoading, router, fetchAllCounts]);

  if (isLoading || !user) { // Show loading while authenticating or if user is null
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="w-8 h-8 border-4 border-sky-200 border-t-sky-500 rounded-full animate-spin"></div>
      </div>
    );
  }

  const myUserId = Number(user.id);
  const selectedTab = TAB_ITEMS.find((t) => t.key === selectedTabKey);

  const renderTabContent = () => {
    if (!selectedTab) return null;
    if (selectedTab.type === "diary") {
      return <DiaryList userId={myUserId} onActionCompleted={() => fetchAllCounts(myUserId)} />;
    }
    if (selectedTab.type === "user_list") {
      switch (selectedTab.key) {
        case "received":
          return <FollowRequestList myUserId={myUserId} onActionCompleted={() => fetchAllCounts(myUserId)} />;
        case "sent":
          return <SentRequestList myUserId={myUserId} onActionCompleted={() => fetchAllCounts(myUserId)} />;
        case "following":
          return <FollowingList myUserId={myUserId} onActionCompleted={() => fetchAllCounts(myUserId)} />;
        case "followers":
          return <FollowerList myUserId={myUserId} onActionCompleted={() => fetchAllCounts(myUserId)} />;
        default:
          return null;
      }
    }
    return null;
  };

  return (
    <div className="bg-white-50 min-h-screen">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="lg:grid lg:grid-cols-12 lg:gap-8">
          <aside className="lg:col-span-4 lg:sticky lg:top-8 self-start space-y-6">
            <div className="bg-white p-6 rounded-2xl border border-gray-200 shadow-lg text-center">
              <div className="relative w-28 h-28 mx-auto mb-4">
                <Image
                  src={user.profileImageUrl || "/images/no-image.png"}
                  alt="프로필"
                  fill
                  className="rounded-full object-cover border-4 border-white shadow-md"
                  sizes="112px"
                />
              </div>
              <h2 className="text-2xl font-bold text-gray-900">{user.nickname}</h2>
              <p className="text-sm text-gray-500 font-mono mb-4">{user.email}</p>
              <p className="text-gray-600 leading-relaxed max-w-sm mx-auto">
                {user.bio || "소개글이 없습니다."}
              </p>
              <div className="grid grid-cols-2 gap-4 pt-4 mt-4 border-t border-gray-100">
                <div>
                  <div className="text-2xl font-bold text-sky-500">{counts.following}</div>
                  <div className="text-xs text-gray-500 uppercase tracking-wider">Following</div>
                </div>
                <div>
                  <div className="text-2xl font-bold text-sky-500">{counts.followers}</div>
                  <div className="text-xs text-gray-500 uppercase tracking-wider">Followers</div>
                </div>
              </div>
            </div>
            <div className="bg-white p-4 rounded-2xl border border-gray-200 space-y-2">
              {TAB_ITEMS.map((tab) => (
                <button
                  key={tab.key}
                  onClick={() => setSelectedTabKey(tab.key)}
                  className={`w-full flex items-center justify-between px-4 py-3 text-left rounded-lg transition-all duration-200 ${
                    selectedTabKey === tab.key ? "bg-sky-500 text-white shadow-sm hover:bg-sky-600" : "hover:bg-gray-100 text-gray-700"
                  }`}
                >
                  <span className="font-semibold">{tab.label}</span>
                  <span
                    className={`px-2.5 py-0.5 text-xs font-bold rounded-full ${
                      selectedTabKey === tab.key ? "bg-white text-sky-600" : "bg-gray-200 text-gray-600"
                    }`}
                  >
                    {counts[tab.key]}
                  </span>
                </button>
              ))}
            </div>
          </aside>
          <main className="lg:col-span-8 mt-8 lg:mt-0">
            <div className="bg-white rounded-2xl border border-gray-200">
              <div className="px-6 py-4 border-b border-gray-100">
                <h3 className="text-2xl font-bold text-gray-900">{selectedTab?.label}</h3>
              </div>
              <div className="p-4 md:p-6">{renderTabContent()}</div>
            </div>
          </main>
        </div>
      </div>
    </div>
  );
}
