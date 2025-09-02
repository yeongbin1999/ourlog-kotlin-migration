"use client";

import React, { useEffect, useState } from "react";
import TimelineCard from "../social/components/TimelineCard";
import { TimelineItem } from "../social/types/timeline";

export default function TimelinePage() {
  const [items, setItems] = useState<TimelineItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetch("/api/v1/timeline")
      .then((res) => {
        if (!res.ok) throw new Error("Failed to fetch timeline");
        return res.json();
      })
      .then((data) => {
        if (Array.isArray(data)) {
          setItems(data);
        } else if (data && Array.isArray(data.data)) {
          setItems(data.data);
        } else if (data && Array.isArray(data.items)) {
          setItems(data.items);
        } else if (data && data.content && Array.isArray(data.content)) {
          setItems(data.content);
        } else {
          console.warn("API 응답이 예상된 배열 형태가 아닙니다:", data);
          setItems([]);
        }
      })
      .catch((err) => {
        console.error(err);
        setError("타임라인을 불러오는 데 실패했습니다.");
      })
      .finally(() => setLoading(false));
  }, []);

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-50 via-white to-gray-50">
      <main className="container mx-auto px-4 sm:px-6 lg:px-8 py-12 max-w-7xl">
        {/* 헤더 섹션 */}
        <header className="text-center mb-16">
          <div className="space-y-5">
            <div>
              <h1 className="text-4xl lg:text-3xl font-bold bg-gradient-to-r from-gray-900 via-gray-800 to-gray-900 bg-clip-text text-transparent tracking-tight mb-4">
                Feed
              </h1>

            </div>
            <p className="text-gray-600 text-lg max-w-2xl mx-auto leading-relaxed">
              다른 사용자들의 감상일기를 둘러보고 새로운 영감을 얻어보세요.
            </p>
          </div>
        </header>

        {/* 로딩 상태 */}
        {loading && (
          <div className="flex justify-center items-center py-32">
            <div className="text-center space-y-4">
              <div className="relative">
                <div className="w-12 h-12 border-4 border-gray-200 border-t-gray-900 rounded-full animate-spin mx-auto" />
                <div className="w-8 h-8 border-4 border-transparent border-t-gray-400 rounded-full animate-spin absolute top-2 left-1/2 transform -translate-x-1/2" />
              </div>
              <p className="text-gray-600 font-medium">멋진 일기들을 불러오는 중...</p>
            </div>
          </div>
        )}

        {/* 에러 상태 */}
        {error && (
          <div className="flex justify-center items-center py-32">
            <div className="bg-white border border-red-100 rounded-2xl p-8 max-w-md shadow-lg">
              <div className="text-center space-y-4">
                <div className="w-16 h-16 bg-red-50 rounded-full flex items-center justify-center mx-auto">
                  <span className="text-2xl">😅</span>
                </div>
                <div>
                  <h3 className="font-semibold text-gray-900 mb-2">앗, 문제가 발생했어요</h3>
                  <p className="text-red-600 text-sm">{error}</p>
                </div>
                <button 
                  onClick={() => window.location.reload()} 
                  className="px-4 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600 transition-colors text-sm font-medium"
                >
                  다시 시도
                </button>
              </div>
            </div>
          </div>
        )}

        {/* 타임라인 그리드 */}
        {!loading && !error && (
          <>
            {Array.isArray(items) && items.length > 0 ? (
              <>
                                 <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
                  {items.map((item, index) => (
                    <div 
                      key={item.id} 
                      className="flex justify-center animate-fadeInUp"
                      style={{ animationDelay: `${index * 0.1}s` }}
                    >
                      <TimelineCard item={item} />
                    </div>
                  ))}
                </div>
                
                {/* 더보기 버튼 (필요시) */}
                <div className="text-center mt-16">
                  <div className="inline-flex items-center gap-2 text-gray-500 text-sm">
                    <div className="h-px bg-gray-300 w-8" />
                    <span>총 {items.length}개의 일기</span>
                    <div className="h-px bg-gray-300 w-8" />
                  </div>
                </div>
              </>
            ) : (
              /* 빈 상태 */
              <div className="flex justify-center items-center py-32">
                <div className="bg-white border border-gray-100 rounded-3xl p-12 max-w-lg text-center shadow-sm">
                  <div className="space-y-6">
                    <div className="w-24 h-24 bg-gradient-to-br from-gray-100 to-gray-200 rounded-full flex items-center justify-center mx-auto">
                      <span className="text-4xl">📝</span>
                    </div>
                    <div className="space-y-2">
                      <h3 className="text-xl font-semibold text-gray-900">
                        아직 공유된 일기가 없어요
                      </h3>
                      <p className="text-gray-500 leading-relaxed">
                        첫 번째 감상일기를 작성해서<br />
                        커뮤니티를 활성화시켜보세요!
                      </p>
                    </div>
                    <button className="px-6 py-3 bg-gray-900 text-white rounded-xl hover:bg-gray-800 transition-colors font-medium">
                      일기 작성하기
                    </button>
                  </div>
                </div>
              </div>
            )}
          </>
        )}
      </main>
      
      {/* 커스텀 애니메이션 스타일 */}
      <style jsx>{`
        @keyframes fadeInUp {
          from {
            opacity: 0;
            transform: translateY(20px);
          }
          to {
            opacity: 1;
            transform: translateY(0);
          }
        }
        
        .animate-fadeInUp {
          animation: fadeInUp 0.6s ease-out forwards;
          opacity: 0;
        }
        
        .line-clamp-2 {
          display: -webkit-box;
          -webkit-line-clamp: 2;
          -webkit-box-orient: vertical;
          overflow: hidden;
        }
      `}</style>
    </div>
  );
}