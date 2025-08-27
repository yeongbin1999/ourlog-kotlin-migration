/* eslint-disable */
import { useInfiniteQuery } from '@tanstack/react-query';
import { searchUsers } from '../generated/api/api';
import { Omit } from 'utility-types'; // 필요시
import { SearchUsersParams } from '@/generated/model/searchUsersParams';

export const useSearchUsersInfinite = (params: Omit<SearchUsersParams, 'pageable'>) => {
  return useInfiniteQuery({
    queryKey: ['searchUsers', params.keyword],
    queryFn: async ({ pageParam = 0 }) => {
      const response = await searchUsers({
        keyword: params.keyword,
        pageable: { page: pageParam, size: 10 },
      });

      const resData = response.data;

      if (!resData || resData.fail) {
        return {
          content: [],
          page: 0,
          size: 10,
          totalElements: 0,
          totalPages: 0,
          hasNext: false,
        };
      }

      const data = resData.data;

      return {
        content: data.content || [],
        page: data.page || 0,
        size: data.size || 10,
        totalElements: data.totalElements || 0,
        totalPages: data.totalPages || 0,
        hasNext: data.page + 1 < data.totalPages,
      };
    },
    getNextPageParam: (lastPage) => (lastPage.hasNext ? lastPage.page + 1 : undefined),
    initialPageParam: 0,
    enabled: !!params.keyword,
  });
};
