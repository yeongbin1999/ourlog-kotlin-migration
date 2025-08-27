# ourlog-kotlin-migration

프로그래머스 백엔드 데브코스 6기 8회차 3차 프로젝트

---

# 🚀 개발 및 배포 프로세스 & Git 컨벤션 가이드

본 문서는 `dev` 브랜치에서 개발하고, `main` 브랜치에서 배포하는 프로젝트의  
Git 브랜치 전략, 커밋/PR 컨벤션, GitHub 자동화 워크플로우, 브랜치 보호 규칙을 정리한 가이드입니다.

---

## 1. 브랜치 전략

- **`dev`**: 개발 브랜치
  - 모든 기능 개발은 feature 브랜치를 만들어 `dev`에 PR로 머지
  - 자동 브랜치 생성 및 Draft PR 대상 브랜치
  - 직접 push 및 외부 PR은 제한

- **`main`**: 배포 브랜치
  - 안정화된 코드를 머지하여 배포
  - `dev` → `main` PR은 관리자 혹은 릴리즈 담당자만 생성 및 승인 가능
  - 직접 push 및 외부 PR 제한

---

## 2. 커밋 및 PR 컨벤션

- **type**: feat, fix, chore, refactor, docs, infra
- **scope**: fe, be

예시:
- `feat(fe): 로그인 페이지 UI 추가`
- `fix(be): API 인증 오류 수정`

PR 제목은 이슈 제목과 동일하며 `(#번호)`가 자동으로 붙습니다.  
예: `feat(be): 로그인 API 추가 (#23)`

---

## 3. 개발 플로우

1. GitHub Projects에서 이슈 생성
- Projects > Todo 컬럼에서 새 이슈 생성
  ```
  feat(be): 로그인 API 추가  
  fix(fe): 회원가입 UI 오류 수정  
  infra: CI/CD 파이프라인 설정  
  ```
2. 이슈 생성 시 `dev` 기준으로 브랜치 자동 생성
  - 예: `feat(be): ~` → `feat/be/123`
3. 생성된 브랜치로 Draft PR 자동 생성 (`Closes #123` 자동 포함)
4. 로컬에서 개발 후 커밋/푸시 → Draft → Ready for Review 전환
5. 리뷰 승인 및 CI 통과 후 `dev`에 머지 → 이슈 자동 종료
6. 충분히 테스트 후 `dev` → `main` PR 생성 및 머지 (관리자 전용)
7. `main` 머지 시 자동 배포

---

## 4. GitHub Actions 자동화 워크플로우

- **Issue → Branch 자동 생성**
  - 이슈가 생성되면 `dev` 브랜치 기준으로 브랜치를 자동 생성
  - 브랜치명은 이슈 제목의 type/scope/번호를 기준으로 규칙적으로 생성

- **Auto PR Title from Issue**
  - 브랜치명에서 이슈 번호를 추출
  - 이슈 제목을 가져와 PR 제목을 `이슈제목 (#번호)` 형태로 자동 변경

- **Close linked issues on dev merge**
  - `dev` 브랜치에 PR이 머지되면
  - PR 제목/본문의 `(#번호)` 패턴을 찾아 연결된 이슈를 자동으로 종료

---

## 5. 브랜치 보호 규칙

| 브랜치 | 보호 규칙 |
|--------|-----------|
| main   | 직접 push 금지, Force push 금지, 모든 CI 통과 필수, 관리자만 PR 가능 |
| dev    | 직접 push 금지, 리뷰 최소 2명 필수, 모든 CI 통과 필수 |

---

## 6. 핵심 요약

- **이슈 제목 규칙**
  - `type(scope): 내용`
  - type: feat, fix, chore, refactor, docs, infra
  - scope: fe, be
  - 예: `feat(be): 로그인 API 추가`

- **전체 플로우**
  1. 이슈 생성 → 브랜치 & Draft PR 자동 생성
  2. 개발 후 PR → 리뷰 승인 및 CI 통과 → `dev` 머지 → 이슈 자동 종료
  3. 충분히 테스트 후 `dev` → `main` PR → 관리자 승인 및 자동 배포

- **PR 제목과 이슈는 자동으로 연결됩니다.**

---

> 이 프로세스를 통해 개발과 배포를 분리하여 안정성과 생산성을 동시에 높입니다.  
> 협업 시 가이드를 꼭 숙지하고 따라주세요.

---

*Last updated: 2025-07-23*