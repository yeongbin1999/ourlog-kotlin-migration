"use client";

import { useSearchParams } from "next/navigation";
import { useEffect, useState } from "react";
import Link from "next/link";
import { FaSearch } from "react-icons/fa";
import Image from "next/image";
import { axiosInstance } from "@/lib/api-client";

type UserProfileResponse = {
  userId: number;
  nickname: string;
  profileImageUrl: string | null;
  email?: string;
  bio?: string;
};

const DEFAULT_IMAGE = "/images/no-image.png";

const escapeRegex = (s: string) => s.replace(/[.*+?^${}()|[\]\\]/g, "\\$&");

const highlightKeyword = (text: string, keyword: string) => {
  if (!keyword?.trim()) return text;
  const safe = escapeRegex(keyword);
  const parts = text.split(new RegExp(`(${safe})`, "gi"));
  return (
    <>
      {parts.map((part, i) =>
        part.toLowerCase() === keyword.toLowerCase() ? (
          <span key={i} className="text-sky-500 font-bold bg-sky-50 rounded-sm">
            {part}
          </span>
        ) : (
          part
        )
      )}
    </>
  );
};

const UserResultCard = ({ user, keyword }: { user: UserProfileResponse; keyword: string }) => (
  <Link href={`/profile/${user.userId}`} className="block group">
    <div className="flex items-center gap-4 p-4 bg-white rounded-xl border border-gray-200 group-hover:border-sky-400 group-hover:shadow-lg transition-all duration-300">
      <div className="relative w-16 h-16">
        <Image
          src={user.profileImageUrl || DEFAULT_IMAGE}
          alt={user.nickname}
          fill
          className="rounded-full object-cover border-2 border-gray-100 transition-transform duration-300 group-hover:scale-105"
          sizes="64px"
        />
      </div>
      <div className="flex-1 min-w-0">
        <p className="font-bold text-lg text-gray-900 truncate">
          {highlightKeyword(user.nickname, keyword)}
        </p>
        {user.bio && <p className="text-sm text-gray-500 truncate mt-1">{user.bio}</p>}
      </div>
    </div>
  </Link>
);

const SkeletonLoader = () => (
  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
    {[...Array(4)].map((_, i) => (
      <div key={i} className="flex items-center gap-4 p-4 bg-white rounded-xl border border-gray-200 animate-pulse">
        <div className="w-16 h-16 rounded-full bg-gray-200" />
        <div className="flex-1 space-y-2">
          <div className="h-5 bg-gray-200 rounded w-3/4" />
          <div className="h-4 bg-gray-200 rounded w-1/2" />
        </div>
      </div>
    ))}
  </div>
);

const EmptyResults = ({ keyword }: { keyword: string }) => (
  <div className="text-center py-24 bg-gray-50 rounded-2xl">
    <div className="inline-block p-5 bg-white border-2 border-gray-100 rounded-full mb-5">
      <FaSearch className="text-4xl text-gray-400" />
    </div>
    <h3 className="text-2xl font-bold text-gray-800">검색 결과가 없습니다</h3>
    <p className="text-gray-500 mt-2 text-lg">
      &quot;<span className="font-semibold text-sky-600">{keyword}</span>&quot;에 대한 사용자를 찾을 수 없습니다.
    </p>
  </div>
);

const SearchClient = () => {
  const searchParams = useSearchParams();
  const keyword = searchParams.get("keyword") || "";
  const [results, setResults] = useState<UserProfileResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!keyword.trim()) {
      setResults([]);
      setLoading(false);
      return;
    }
    setLoading(true);
    setError(null);

    axiosInstance
      .get(`/api/v1/users/search`, { params: { keyword, page: 0, size: 20 } }) // ✅ 대괄호 X
      .then((res) => {
        const data = res.data?.data ?? res.data;
        setResults(Array.isArray(data) ? data : data?.content ?? []);
      })
      .catch(() => setError("검색 중 오류가 발생했습니다."))
      .finally(() => setLoading(false));
  }, [keyword]);

  return (
    <div className="max-w-4xl mx-auto px-4 py-12">
      <header className="mb-10 text-center">
        <h1 className="text-4xl font-extrabold text-gray-900 tracking-tight">Search Results</h1>
        {keyword && !loading && (
          <p className="text-lg text-gray-500 mt-2">
            Found {results.length} users for &quot;<span className="font-semibold text-sky-600">{keyword}</span>&quot;
          </p>
        )}
      </header>

      <div>
        {loading ? (
          <SkeletonLoader />
        ) : error ? (
          <div className="text-center text-red-500 py-20">{error}</div>
        ) : results.length > 0 ? (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {results.map((user) => (
              <UserResultCard key={user.userId} user={user} keyword={keyword} />
            ))}
          </div>
        ) : keyword ? (
          <EmptyResults keyword={keyword} />
        ) : (
          <div className="text-center py-20 text-gray-500">
            <h3 className="text-xl font-semibold">검색을 시작해보세요</h3>
            <p>헤더의 검색창을 이용해 사용자를 찾아보세요.</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default SearchClient;
