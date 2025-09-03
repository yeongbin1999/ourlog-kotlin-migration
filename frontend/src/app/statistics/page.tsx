"use client";

import { useEffect } from "react";
import { useRouter } from "next/navigation";
import ContentStatsDashboard from "./components/content-stats-dashboard";
import { useAuthStore } from "@/stores/authStore";

export default function Page() {
  const router = useRouter();
  const { isAuthenticated, isLoading } = useAuthStore();

  useEffect(() => {
    if (!isLoading && !isAuthenticated) {
      router.push("/login");
    }
  }, [isAuthenticated, isLoading, router]);

  if (isLoading || !isAuthenticated) {
    return (
      <main className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-gray-900 mx-auto mb-4"></div>
          <p className="text-gray-600 font-medium">인증 확인 중...</p>
        </div>
      </main>
    );
  }

  return <ContentStatsDashboard />;
}
