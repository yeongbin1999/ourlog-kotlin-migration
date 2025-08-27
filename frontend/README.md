# OurLog Frontend

Next.js 기반의 다이어리 애플리케이션 프론트엔드입니다.

## 🚀 OpenAPI 자동화 설정

이 프로젝트는 **Orval**을 사용하여 백엔드 OpenAPI 스펙에서 TypeScript 클라이언트를 자동 생성합니다.

### 📦 설치

```bash
npm install
```

### 🔧 API 클라이언트 생성

1. **OpenAPI 스펙 파일 준비**
   - 백엔드에서 OpenAPI 스펙을 `openapi.json` 파일로 export
   - 또는 백엔드 서버의 `/api-docs` 엔드포인트 URL 사용

2. **API 클라이언트 생성**
   ```bash
   npm run generate:api
   ```

3. **생성된 파일들**
   - `src/generated/api/` - API 함수들
   - `src/generated/model/` - TypeScript 타입 정의들

### 🎯 사용법

#### 기본 API 훅 사용
```typescript
import { useDiaryList, useCreateDiary } from '../hooks/useGeneratedApi';

function DiaryList() {
  const { data: diaries, isLoading } = useDiaryList();
  const createDiary = useCreateDiary();

  const handleCreate = () => {
    createDiary.mutate({
      title: '새 다이어리',
      content: '내용...',
    });
  };

  return (
    <div>
      {isLoading ? '로딩 중...' : (
        diaries?.map(diary => <div key={diary.id}>{diary.title}</div>)
      )}
    </div>
  );
}
```

#### 직접 생성된 API 함수 사용
```typescript
import { getDiaryDetail } from '../generated/api';
import type { Diary } from '../generated/model';

// 타입 안전성이 보장된 API 호출
const diary: Diary = await getDiaryDetail('diary-id');
```

### 🔄 개발 워크플로우

1. 백엔드 API 변경 시 OpenAPI 스펙 업데이트
2. `npm run generate:api` 실행
3. 생성된 타입과 함수들 자동으로 반영
4. 컴파일 에러로 타입 불일치 즉시 확인

### ⚙️ 설정 파일

- `orval.config.ts` - Orval 설정
- `src/lib/api-client.ts` - 커스텀 axios 인스턴스
- `src/hooks/useGeneratedApi.ts` - React Query 훅들

### 🎯 주요 장점

- **타입 안전성**: 백엔드와 프론트엔드 타입 동기화
- **자동 완성**: IDE에서 API 함수와 타입 자동 완성
- **에러 방지**: 컴파일 타임에 API 호출 오류 감지
- **개발 효율성**: API 변경 시 자동으로 클라이언트 업데이트
- **일관성**: 모든 API 호출이 동일한 패턴 사용

## 🛠️ 개발

```bash
# 개발 서버 실행
npm run dev

# 빌드
npm run build

# 린트
npm run lint
```

## 📁 프로젝트 구조

```
src/
├── app/                 # Next.js App Router
├── components/          # 재사용 가능한 컴포넌트
├── hooks/              # 커스텀 훅들
├── lib/                # 유틸리티 함수들
├── stores/             # Zustand 상태 관리
├── generated/          # Orval로 생성된 API 클라이언트
│   ├── api/           # API 함수들
│   └── model/         # 타입 정의들
└── ...
```
