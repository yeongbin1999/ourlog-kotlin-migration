import { useInfiniteQuery } from '@tanstack/react-query';
import { searchUsers } from '../generated/api/api';
import { Omit } from 'utility-types';
import { SearchUsersParams } from '@/generated/model/searchUsersParams';

export const useSearchUsersInfinite = (params: Omit<SearchUsersParams, 'pageable'>) => {
  return useInfiniteQuery({
    queryKey: ['searchUsers', params.keyword],
    queryFn: async ({ pageParam = 0 }) => {
      const response = await searchUsers({
        keyword: params.keyword,
        pageable: { page: pageParam, size: 10 },
      });

      if (!response.data) {
        return {
          content: [],
          page: 0,
          size: 10,
          totalElements: 0,
          totalPages: 0,
          hasNext: false,
        };
      }

      return {
        content: response.data.content || [],
        page: response.data.page || 0,
        size: response.data.size || 10,
        totalElements: response.data.totalElements || 0,
        totalPages: response.data.totalPages || 0,
        hasNext: response.data.hasNext,
      };
    },
    getNextPageParam: (lastPage) => (lastPage.hasNext ? lastPage.page + 1 : undefined),
    initialPageParam: 0,
    enabled: !!params.keyword,
  });
};
