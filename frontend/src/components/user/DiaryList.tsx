'use client';

import React, { useEffect, useState, useCallback, MouseEvent } from 'react';
import { useRouter } from 'next/navigation';
import { axiosInstance } from '@/lib/api-client';
import { FaEdit, FaTrashAlt } from 'react-icons/fa';

type Diary = {
  id: number;
  title: string;
  contentText: string;
  rating: number;
  isPublic: boolean;
  createdAt: string;
  contentType: 'MOVIE' | 'BOOK' | 'MUSIC' | string;
  genres?: string[];
  otts?: string[];
  tags?: string[];
};

const typeLabel = (raw?: string) => {
  const key = (raw ?? "").toUpperCase();
  return { MOVIE: "🎬 영화", BOOK: "📚 도서", MUSIC: "🎵 음악" }[key] ?? raw ?? "";
};

function RatingStars({ rating }: { rating: number }) {
  const r = Math.max(0, Math.min(5, Math.round(rating)));
  return (
    <div className="flex items-center gap-0.5" aria-label={`별점 ${r}점`}>
      {[...Array(5)].map((_, i) => (
        <span key={i} className={`text-lg ${i < r ? "text-amber-400" : "text-gray-300"}`}>★</span>
      ))}
    </div>
  );
}

function CategorizedTags({ genres = [], otts = [], tags = [] }: Pick<Diary, 'genres' | 'otts' | 'tags'>) {
  const allTags = [
    ...genres.map(tag => ({ tag, color: 'bg-sky-50 text-sky-800 border-sky-100', prefix: '' })),
    ...otts.map(tag => ({ tag, color: 'bg-emerald-50 text-emerald-800 border-emerald-100', prefix: '' })),
    ...tags.map(tag => ({ tag, color: 'bg-gray-100 text-gray-800 border-gray-200', prefix: '#' })),
  ];
  if (allTags.length === 0) return null;
  return (
    <div className="flex items-center gap-1.5 overflow-x-auto pb-2 scrollbar-hide">
      {allTags.map(({ tag, color, prefix }) => (
        <span key={prefix + tag} className={`flex-shrink-0 px-2.5 py-1 text-xs font-semibold rounded-full border ${color}`}>{prefix}{tag}</span>
      ))}
    </div>
  );
}

const DiaryCard = ({ diary, onDelete, onEdit }: {
  diary: Diary;
  onDelete: (diaryId: number) => void;
  onEdit: (diaryId: number) => void;
}) => {
  const router = useRouter();

  const handleCardClick = (e: React.MouseEvent<HTMLDivElement>) => {
    if ((e.target as HTMLElement).closest('button, a')) return;
    router.push(`/diaries/${diary.id}`);
  };

  return (
    <div
      className="relative bg-white rounded-xl shadow-sm p-5 border border-gray-200 hover:shadow-md transition-all duration-300 cursor-pointer"
      onClick={handleCardClick}
    >
      {/* 수정/삭제 버튼을 우측 상단으로 이동 */}
      <div className="absolute top-4 right-4 flex items-center gap-1">
          <button 
              onClick={(e) => { e.stopPropagation(); onEdit(diary.id); }} 
              className="p-2 rounded-full hover:bg-gray-100 text-gray-500 hover:text-sky-600 transition-colors"
              aria-label="수정"
          >
              <FaEdit className="text-base" />
          </button>
          <button 
              onClick={(e) => { e.stopPropagation(); onDelete(diary.id); }} 
              className="p-2 rounded-full hover:bg-gray-100 text-gray-500 hover:text-red-600 transition-colors"
              aria-label="삭제"
          >
              <FaTrashAlt className="text-base" />
          </button>
      </div>
      
      <div className="pr-16"> {/* 버튼 영역만큼 오른쪽에 패딩을 줘서 텍스트가 겹치지 않도록 함 */}
        <span className="text-xs font-bold text-sky-600">{typeLabel(diary.contentType)}</span>
        <h3 className="font-bold text-lg text-gray-900 truncate my-1">{diary.title}</h3>
      </div>
      
      <p className="text-sm text-gray-600 my-2 line-clamp-2">{diary.contentText}</p>

      {diary.rating > 0 && 
        <div className="flex items-center gap-2 mt-3">
          <RatingStars rating={diary.rating} />
        </div>
      }
      <div className="mt-3">
          <CategorizedTags genres={diary.genres} otts={diary.otts} tags={diary.tags} />
      </div>
    </div>
  );
};

export default function DiaryList({ userId, onActionCompleted }: { userId: number; onActionCompleted?: () => void }) {
  const [diaries, setDiaries] = useState<Diary[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const router = useRouter();

  const loadDiaries = useCallback(async (pageToLoad: number) => {
    setLoading(true);
    try {
      const { data } = await axiosInstance.get(`/api/v1/diaries/users/${userId}`, {
        params: { page: pageToLoad, size: 5 },
      });
      setDiaries(data.data?.content ?? []);
      setTotalPages(data.data?.totalPages ?? 0);
    } catch (e) {
      console.error('❌ 다이어리 불러오기 실패:', e);
    } finally {
      setLoading(false);
    }
  }, [userId]);

  useEffect(() => {
    loadDiaries(page);
  }, [page, loadDiaries]);
    
  const handleDelete = async (diaryId: number) => {
    if (window.confirm('정말로 이 다이어리를 삭제하시겠습니까?')) {
      try {
        await axiosInstance.delete(`/api/v1/diaries/${diaryId}`);
        alert('다이어리가 삭제되었습니다.');
        loadDiaries(page);
        onActionCompleted?.();
      } catch (error) {
        console.error('삭제 처리 실패', error);
        alert('삭제 중 오류가 발생했습니다.');
      }
    }
  };
  
  const handleEdit = (diaryId: number) => {
    router.push(`/diaries/${diaryId}/edit`);
  };

  if (loading) return <div className="text-center py-10">📖 다이어리를 불러오는 중...</div>;
  if (diaries.length === 0) return <div className="text-center py-10 text-gray-500">작성된 다이어리가 없습니다.</div>;

  return (
    <div className="space-y-4">
      {diaries.map((diary) => (
        <DiaryCard 
          key={diary.id} 
          diary={diary} 
          onDelete={handleDelete}
          onEdit={handleEdit}
        />
      ))}

      {totalPages > 1 && (
        <div className="flex justify-center items-center gap-2 pt-4">
          <button onClick={() => setPage(p => p - 1)} disabled={page === 0} className="px-3 py-1.5 text-sm font-semibold bg-white border border-gray-300 rounded-md hover:bg-gray-100 disabled:opacity-50">이전</button>
          <span className="text-sm text-gray-600">{page + 1} / {totalPages}</span>
          <button onClick={() => setPage(p => p + 1)} disabled={page + 1 >= totalPages} className="px-3 py-1.5 text-sm font-semibold bg-white border border-gray-300 rounded-md hover:bg-gray-100 disabled:opacity-50">다음</button>
        </div>
      )}
    </div>
  );
}