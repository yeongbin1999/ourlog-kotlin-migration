import { useState } from "react";
import CommentMenuButton from "./CommentMenuButton";
import { Comment } from "../../types/detail";
import { useRouter } from "next/navigation";
import { axiosInstance } from "@/lib/api-client";

export default function CommentInfo({
  comments,
  setComments,
}: {
  comments: Comment[];
  setComments: React.Dispatch<React.SetStateAction<Comment[]>>;
}) {
  const [editingId, setEditingId] = useState<number | null>(null);
  const [editContent, setEditContent] = useState("");
  const router = useRouter();

  const handleEdit = (comment: Comment) => {
    setEditingId(comment.id);
    setEditContent(comment.content);
  };

  const handleCancel = () => {
    setEditingId(null);
    setEditContent("");
  };

  const handleDelete = async (commentId: number) => {
    if (!confirm("정말 삭제하시겠습니까?")) return;

    try {
      const res = await axiosInstance.delete(`api/v1/comments/${commentId}`);
      if (res == null) throw new Error("Failed to delete comment");

      setComments((prev) => prev.filter((comment) => comment.id !== commentId));
    } catch (err) {
      console.error(err);
    }
  };

  const handleUpdate = async (e: React.FormEvent, id: number) => {
    e.preventDefault();

    const payLoad = {
      id,
      content: editContent,
    };
    try {
      const res = await axiosInstance.put("/api/v1/comments", payLoad);
      if (res.data == null) throw new Error("댓글 수정 실패");

      setComments((prev) =>
        prev.map((comment) =>
          comment.id === id ? { ...comment, content: editContent } : comment
        )
      );

      setEditingId(null);
      setEditContent("");
    } catch (error) {
      console.error(error);
      alert("댓글 수정 중 오류가 발생했습니다.");
    }
  };

  if (comments.length === 0) {
    return (
      <div className="text-center py-12">
        <div className="text-gray-400 text-6xl mb-4">💬</div>
        <p className="text-gray-500 text-lg font-medium">
          등록된 댓글이 없습니다
        </p>
        <p className="text-gray-400 text-sm mt-2">
          첫 번째 댓글을 작성해보세요!
        </p>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {comments.map((comment, index) => (
        <div key={comment.id} className="group">
          <div className="flex gap-4">
            {/* 아바타 (클릭 시 프로필 이동) */}
            <div className="flex-shrink-0">
              <div
                className="w-12 h-12 bg-gray-900 text-white rounded-full flex items-center justify-center font-semibold text-lg cursor-pointer"
                title={`${comment.nickname}님의 프로필로 이동`}
                onClick={() => router.push(`/profile/${comment.id}`)}
              >
                {comment.profileImageUrl ? (
                  <img
                    src={comment.profileImageUrl}
                    alt={`${comment.nickname}님의 프로필 이미지`}
                    className="w-full h-full object-cover rounded-full"
                  />
                ) : (
                  comment.nickname?.charAt(0).toUpperCase() || "?"
                )}
              </div>
            </div>

            {/* 댓글 내용 */}
            <div className="flex-1 min-w-0">
              <div className="bg-gray-50 rounded-2xl p-4 relative">
                {editingId === comment.id ? (
                  <form
                    onSubmit={(e) => handleUpdate(e, comment.id)}
                    className="space-y-3"
                  >
                    <textarea
                      className="w-full p-3 border border-gray-200 rounded-xl resize-none focus:outline-none focus:ring-2 focus:ring-gray-300 focus:border-transparent text-gray-800"
                      value={editContent}
                      rows={3}
                      onChange={(e) => setEditContent(e.target.value)}
                    />
                    <div className="flex justify-end gap-2">
                      <button
                        type="button"
                        onClick={handleCancel}
                        className="px-4 py-2 text-sm font-medium text-gray-600 hover:text-gray-800 transition-colors duration-200"
                      >
                        취소
                      </button>
                      <button
                        type="submit"
                        className="px-4 py-2 bg-gray-900 text-white text-sm font-medium rounded-lg hover:bg-gray-700 transition-colors duration-200"
                      >
                        저장
                      </button>
                    </div>
                  </form>
                ) : (
                  <p className="text-gray-800 leading-relaxed">
                    {comment.content}
                  </p>
                )}
              </div>

              {/* 댓글 메타 정보 */}
              <div className="flex items-center justify-between mt-3 px-2">
                <div className="flex items-center gap-3 text-sm text-gray-500">
                  <span
                    className="font-semibold text-gray-700 cursor-pointer"
                    title={`${comment.nickname}님의 프로필로 이동`}
                    onClick={() => router.push(`/profile/${comment.id}`)}
                  >
                    {comment.nickname || "익명"}
                  </span>
                  <span>•</span>
                  <span>
                    {new Date(comment.createdAt).toLocaleDateString("ko-KR", {
                      year: "numeric",
                      month: "short",
                      day: "numeric",
                      hour: "2-digit",
                      minute: "2-digit",
                    })}
                  </span>
                </div>
                <div className="opacity-0 group-hover:opacity-100 transition-opacity duration-200">
                  <CommentMenuButton
                    onEdit={() => handleEdit(comment)}
                    onDelete={() => handleDelete(comment.id)}
                  />
                </div>
              </div>
            </div>
          </div>

          {/* 댓글 사이 구분선 */}
          {index < comments.length - 1 && (
            <div className="border-t border-gray-100 mt-6" />
          )}
        </div>
      ))}
    </div>
  );
}