import { useSearchParams, useRouter, usePathname } from 'next/navigation';
import { useCallback } from 'react';

export function useUrlParams() {
  const searchParams = useSearchParams();
  const router = useRouter();
  const pathname = usePathname();

  const getParam = useCallback((key: string): string | null => {
    return searchParams.get(key);
  }, [searchParams]);

  const setParam = useCallback((key: string, value: string) => {
    const params = new URLSearchParams(searchParams.toString());
    params.set(key, value);
    router.push(`${pathname}?${params.toString()}`);
  }, [searchParams, router, pathname]);

  const removeParam = useCallback((key: string) => {
    const params = new URLSearchParams(searchParams.toString());
    params.delete(key);
    const newUrl = params.toString() ? `${pathname}?${params.toString()}` : pathname;
    router.push(newUrl);
  }, [searchParams, router, pathname]);

  const setMultipleParams = useCallback((params: Record<string, string>) => {
    const newParams = new URLSearchParams(searchParams.toString());
    Object.entries(params).forEach(([key, value]) => {
      newParams.set(key, value);
    });
    router.push(`${pathname}?${newParams.toString()}`);
  }, [searchParams, router, pathname]);

  return {
    getParam,
    setParam,
    removeParam,
    setMultipleParams,
    searchParams: searchParams.toString(),
  };
} 