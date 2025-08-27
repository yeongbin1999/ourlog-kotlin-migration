"use client";

import React, { useEffect, useState } from "react";

export default function Home() {
  const [visibleElements, setVisibleElements] = useState(new Set());

  useEffect(() => {
    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry, idx) => {
          if (entry.isIntersecting) {
            setTimeout(() => {
              setVisibleElements((prev) => new Set([...prev, entry.target.id]));
            }, idx * 150);
          }
        });
      },
      { threshold: 0.1 }
    );

    const elements = document.querySelectorAll("[data-fade]");
    elements.forEach((el) => observer.observe(el));

    return () => observer.disconnect();
  }, []);

  const handleCTAClick = () => {
    alert("회원가입/로그인 페이지로 이동합니다!");
  };

  const handleLearnMore = () => {
    document.getElementById("features")?.scrollIntoView({ behavior: "smooth" });
  };

  const fadeClass = (id: string) =>
    `transition-all duration-1000 ease-[cubic-bezier(0.22,1,0.36,1)] transform ${
      visibleElements.has(id)
        ? "opacity-100 translate-y-0 scale-100"
        : "opacity-0 translate-y-8 scale-[0.98]"
    }`;

  return (
    <div className="bg-gray-50 text-gray-900">
      {/* Hero */}
      <section className="bg-gradient-to-b from-white to-gray-50 min-h-screen flex items-center">
        <div className="max-w-6xl mx-auto px-6">
          <div className="grid lg:grid-cols-2 gap-20 items-center">
            <div className={fadeClass("hero-text")} data-fade id="hero-text">
              <h1 className="text-5xl lg:text-6xl font-bold mb-6 leading-tight tracking-tight">
                당신의 감상을<br />기록하세요 ✍️
              </h1>
              <p className="text-xl text-gray-600 mb-10 leading-relaxed">
                영화, 책, 음악까지.<br />
                모든 콘텐츠를 하나의 감상일기로 관리하세요.
              </p>
              <div className="flex flex-col sm:flex-row gap-4">
                <button
                  onClick={handleCTAClick}
                  className="bg-gray-900 text-white px-8 py-4 rounded-lg text-base font-semibold transition-all duration-300 transform hover:-translate-y-1 hover:shadow-xl"
                >
                  지금 시작하기
                </button>
                <button
                  onClick={handleLearnMore}
                  className="bg-transparent text-gray-600 px-8 py-4 border-2 border-gray-300 rounded-lg text-base font-semibold transition-all duration-300 hover:border-gray-900 hover:text-gray-900 hover:-translate-y-1 hover:shadow-md"
                >
                  더 알아보기
                </button>
              </div>
            </div>

            {/* Hero 카드 */}
            <div
              className={`${fadeClass("hero-cards")} relative w-80 h-96`}
              data-fade
              id="hero-cards"
            >
              {[
                {
                  icon: "📚",
                  title: "미드나잇 라이브러리",
                  desc: "인생의 선택들에 대해 깊이 생각하게 만드는 책이었다. 후회와 가능성이라는 주제를 판타지적 설정으로 풀어낸 것이 인상적...",
                  rate: "★★★★★",
                  score: "5.0",
                  pos: "top-0 left-0 -rotate-2 z-30",
                },
                {
                  icon: "🎬",
                  title: "라라랜드",
                  desc: "음악과 영상미가 정말 아름다웠다...",
                  rate: "★★★★☆",
                  score: "4.2",
                  pos: "top-5 right-0 rotate-3 z-20",
                },
                {
                  icon: "🎵",
                  title: "Stay - The Kid LAROI",
                  desc: "중독성 있는 멜로디와 가사가 너무 좋다...",
                  rate: "★★★★☆",
                  score: "4.5",
                  pos: "bottom-0 left-5 -rotate-1 z-10",
                },
              ].map((card, i) => (
                <div
                  key={i}
                  className={`absolute w-70 h-80 bg-white rounded-2xl border border-gray-200 p-6 shadow-lg transform ${card.pos} transition-all duration-500 hover:rotate-0 hover:scale-105 hover:shadow-2xl`}
                >
                  <div className="mb-4">
                    <span className="bg-gray-100 text-gray-700 px-2 py-1 rounded text-xs font-semibold">
                      {card.icon}
                    </span>
                  </div>
                  <h3 className="text-lg font-bold text-gray-900 mb-3">
                    {card.title}
                  </h3>
                  <p className="text-sm text-gray-600 mb-4 leading-relaxed">
                    {card.desc}
                  </p>
                  <div className="flex items-center gap-2 mt-auto">
                    <span className="text-yellow-400">{card.rate}</span>
                    <span className="text-sm text-gray-600">{card.score}</span>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>
      </section>

      {/* Features */}
      <section id="features" className="py-32 bg-white">
        <div className="max-w-6xl mx-auto px-6">
          <div
            className={`${fadeClass("features-header")} text-center mb-20`}
            data-fade
            id="features-header"
          >
            <h2 className="text-4xl font-bold mb-4">주요 기능</h2>
            <p className="text-lg text-gray-600 max-w-2xl mx-auto">
              OurLog와 함께 당신만의 감상 세계를 체계적으로 관리하세요
            </p>
          </div>

          <div className="grid md:grid-cols-2 gap-8">
            {[
              {
                icon: "✍️",
                title: "감상일기 작성",
                desc: "영화, 책, 드라마, 음악에 대한 당신의 생각과 감정을 자유롭게 기록하고 별점으로 평가해보세요.",
              },
              {
                icon: "🔍",
                title: "콘텐츠 탐색",
                desc: "다양한 장르의 콘텐츠를 쉽게 검색하고 발견하세요. 개인화된 추천으로 새로운 작품을 만나보세요.",
              },
              {
                icon: "👥",
                title: "친구와 감상 공유",
                desc: "친구들과 감상을 공유하고 서로의 취향을 발견해보세요.",
              },
              {
                icon: "📈",
                title: "통계 보기",
                desc: "당신의 감상 패턴과 취향을 한눈에 확인하세요.",
              },
            ].map((feature, i) => (
              <div
                key={i}
                className={`${fadeClass(
                  `feature-${i}`
                )} bg-gray-50 border border-gray-200 rounded-2xl p-10 hover:bg-white hover:border-gray-300 hover:-translate-y-2 hover:shadow-lg`}
                data-fade
                id={`feature-${i}`}
              >
                <div className="w-12 h-12 bg-gray-900 text-white rounded-xl flex items-center justify-center text-xl mb-5">
                  {feature.icon}
                </div>
                <h3 className="text-xl font-bold mb-3">{feature.title}</h3>
                <p className="text-gray-600">{feature.desc}</p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Workflow */}
      <section className="py-32 bg-gray-50">
        <div className="max-w-6xl mx-auto px-6">
          <div
            className={`${fadeClass("workflow-header")} text-center mb-20`}
            data-fade
            id="workflow-header"
          >
            <h2 className="text-4xl font-bold text-gray-900 mb-6 tracking-tight">
              간단한 3단계
            </h2>
            <p className="text-lg text-gray-600 font-light tracking-wide leading-relaxed">
              누구나 쉽게 시작할 수 있는<br />
              감상 기록 여정을 안내합니다
            </p>
          </div>

          <div className="grid md:grid-cols-3 gap-16">
            {[
              {
                num: "01",
                title: "검색",
                desc: "보고 싶은 영화, 읽고 싶은 책,\n들어본 음악을 검색해보세요",
              },
              {
                num: "02",
                title: "감상",
                desc: "작품에 대한 감상과 평점을\n자유롭게 기록해보세요",
              },
              {
                num: "03",
                title: "공유",
                desc: "친구들과 감상을 공유하고\n새로운 작품을 추천받아보세요",
              },
            ].map((step, i) => (
              <div
                key={i}
                className={`${fadeClass(
                  `workflow-step-${i}`
                )} text-center transition-all duration-500 hover:-translate-y-2`}
                data-fade
                id={`workflow-step-${i}`}
              >
                <div className="w-16 h-16 bg-gray-900 text-white rounded-full flex items-center justify-center text-xl font-bold mx-auto mb-6 transition-transform duration-300 hover:scale-110">
                  {step.num}
                </div>
                <h3 className="text-xl font-bold text-gray-900 mb-4 tracking-tight">
                  {step.title}
                </h3>
                <p className="text-gray-600 leading-relaxed text-base font-light whitespace-pre-line tracking-wide">
                  {step.desc}
                </p>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Showcase */}
      <section className="py-32 bg-gray-900">
        <div className="max-w-6xl mx-auto px-6">
          <div
            className={`${fadeClass("showcase-header")} text-center mb-20`}
            data-fade
            id="showcase-header"
          >
            <h2 className="text-4xl font-bold text-white mb-6">실제 사용 예시</h2>
            <p className="text-2xl text-gray-300 font-light">
              다른 사용자들의 줄거리와 감상일기를 살펴보세요
            </p>
          </div>

          <div className="grid md:grid-cols-2 gap-12">
            {[
              {
                poster:
                  "https://image.tmdb.org/t/p/w500/mSi0gskYpmf1FbXngM37s2HppXh.jpg",
                title: "기생충",
                creator: "봉준호",
                year: "2019",
                genre: "코미디, 스릴러, 드라마",
                summary:
                  "전원 백수로 살아가던 기택 가족. 막내 기우가 명문대생의 소개로 박 사장 집에서 과외를 시작하게 되고, 덕분에 온 가족이 박 사장 집에 입성하게 되는데...",
                review:
                  "봉준호 감독의 연출력이 뛰어났고 메시지가 강렬했습니다.",
                rating: "★★★★★ 5.0",
                tags: ["재미", "충격"],
                user: "김영화",
                role: "영화 애호가",
              },
              {
                poster:
                  "https://www.nl.go.kr/seoji/fu/ecip/dbfiles/CIP_FILES_TBL/2516591_3.jpg",
                title: "희랍어 시간",
                creator: "한강",
                year: "2011",
                genre: "한국문학, 소설",
                summary:
                  "언어를 잃어버린 여자와 그녀를 사랑하는 남자의 이야기. 상실과 회복, 사랑과 언어에 대한 깊이 있는 성찰...",
                review:
                  "한강 특유의 섬세한 문체와 감정선이 인상적이었어요.",
                rating: "★★★★★ 4.8",
                tags: ["감동", "성찰"],
                user: "박독서",
                role: "책벌레",
              },
            ].map((example, index) => (
              <div
                key={index}
                className={`${fadeClass(
                  `example-${index}`
                )} bg-white rounded-3xl overflow-hidden transition-all duration-500 hover:-translate-y-2 hover:scale-[1.02] hover:shadow-2xl`}
                data-fade
                id={`example-${index}`}
              >
                <div className="p-12">
                  <div className="flex gap-8 mb-8">
                    <div className="w-24 h-36 rounded-lg overflow-hidden shadow-lg flex-shrink-0 transform transition-transform duration-300 hover:scale-105">
                      <img
                        src={example.poster}
                        alt={example.title}
                        className="w-full h-full object-cover"
                      />
                    </div>
                    <div className="flex-1">
                      <h3 className="text-3xl font-bold text-gray-900 mb-2">
                        {example.title}
                      </h3>
                      <p className="text-gray-600 mb-2 text-lg font-medium">
                        {example.creator}
                      </p>
                      <p className="text-gray-500 text-sm mb-2">
                        {example.year}
                      </p>
                      <p className="text-gray-500 text-sm">{example.genre}</p>
                    </div>
                  </div>
                  <div className="mb-6">
                    <h4 className="text-gray-900 font-bold mb-3 text-lg">
                      줄거리
                    </h4>
                    <p className="text-gray-600 text-sm leading-relaxed">
                      {example.summary}
                    </p>
                  </div>
                  <div className="mb-8">
                    <h4 className="text-gray-900 font-bold mb-3 text-lg">
                      감상일기
                    </h4>
                    <p className="text-gray-600 text-sm leading-relaxed">
                      {example.review}
                    </p>
                  </div>
                  <div className="flex items-center gap-4 mb-8">
                    <span className="text-yellow-500 text-2xl">
                      {example.rating}
                    </span>
                  </div>
                  <div className="mb-8">
                    <h4 className="text-gray-900 font-bold mb-3 text-lg">
                      감정 태그
                    </h4>
                    <div className="flex gap-3 flex-wrap">
                      {example.tags.map((tag, tagIndex) => (
                        <span
                          key={tagIndex}
                          className="bg-gray-900 text-white px-4 py-2 rounded-full text-sm font-semibold"
                        >
                          #{tag}
                        </span>
                      ))}
                    </div>
                  </div>
                  <div className="flex items-center gap-4 pt-6 border-t border-gray-200">
                    <div className="w-12 h-12 bg-gray-400 text-white rounded-full flex items-center justify-center text-lg font-bold">
                      {example.user[0]}
                    </div>
                    <div>
                      <p className="text-gray-900 font-bold text-lg">
                        {example.user}
                      </p>
                      <p className="text-gray-500 text-sm">{example.role}</p>
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* CTA */}
      <section className="py-32 bg-white text-center">
        <div className={`${fadeClass("cta")}`} data-fade id="cta">
          <h2 className="text-5xl font-bold mb-4">지금 바로 시작하세요 🚀</h2>
          <p className="text-lg text-gray-500 mb-10">
            당신만의 감상 여행을 OurLog와 함께 시작해보세요
          </p>
          <button
            onClick={handleCTAClick}
            className="bg-gray-900 text-white px-16 py-6 rounded-full text-2xl font-semibold hover:bg-gray-800 transition-all duration-300 transform hover:-translate-y-1 hover:shadow-xl"
          >
            감상 시작하기
          </button>
        </div>
      </section>
    </div>
  );
}
