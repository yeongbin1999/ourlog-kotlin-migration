"use client";

import dynamic from "next/dynamic";
import { Suspense } from "react";

const SearchClient = dynamic(() => import("./SearchClient"), { ssr: false });

const SearchPage = () => {
  return (
    <Suspense fallback={<div>Loading search page...</div>}>
      <SearchClient />
    </Suspense>
  );
};

export default SearchPage;
