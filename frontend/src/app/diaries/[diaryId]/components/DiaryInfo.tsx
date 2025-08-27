import { DiaryInfoProps } from "../../types/detail";

export default function DiaryInfo({
  rating,
  contentText,
  tagNames,
  onEdit,
  onDelete,
}: DiaryInfoProps & {
  onEdit: () => void;
  onDelete: () => void;
}) {
  return (
    <div className="bg-white border border-gray-200 rounded-3xl shadow-sm overflow-hidden">
      {/* 내용 섹션 */}
      <div className="p-8 lg:p-10">
        {/* 감상 내용 */}
        <div className="prose prose-gray max-w-none mb-8">
          <div className="text-gray-800 text-lg leading-relaxed whitespace-pre-line font-medium">
            {contentText}
          </div>
        </div>

        {/* 별점 */}
        <div className="flex items-center justify-center gap-4 mb-8 p-6 bg-gray-50 rounded-2xl">
          <div className="flex items-center gap-1">
            {[1, 2, 3, 4, 5].map((value) => (
              <span
                key={value}
                className={`text-3xl transition-colors duration-200 ${
                  value <= rating ? "text-yellow-400" : "text-gray-300"
                }`}
              >
                ★
              </span>
            ))}
          </div>
          <div className="text-xl font-bold text-gray-900">
            {rating.toFixed(1)}
          </div>
          <div className="text-base text-gray-500 font-medium">/ 5.0</div>
        </div>

        {/* 태그 */}
        {tagNames.length > 0 && (
          <div className="mb-8">
            <h3 className="text-sm font-semibold text-gray-500 uppercase tracking-wider mb-4">
              감정 태그
            </h3>
            <div className="flex flex-wrap gap-3">
              {tagNames.map((tag, index) => (
                <span
                  key={`tag-${index}`}
                  className="bg-gray-900 text-white px-4 py-2 rounded-full text-sm font-medium hover:bg-gray-700 transition-colors duration-200"
                >
                  #{tag}
                </span>
              ))}
            </div>
          </div>
        )}
      </div>

      {/* 버튼 섹션 */}
      <div className="bg-gray-50 px-8 lg:px-10 py-6 border-t border-gray-200">
        <div className="flex justify-end gap-3">
          <button
            onClick={onEdit}
            className="px-6 py-3 rounded-xl border-2 border-gray-300 text-gray-700 font-semibold hover:border-gray-900 hover:text-gray-900 transition-all duration-200 hover:-translate-y-0.5"
          >
            수정하기
          </button>
          <button
            onClick={onDelete}
            className="px-6 py-3 rounded-xl border-2 border-red-200 text-red-600 font-semibold hover:border-red-500 hover:text-red-700 hover:bg-red-50 transition-all duration-200 hover:-translate-y-0.5"
          >
            삭제하기
          </button>
        </div>
      </div>
    </div>
  );
}
