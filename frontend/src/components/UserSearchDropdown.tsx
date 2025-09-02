/* eslint-disable */
"use client";

import React, { useState, useRef, useEffect } from "react";
import { useDebounce } from "../hooks/useDebounce";
import { useSearchUsersInfinite } from "../hooks/useSearchUsersInfinite";
import { useRouter } from "next/navigation";

// 검색, 오류, 정보 아이콘 SVG 컴포넌트
const SearchIcon = () => (
  <svg
    xmlns="http://www.w3.org/2000/svg"
    fill="none"
    viewBox="0 0 24 24"
    strokeWidth={1.5}
    stroke="currentColor"
    className="w-5 h-5 text-gray-400"
  >
    <path
      strokeLinecap="round"
      strokeLinejoin="round"
      d="m21 21-5.197-5.197m0 0A7.5 7.5 0 1 0 5.196 5.196a7.5 7.5 0 0 0 10.607 10.607Z"
    />
  </svg>
);

const ExclamationTriangleIcon = () => (
  <svg
    xmlns="http://www.w3.org/2000/svg"
    fill="none"
    viewBox="0 0 24 24"
    strokeWidth={1.5}
    stroke="currentColor"
    className="w-6 h-6 mx-auto text-red-400"
  >
    <path
      strokeLinecap="round"
      strokeLinejoin="round"
      d="M12 9v3.75m-9.303 3.376c-.866 1.5.217 3.374 1.948 3.374h14.71c1.73 0 2.813-1.874 1.948-3.374L13.949 3.378c-.866-1.5-3.032-1.5-3.898 0L2.697 16.126ZM12 15.75h.007v.008H12v-.008Z"
    />
  </svg>
);

const InfoCircleIcon = () => (
    <svg
      xmlns="http://www.w3.org/2000/svg"
      fill="none"
      viewBox="0 0 24 24"
      strokeWidth={1.5}
      stroke="currentColor"
      className="w-6 h-6 mx-auto text-gray-400"
    >
      <path
        strokeLinecap="round"
        strokeLinejoin="round"
        d="M11.25 11.25l.041-.02a.75.75 0 011.063.852l-.708 2.836a.75.75 0 001.063.852l.041-.021M21 12a9 9 0 11-18 0 9 9 0 0118 0zm-9-3.75h.008v.008H12V8.25z"
      />
    </svg>
);


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
  const allUsers = data?.pages.flatMap((page) => page.content) ?? [];

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
      target.scrollHeight - target.scrollTop <= target.clientHeight + 20 && // 약간의 여유를 줌
      hasNextPage &&
      !isFetchingNextPage
    ) {
      fetchNextPage();
    }
  };

  const goToUserDetail = (userId: string | number) => {
    router.push(`/profile/${userId}`);
    setKeyword("");
  };

  const renderDropdownContent = () => {
    if (status === "pending") {
      return (
        <div className="flex justify-center items-center p-8">
          <div className="w-6 h-6 border-4 border-t-transparent border-blue-500 rounded-full animate-spin"></div>
        </div>
      );
    }

    if (status === "error") {
      return (
        <div className="p-4 text-center text-red-500">
          <ExclamationTriangleIcon />
          <p className="mt-2 text-sm">사용자 정보를 불러오는 중<br/>오류가 발생했습니다.</p>
        </div>
      );
    }
    
    if (status === "success") {
      if (allUsers.length === 0) {
        return (
          <div className="p-4 text-center text-gray-500">
            <InfoCircleIcon />
            <p className="mt-2 text-sm">검색 결과가 없습니다.</p>
          </div>
        );
      }
      return (
        <>
          {allUsers.map((user, index) => (
            <div
              key={`${user.userId}-${index}`}
              className="flex items-center px-4 py-2.5 hover:bg-gray-100 cursor-pointer transition-colors duration-150"
              onClick={() => goToUserDetail(user.userId?.toString() || "")}
            >
              {/* Avatar Placeholder */}
              <div className="w-9 h-9 rounded-full bg-gray-200 flex items-center justify-center mr-3 flex-shrink-0">
                <span className="text-sm font-semibold text-gray-600">
                  {user.nickname?.charAt(0).toUpperCase()}
                </span>
              </div>
              <div className="truncate">
                <div className="font-medium text-gray-800 text-sm truncate">{user.nickname}</div>
                <div className="text-xs text-gray-500 truncate">{user.email}</div>
              </div>
            </div>
          ))}

          {isFetchingNextPage && (
            <div className="flex justify-center items-center p-4">
              <div className="w-5 h-5 border-2 border-t-transparent border-blue-500 rounded-full animate-spin"></div>
            </div>
          )}

          {!hasNextPage && allUsers.length > 0 && (
            <div className="p-3 text-center text-gray-400 text-xs font-light">
              더 이상 검색 결과가 없습니다.
            </div>
          )}
        </>
      );
    }

    return null;
  };

  return (
    <div className="relative w-64" ref={dropdownRef}>
      <div className="relative">
        <div className="absolute inset-y-0 left-0 flex items-center pl-3 pointer-events-none">
          <SearchIcon />
        </div>
        <input
          type="text"
          placeholder="사용자 검색..."
          value={keyword}
          onChange={(e) => setKeyword(e.target.value)}
          className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-full focus:outline-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-sm transition-colors"
        />
      </div>

      {isOpen && (
        <div className="absolute top-full w-full mt-2 bg-white border border-gray-200 rounded-xl shadow-lg z-50 overflow-hidden">
          <div className="max-h-64 overflow-y-auto" onScroll={onScroll}>
            {renderDropdownContent()}
          </div>
        </div>
      )}
    </div>
  );
};

export default UserSearchDropdown;