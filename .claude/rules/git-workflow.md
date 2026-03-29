# Git Milestone Workflow (랄프톤 전용 규칙)

## Commit Convention
- Conventional Commits: `feat:`, `fix:`, `test:`, `chore:`, `docs:`, `refactor:`
- 커밋 메시지는 영어로 작성, 본문에 한국어 설명 가능

## Ralph Loop Git Integration
- 마일스톤 하나를 달성할 때마다 (Ralph Loop 한 사이클이 끝날 때마다) 다음을 수행:
  1. `git status`로 변경점 확인
  2. 관련 파일만 `git add` (민감한 파일 제외)
  3. `git commit -m "feat: [마일스톤 핵심 내용 요약]"` (Conventional Commits)
  4. `git push origin main` (또는 현재 작업 브랜치)
- 이 과정이 성공적으로 끝난 뒤에만 다음 마일스톤으로 넘어간다

## Branch Strategy
- `main`: 안정 브랜치
- Feature 브랜치: 필요시 `feature/[기능명]`으로 분기

## Commit Granularity
- 각 Ralph Loop 사이클 = 1 마일스톤 커밋
- TDD Red-Green-Refactor 사이클은 하나의 마일스톤 커밋으로 묶기
- 중간 작업 저장이 필요하면 `git stash` 활용
