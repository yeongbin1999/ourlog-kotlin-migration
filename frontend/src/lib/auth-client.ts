import axios from 'axios';

/**
 * @file auth-client.ts
 * @description 인증 관련 API(로그인, 로그아웃 등)를 호출하기 위한 Axios 인스턴스입니다.
 * @module lib/auth-client
 */

// API의 기본 URL을 환경 변수에서 가져오거나, 없을 경우 기본값을 사용합니다.
const API_BASE_URL = process.env.NEXT_PUBLIC_API_URL || '/';

/**
 * 인증용 Axios 인스턴스
 *
 * 로그인, 로그아웃, 토큰 재발급 등 인증 관련 API를 호출할 때 사용됩니다.
 * `withCredentials: true` 옵션을 통해 요청 시 쿠키를 포함하도록 설정합니다.
 * 이 인스턴스는 요청/응답 인터셉터를 설정하지 않아, 순수한 API 호출을 보장합니다.
 * (예: 토큰 재발급 요청이 무한 루프에 빠지는 것을 방지)
 */
const authClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true,
});

export default authClient;
