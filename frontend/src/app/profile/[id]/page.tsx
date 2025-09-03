'use client';

import { useParams } from 'next/navigation';
import PublicProfileHeader from '@/components/user/PublicProfileHeader';
import DiaryList from '@/components/user/DiaryList';

export default function ProfilePage() {
  const { id } = useParams();
  const userId = Number(id);

  return (
    <div className="bg-white min-h-screen">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8 md:py-12">
        {/* --- 상단: 프로필 정보 --- */}
        <PublicProfileHeader userId={userId} />

        {/* --- 하단: 다이어리 그리드 --- */}
        <main className="mt-12">
          {/* 구분선 및 탭(디자인 확장성 고려) */}
          <div className="border-t border-gray-200">
            <div className="-mt-px flex justify-center space-x-8">
              <span className="border-t-2 border-black px-1 pt-4 text-sm font-medium text-gray-900">
                게시물
              </span>
              {/* 필요시 '저장됨', '태그됨' 등 탭 추가 가능 */}
            </div>
          </div>
          <div className="mt-6">
            <DiaryList userId={userId} />
          </div>
        </main>
      </div>
    </div>
  );
}