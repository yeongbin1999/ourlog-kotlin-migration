/* eslint-disable */
"use client";

import { useInfiniteQuery } from "@tanstack/react-query";
import { axiosInstance } from "@/lib/api-client";

type SearchUserItem = {
  userId: number;
  nickname: string;
  email?: string;
  profileImageUrl?: string | null;
};

type PageDTO<T> = {
  content: T[];
  page?: number;
  size?: number;
  totalElements?: number;
  totalPages?: number;
  last?: boolean;
  hasNext?: boolean;
};

type Params = {
  keyword: string;
  size?: number; // 기본 10
};

async function fetchUsers(keyword: string, page: number, size: number) {
  const { data } = await axiosInstance.get("/api/v1/users/search", {
    params: { keyword, page, size },
  });

  if (Array.isArray(data?.data)) {
    return { content: data.data as SearchUserItem[], last: true } as PageDTO<SearchUserItem>;
  }
  if (Array.isArray(data)) {
    return { content: data as SearchUserItem[], last: true } as PageDTO<SearchUserItem>;
  }
  return (data?.data ?? data) as PageDTO<SearchUserItem>;
}

export const useSearchUsersInfinite = ({ keyword, size = 10 }: Params) => {
  return useInfiniteQuery({
    queryKey: ["searchUsers", keyword, size],
    enabled: keyword.trim().length > 0,
    initialPageParam: 0,
    queryFn: ({ pageParam = 0 }) => fetchUsers(keyword, pageParam, size),
    getNextPageParam: (lastPage, allPages) => {
      if (lastPage?.last === true || lastPage?.hasNext === false) return undefined;
      const next = (lastPage?.page ?? allPages.length - 1) + 1;
      const totalPages = lastPage?.totalPages;
      if (typeof totalPages === "number" && next >= totalPages) return undefined;
      return next;
    },
    select: (data) => data, 
  });
};
