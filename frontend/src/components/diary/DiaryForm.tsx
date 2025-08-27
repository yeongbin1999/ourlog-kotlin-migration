"use client";

import { useRouter } from "next/navigation";
import { useState, useEffect, useCallback } from "react";
import Image from "next/image";
import { axiosInstance } from "@/lib/api-client"

interface Tag {
  id: number;
  name: string;
  color?: string; 
}

const tagColors = [
  "bg-emerald-100 text-emerald-700",
  "bg-blue-100 text-blue-700",
  "bg-yellow-100 text-yellow-800",
  "bg-purple-100 text-purple-700",
  "bg-pink-100 text-pink-700",
  "bg-green-100 text-green-700",
  "bg-indigo-100 text-indigo-700",
  "bg-red-100 text-red-700",
];

function getRandomColor() {
  const randomIndex = Math.floor(Math.random() * tagColors.length);
  return tagColors[randomIndex];
}

interface OTTPlatform {
  id: number;
  name: string;
  icon: string;
}

const OTT_PLATFORMS: OTTPlatform[] = [
  { id: 1, name: "Netflix", icon: "🎬" },
  { id: 2, name: "Disney+", icon: "🏰" },
  { id: 3, name: "Prime Video", icon: "📦" },
  { id: 4, name: "TVING", icon: "📺" },
  { id: 5, name: "Watcha", icon: "👀" },
];

export type ContentType = "MOVIE" | "MUSIC" | "BOOK";

export interface DiaryFormProps {
  mode: "create" | "edit";
  diaryId?: number;
  externalId: string;
  type: ContentType;
  title: string;
  creatorName: string;
  description: string;
  posterUrl: string;
  releasedAt: string;
  genres: string[];
  initialValues: {
    title: string;
    contentText: string;
    isPublic: boolean;
    rating: number;
    tagNames: string[];
    genreNames: string[];
    ottNames: string[];
  };
  onSubmit?: (data: {
    title: string;
    contentText: string;
    isPublic: boolean;
    rating: number;
    externalId?: string;
    type: ContentType;
    tagIds: number[];
    ottIds?: number[];
  }) => Promise<void>;
}

export default function DiaryForm({
  externalId,
  type,
  title: contentTitle,
  creatorName,
  description,
  posterUrl,
  releasedAt,
  genres,
  initialValues,
  mode = "create",
  diaryId,
}: DiaryFormProps) {
  const router = useRouter();

  const [title, setTitle] = useState(initialValues?.title ?? "");
  const [contentText, setContentText] = useState(initialValues?.contentText ?? "");
  const [isPublic, setIsPublic] = useState(initialValues?.isPublic ?? true);
  const [rating, setRating] = useState(initialValues?.rating ?? 0);
  const [allTags, setAllTags] = useState<Tag[]>([]);
  const [selectedTagIds, setSelectedTagIds] = useState<number[]>([]);
  const [selectedOttId, setSelectedOttId] = useState<number | null>(null);
  const [newTagName, setNewTagName] = useState("");
  const [isOttDropdownOpen, setIsOttDropdownOpen] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);

  const fetchTags = useCallback(async () => {
    try {
      const res = await axiosInstance.get("/api/v1/tags");
      const tagsWithColor = res.data.data.map((tag: Tag) => ({
        ...tag,
        color: getRandomColor(),
      }));
      setAllTags(tagsWithColor);
    } catch (err) {
      console.error("태그 불러오기 실패:", err);
    }
  }, []);

  useEffect(() => {
    fetchTags();
  }, [fetchTags]);

  useEffect(() => {
    if (initialValues && allTags.length > 0) {
      const tagIds = allTags
        .filter((tag) => initialValues.tagNames.includes(tag.name))
        .map((tag) => tag.id);
      setSelectedTagIds(tagIds);

      // OTT 초기화 수정
      if (type === "MOVIE") {
        const valid = OTT_PLATFORMS.find(p =>
          initialValues.ottNames.includes(p.name)
        );
        setSelectedOttId(valid?.id ?? null);
      } else {
        setSelectedOttId(null);
      }
    }
  }, [initialValues, allTags, type]);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsSubmitting(true);
  
    const payload = {
      title,
      contentText,
      isPublic,
      rating,
      tagNames: allTags.filter((tag) => selectedTagIds.includes(tag.id)).map((tag) => tag.name),
      ottIds: selectedOttId ? [selectedOttId] : [],
      externalId,
      type,
    };
  
    try {
      let res;
      if (mode === "edit") {
        res = await axiosInstance.put(`/api/v1/diaries/${diaryId}`, payload);
      } else {
        res = await axiosInstance.post("/api/v1/diaries", payload);
      }
    
      alert("감상일기가 저장되었습니다.");
      const redirectId = res.data.data?.id ?? diaryId;
      const redirectUrl = mode === "edit" ? `/diaries/${redirectId}?refresh=1` : `/diaries/${redirectId}`;
      router.push(redirectUrl);
    } catch (err: unknown) {
      const errorMessage = err instanceof Error && 'response' in err 
        ? (err as { response?: { data?: { msg?: string } } }).response?.data?.msg 
        : "오류가 발생했습니다.";
      alert(errorMessage || "오류가 발생했습니다.");
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleTagToggle = (id: number) => {
    setSelectedTagIds((prev) =>
      prev.includes(id) ? prev.filter((t) => t !== id) : [...prev, id]
    );
  };

  const handleOttSelect = (id: number) => {
    setSelectedOttId(id);
    setIsOttDropdownOpen(false);
  };

  const handleCreateNewTag = () => {
    if (!newTagName.trim()) return;
    const newId = allTags.length + 1;
    const color = "bg-indigo-100 text-indigo-700";
    const tag = { id: newId, name: newTagName.trim(), color };
    setAllTags((prev) => [...prev, tag]);
    setSelectedTagIds((prev) => [...prev, newId]);
    setNewTagName("");
  };

  const handleStarClick = (value: number) => setRating(value);

  const getTypeLabel = (type: string) => ({
    MOVIE: "영화",
    BOOK: "도서",
    MUSIC: "음악",
  }[type] || type);

  return (
    <div className="min-h-screen bg-gray-50 p-6">
      <div className="max-w-6xl mx-auto">
        <div className="text-center mb-8">
          <h1 className="text-3xl font-bold text-gray-900 mb-2">
            감상일기 {mode === "edit" ? "수정" : "작성"}
          </h1>
          <p className="text-gray-600">
            {getTypeLabel(type)} 감상 후기를 자유롭게 {mode === "edit" ? "수정" : "작성"}해보세요.
          </p>
        </div>
  
        <div className="grid grid-cols-1 lg:grid-cols-5 gap-8">
          {/* 콘텐츠 정보 카드 */}
          <div className="lg:col-span-2">
            <div className="bg-white rounded-3xl shadow-lg border border-gray-100 p-8 sticky top-6 text-sm text-gray-500 space-y-3">
              <div className="text-center">
                <div className="w-48 h-72 mx-auto mb-6 relative rounded-2xl overflow-hidden shadow">
                  <Image
                    src={posterUrl || "/images/no-image.png"}
                    alt="포스터"
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
              <div className="bg-gray-50 rounded-xl px-4 py-3">
                <span className="text-xs text-gray-400 block mb-1">제목</span>
                {contentTitle || "제목 없음"}
              </div>
              <div className="bg-gray-50 rounded-xl px-4 py-3">
                <span className="text-xs text-gray-400 block mb-1">제작자</span>
                {creatorName || "정보 없음"}
              </div>
              <div className="grid grid-cols-2 gap-3">
                <div className="bg-gray-50 rounded-xl px-4 py-3">
                  <span className="text-xs text-gray-400 block mb-1">출시일</span>
                  {releasedAt.slice(0, 10) || "알 수 없음"}
                </div>
                <div className="bg-gray-50 rounded-xl px-4 py-3">
                  <span className="text-xs text-gray-400 block mb-1">장르</span>
                  {genres.length > 0 ? genres.join(", ") : "없음"}
                </div>
              </div>
              {type === "MOVIE" && (
                <div className="bg-gray-50 rounded-xl px-4 py-3 text-left">
                  <span className="text-xs text-gray-400 block mb-1">줄거리</span>
                  {description || "줄거리 정보가 없습니다."}
                </div>
              )}
            </div>
          </div>
  
          {/* 작성 폼 */}
          <div className="lg:col-span-3">
            <form onSubmit={handleSubmit}>
              <div className="bg-white rounded-3xl shadow-lg border border-gray-100 p-8 space-y-8">
  
                {/* 공개 여부 */}
                <div className="flex items-center justify-between">
                  <h2 className="text-xl font-bold text-gray-900">일기 작성</h2>
                  <label className="flex items-center gap-3 cursor-pointer">
                    <span className="text-sm font-medium text-gray-700">공개 설정</span>
                    <div className="relative">
                      <input type="checkbox" checked={isPublic} onChange={(e) => setIsPublic(e.target.checked)} className="sr-only" />
                      <div className={`w-12 h-6 rounded-full transition-colors ${isPublic ? 'bg-black' : 'bg-gray-300'}`}>
                        <div className={`w-5 h-5 bg-white rounded-full shadow-sm transition-transform duration-200 mt-0.5 ${isPublic ? 'translate-x-6 ml-0.5' : 'translate-x-0.5'}`}></div>
                      </div>
                    </div>
                  </label>
                </div>
  
                {/* 제목 */}
                <div>
                  <label className="block text-sm font-semibold text-gray-700 mb-3">제목</label>
                  <input type="text" value={title} onChange={(e) => setTitle(e.target.value)}
                    className="w-full border-2 border-gray-200 px-4 py-3 rounded-2xl focus:border-black focus:outline-none text-lg"
                    placeholder="일기 제목을 입력하세요"
                  />
                </div>
  
                {/* 내용 */}
                <div>
                  <label className="block text-sm font-semibold text-gray-700 mb-3">감상 후기</label>
                  <textarea value={contentText} onChange={(e) => setContentText(e.target.value)}
                    className="w-full border-2 border-gray-200 px-4 py-4 rounded-2xl focus:border-black focus:outline-none resize-none"
                    rows={6} placeholder="작품을 보고 느낀 점을 자유롭게 작성해보세요..."
                  />
                </div>
  
                {/* OTT */}
                {type === 'MOVIE' && (
                  <div>
                    <label className="block text-sm font-semibold text-gray-700 mb-3">시청 플랫폼</label>
                    <div className="relative">
                      <button
                        type="button"
                        onClick={() => setIsOttDropdownOpen(!isOttDropdownOpen)}
                        className="w-full border-2 border-gray-200 px-4 py-3 rounded-2xl focus:border-black focus:outline-none text-gray-700 bg-white text-left flex items-center justify-between hover:border-gray-300 transition-all"
                      >
                        <span>
                          {selectedOttId
                            ? `${OTT_PLATFORMS.find(p => p.id === selectedOttId)?.icon} ${OTT_PLATFORMS.find(p => p.id === selectedOttId)?.name}`
                            : '플랫폼을 선택해주세요'}
                        </span>
                        <svg className={`w-5 h-5 text-gray-400 transition-transform duration-200 ${isOttDropdownOpen ? 'rotate-180' : ''}`} fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                        </svg>
                      </button>
                      {isOttDropdownOpen && (
                        <div className="absolute top-full left-0 right-0 mt-1 bg-white border-2 border-gray-200 rounded-2xl shadow-lg z-10 max-h-60 overflow-y-auto">
                          {OTT_PLATFORMS.map((platform) => (
                            <button
                              type="button"
                              key={platform.id}
                              onClick={() => handleOttSelect(platform.id)}
                              className="w-full px-4 py-3 text-left hover:bg-gray-50 transition-colors flex items-center gap-3 first:rounded-t-2xl last:rounded-b-2xl">
                              <span className="text-lg">{platform.icon}</span>
                              <span>{platform.name}</span>
                            </button>
                          ))}
                        </div>
                      )}
                    </div>
                  </div>
                )}
  
                {/* 태그 */}
                <div>
                  <label className="block text-sm font-semibold text-gray-700 mb-3">감정 태그</label>
                  <div className="flex flex-wrap gap-2 mb-4">
                    {allTags.map((tag) => (
                      <button
                        type="button"
                        key={tag.id}
                        onClick={() => handleTagToggle(tag.id)}
                        className={`px-4 py-2 rounded-full text-sm font-medium border-2 transition-all ${
                          selectedTagIds.includes(tag.id)
                            ? `${tag.color} border-gray-300 shadow-sm`
                            : 'bg-white text-gray-600 hover:bg-gray-50 border-gray-200'
                        }`}>
                        {tag.name}
                      </button>
                    ))}
                  </div>
                  <div className="flex gap-3">
                    <input type="text" value={newTagName} onChange={(e) => setNewTagName(e.target.value)}
                      onKeyDown={(e) => e.key === 'Enter' && handleCreateNewTag()}
                      className="flex-1 border-2 border-gray-200 px-4 py-2 rounded-2xl focus:border-black focus:outline-none"
                      placeholder="새로운 감정 태그 추가"
                    />
                    <button
                      type="button"
                      onClick={handleCreateNewTag}
                      className="w-12 h-12 bg-black text-white rounded-2xl flex items-center justify-center text-xl font-bold hover:bg-gray-800 transition">
                      +
                    </button>
                  </div>
                </div>
  
                {/* 평점 */}
                <div>
                  <label className="block text-sm font-semibold text-gray-700 mb-3">평점</label>
                  <div className="flex items-center gap-4">
                    <div className="flex items-center gap-1">
                      {[1, 2, 3, 4, 5].map((star) => (
                        <button
                          type="button"
                          key={star}
                          onClick={() => handleStarClick(star)}
                          className={`text-3xl transition hover:scale-110 ${
                            star <= rating ? 'text-yellow-400' : 'text-gray-300 hover:text-yellow-200'
                          }`}>
                          ★
                        </button>
                      ))}
                    </div>
                    <div className="flex items-center gap-2">
                      <input type="number" value={rating}
                        onChange={(e) => setRating(Math.min(5, Math.max(0, parseFloat(e.target.value) || 0)))}
                        min="0" max="5" step="0.1"
                        className="w-20 border-2 border-gray-200 px-3 py-2 rounded-xl text-center focus:border-black focus:outline-none"
                      />
                      <span className="text-gray-500 font-medium">/ 5.0</span>
                    </div>
                  </div>
                </div>
  
                {/* 제출 버튼 */}
                <div className="pt-4">
                  <button type="submit" disabled={isSubmitting || !title || !contentText}
                    className="w-full bg-black text-white py-4 rounded-2xl text-lg font-semibold hover:bg-gray-800 disabled:opacity-50 shadow-lg">
                    {isSubmitting ? (
                      <div className="flex items-center justify-center gap-2">
                        <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin" />
                        {mode === "edit" ? "수정 중..." : "작성 중..."}
                      </div>
                    ) : mode === "edit" ? "수정 완료" : "일기 작성 완료"}
                  </button>
                </div>
  
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
  );
}