import type { NextConfig } from "next";

const backendApi = process.env.NEXT_PUBLIC_BACKEND_API || "http://localhost:8080";

const nextConfig: NextConfig = {
  eslint: {
    ignoreDuringBuilds: true,
  },
  images: {
    domains: [
      "i.scdn.co",
      "image.tmdb.org",
      "www.nl.go.kr",
    ],
  },

  async rewrites() {
    return [
      {
        source: "/api/:path*",
        destination: `${backendApi}/api/:path*`,
      },
    ];
  },
};

export default nextConfig;
