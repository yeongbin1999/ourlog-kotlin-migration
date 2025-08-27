"use client";

import { useState, Suspense } from "react";
import { useSearchParams, useRouter } from "next/navigation";
import Image from "next/image";
import { axiosInstance } from "@/lib/api-client"

interface SearchResult {
  externalId: string;
  title: string;
  creatorName: string;
  description?: string;
  posterUrl?: string;
  releasedAt?: string;
  genres?: string[];
}

function formatDate(dateStr?: string): string {
  if (!dateStr) return "";
  const date = new Date(dateStr);
  return date.toISOString().split("T")[0];
}

function SelectContentClient() {
  const router = useRouter();
  const searchParams = useSearchParams();
  const [type] = useState(searchParams.get("type") || "MOVIE");
  const [keyword, setKeyword] = useState("");
  const [results, setResults] = useState<SearchResult[]>([]);
  const [selectedExternalId, setSelectedExternalId] = useState<string | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const handleSearch = async () => {
    if (!type || !keyword.trim()) return;
    setIsLoading(true);
    try {
      const res = await axiosInstance.get(
        `/api/v1/contents/search`,
        {
          params: { type, title: keyword }
        }
      );
      setResults(res.data.data || []);
    } catch (err) {
      console.error("검색 실패:", err);
    } finally {
      setIsLoading(false);
    }
  };

  const handleSubmit = () => {
    if (!type || !selectedExternalId) {
      alert("콘텐츠를 선택해주세요.");
      return;
    }

    const selectedContent = results.find(item => item.externalId === selectedExternalId);
    if (!selectedContent) {
      alert("선택한 콘텐츠 정보를 찾을 수 없습니다.");
      return;
    }

    const query = new URLSearchParams({
      externalId: selectedContent.externalId,
      type: type,
      title: selectedContent.title,
      creatorName: selectedContent.creatorName,
      description: selectedContent.description ?? '',
      posterUrl: selectedContent.posterUrl ?? '',
      releasedAt: selectedContent.releasedAt ?? '',
      genres: selectedContent.genres?.join(',') ?? '',
    });

    router.push(`/diaries/write?${query.toString()}`);
  };

  const handleSearchAgain = () => {
    setResults([]);
    setSelectedExternalId(null);
    setKeyword("");
  };

  const getTypeLabel = (type: string) => {
    const labels = { MOVIE: "영화", BOOK: "도서", MUSIC: "음악" };
    return labels[type as keyof typeof labels] || type;
  };

  return (
    <div className="min-h-screen bg-white p-6 font-sans">
      <div className="max-w-5xl mx-auto space-y-10">
        {/* 헤더 */}
        <div className="text-center space-y-3">
          <h1 className="text-3xl font-bold text-gray-900 tracking-tight">감상 작품 선택</h1>
          <p className="text-lg text-gray-600">어떤 {getTypeLabel(type).toLowerCase()}를(을) 감상하셨나요?</p>
        </div>

        <div className="bg-white rounded-2xl shadow-xl border border-gray-100 overflow-hidden">
          <div className="grid grid-cols-1 lg:grid-cols-2 h-[650px]">
            {/* 검색 영역 */}
            <div className="p-12 flex flex-col justify-center">
              <div className="max-w-sm mx-auto w-full space-y-4">
                <input
                  type="text"
                  className="w-full border border-gray-300 px-5 py-3 rounded-lg text-base focus:border-black focus:outline-none transition-all placeholder-gray-400"
                  value={keyword}
                  onChange={(e) => setKeyword(e.target.value)}
                  placeholder="작품 제목을 입력하세요"
                  onKeyDown={(e) => e.key === "Enter" && handleSearch()}
                />
                <button
                  onClick={handleSearch}
                  disabled={isLoading || !keyword.trim()}
                  className="w-full bg-black text-white py-3.5 rounded-lg text-base font-medium hover:bg-gray-800 transition-all disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {isLoading ? "검색 중..." : "검색하기"}
                </button>
              </div>
            </div>

            {/* 검색 결과 영역 */}
            <div className="bg-gray-50 p-8 h-full flex flex-col">
              {results.length > 0 ? (
                <>
                  <div className="flex items-center justify-between mb-5">
                    <h3 className="text-lg font-semibold text-gray-900">검색 결과</h3>
                    <span className="text-sm text-gray-500 bg-white px-3 py-1 rounded-full">
                      {results.length}개
                    </span>
                  </div>

                  <div
                    className="flex-1 overflow-y-scroll space-y-3 mb-5 pr-2"
                    style={{ maxHeight: '400px' }}
                  >
                    {results.map((item) => (
                      <div
                        key={item.externalId}
                        className={`border rounded-xl p-4 cursor-pointer transition-all hover:shadow-md ${
                          selectedExternalId === item.externalId
                            ? "border-black bg-white shadow"
                            : "border-gray-200 bg-white hover:border-gray-300"
                        }`}
                        onClick={() => setSelectedExternalId(item.externalId)}
                      >
                        <div className="flex gap-4 items-start">
                          <div className="flex-shrink-0">
                            <div className="w-16 h-28 relative rounded-lg overflow-hidden shadow-sm">
                              <Image
                                src={item.posterUrl || "/images/no-image.png"}
                                alt={item.title}
                                fill
                                className="object-cover"
                                onError={(e) => {
                                  const target = e.target as HTMLImageElement;
                                  target.onerror = null;
                                  target.src = "/images/no-image.png";
                                }}
                              />
                            </div>
                          </div>
                          <div className="flex-1 min-w-0 space-y-1">
                            <h4 className="font-semibold text-gray-900 text-sm line-clamp-2 leading-tight">
                              {item.title}
                            </h4>
                            <p className="text-gray-600 text-sm truncate">{item.creatorName}</p>
                            <p className="text-gray-400 text-xs">{formatDate(item.releasedAt)}</p>
                          </div>
                          {selectedExternalId === item.externalId && (
                            <div className="flex-shrink-0">
                              <div className="w-6 h-6 bg-black rounded-full flex items-center justify-center">
                                <svg className="w-3 h-3 text-white" fill="currentColor" viewBox="0 0 20 20">
                                  <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                                </svg>
                              </div>
                            </div>
                          )}
                        </div>
                      </div>
                    ))}
                  </div>

                  <div className="flex gap-4">
                    <button
                      onClick={handleSearchAgain}
                      className="flex-1 bg-white border border-gray-300 text-gray-700 py-3.5 rounded-md font-medium hover:bg-gray-100 transition-all"
                    >
                      다시 검색
                    </button>
                    <button
                      onClick={handleSubmit}
                      disabled={!selectedExternalId}
                      className="flex-1 bg-black text-white py-3.5 rounded-md font-medium hover:bg-gray-800 transition-all disabled:opacity-50 disabled:cursor-not-allowed"
                    >
                      선택 완료
                    </button>
                  </div>
                </>
              ) : (
                <div className="h-full flex items-center justify-center">
                  <div className="text-center space-y-3">
                    <div className="w-20 h-20 bg-gray-100 rounded-xl mx-auto mb-6 flex items-center justify-center">
                      <svg className="w-8 h-8 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                      </svg>
                    </div>
                    <h3 className="text-lg font-medium text-gray-700">
                      {isLoading ? "검색 중..." : "검색을 시작해보세요."}
                    </h3>
                    <p className="text-gray-500 text-sm">
                      {!isLoading && "작품 제목을 입력하고 검색 버튼을 눌러주세요."}
                    </p>
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

function LoadingFallback() {
  return (
    <div className="min-h-screen bg-white p-6 font-sans">
      <div className="max-w-5xl mx-auto space-y-10">
        <div className="text-center space-y-3">
          <h1 className="text-3xl font-bold text-gray-900 tracking-tight">감상 작품 선택</h1>
          <p className="text-lg text-gray-600">로딩 중...</p>
        </div>
        <div className="bg-white rounded-2xl shadow-xl border border-gray-100 overflow-hidden">
          <div className="grid grid-cols-1 lg:grid-cols-2 h-[650px]">
            <div className="p-12 flex flex-col justify-center">
              <div className="max-w-sm mx-auto w-full space-y-4">
                <div className="w-full h-12 bg-gray-200 rounded-lg animate-pulse"></div>
                <div className="w-full h-12 bg-gray-200 rounded-lg animate-pulse"></div>
              </div>
            </div>
            <div className="bg-gray-50 p-8 h-full flex flex-col justify-center items-center">
              <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-gray-900"></div>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default function SelectContentPage() {
  return (
    <Suspense fallback={<LoadingFallback />}>
      <SelectContentClient />
    </Suspense>
  );
}