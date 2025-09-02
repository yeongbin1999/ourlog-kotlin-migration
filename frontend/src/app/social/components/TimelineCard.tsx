"use client";

import { TimelineItem } from "../types/timeline";
import { FaHeart, FaRegHeart, FaComment } from "react-icons/fa";
import { useState } from "react";
import { useRouter } from "next/navigation";
import Image from "next/image";

interface Props {
  item: TimelineItem;
}

export default function TimelineCard({ item }: Props) {
  const [isLiked, setIsLiked] = useState(item.isLiked);
  const [likeCount, setLikeCount] = useState(item.likeCount);
  const router = useRouter();

  const handleLikeClick = async (e: React.MouseEvent) => {
    e.stopPropagation();
    try {
      const method = isLiked ? "DELETE" : "POST";

      const response = await fetch(`/api/v1/likes/${item.id}`, {
          method,
      });

      if (!response.ok) throw new Error("좋아요 요청 실패");

      const data = await response.json();

      setIsLiked(data.liked);
      setLikeCount(data.likeCount);
    } catch (err) {
      console.error("좋아요 요청 실패:", err);
      alert("좋아요 요청 중 오류가 발생했습니다.");
    }
  };

  const handleCardClick = () => {
    router.push(`/diaries/${item.id}`);
  };

  return (
    <article
      className="bg-white border border-gray-200 rounded-2xl shadow-sm hover:shadow-lg overflow-hidden cursor-pointer transition-all duration-300 w-full max-w-sm group"
      onClick={handleCardClick}
    >
      {/* 이미지 섹션 */}
      {item.imageUrl && (
        <div className="relative h-48 w-full overflow-hidden">
          <Image
            src={item.imageUrl}
            alt={`${item.title} 포스터`}
            fill
            className="object-cover group-hover:scale-105 transition-transform duration-500"
            sizes="(max-width: 768px) 100vw, (max-width: 1200px) 50vw, 33vw"
          />
        </div>
      )}

      <div className="p-6 space-y-6">
        {/* 제목과 본문 */}
        <div className="space-y-3">
          <h3 className="font-bold text-2xl text-gray-900 leading-tight line-clamp-2 group-hover:text-gray-700 transition-colors">
            {item.title}
          </h3>

          {/* 내용 미리보기 (있다면) - 한 줄만 */}
          {item.content && (
            <p className="text-gray-600 text-sm line-clamp-1 leading-relaxed">
              {item.content}
            </p>
          )}
        </div>

        {/* 사용자 정보 */}
        <div className="flex items-center gap-3">
          <div className="relative w-8 h-8">
            {item.user.profileImageUrl ? (
              <Image
                src={item.user.profileImageUrl}
                alt={`${item.user.nickname} 프로필`}
                fill
                className="rounded-full object-cover"
              />
            ) : (
              <div className="w-full h-full bg-gray-300 rounded-full flex items-center justify-center">
                <span className="text-gray-600 text-xs font-medium">
                  {item.user.nickname.charAt(0).toUpperCase()}
                </span>
              </div>
            )}
          </div>
          <span className="font-semibold text-gray-900 text-sm">{item.user.nickname}</span>
        </div>

        {/* 인터랙션 버튼들 */}
        <div className="flex items-center justify-between pt-2 border-t border-gray-100">
          <div className="flex items-center gap-4">
            {/* 좋아요 버튼 */}
            <button
              onClick={handleLikeClick} 
              className="flex items-center gap-2 px-3 py-2 rounded-lg hover:bg-red-50 transition-all duration-200 group/like"
              aria-label={isLiked ? "좋아요 취소" : "좋아요"}
            >
              {isLiked ? (
                <FaHeart className="text-red-500 text-base group-hover/like:scale-110 transition-transform" />
              ) : (
                <FaRegHeart className="text-gray-400 group-hover/like:text-red-500 text-base group-hover/like:scale-110 transition-all" />
              )}
              <span className={`text-sm font-medium ${isLiked ? 'text-red-600' : 'text-gray-600'}`}>
                {likeCount}
              </span>
            </button>

            {/* 댓글 버튼 */}
            <div className="flex items-center gap-2 px-3 py-2 rounded-lg hover:bg-blue-50 transition-colors cursor-pointer group/comment">
              <FaComment className="text-gray-400 group-hover/comment:text-blue-500 text-base transition-colors" />
              <span className="text-sm font-medium text-gray-600 group-hover/comment:text-blue-600 transition-colors">
                {item.commentCount}
              </span>
            </div>
          </div>

          {/* 날짜를 맨 오른쪽에 배치 */}
          <time className="text-xs text-gray-500 font-medium whitespace-nowrap">
            {new Date(item.createdAt).toLocaleDateString("ko-KR", {
              year: "numeric",
              month: "2-digit", 
              day: "2-digit",
            })}
          </time>
        </div>
      </div>
    </article>
  );
}