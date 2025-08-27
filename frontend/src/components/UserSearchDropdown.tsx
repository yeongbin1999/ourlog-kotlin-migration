/* eslint-disable */
"use client";

import React, { useState, useRef, useEffect } from "react";
import { useDebounce } from "../hooks/useDebounce";
import { useSearchUsersInfinite } from "../hooks/useSearchUsersInfinite";
import { useRouter } from "next/navigation";

const UserSearchDropdown = () => {
  const [keyword, setKeyword] = useState("");
  const debouncedKeyword = useDebounce(keyword, 300);
  const dropdownRef = useRef<HTMLDivElement>(null);
  const router = useRouter();

  const {
    data,
    fetchNextPage,
    hasNextPage,
    isFetchingNextPage,
    status,
  } = useSearchUsersInfinite({ keyword: debouncedKeyword });

  const isOpen = debouncedKeyword.trim().length > 0;

  useEffect(() => {
    function handleClickOutside(event: MouseEvent) {
      if (
        dropdownRef.current &&
        !dropdownRef.current.contains(event.target as Node)
      ) {
        setKeyword("");
      }
    }
    if (isOpen) {
      document.addEventListener("mousedown", handleClickOutside);
    }
    return () => {
      document.removeEventListener("mousedown", handleClickOutside);
    };
  }, [isOpen]);

  const onScroll = (e: React.UIEvent<HTMLDivElement>) => {
    const target = e.currentTarget;
    if (
      target.scrollHeight - target.scrollTop <= target.clientHeight + 10 &&
      hasNextPage &&
      !isFetchingNextPage
    ) {
      fetchNextPage();
    }
  };

  const goToUserDetail = (userId: string) => {
    router.push(`/profile/${userId}`);
    setKeyword("");
  };

  return (
    <div className="relative w-64" ref={dropdownRef}>
      <input
        type="text"
        placeholder="사용자 검색..."
        value={keyword}
        onChange={(e) => setKeyword(e.target.value)}
        className="w-full px-3 py-1.5 border border-gray-300 rounded-full focus:outline-none focus:ring-2 focus:ring-black text-sm"
      />

      {isOpen && (
        <div
          className="absolute top-full left-0 right-0 max-h-60 overflow-y-auto mt-1 bg-white border border-gray-300 rounded-md shadow-lg z-50"
          onScroll={onScroll}
        >
          {status === "pending" && (
            <div className="p-4 text-center text-gray-500">로딩 중...</div>
          )}

          {status === "error" && (
            <div className="p-4 text-center text-red-500">
              사용자 정보를 불러오는 중 오류가 발생했습니다.
            </div>
          )}

          {status === "success" && (
            <>
              {data.pages.flatMap((page) => page.content).length === 0 && (
                <div className="p-4 text-center text-gray-500">
                  검색 결과가 없습니다.
                </div>
              )}

              {data.pages.flatMap((page) => page.content).map((user) => (
                <div
                  key={user.userId}
                  className="px-4 py-2 hover:bg-gray-100 cursor-pointer"
                  onClick={() => goToUserDetail(user.userId?.toString() || '')}
                >
                  <div className="font-medium">{user.nickname}</div>
                  <div className="text-xs text-gray-500">{user.email}</div>
                </div>
              ))}

              {isFetchingNextPage && (
                <div className="p-2 text-center text-gray-500">더 불러오는 중...</div>
              )}

              {!hasNextPage && (
                <div className="p-2 text-center text-gray-400 text-sm">
                  더 이상 검색 결과가 없습니다.
                </div>
              )}
            </>
          )}
        </div>
      )}
    </div>
  );
};

export default UserSearchDropdown;