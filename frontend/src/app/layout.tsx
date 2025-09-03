import type { Metadata } from "next";
import { Inter, Anton } from "next/font/google";
import "./globals.css";

import Header from "@/components/layout/Header";
import Footer from "@/components/layout/Footer";
import QueryProvider from "@/components/QueryProvider";
import { Toaster } from 'sonner';

const inter = Inter({
  variable: "--font-inter",
  subsets: ["latin"],
});

const anton = Anton({
  variable: "--font-anton",
  weight: "400",
  subsets: ["latin"],
});

export const metadata: Metadata = {
  metadataBase: new URL('http://localhost:8080'),
  title: "OURLOG",
  description: "OURLOG is a platform for writing and sharing your thoughts on movies, dramas, books, and music. Create meaningful diary entries about your cultural experiences and connect with others who share your interests.",
  icons: {
    icon: '/favicon.svg',
  },
  openGraph: {
    title: 'OURLOG',
    description: '영화, 드라마, 책, 음악 감상 일기를 쓰는 플랫폼',
    images: ['/favicon.svg'],
  },
  twitter: {
    card: 'summary',
    title: 'OURLOG',
    description: '영화, 드라마, 책, 음악 감상 일기를 쓰는 플랫폼',
    images: ['/favicon.svg'],
  },
};

export default function RootLayout({
  children,
}: Readonly<{
  children: React.ReactNode;
}>) {
  return (
    <html lang="kr">
      <body
        className={`${inter.variable} ${anton.variable} font-sans antialiased`}
      >
        <QueryProvider>
          <Header />
          <main className="min-h-screen pt-24 px-4 pb-8">{children}</main>
          <Footer />
          <Toaster position="top-center" />
        </QueryProvider>
      </body>
    </html>
  );
}