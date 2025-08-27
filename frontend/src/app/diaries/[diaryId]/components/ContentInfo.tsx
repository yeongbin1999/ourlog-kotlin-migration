import { Content } from "../../types/detail";

export default function ContentInfo({
  content,
  genreNames,
  ottNames,
}: {
  content: Content;
  genreNames: string[];
  ottNames: string[];
}) {
  return (
    <div className="bg-white border border-gray-200 rounded-2xl shadow-sm overflow-hidden">
      <div className="p-6 lg:p-8">
        <div className="grid lg:grid-cols-5 gap-6">
          {/* 포스터 */}
          <div className="lg:col-span-2 flex justify-center">
            <div className="w-[90%] aspect-[3/4] bg-gray-100 rounded-xl overflow-hidden shadow-md">
              {content.posterUrl ? (
                <img
                  src={content.posterUrl}
                  alt={`${content.title} 포스터`}
                  className="w-full h-full object-cover hover:scale-105 transition-transform duration-300"
                />
              ) : (
                <div className="w-full h-full flex items-center justify-center text-gray-400 text-sm font-medium">
                  이미지 없음
                </div>
              )}
            </div>
          </div>

          {/* 텍스트 정보 */}
          <div className="lg:col-span-3 space-y-8">
            <div>
              <h2 className="text-2xl font-bold text-gray-900 mb-3 leading-snug">
                {content.title}
              </h2>
              <p className="text-gray-600 text-base leading-relaxed">
                {content.description}
              </p>
            </div>

            <div className="space-y-6">
              {/* 출시일 */}
              <div>
                <span className="text-sm font-semibold text-gray-500 uppercase tracking-wider">
                  출시일
                </span>
                <div className="mt-2 flex flex-wrap gap-2">
                  <span className="bg-gray-50 text-gray-700 px-3 py-1 rounded-full text-xs font-medium border border-gray-200">
                    {new Date(content.releasedAt).toLocaleDateString("ko-KR", {
                      year: "numeric",
                      month: "long",
                      day: "numeric",
                    })}
                  </span>
                </div>
              </div>

              {/* 장르 */}
              {genreNames.length > 0 && (
                <div>
                  <span className="text-sm font-semibold text-gray-500 uppercase tracking-wider">
                    장르
                  </span>
                  <div className="mt-2 flex flex-wrap gap-2">
                    {genreNames.map((genre, i) => (
                      <span
                        key={i}
                        className="bg-gray-100 text-gray-800 px-3 py-1 rounded-full text-xs font-medium border border-gray-200 hover:bg-gray-200 transition-colors duration-200"
                      >
                        {genre}
                      </span>
                    ))}
                  </div>
                </div>
              )}

              {/* OTT (영화일 경우만 표시) */}
              {content.type === "MOVIE" && ottNames.length > 0 && (
                <div>
                  <span className="text-sm font-semibold text-gray-500 uppercase tracking-wider">
                    시청 플랫폼
                  </span>
                  <div className="mt-2 flex flex-wrap gap-2">
                    {ottNames.map((ott, i) => (
                      <span
                        key={i}
                        className="bg-blue-50 text-blue-700 px-3 py-1 rounded-full text-xs font-medium border border-blue-200 hover:bg-blue-100 transition-colors duration-200"
                      >
                        {ott}
                      </span>
                    ))}
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
