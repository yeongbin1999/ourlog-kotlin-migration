"use client";

import dynamic from "next/dynamic";
import { Suspense } from "react";

const SearchClient = dynamic(() => import("./SearchClient"), { ssr: false });

const SearchPage = () => {
  return (
    <main className="pt-24">
      <Suspense fallback={<div className="pt-24 text-center">Loading...</div>}>
        <SearchClient />
      </Suspense>
    </main>
  );
};

export default SearchPage;