"use client";

import { useSearchParams } from "next/navigation";
import { useEffect, useState } from "react";
import axios from "axios";
import Link from "next/link";

type UserProfileResponse = {
  userId: number;
  nickname: string;
  profileImageUrl: string | null;
};

const DEFAULT_IMAGE = "/default-profile.png";

const highlightKeyword = (text: string, keyword: string) => {
  if (!keyword.trim()) return text;
  const regex = new RegExp(`(${keyword})`, "gi");
  const parts = text.split(regex);
  return parts.map((part, index) =>
    regex.test(part) ? (
      <span key={index} className="text-blue-600 font-semibold">
        {part}
      </span>
    ) : (
      <span key={index}>{part}</span>
    )
  );
};

const SearchClient = () => {
  const searchParams = useSearchParams();
  const keyword = searchParams.get("keyword") || "";
  const [results, setResults] = useState<UserProfileResponse[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (!keyword) return;
    setLoading(true);
    axios
      .get(`${process.env.NEXT_PUBLIC_API_BASE_URL}/api/v1/users/search?keyword=${encodeURIComponent(keyword)}`)
      .then((res) => setResults(res.data))
      .finally(() => setLoading(false));
  }, [keyword]);

  return (
    <div className="p-8">
      {loading ? (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {[...Array(6)].map((_, i) => (
            <div
              key={i}
              className="flex items-center space-x-4 p-4 border rounded-md animate-pulse"
            >
              <div className="w-12 h-12 rounded-full bg-gray-300" />
              <div className="flex-1 h-4 bg-gray-300 rounded w-2/3" />
            </div>
          ))}
        </div>
      ) : results.length === 0 ? (
        <div className="text-center text-gray-500 text-lg mt-8">
          No users found for &quot;<span className="font-semibold text-black">{keyword}</span>&quot;
        </div>
      ) : (
        <ul className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {results.map((user) => (
            <Link key={user.userId} href={`/profile/${user.userId}`}>
              <li className="cursor-pointer flex items-center space-x-4 border p-4 rounded-md hover:bg-gray-100 transition">
                <img
                  src={user.profileImageUrl || DEFAULT_IMAGE}
                  alt={user.nickname}
                  className="w-12 h-12 rounded-full object-cover"
                />
                <span className="text-lg font-semibold">
                  {highlightKeyword(user.nickname, keyword)}
                </span>
              </li>
            </Link>
          ))}
        </ul>
      )}
    </div>
  );
};

export default SearchClient;
