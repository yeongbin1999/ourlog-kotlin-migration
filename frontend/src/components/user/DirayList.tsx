'use client';

import React, { useEffect, useState } from 'react';
import { axiosInstance } from '@/lib/api-client';
import { useRouter } from 'next/navigation';

type Diary = {
  id: number;
  title: string;
  contentText: string;
  rating: number;
  isPublic: boolean;
  createdAt: string;
  modifiedAt: string;
  releasedAt: string | null;
  genres: string[];
  tags: string[];
  otts: string[];
};

export default function DiaryList({ userId }: { userId: number }) {
  const [diaries, setDiaries] = useState<Diary[]>([]);
  const [page, setPage] = useState(0); // 0-based
  const [totalPages, setTotalPages] = useState(1);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const router = useRouter();

  const loadDiaries = async (pageToLoad: number) => {
    setLoading(true);
    setError(null);

    try {
      const res = await axiosInstance.get(`/api/v1/diaries/users/${userId}`, {
        params: { page: pageToLoad, size: 3 }, // ✅ 3개씩 보여주기
      });

      const rsData = res.data as {
        resultCode: string;
        msg: string;
        data: {
          content: Diary[];
          totalPages: number;
          number: number;
          size: number;
          totalElements: number;
          last: boolean;
        };
      };

      if (!rsData || rsData.resultCode.startsWith('FAIL')) {
        setError('다이어리 목록을 불러오는 데 실패했습니다.');
        return;
      }

      setDiaries(rsData.data.content);
      setTotalPages(rsData.data.totalPages || 1); // ✅ 최소 1
    } catch (e) {
      console.error('❌ 다이어리 불러오기 실패:', e);
      setError('네트워크 오류가 발생했습니다.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadDiaries(page);
  }, [page]);

  const handlePageChange = (newPage: number) => {
    if (newPage >= 0 && newPage < totalPages) {
      setPage(newPage);
    }
  };

  const handleDiaryClick = (diaryId: number) => {
    router.push(`/diaries/${diaryId}`);
  };

  if (error) return <div className="text-center text-red-600">{error}</div>;

  return (
    <div className="space-y-6">
      {diaries.length === 0 && !loading && (
        <div className="text-center text-gray-500">작성된 다이어리가 없습니다.</div>
      )}

      {diaries.map((diary) => (
        <div
          key={diary.id}
          className="p-6 border rounded-xl shadow-sm bg-white cursor-pointer"
          onClick={() => handleDiaryClick(diary.id)}
        >
          <div className="flex justify-between items-center mb-2">
            <h3 className="text-xl font-semibold">{diary.title}</h3>
            <span className="text-sm text-gray-500">{diary.releasedAt || '개봉일 미정'}</span>
          </div>
          <p className="text-sm text-gray-700 mb-2 line-clamp-3">{diary.contentText}</p>
          <div className="flex flex-wrap gap-2 text-xs text-gray-500">
            <span>⭐ 평점: {diary.rating}</span>
            <span>공개 여부: {diary.isPublic ? '공개' : '비공개'}</span>
            <span>작성일: {diary.createdAt?.slice(0, 10)}</span>
            <span>수정일: {diary.modifiedAt?.slice(0, 10)}</span>
          </div>
          <div className="mt-2 flex flex-wrap gap-2 text-xs">
            {diary.genres.map((g, i) => (
              <span key={`genre-${i}`} className="px-2 py-1 bg-blue-100 rounded">{g}</span>
            ))}
            {diary.tags.map((t, i) => (
              <span key={`tag-${i}`} className="px-2 py-1 bg-green-100 rounded">{t}</span>
            ))}
            {diary.otts.map((o, i) => (
              <span key={`ott-${i}`} className="px-2 py-1 bg-yellow-100 rounded">{o}</span>
            ))}
          </div>
        </div>
      ))}

      {loading && <div className="text-center">📖 다이어리 로딩 중...</div>}

      {/* Pagination UI */}
      <div className="flex justify-center items-center gap-2 mt-6">
        <button
          onClick={() => handlePageChange(page - 1)}
          disabled={page === 0}
          className="px-3 py-1 border rounded disabled:opacity-50"
        >
          ◀ 이전
        </button>

        {Array.from({ length: totalPages > 0 ? totalPages : 1 }, (_, i) => (
          <button
            key={i}
            onClick={() => handlePageChange(i)}
            className={`px-3 py-1 rounded ${
              i === page ? 'bg-gray-800 text-white' : 'border'
            }`}
          >
            {i + 1}
          </button>
        ))}

        <button
          onClick={() => handlePageChange(page + 1)}
          disabled={page + 1 >= totalPages}
          className="px-3 py-1 border rounded disabled:opacity-50"
        >
          다음 ▶
        </button>
      </div>
    </div>
  );
}