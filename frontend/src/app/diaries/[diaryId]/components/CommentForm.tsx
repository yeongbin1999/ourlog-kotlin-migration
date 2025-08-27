import { useState } from "react";
import { Comment } from "../../types/detail";
import { axiosInstance } from "@/lib/api-client";

export default function CommentForm({
  diaryId,
  onCommentAdd,
}: {
  diaryId: number;
  onCommentAdd: (newComment: Comment) => void;
}) {
  const [content, setContent] = useState("");
  const [isSubmitting, setIsSubmitting] = useState(false);

  const handleSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    if (!content.trim()) {
      alert("댓글 내용을 입력해주세요");
      return;
    }

    setIsSubmitting(true);
    const payload = {
      diaryId,
      content,
    };

    try {
      const result = await axiosInstance.post("/api/v1/comments", payload);
      setContent("");
      onCommentAdd(result.data.data);
    } catch (error) {
      console.error(error);
      alert("댓글 등록 중 오류가 발생했습니다.");
    } finally {
      setIsSubmitting(false);
    }
  };

  return (
    <div className="bg-white border border-gray-200 rounded-3xl shadow-sm overflow-hidden">
      <div className="p-8">
        <h2 className="text-2xl font-bold text-gray-900 mb-6">댓글 작성</h2>
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="relative">
            <textarea
              className="w-full border-2 border-gray-200 rounded-2xl p-4 h-32 resize-none text-gray-800 placeholder-gray-500 focus:outline-none focus:border-gray-900 transition-colors duration-200"
              placeholder="이 작품에 대한 당신의 생각을 들려주세요..."
              value={content}
              onChange={(e) => setContent(e.target.value)}
              disabled={isSubmitting}
            />
            <div className="absolute bottom-4 right-4 text-sm text-gray-400">
              {content.length}/500
            </div>
          </div>
          <div className="flex justify-end">
            <button
              type="submit"
              disabled={isSubmitting || !content.trim()}
              className="bg-gray-900 text-white px-8 py-3 rounded-2xl font-semibold hover:bg-gray-700 transition-all duration-200 hover:-translate-y-0.5 disabled:opacity-50 disabled:cursor-not-allowed disabled:transform-none"
            >
              {isSubmitting ? "등록 중..." : "댓글 등록"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}