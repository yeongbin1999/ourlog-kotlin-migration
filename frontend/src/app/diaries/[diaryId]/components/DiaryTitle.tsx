import Link from "next/link";

export default function DiaryTitle({ title }: { title: string }) {
  return (
    <div className="space-y-10">
      {/* 뒤로가기 버튼 */}
      <Link
        href="/"
        className="inline-flex items-center gap-2 text-gray-500 hover:text-gray-900 transition-colors duration-200 group"
      >
        <svg
          className="w-5 h-5 transition-transform duration-200 group-hover:-translate-x-1"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path
            strokeLinecap="round"
            strokeLinejoin="round"
            strokeWidth={2}
            d="M10 19l-7-7m0 0l7-7m-7 7h18"
          />
        </svg>
        <span className="text-sm font-medium tracking-wide">
          홈으로 돌아가기
        </span>
      </Link>

      {/* 제목 + 밑줄 */}
      <div className="text-left">
        <h1 className="inline-block text-3xl lg:text-4xl font-bold text-gray-900 tracking-tight leading-snug">
          {title}
        </h1>
        <div className="h-[2px] bg-gray-900 rounded-full mt-1 w-full" />
      </div>
    </div>
  );
}
