"use client";

import { MouseEvent, useState } from "react";
import { useRouter } from "next/navigation";
import Image from "next/image";
import { FaHeart, FaRegHeart, FaComment } from "react-icons/fa";
import type { TimelineItem as BaseTimelineItem } from "../types/timeline";

type ContentMeta = {
  type?: string;
  posterUrl?: string;
  releasedAt?: string;
  title?: string;
};

type TimelineItem = BaseTimelineItem & {
  // 다양한 메타 키 대응
  contentMeta?: ContentMeta;
  contentInfo?: ContentMeta;
  contentDetail?: ContentMeta;
  contentObj?: ContentMeta;

  // 폼/상세 기반 확장 필드
  rating?: number;
  isPublic?: boolean;
  tagNames?: string[];
  tags?: string[];

  // 백업 단일 필드
  type?: string;
  contentType?: string;
  posterUrl?: string;
  releasedAt?: string;
  imageUrl?: string;
};

const TAG_COLORS = [
  "bg-blue-50 text-gray-800 border-blue-100",
  "bg-blue-50 text-gray-800 border-blue-100", 
  "bg-blue-50 text-gray-800 border-blue-100",
  "bg-blue-50 text-gray-800 border-blue-100",
  "bg-blue-50 text-gray-800 border-blue-100",
  "bg-blue-50 text-gray-800 border-blue-100",
  "bg-blue-50 text-gray-800 border-blue-100",
  "bg-blue-50 text-gray-800 border-blue-100",
];

const getTagColor = (i: number) => TAG_COLORS[i % TAG_COLORS.length];

const typeLabel = (raw?: string) => {
  const key = (raw ?? "").toUpperCase();
  return (
    { MOVIE: "🎬 영화", BOOK: "📚 도서", MUSIC: "🎵 음악" }[key] ??
    raw ??
    ""
  );
};

function RatingStars({ rating }: { rating: number }) {
  const r = Math.max(0, Math.min(5, Math.round(rating)));
  return (
    <div className="flex items-center gap-1" aria-label={`별점 ${r}점`}>
      {[...Array(5)].map((_, i) => (
        <span
          key={i}
          className={`text-lg transition-colors duration-200 ${
            i < r ? "text-amber-400" : "text-gray-300"
          }`}
        >
          ★
        </span>
      ))}

    </div>
  );
}

function TagPills({ tags = [] as string[] }) {
  if (!tags.length) return null;
  return (
    <div className="mt-3 flex flex-wrap gap-1.5">
      {tags.slice(0, 8).map((t, i) => (
        <span
          key={`${t}-${i}`}
          className={`px-3 py-1 rounded-full text-xs font-semibold shadow-sm transition-all duration-200 hover:scale-105 border ${getTagColor(i)}`}
        >
          {t.startsWith("#") ? t : `#${t}`}
        </span>
      ))}
    </div>
  );
}

export default function TimelineCard({ item }: { item: TimelineItem }) {
  const router = useRouter();

  // meta 우선 선택
  const meta: ContentMeta =
    item.contentMeta ??
    item.contentInfo ??
    item.contentDetail ??
    item.contentObj ??
    {};

  // 타입 결정
  const _typeRaw =
    meta.type ||
    item.type ||
    item.contentType ||
    (item as any)?.content_type;

  const _typeText = typeLabel(_typeRaw);

  // 포스터/날짜/제목
  const poster =
    meta.posterUrl || item.posterUrl || item.imageUrl || "/images/no-image.png";
  const released = meta.releasedAt || item.releasedAt || item.createdAt || "";
  const title = meta.title || item.title || "제목 없음";
  const preview = item.content ? item.content.slice(0, 100) : "";

  // 사용자 입력 태그 우선
  const tags =
    (item.tagNames && item.tagNames.length ? item.tagNames : item.tags) || [];

  // 평점(없으면 0)
  const rating = typeof item.rating === "number" ? item.rating : 0;

  const dateText = released ? new Date(released).toLocaleDateString("ko-KR") : "";

  // ♥ 상태
  const [isLiked, setIsLiked] = useState(!!item.isLiked);
  const [likeCount, setLikeCount] = useState(item.likeCount ?? 0);

  const handleLikeClick = async (e: MouseEvent) => {
    e.stopPropagation();
    try {
      const method = isLiked ? "DELETE" : "POST";
      const res = await fetch(`/api/v1/likes/${item.id}`, { method });
      if (!res.ok) throw new Error("좋아요 요청 실패");
      const data = await res.json();
      setIsLiked(!!data.liked);
      setLikeCount(
        typeof data.likeCount === "number"
          ? data.likeCount
          : likeCount + (isLiked ? -1 : 1)
      );
    } catch (err) {
      console.error(err);
      alert("좋아요 요청 중 오류가 발생했습니다.");
    }
  };

  const handleCardClick = () => router.push(`/diaries/${item.id}`);

  return (
    <article
      className="group relative bg-white rounded-2xl shadow-lg overflow-hidden transform transition-all duration-500 hover:-translate-y-2 hover:shadow-2xl flex flex-col cursor-pointer w-full max-w-sm border border-gray-200"
      onClick={handleCardClick}
    >
      {/* 미니멀 호버 효과 */}
      <div className="absolute inset-0 bg-gray-50 opacity-0 group-hover:opacity-30 transition-opacity duration-300 rounded-2xl" />
      
      {/* 이미지 + 공개/비공개 배지 */}
      <div className="relative h-48 w-full overflow-hidden">
        <Image
          src={poster}
          alt={`${title} 포스터`}
          fill
          className="object-cover transition-transform duration-700 group-hover:scale-110"
          sizes="(max-width: 768px) 100vw, (max-width: 1200px) 50vw, 33vw"
        />
        
        {/* 그라디언트 오버레이 */}
        <div className="absolute inset-0 bg-gradient-to-t from-black/20 via-transparent to-transparent" />
        
        {typeof item.isPublic === "boolean" && (
          <div className="absolute top-3 right-3 flex items-center bg-white/90 backdrop-blur-md text-gray-700 text-xs font-bold px-3 py-1.5 rounded-full shadow-lg border border-white/20">
            {item.isPublic ? (
              <>
                <svg xmlns="http://www.w3.org/2000/svg" className="h-3 w-3 mr-1.5 text-emerald-500" viewBox="0 0 20 20" fill="currentColor">
                  <path d="M10 12a2 2 0 100-4 2 2 0 000 4z" />
                  <path
                    fillRule="evenodd"
                    d="M.458 10C1.732 5.943 5.522 3 10 3s8.268 2.943 9.542 7c-1.274 4.057-5.022 7-9.542 7S1.732 14.057.458 10zM14 10a4 4 0 11-8 0 4 4 0 018 0z"
                    clipRule="evenodd"
                  />
                </svg>
                <span className="text-emerald-600">공개</span>
              </>
            ) : (
              <>
                <svg xmlns="http://www.w3.org/2000/svg" className="h-3 w-3 mr-1.5 text-orange-500" viewBox="0 0 20 20" fill="currentColor">
                  <path
                    fillRule="evenodd"
                    d="M13.477 14.89A6 6 0 015.11 6.524l8.367 8.367zm1.414-1.414L6.524 5.11a6 6 0 018.367 8.367zM18 10a8 8 0 11-16 0 8 8 0 0116 0z"
                    clipRule="evenodd"
                  />
                </svg>
                <span className="text-orange-600">비공개</span>
              </>
            )}
          </div>
        )}

        {/* 타입 배지 */}
        {_typeText && (
          <div className="absolute top-3 left-3 bg-black/70 backdrop-blur-md text-white text-xs font-bold px-3 py-1.5 rounded-full shadow-lg">
            {_typeText}
          </div>
        )}
      </div>

      {/* 본문 */}
      <div className="relative p-6 flex-grow flex flex-col space-y-4">
        {/* 제목 */}
        <h3 className="text-xl font-bold text-gray-900 group-hover:text-gray-400 transition-colors duration-300 line-clamp-2 leading-tight">
          {title}
        </h3>

        {/* 평점 */}
        {rating > 0 && (
          <div className="flex items-center">
            <RatingStars rating={rating} />
          </div>
        )}

        {/* 내용 미리보기 */}
        {preview && (
          <p className="text-gray-600 text-sm leading-relaxed line-clamp-3 group-hover:text-gray-700 transition-colors duration-300">
            {preview}
            {item.content && item.content.length > 100 ? "…" : ""}
          </p>
        )}

        {/* 사용자 입력 태그 */}
        <TagPills tags={tags} />

        {/* 작성자 */}
        {item.user?.nickname && (
          <div className="flex items-center gap-3 pt-2">
            <div className="relative w-9 h-9">
              {item.user.profileImageUrl ? (
                <Image
                  src={item.user.profileImageUrl}
                  alt={`${item.user.nickname} 프로필`}
                  fill
                  className="rounded-full object-cover ring-2 ring-gray-100"
                />
              ) : (
                <div className="w-full h-full bg-gray-400 rounded-full flex items-center justify-center">
                  <span className="text-white text-sm font-bold">
                    {item.user.nickname.charAt(0).toUpperCase()}
                  </span>
                </div>
              )}
            </div>
            <span className="font-semibold text-gray-800 text-sm group-hover:text-black transition-colors duration-300">
              {item.user.nickname}
            </span>
          </div>
        )}
      </div>

      {/* 푸터 */}
      <div className="relative bg-gray-50 px-6 py-4 flex items-center justify-between border-t border-gray-100">
        <div className="flex items-center gap-6">
          <button
            onClick={handleLikeClick}
            className={`group/like flex items-center gap-2 px-3 py-2 rounded-xl transition-all duration-300 ${
              isLiked 
                ? "bg-red-50 text-red-600 shadow-sm" 
                : "hover:bg-red-50 text-gray-500 hover:text-red-500"
            }`}
            aria-label={isLiked ? "좋아요 취소" : "좋아요"}
          >
            {isLiked ? (
              <FaHeart className="text-lg transition-transform duration-200 group-hover/like:scale-110" />
            ) : (
              <FaRegHeart className="text-lg transition-transform duration-200 group-hover/like:scale-110" />
            )}
            <span className="text-sm font-semibold">
              {likeCount}
            </span>
          </button>

          <div className="group/comment flex items-center gap-2 px-3 py-2 rounded-xl hover:bg-blue-50 text-gray-500 hover:text-blue-500 transition-all duration-300">
            <FaComment className="text-lg transition-transform duration-200 group-hover/comment:scale-110" />
            <span className="text-sm font-semibold">
              {typeof item.commentCount === "number" ? item.commentCount : 0}
            </span>
          </div>
        </div>

        <time className="text-xs text-gray-400 font-medium whitespace-nowrap bg-white/60 px-2 py-1 rounded-lg">
          {dateText}
        </time>
      </div>
    </article>
  );
}