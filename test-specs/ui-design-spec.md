# UI/UX Design Specification — Physical AI Sales Context App

## Overview

A demo-quality Android app targeting Salesforce/HubSpot mobile visual standards.
Helps salespeople review AI-generated conversation summaries with customers in "card news" format.
All designs use Material3 (Material You) component library with Jetpack Compose.

---

## 1. Design System

### 1.1 Color Palette

#### Primary — Professional Blue
| Token | Light Mode | Dark Mode |
|---|---|---|
| `primary` | #1565C0 | #90CAF9 |
| `onPrimary` | #FFFFFF | #003C8F |
| `primaryContainer` | #1976D2 | #1565C0 |
| `onPrimaryContainer` | #FFFFFF | #E3F2FD |
| `primaryVariant` | #42A5F5 | #1E88E5 |

#### Secondary — Warm Accent
| Token | Light Mode | Dark Mode |
|---|---|---|
| `secondary` | #FF6F00 | #FFB74D |
| `onSecondary` | #FFFFFF | #3E2000 |
| `secondaryContainer` | #FFA726 | #FF6F00 |
| `onSecondaryContainer` | #FFFFFF | #FFF3E0 |

#### Background & Surface
| Token | Light Mode | Dark Mode |
|---|---|---|
| `background` | #F5F7FA | #121212 |
| `onBackground` | #1A1A2E | #E0E0E0 |
| `surface` | #FFFFFF | #1E1E1E |
| `onSurface` | #1A1A2E | #E0E0E0 |
| `surfaceVariant` | #EEF2F7 | #2C2C2C |
| `onSurfaceVariant` | #5A6472 | #B0B0B0 |
| `outline` | #C4CDD8 | #4A4A4A |
| `outlineVariant` | #E8EDF2 | #3A3A3A |

#### Sentiment Colors (fixed, no dark-mode inversion)
| Sentiment | Color | Hex | Usage |
|---|---|---|---|
| Positive (긍정) | Green | #4CAF50 | Statements, badges, chart segments |
| Negative (부정) | Red | #F44336 | Statements, badges, chart segments |
| Neutral (중립) | Gray | #9E9E9E | Statements, badges, chart segments |
| Commitment (약속) | Blue | #2196F3 | Statements, badges, chart segments |
| Concern (우려) | Orange | #FF9800 | Statements, badges, chart segments |
| Question (질문) | Purple | #9C27B0 | Statements, badges, chart segments |

#### Keyword Category Colors
| Category | Dot Color | Hex |
|---|---|---|
| Technology (기술) | Blue | #1976D2 |
| Business (비즈니스) | Green | #388E3C |
| Product (제품) | Purple | #7B1FA2 |
| Competitor (경쟁사) | Red | #D32F2F |
| General (일반) | Gray | #757575 |

#### Avatar Palette (8-color hash)
```
Index 0: #E53935 (Red)
Index 1: #8E24AA (Purple)
Index 2: #1E88E5 (Blue)
Index 3: #00897B (Teal)
Index 4: #43A047 (Green)
Index 5: #FB8C00 (Orange)
Index 6: #6D4C41 (Brown)
Index 7: #546E7A (Blue Gray)
```
Selection: `customerId.hashCode().absoluteValue % 8`

### 1.2 Typography

All text uses `sp` units exclusively for dynamic font size support.

| Style | Weight | Size | Line Height | Usage |
|---|---|---|---|---|
| `displayLarge` | Bold (700) | 28sp | 36sp | Screen titles |
| `headlineMedium` | SemiBold (600) | 24sp | 32sp | Section titles |
| `titleLarge` | SemiBold (600) | 20sp | 28sp | Card titles |
| `titleMedium` | Medium (500) | 16sp | 24sp | List item titles |
| `bodyLarge` | Regular (400) | 16sp | 24sp | Body content |
| `bodyMedium` | Regular (400) | 14sp | 20sp | Card summary text |
| `bodySmall` | Regular (400) | 12sp | 16sp | Captions, timestamps |
| `labelMedium` | Medium (500) | 12sp | 16sp | Chip labels, badges |
| `labelSmall` | Medium (500) | 11sp | 14sp | Frequency badges |

Korean font: System default (Noto Sans KR on most devices). No custom font import needed.

### 1.3 Spacing & Layout

| Token | Value | Usage |
|---|---|---|
| `screenPaddingHorizontal` | 16dp | Left/right screen padding |
| `screenPaddingVertical` | 16dp | Top/bottom screen padding |
| `cardPadding` | 16dp | Internal card padding |
| `cardCornerRadius` | 12dp | Standard card corners |
| `cardCornerRadiusLarge` | 16dp | Header/hero cards |
| `cardElevation` | 2dp | Standard card elevation |
| `cardElevationHigh` | 8dp | Featured/elevated cards |
| `listItemHeight` | 72–88dp | List row height |
| `listItemSpacing` | 8dp | Between list items |
| `bottomNavHeight` | 56dp | Bottom navigation bar |
| `topAppBarHeight` | 64dp | Top app bar |
| `chipSpacing` | 6dp | Between chips |
| `iconSizeSmall` | 16dp | Inline icons |
| `iconSizeMedium` | 24dp | Standard icons |
| `iconSizeLarge` | 48dp | Empty state icons |
| `avatarSizeSmall` | 40dp | List avatars |
| `avatarSizeLarge` | 56dp | Detail page avatars |
| `sentimentBarHeight` | 8dp | Sentiment stacked bar height |
| `touchTarget` | 48dp | Minimum touch target (accessibility) |

### 1.4 Shape System

| Shape | Corner Radius | Usage |
|---|---|---|
| `ShapeSmall` | 4dp | Chips, badges, small elements |
| `ShapeMedium` | 8dp | Buttons, text fields |
| `ShapeLarge` | 12dp | Cards, list items |
| `ShapeExtraLarge` | 16dp | Hero cards, bottom sheets |
| `ShapeCircle` | 50% | Avatars, FABs |

---

## 2. Screen Designs

### Screen 1: Customer List (고객 목록)

**Route:** `NavGraph.CustomerList`
**ViewModel:** `CustomerListViewModel`

#### Layout Structure

```
┌─────────────────────────────────────┐
│ [App Logo 24dp] 세일즈 컨텍스트  🔍 │  TopAppBar (64dp)
│                                     │  - Logo: app icon left-aligned
│                                     │  - Title: titleLarge, white
│                                     │  - Search icon: right action
├─────────────────────────────────────┤
│ ┌───────────────────────────────┐   │
│ │ 🟢 [Avatar] 삼성전자 로봇사업부 │   │  ElevatedCard, 8dp elevation
│ │    Samsung Electronics | 카드 15개│   │  - Avatar: 40dp circle, initials
│ │    마지막 활동: 2일 전        ❤️ │   │  - Green dot: recent (<7 days)
│ └───────────────────────────────┘   │  - Heart: Favorite toggle
│ ┌───────────────────────────────┐   │
│ │ 🔴 [Avatar] LG전자 AI연구소    │   │  - Red dot: needs attention (>30d)
│ │    LG Electronics | 카드 8개  │   │
│ │    마지막 활동: 2주 전        ❤️ │   │
│ └───────────────────────────────┘   │
│ 🟡 [Avatar] ...                     │  - Yellow dot: 7-30 days
│                                     │
│ [Shimmer skeleton × 3]              │  Loading state
│ [Pull-to-refresh indicator]         │
│                                     │
│ [Empty state: 👤 icon + text]       │  Empty state
├─────────────────────────────────────┤
│  👤 고객    🔍 검색    📤 업로드    │  BottomNavigationBar (56dp)
└─────────────────────────────────────┘
```

#### Customer Card Component Spec

```kotlin
@Composable
fun CustomerCard(
    customer: CustomerUiModel,
    onCardClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
)
```

| Element | Spec |
|---|---|
| Card type | `ElevatedCard` |
| Card elevation | 8dp (above standard 2dp for list prominence) |
| Card corner radius | 12dp |
| Card height | 88dp |
| Card padding | 16dp horizontal, 12dp vertical |
| Avatar size | 40dp circle |
| Avatar initials | First 2 chars of company name (e.g., "삼성" → "삼성") |
| Avatar bg color | `hashColor(customer.id)` from 8-color palette |
| Activity dot | 12dp circle, positioned top-right of avatar |
| Activity dot colors | Green (#4CAF50) <7d, Yellow (#FFC107) 7-30d, Red (#F44336) >30d |
| Company name | `titleMedium`, single line, ellipsis |
| Card count | `bodySmall`, secondary color, "카드 {N}개" |
| Last activity | `bodySmall`, secondary color, relative time |
| Favorite icon | `Icons.Default.Favorite` (filled red) / `Icons.Default.FavoriteBorder` (outline) |
| Favorite animation | Scale 1.0 → 1.3 → 1.0, 200ms, EaseInOut |

#### States

**Loading state:** 3 shimmer skeleton cards matching real layout.

**Empty state:**
```
[👤 48dp gray icon]
등록된 고객이 없습니다
아직 등록된 고객 정보가 없어요.
[다시 시도 — OutlinedButton]
```

**Error state:**
```
[⚠️ 48dp red icon]
데이터를 불러올 수 없습니다
네트워크 연결을 확인해주세요.
[다시 시도 — FilledButton]
```

#### Interactions

| Action | Behavior |
|---|---|
| Card tap | Navigate to CardNewsList for this customer |
| Favorite tap | Toggle favorite, scale animation, persist to local DB |
| Pull-to-refresh | Refresh customer list, custom indicator with app logo rotating |
| Search icon tap | Navigate to Search screen with customer filter pre-set |

---

### Screen 2: Card News List (카드뉴스 목록)

**Route:** `NavGraph.CardNewsList/{customerId}`
**ViewModel:** `CardNewsListViewModel`

#### Layout Structure

```
┌─────────────────────────────────────┐
│ ←  삼성전자 로봇사업부        정렬 ▼ │  TopAppBar
│                                     │  - Back arrow: navigate up
│                                     │  - Customer name: titleLarge
│                                     │  - Sort dropdown: trailing action
├─────────────────────────────────────┤
│ [7일] [30일] [전체]  [감정 필터 ▼]  │  FilterChipRow (horizontal scroll, 48dp)
│                                     │  No left padding clip for scroll
├─────────────────────────────────────┤
│ ┌───────────────────────────────┐   │
│ │ 📋 온디바이스 아키텍처 논의    │   │  Card News Item (ElevatedCard 2dp)
│ │ ─────────────────────────── │   │  - Left accent bar: 4dp, sentiment color
│ │ 고객이 온디바이스 AI 방식을 선호│   │  - Title: titleMedium, 1 line
│ │ 하며 보안 이슈를 강조했음. 엣지 │   │  - Summary: bodyMedium, 3 lines max
│ │ ─────────────────────────── │   │
│ │ [■긍정■■■][■우려][■■질문■■] │   │  - SentimentBar (8dp height)
│ │ 😊 긍정 3  ⚠️ 우려 1  ❓ 질문 2│   │  - Sentiment counts row
│ │ [엣지컴퓨팅] [온디바이스] [보안]│   │  - Keyword chips (scroll)
│ │ 2026-03-25 | 📋 회의 45분     │   │  - Date + type + duration footer
│ └───────────────────────────────┘   │
│                                     │
│ [Shimmer item] ← pagination loading │  Bottom of list lazy load
└─────────────────────────────────────┘
```

#### Card News Item Component Spec

```kotlin
@Composable
fun CardNewsItem(
    cardNews: CardNewsUiModel,
    onClick: () -> Unit,
)
```

| Element | Spec |
|---|---|
| Card type | `ElevatedCard` |
| Card elevation | 2dp |
| Card corner radius | 12dp |
| Left accent bar | 4dp wide, full height, color = dominant sentiment color |
| Topic icon | 24dp, conversation type icon (see icon table below) |
| Title | `titleMedium`, 1 line, ellipsis |
| Summary | `bodyMedium`, 3 lines max, ellipsis |
| SentimentBar | Full width, 8dp height, stacked segments |
| Sentiment counts | `labelMedium`, each with emoji prefix and count |
| Keyword chips | `SuggestionChip`, colored dot by category, max 3 visible, "+N 더보기" |
| Footer | `bodySmall`, secondary color: "날짜 | 유형 | 시간" |

#### Conversation Type Icons

| Type | Icon | Description |
|---|---|---|
| MEETING | `Icons.Default.Groups` (📋) | 대면 회의 |
| CALL | `Icons.Default.Phone` (📞) | 전화 통화 |
| EMAIL | `Icons.Default.Email` (📧) | 이메일 |
| DEMO | `Icons.Default.Slideshow` (🎯) | 제품 시연 |
| SUPPORT | `Icons.Default.Build` (🛠) | 기술 지원 |

#### Filter Chips

| Chip | Filter Applied |
|---|---|
| 7일 | Last 7 days |
| 30일 | Last 30 days |
| 전체 | All time |
| 감정 필터 ▼ | Dropdown: 긍정 / 우려 / 질문 / 약속 / 중립 |

#### Sort Options (dropdown)
- 최신순 (default)
- 오래된순
- 감정: 긍정 먼저
- 감정: 우려 먼저

#### Pagination

Infinite scroll with shimmer skeleton at bottom when loading next page.
Page size: 20 items.

---

### Screen 3: Card Detail (카드 상세)

**Route:** `NavGraph.CardDetail/{cardId}`
**ViewModel:** `CardDetailViewModel`

#### Layout Structure

```
┌─────────────────────────────────────┐
│ ←  카드 상세                      ⋮ │  TopAppBar
│                                     │  - Overflow menu: Share, Delete
├─────────────────────────────────────┤
│ ┌───────────────────────────────┐   │
│ │ [Gradient: #1565C0 → #1976D2] │   │  Header Card
│ │  📋                           │   │  - Large topic icon (32dp, white)
│ │  온디바이스 아키텍처 논의        │   │  - Title: headlineMedium, white
│ │  삼성전자 로봇사업부             │   │  - Customer: bodyMedium, white 80%
│ │  2026-03-25 화 14:00 | 45분   │   │  - Date+duration: bodySmall, white 70%
│ │                   [😊 긍정적] │   │  - Sentiment badge: bottom-right
│ └───────────────────────────────┘   │  - Corner radius: 16dp
│                                     │
│ 📊 감정 분석                         │  Section Header
│ ┌───────────────────────────────┐   │
│ │ [████긍정████][██우려██][질문] │   │  SentimentBar: full width, 12dp height
│ │  긍정 50%    우려 17%  질문 33% │   │  - Percentages below each segment
│ └───────────────────────────────┘   │  - Legend dots colored
│                                     │
│ 💬 주요 발언                         │  Section Header
│                                     │
│  🕐 0:00                            │  Timeline item
│  [Avatar 32dp] 고객: 김철수          │  - Speaker + avatar
│  ┌────────────────────────────┐     │  StatementBubble (customer, left)
│  │"클라우드보다 온디바이스가    │     │  - Gray background (#F5F5F5)
│  │  보안상 더 좋을 것 같습니다"  │     │  - Rounded rect, tail left
│  └────────────────────────────┘     │
│                         [😊 긍정]   │  Sentiment badge, right-aligned
│                                     │
│  🕐 5:30                            │
│              우리: 박영희 [Avatar] │  Speaker right-aligned (our team)
│     ┌────────────────────────────┐  │  StatementBubble (ours, right)
│     │"저희 엣지 솔루션이 이 경우에│  │  - Blue background (#E3F2FD)
│     │  가장 적합합니다"            │  │  - Rounded rect, tail right
│     └────────────────────────────┘  │
│  [✅ 약속]                           │
│                                     │
│ 🏷️ 키워드                            │  Section Header
│ [🔵엣지컴퓨팅 ×5][🔵온디바이스 ×3]  │  Keyword chips, wrapping layout
│ [🟢보안 ×2][🔴클라우드 ×1]           │
│                                     │
└─────────────────────────────────────┘
│ ── Knowledge BottomSheet ──────────  │  ModalBottomSheet (opens on chip tap)
│  ┌─ drag handle ─────────────────┐  │
│  │  📚 엣지컴퓨팅 심화 지식        │  │  - Drag handle (32×4dp, gray)
│  │  ──────────────────────────  │  │  - Title: titleLarge
│  │  엣지컴퓨팅이란 데이터를 네트  │  │  - Article content: bodyMedium
│  │  워크 엣지에서 처리하는...      │  │
│  │  ┌──────────────────────────┐ │  │  Contextual box (yellow bg #FFF9C4)
│  │  │ 이 고객 맥락에서: 삼성이   │ │  │  - "이 고객 맥락에서:" label, bold
│  │  │ 온디바이스를 선호하므로... │ │  │  - Contextual explanation
│  │  └──────────────────────────┘ │  │
│  │  관련 키워드:                   │  │
│  │  [온디바이스] [TinyML] [ONNX]  │  │  Related keyword chips (tappable)
│  └────────────────────────────────┘  │
```

#### Header Card Spec

| Element | Spec |
|---|---|
| Background | Gradient: `Brush.linearGradient(#1565C0, #1976D2)` |
| Corner radius | 16dp |
| Padding | 20dp |
| Topic icon | 32dp, white, centered top-left area |
| Title | `headlineMedium`, white, max 2 lines |
| Customer name | `bodyMedium`, white with 80% alpha |
| Date/duration | `bodySmall`, white with 70% alpha |
| Sentiment badge | Bottom-right corner, see SentimentBadge spec |

#### Sentiment Chart Spec

| Element | Spec |
|---|---|
| Height | 12dp (taller than list version) |
| Corner radius | 6dp (fully rounded ends) |
| Rendering | `Canvas` composable for smooth anti-aliasing |
| Segments | Proportional to count, minimum 2dp visible width |
| Percentage labels | `labelMedium`, below each segment, colored |
| Legend | Colored dot (8dp) + label, horizontal row |

#### Timeline Statement Spec

```kotlin
@Composable
fun StatementBubble(
    statement: StatementUiModel,
    isCustomer: Boolean,
)
```

| Element | Customer (left) | Our Team (right) |
|---|---|---|
| Alignment | Start (left) | End (right) |
| Background | #F5F5F5 (light gray) | #E3F2FD (light blue) |
| Text color | #1A1A2E (dark) | #0D47A1 (dark blue) |
| Bubble tail | Left-bottom | Right-bottom |
| Avatar position | Left of speaker label | Right of speaker label |
| Speaker label | `labelMedium`, secondary color | `labelMedium`, secondary color |
| Timestamp | `bodySmall`, above bubble, gray | `bodySmall`, above bubble, gray |
| Sentiment badge | Below bubble, right-aligned | Below bubble, left-aligned |

#### Knowledge BottomSheet Spec

| Element | Spec |
|---|---|
| Sheet type | `ModalBottomSheet` |
| Initial value | `SheetValue.PartiallyExpanded` (half screen) |
| Full expanded | Full screen minus status bar |
| Drag handle | 32×4dp rounded rect, `surfaceVariant` color |
| Context box bg | #FFF9C4 (yellow tint) |
| Context box border | 1dp, #F9A825 |
| Context box corner | 8dp |
| Related chips | `SuggestionChip`, tappable (open new knowledge panel) |

---

### Screen 4: Search (검색)

**Route:** `NavGraph.Search`
**ViewModel:** `SearchViewModel`

#### Layout Structure

```
┌─────────────────────────────────────┐
│ ┌─────────────────────────────────┐ │  SearchBar (Material3)
│ │ 🔍 고객명, 키워드 검색...      ✕ │ │  - Clear button appears when typing
│ └─────────────────────────────────┘ │  - 56dp height, 8dp corner radius
│ [고객 ▼] [날짜 범위 ▼] [초기화]    │  Filter row (horizontal scroll)
├─────────────────────────────────────┤
│ 최근 검색어                       ✕ │  Recent searches (shown before typing)
│ [온디바이스] [보안] [가격 협상]      │  - Clickable chips
│ [LG전자] [로봇사업부]               │  - Clear all (✕) button
├─────────────────────────────────────┤
│ 검색 결과 3건                        │  Result count header
│                                     │
│ ┌───────────────────────────────┐   │  Search result item
│ │ [Mini CardNews Item]           │   │  - Same layout as CardNewsList item
│ │ [고객명 chip]                   │   │  - Customer name chip added at top
│ │ 검색어 하이라이트 (노란 bg)      │   │  - Matching text: yellow #FFF176 bg
│ └───────────────────────────────┘   │
│                                     │
│ ┌───────────────────────────────┐   │
│ │ [No Results illustration]      │   │  Empty state (if no results)
│ │  검색 결과가 없습니다            │   │
│ │  다른 키워드로 검색해보세요.      │   │
│ └───────────────────────────────┘   │
└─────────────────────────────────────┘
```

#### Search Behavior

| State | UI |
|---|---|
| Empty query | Show recent searches + filter row |
| Typing (< 2 chars) | Show recent searches, no results |
| Typing (>= 2 chars) | Debounced search (300ms), show results |
| No results | Empty state illustration |
| Error | Error state with retry |

#### Text Highlighting

Match spans highlighted with `AnnotatedString`:
```kotlin
SpanStyle(background = Color(0xFFFFF176)) // Yellow highlight
```
Applied to title, summary, and keyword chips that match the query.

#### Filter Options

| Filter | Options |
|---|---|
| 고객 필터 | All customers or specific customer (dropdown) |
| 날짜 범위 | 7일 / 30일 / 90일 / 직접 입력 |
| 초기화 | Clear all filters |

---

### Screen 5: Upload (업로드)

**Route:** `NavGraph.Upload`
**ViewModel:** `UploadViewModel`

#### Layout Structure

```
┌─────────────────────────────────────┐
│ 대화 업로드                           │  TopAppBar
├─────────────────────────────────────┤
│                                     │
│ 고객 선택 *                          │  Section label (required)
│ ┌─────────────────────────────────┐ │
│ │ 삼성전자 로봇사업부              ▼ │ │  ExposedDropdownMenuBox
│ └─────────────────────────────────┘ │  - Material3 outlined style
│                                     │
│ 대화 유형 *                          │  Section label (required)
│ ┌────────┐ ┌────────┐ ┌────────┐   │
│ │📋 회의 │ │📞 통화 │ │📧 이메일│   │  SegmentedButton (3 options)
│ └────────┘ └────────┘ └────────┘   │  - Active: filled primary color
│                                     │
│ 파일 첨부                            │  Section label
│ ┌ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┐  │  Dashed border drop zone
│  📎                                │  - Dashed border: 2dp, #C4CDD8
│  파일을 선택하거나 여기에 놓으세요    │  - Corner radius: 12dp
│  음성: .m4a, .wav, .mp3             │  - Height: 120dp
│  텍스트: .txt, .pdf                 │  - On tap: file picker intent
│ └ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ─ ┘  │
│                                     │
│ [Selected: 📄 conversation.m4a ✕]  │  Attached file chip (shows after select)
│                                     │  - Remove (✕) to deselect
│ 메모 (선택사항)                       │  Section label (optional)
│ ┌─────────────────────────────────┐ │
│ │                                 │ │  OutlinedTextField
│ │                                 │ │  - Hint: "추가 메모를 입력하세요..."
│ │                                 │ │  - minLines: 3, maxLines: 5
│ └─────────────────────────────────┘ │
│                                     │
│ ┌─────────────────────────────────┐ │
│ │          📤 업로드 시작           │ │  FilledButton, full width, 56dp height
│ └─────────────────────────────────┘ │  - Disabled until customer + file selected
│                                     │
│ ── Upload Progress ─────────────── │  Shown during upload
│ ████████████░░░░  80%              │  LinearProgressIndicator
│ 업로드 중... 카드 생성 대기           │  Status text
│                                     │
│ ── Success State ───────────────── │  Shown on success
│ ✅  업로드 완료!                     │  Green checkmark icon (48dp)
│ 카드 3개가 생성되었습니다.             │  Body text
│ ┌─────────────────────────────────┐ │
│ │          카드 보러가기            │ │  FilledButton → CardNewsList
│ └─────────────────────────────────┘ │
│ ┌─────────────────────────────────┐ │
│ │          새로 업로드              │ │  OutlinedButton → reset form
│ └─────────────────────────────────┘ │
└─────────────────────────────────────┘
```

#### Upload States

| State | UI Elements |
|---|---|
| `IDLE` | Form fully interactive, button enabled when valid |
| `VALIDATING` | Button shows CircularProgressIndicator (small) |
| `UPLOADING` | Progress bar, status text, form disabled |
| `PROCESSING` | Progress bar pulsing, "카드 생성 중..." text |
| `SUCCESS` | Success icon animation (scale in), card count, navigation buttons |
| `ERROR` | Error snackbar with retry action |

#### Validation Rules

| Field | Validation |
|---|---|
| Customer | Required, must select from dropdown |
| File | Required, must be one of: .m4a, .wav, .mp3, .txt, .pdf |
| File size | Max 100MB, show error if exceeded |
| Conversation type | Required, one of: MEETING, CALL, EMAIL, DEMO, SUPPORT |

---

## 3. Shared Components Specification

### 3.1 Avatar Component

```kotlin
@Composable
fun CustomerAvatar(
    customerId: String,
    companyName: String,
    size: Dp = 40.dp,
    showActivityDot: Boolean = false,
    lastActivityDays: Int? = null,
)
```

| Property | Spec |
|---|---|
| Shape | Circle (50% corner radius) |
| Default size | 40dp |
| Detail size | 56dp |
| Initials | First 2 characters of `companyName` |
| Initials color | White |
| Initials size | `size * 0.4f` sp |
| Background color | `AvatarPalette[abs(customerId.hashCode()) % 8]` |
| Activity dot size | 12dp |
| Activity dot position | Overlay, bottom-right of avatar circle |
| Activity dot border | 2dp white border (to separate from avatar) |
| Dot: green | `lastActivityDays <= 7` |
| Dot: yellow | `lastActivityDays in 8..30` |
| Dot: red | `lastActivityDays > 30` |
| Dot: none | `showActivityDot = false` or `lastActivityDays = null` |

### 3.2 SentimentBar Component

```kotlin
@Composable
fun SentimentBar(
    sentimentCounts: SentimentCounts,
    height: Dp = 8.dp,
    cornerRadius: Dp = 4.dp,
    modifier: Modifier = Modifier,
)

data class SentimentCounts(
    val positive: Int,
    val negative: Int,
    val neutral: Int,
    val commitment: Int,
    val concern: Int,
    val question: Int,
)
```

| Property | Spec |
|---|---|
| Rendering | `Canvas` composable (not Box/Row) |
| Height | 8dp (list), 12dp (detail) |
| Corner radius | 4dp on outer ends only |
| Segment order | Positive, Commitment, Neutral, Question, Concern, Negative |
| Segment width | Proportional to count / total |
| Min segment width | 4dp (if count > 0) |
| Colors | Sentiment color constants |
| Zero count | Segment not drawn |
| Animation | `animateFloatAsState` on proportion changes, 400ms |

### 3.3 ShimmerLoading Component

```kotlin
@Composable
fun ShimmerItem(
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.large,
)

@Composable
fun CustomerListShimmer() // 3 CustomerCard-shaped skeletons

@Composable
fun CardNewsListShimmer() // 2 CardNewsItem-shaped skeletons
```

| Property | Spec |
|---|---|
| Base color | `surfaceVariant` |
| Highlight color | `surface` |
| Animation | `InfiniteTransition`, left-to-right gradient sweep |
| Duration | 1000ms per cycle |
| Easing | `LinearEasing` |
| Implementation | `Brush.linearGradient` animated with `translateAnim` |

Shimmer gradient implementation pattern:
```kotlin
val shimmerColors = listOf(
    MaterialTheme.colorScheme.surfaceVariant,
    MaterialTheme.colorScheme.surface,
    MaterialTheme.colorScheme.surfaceVariant,
)
val transition = rememberInfiniteTransition()
val translateAnim = transition.animateFloat(
    initialValue = 0f,
    targetValue = 1000f,
    animationSpec = infiniteRepeatable(
        animation = tween(durationMillis = 1000, easing = LinearEasing)
    )
)
val brush = Brush.linearGradient(
    colors = shimmerColors,
    start = Offset.Zero,
    end = Offset(x = translateAnim.value, y = translateAnim.value)
)
```

### 3.4 EmptyState Component

```kotlin
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
)
```

| Property | Spec |
|---|---|
| Icon size | 48dp |
| Icon color | `onSurfaceVariant` (gray) |
| Title style | `titleMedium`, `onSurface` |
| Subtitle style | `bodyMedium`, `onSurfaceVariant` |
| Vertical spacing | 16dp between elements |
| Centering | `Column(horizontalAlignment = Center)` |
| Action button | `OutlinedButton`, shown only when `actionLabel != null` |

### 3.5 ErrorState Component

```kotlin
@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
)
```

| Property | Spec |
|---|---|
| Icon | `Icons.Default.ErrorOutline` |
| Icon color | `error` |
| Icon size | 48dp |
| Message style | `bodyMedium`, `onSurface` |
| Button | `FilledButton`, "다시 시도" |

### 3.6 KeywordChip Component

```kotlin
@Composable
fun KeywordChip(
    keyword: KeywordUiModel,
    onClick: () -> Unit,
)

data class KeywordUiModel(
    val term: String,
    val category: KeywordCategory,
    val frequency: Int,
)
```

| Property | Spec |
|---|---|
| Base | `FilterChip` (Material3) |
| Selected state | Not used (always unselected appearance) |
| Leading | 8dp circle, colored by `category` |
| Label | `term` text, `labelMedium` |
| Trailing | Frequency badge: small rounded rect, `labelSmall`, `surfaceVariant` bg |
| On tap | Open Knowledge BottomSheet for this keyword |
| Category colors | See keyword category color table above |

### 3.7 SentimentBadge Component

```kotlin
@Composable
fun SentimentBadge(
    sentiment: SentimentType,
    modifier: Modifier = Modifier,
)
```

| Sentiment | Emoji | Label | Background |
|---|---|---|---|
| POSITIVE | 😊 | 긍정 | #4CAF50 at 10% alpha |
| NEGATIVE | 😟 | 부정 | #F44336 at 10% alpha |
| NEUTRAL | 😐 | 중립 | #9E9E9E at 10% alpha |
| COMMITMENT | ✅ | 약속 | #2196F3 at 10% alpha |
| CONCERN | ⚠️ | 우려 | #FF9800 at 10% alpha |
| QUESTION | ❓ | 질문 | #9C27B0 at 10% alpha |

| Property | Spec |
|---|---|
| Shape | `ShapeSmall` (4dp corner) |
| Padding | 4dp vertical, 8dp horizontal |
| Text style | `labelMedium` |
| Text color | Sentiment color at 100% (dark enough for contrast) |
| Border | 1dp, sentiment color at 30% alpha |

### 3.8 StatementBubble Component

```kotlin
@Composable
fun StatementBubble(
    statement: StatementUiModel,
    isOurTeam: Boolean,
)

data class StatementUiModel(
    val speakerName: String,
    val text: String,
    val timestamp: String,
    val sentiment: SentimentType,
)
```

| Property | Customer | Our Team |
|---|---|---|
| Alignment | `Arrangement.Start` | `Arrangement.End` |
| Bubble background | #F5F5F5 | #E3F2FD |
| Bubble text color | #1A1A2E | #0D47A1 |
| Bubble corner radius | 4dp on top-left, 12dp elsewhere | 4dp on top-right, 12dp elsewhere |
| Bubble max width | 80% of screen width | 80% of screen width |
| Bubble padding | 12dp |12dp |
| Avatar | Left of speaker row | Right of speaker row |
| Speaker text | `labelMedium` | `labelMedium` |
| Statement text | `bodyMedium` | `bodyMedium` |
| Sentiment badge | Below bubble, end-aligned | Below bubble, start-aligned |
| Timeline dot | 8dp circle, sentiment color | 8dp circle, sentiment color |
| Connector line | 2dp vertical, `outlineVariant` | 2dp vertical, `outlineVariant` |

---

## 4. Required Dependencies

Add to `libs.versions.toml`:

```toml
[versions]
coil = "2.5.0"
accompanist = "0.34.0"

[libraries]
coil-compose = { module = "io.coil-kt:coil-compose", version.ref = "coil" }
accompanist-placeholder = { module = "com.google.accompanist:accompanist-placeholder-material3", version.ref = "accompanist" }
```

Add to `app/build.gradle.kts`:

```kotlin
dependencies {
    implementation(libs.coil.compose)
    implementation(libs.accompanist.placeholder)
}
```

### Why These Dependencies

| Dependency | Purpose |
|---|---|
| `coil-compose` | Image loading for any future avatar photo support; efficient async loading |
| `accompanist-placeholder` | Ready-made shimmer/placeholder composables for loading states |

Note: Do NOT add Lottie unless animations spec is explicitly requested. Keep dependencies minimal.

---

## 5. Animation Specifications

### 5.1 Navigation Transitions

Applied via `NavHost` `enterTransition` / `exitTransition`:

```kotlin
// Forward navigation
enterTransition = {
    slideInHorizontally(
        initialOffsetX = { fullWidth -> fullWidth },
        animationSpec = tween(300, easing = FastOutSlowInEasing)
    ) + fadeIn(animationSpec = tween(300))
}
exitTransition = {
    slideOutHorizontally(
        targetOffsetX = { fullWidth -> -fullWidth / 3 },
        animationSpec = tween(300, easing = FastOutSlowInEasing)
    ) + fadeOut(animationSpec = tween(300))
}

// Back navigation (reverse)
popEnterTransition = {
    slideInHorizontally(
        initialOffsetX = { fullWidth -> -fullWidth / 3 },
        animationSpec = tween(300, easing = FastOutSlowInEasing)
    ) + fadeIn(animationSpec = tween(300))
}
popExitTransition = {
    slideOutHorizontally(
        targetOffsetX = { fullWidth -> fullWidth },
        animationSpec = tween(300, easing = FastOutSlowInEasing)
    ) + fadeOut(animationSpec = tween(300))
}
```

### 5.2 List Item Stagger Animation

First load only (track with `LaunchedEffect(key = items.isEmpty())`):

```kotlin
// Each item fades in + slides up with 50ms delay per index
val visibleState = remember { MutableTransitionState(false) }
LaunchedEffect(index) {
    delay(index * 50L)
    visibleState.targetState = true
}
AnimatedVisibility(
    visibleState = visibleState,
    enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 })
)
```

Maximum stagger: first 10 items only (index 0..9). Items beyond index 9 appear immediately.

### 5.3 Knowledge BottomSheet Animation

```kotlin
// ModalBottomSheet with spring animation (default Material3 behavior)
// No custom animation needed — Material3 spring is correct
val sheetState = rememberModalBottomSheetState(
    skipPartiallyExpanded = false
)
```

### 5.4 Favorite Toggle Animation

```kotlin
val scale by animateFloatAsState(
    targetValue = if (isFavorited) 1.3f else 1.0f,
    animationSpec = keyframes {
        durationMillis = 200
        1.3f at 100 with EaseIn
        1.0f at 200 with EaseOut
    }
)
val color by animateColorAsState(
    targetValue = if (isFavorited) Color.Red else Color.Gray,
    animationSpec = tween(200)
)
Icon(
    imageVector = if (isFavorited) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
    tint = color,
    modifier = Modifier.scale(scale)
)
```

### 5.5 Upload Success Animation

On success, use `AnimatedVisibility` with scale+fade:
```kotlin
AnimatedVisibility(
    visible = isSuccess,
    enter = scaleIn(initialScale = 0.5f) + fadeIn(),
)
```

### 5.6 Pull-to-Refresh Indicator

Use `rememberPullToRefreshState()` (Material3 built-in).
Custom indicator: replace default with app logo that rotates during refresh.

```kotlin
PullToRefreshContainer(
    state = pullToRefreshState,
    indicator = { state ->
        AppLogoPullIndicator(state = state)
    }
)
```

---

## 6. Accessibility Requirements

### 6.1 Content Descriptions

Every interactive icon and non-text element must have `contentDescription`:

| Element | contentDescription |
|---|---|
| Avatar | `"${companyName} 고객 아바타"` |
| Activity dot (green) | `"최근 활동 있음 (7일 이내)"` |
| Activity dot (yellow) | `"보통 활동 (7-30일 전)"` |
| Activity dot (red) | `"주의 필요 (30일 이상 비활동)"` |
| Favorite (filled) | `"즐겨찾기 해제"` |
| Favorite (border) | `"즐겨찾기 추가"` |
| Search icon | `"검색"` |
| Sort icon | `"정렬"` |
| Back arrow | `"이전으로"` |
| Overflow menu | `"더 보기"` |
| SentimentBar | `"감정 분석: 긍정 ${positive}%, 우려 ${concern}%, 질문 ${question}%"` |
| Upload drop zone | `"파일 첨부 영역. 탭하여 파일 선택"` |
| Topic icon | `"${conversationType.koreanName} 대화"` |

### 6.2 Touch Targets

All interactive elements must meet 48dp minimum touch target:
- Use `Modifier.minimumInteractiveComponentSize()` on small interactive elements
- Chip touch targets: padding ensures 48dp height
- Icon buttons: `IconButton` composable (48dp by default)

### 6.3 Color Contrast

| Text / Background | Ratio | Compliant |
|---|---|---|
| White on `#1565C0` | 7.2:1 | AA + AAA |
| `#1A1A2E` on white | 16.5:1 | AA + AAA |
| `#5A6472` on white | 5.1:1 | AA |
| Sentiment colors on white | Must be tested | Use text-only, not bg-only |

For sentiment badges: use text + tinted background (not color-only), ensuring text contrast ≥ 4.5:1.

### 6.4 Screen Reader Support

Sentiment values must be readable without color:
```kotlin
// SentimentBar: provide semantic description
Modifier.semantics {
    contentDescription = "감정 분포: 긍정 ${positive}건, 우려 ${concern}건, 질문 ${question}건"
}
```

### 6.5 Dynamic Font Size

- All text uses `sp` units (never `dp` for text)
- Layouts must not overflow at 200% font scale
- Use `wrapContentHeight()` on cards instead of fixed heights when text is inside
- Test with `Settings > Accessibility > Font size > Largest`

---

## 7. Material3 Theme Setup

```kotlin
// Theme.kt
@Composable
fun SalesContextTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) darkColorScheme(
        primary = Color(0xFF90CAF9),
        onPrimary = Color(0xFF003C8F),
        primaryContainer = Color(0xFF1565C0),
        secondary = Color(0xFFFFB74D),
        background = Color(0xFF121212),
        surface = Color(0xFF1E1E1E),
        surfaceVariant = Color(0xFF2C2C2C),
    ) else lightColorScheme(
        primary = Color(0xFF1565C0),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFF1976D2),
        secondary = Color(0xFFFF6F00),
        background = Color(0xFFF5F7FA),
        surface = Color(0xFFFFFFFF),
        surfaceVariant = Color(0xFFEEF2F7),
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = SalesContextTypography,
        shapes = SalesContextShapes,
        content = content,
    )
}
```

---

## 8. Navigation Graph

```kotlin
@Composable
fun SalesContextNavGraph(
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = "customer_list",
    ) {
        composable("customer_list") { CustomerListScreen(navController) }
        composable("card_news_list/{customerId}") { backStackEntry ->
            val customerId = backStackEntry.arguments?.getString("customerId") ?: return@composable
            CardNewsListScreen(navController, customerId)
        }
        composable("card_detail/{cardId}") { backStackEntry ->
            val cardId = backStackEntry.arguments?.getString("cardId") ?: return@composable
            CardDetailScreen(navController, cardId)
        }
        composable("search") { SearchScreen(navController) }
        composable("upload") { UploadScreen(navController) }
    }
}
```

Bottom navigation routes: `customer_list`, `search`, `upload`

---

## 9. Implementation Priority Order

For hackathon demo purposes, implement in this order:

1. **Design system** — Theme, colors, typography, shapes (30 min)
2. **CustomerList screen** — Most prominent entry screen (45 min)
3. **CardNewsList screen** — Core browsing experience (45 min)
4. **CardDetail screen** — The "wow" screen with sentiment chart + timeline (60 min)
5. **Shared components** — Avatar, SentimentBar, KeywordChip, ShimmerLoading (30 min)
6. **Search screen** — High-value for demo (30 min)
7. **Upload screen** — Completes the workflow (30 min)
8. **Animations** — Add last, all at once (20 min)

Total estimated time with TDD: ~5 hours for fully polished demo quality.

---

## 10. Demo Data Recommendations

Use hardcoded fake data for the hackathon demo to ensure reliability:

```kotlin
object DemoData {
    val customers = listOf(
        CustomerUiModel(
            id = "samsung-robot",
            companyName = "삼성전자 로봇사업부",
            fullCompanyName = "Samsung Electronics",
            cardCount = 15,
            lastActivityDays = 2,
            isFavorite = true,
        ),
        CustomerUiModel(
            id = "lg-ai",
            companyName = "LG전자 AI연구소",
            fullCompanyName = "LG Electronics",
            cardCount = 8,
            lastActivityDays = 14,
            isFavorite = false,
        ),
        CustomerUiModel(
            id = "hyundai-auto",
            companyName = "현대자동차 미래기술",
            fullCompanyName = "Hyundai Motor Company",
            cardCount = 22,
            lastActivityDays = 1,
            isFavorite = true,
        ),
    )
}
```

Fallback to demo data when API returns error — ensures demo never shows empty/error states.
