"use client";

import { useSearchParams } from "next/navigation";
import DiaryForm from "@/components/diary/DiaryForm";

export default function DiaryWriteClient() {
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
    return (
      <div className="p-6 text-center text-gray-500">
        잘못된 접근입니다. 콘텐츠를 먼저 선택해주세요.
      </div>
    );
  }

  return (
    <DiaryForm
      mode="create"
      externalId={externalId}
      type={type}
      title={contentTitle}
      creatorName={creatorName}
      description={description}
      posterUrl={posterUrl}
      releasedAt={releasedAt}
      genres={genres}
      initialValues={{
        title: "",
        contentText: "",
        isPublic: true,
        rating: 0,
        tagNames: [],
        genreNames: genres,
        ottNames: [],
      }}
    />
  );
}
