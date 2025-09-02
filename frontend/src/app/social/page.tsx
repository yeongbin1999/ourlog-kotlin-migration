"use client";

import React, { useEffect, useState } from "react";
import { Rss } from "lucide-react"; // RSS 아이콘 추가
import TimelineCard from "../social/components/TimelineCard";
import { TimelineItem } from "../social/types/timeline";
import { axiosInstance } from "@/lib/api-client";
import { unwrapList } from "@/lib/unwrap";

export default function TimelinePage() {
  const [items, setItems] = useState<TimelineItem[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const run = async () => {
      setLoading(true);
      setError(null);
      try {
        const res = await axiosInstance.get("/api/v1/timeline");
        const base: any[] = unwrapList(res.data);

        const enriched = await Promise.all(
          base.map(async (it) => {
            try {
              const d = await axiosInstance.get(`/api/v1/diaries/${it.id}`);
              const diary = d.data?.data ?? {};
              return {
                ...it,
                contentType: diary.contentType,
                rating: typeof diary.rating === "number" ? diary.rating : it.rating,
                isPublic: typeof diary.isPublic === "boolean" ? diary.isPublic : it.isPublic,
                tagNames: Array.isArray(diary.tagNames) ? diary.tagNames : it.tagNames,
                releasedAt: diary.releasedAt ?? it.releasedAt,
                title: it.title ?? diary.title,
              };
            } catch {
              return it;
            }
          })
        );
        setItems(enriched);
      } catch (e) {
        console.error(e);
        setError("타임라인을 불러오는 데 실패했습니다.");
      } finally {
        setLoading(false);
      }
    };
    run();
  }, []);

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-50 via-white to-gray-50">
      <main className="container mx-auto px-4 sm:px-6 lg:px-8 py-12 max-w-7xl">
        <header className="flex items-center justify-between mb-12">
          <div>
            <h1 className="flex items-center gap-3 text-3xl font-bold text-gray-800">
              <Rss className="w-7 h-7 text-sky-500" />
              <span>Feed</span>
            </h1>
            <p className="mt-2 text-gray-500">
              다른 사용자들의 감상일기를 둘러보고 새로운 영감을 얻어보세요.
            </p>
          </div>
        </header>

        {/* 로딩 상태 */}
        {loading && (
          <div className="flex justify-center items-center py-32">
            <div className="flex flex-col items-center gap-4">
              <div className="w-10 h-10 border-4 border-gray-200 border-t-gray-900 rounded-full animate-spin" />
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
                      <TimelineCard item={item as any} />
                    </div>
                  ))}
                </div>
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
      `}</style>
    </div>
  );
}