"use client";

import { Suspense } from "react";
import { useSearchParams } from "next/navigation";
import DiaryForm from "@/components/diary/DiaryForm";

function DiaryWriteClient() {
  const searchParams = useSearchParams();

  const externalId = searchParams.get("externalId") ?? "";
  const type = (searchParams.get("type") ?? "MOVIE") as "MOVIE" | "BOOK" | "MUSIC";

  const contentTitle = searchParams.get("title") ?? "";
  const creatorName = searchParams.get("creatorName") ?? "";
  const description = searchParams.get("description") ?? "";
  const posterUrl = searchParams.get("posterUrl") ?? "";
  const releasedAt = searchParams.get("releasedAt") ?? "";
  const genres = searchParams.get("genres")?.split(",") ?? [];

  if (!externalId || !type) {
    return <div className="p-6 text-center text-gray-500">잘못된 접근입니다. 콘텐츠를 먼저 선택해주세요.</div>;
  }

  return (
    <DiaryForm
      mode="create"
      initialValues={{
        title: "",
        contentText: "",
        isPublic: true,
        rating: 0,
        tagNames: [],
        ottNames: [],
        genreNames: genres,
      }}
      externalId={externalId}
      type={type}
      title={contentTitle}
      creatorName={creatorName}
      description={description}
      posterUrl={posterUrl}
      releasedAt={releasedAt}
      genres={genres}
    />
  );  
}

function LoadingFallback() {
  return (
    <div className="p-6 text-center">
      <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-gray-900 mx-auto"></div>
      <p className="mt-4 text-gray-500">로딩 중...</p>
    </div>
  );
}

export default function DiaryWritePage() {
  return (
    <Suspense fallback={<LoadingFallback />}>
      <DiaryWriteClient />
    </Suspense>
  );
}