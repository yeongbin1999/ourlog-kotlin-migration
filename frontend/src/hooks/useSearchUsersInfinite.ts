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
      const data: any = resData?.data ?? {};

      const pageNumber = data.number ?? data.page ?? 0;
      const totalPages = data.totalPages ?? 0;
      const hasNext = typeof data.last === 'boolean' ? !data.last : pageNumber + 1 < totalPages;

      return {
        content: data.content ?? [],
        page: pageNumber,
        size: data.size ?? 10,
        totalElements: data.totalElements ?? 0,
        totalPages,
        hasNext,
      };
    },
    getNextPageParam: (lastPage) => (lastPage.hasNext ? lastPage.page + 1 : undefined),
    initialPageParam: 0,
    enabled: !!params.keyword?.trim(),
  });
};
