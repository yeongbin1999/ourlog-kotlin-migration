"use client";

import { useRouter } from "next/navigation";
import { useState } from "react";
import { motion } from "framer-motion";

const CONTENT_TYPES = [
  { key: "MOVIE", label: "영화" },
  { key: "BOOK", label: "도서" },
  { key: "MUSIC", label: "음악" },
] as const;

export default function SelectTypePage() {
  const router = useRouter();
  const [selectedType, setSelectedType] = useState("");

  const handleTypeSelect = (type: string) => {
    setSelectedType(type);
    setTimeout(() => {
      router.push(`/diaries/select-content?type=${type}`);
    }, 250);
  };

  return (
    <div className="min-h-screen bg-white flex items-center justify-center p-6 font-sans">
      <div className="bg-white rounded-2xl px-10 py-14 w-full max-w-2xl shadow-xl border border-gray-100">
        {/* 제목 */}
        <div className="text-center mb-12">
          <h1 className="text-3xl font-bold text-gray-900 mb-4 tracking-tight leading-tight">
            어떤 작품을 감상하셨나요?
          </h1>
          <p className="text-lg text-gray-600 tracking-tight leading-relaxed">
            기록하고 싶은 콘텐츠 종류를 선택해주세요.
          </p>
        </div>

        {/* 버튼 영역 */}
        <div className="flex flex-col gap-3">
          {CONTENT_TYPES.map((type) => (
            <motion.button
              key={type.key}
              whileTap={{ scale: 0.97 }}
              onClick={() => handleTypeSelect(type.key)}
              className={`w-full py-4 rounded-xl text-lg font-semibold transition-all duration-200 tracking-wide ${
                selectedType === type.key
                  ? "bg-black text-white scale-100"
                  : "bg-gray-50 text-gray-900 hover:bg-gray-100 border border-gray-200"
              }`}
            >
              {type.label}
            </motion.button>
          ))}
        </div>

        {/* 안내 텍스트 */}
        <div className="text-center mt-8">
          <p className="text-gray-400 text-sm">
            선택하시면 다음 단계로 이동합니다.
          </p>
        </div>
      </div>
    </div>
  );
}
