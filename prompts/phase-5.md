RALPH_BACKLOG.md를 읽고 첫 번째 미완료(- [ ]) 마일스톤을 찾아서 실행하라.
이미 완료된(- [x]) 마일스톤은 건너뛴다.
이 Phase의 마일스톤이 모두 완료되면 세션을 종료한다.

세션 시작 체크:
1. RALPH_BACKLOG.md 읽기
2. git log --oneline -3
3. ./gradlew.bat compileDebugKotlin --no-daemon 2>&1 | tail -3

---

## UI 디자인 가이드
이 Phase에서 만드는 모든 Screen은 데모 품질이어야 한다.
반드시 test-specs/ui-design-spec.md를 먼저 읽고 디자인 사양을 따른다.

### 공통 컴포넌트 (M-35에서 먼저 구현)
1. ShimmerLoading — accompanist placeholder 사용, 모든 리스트에 적용
2. Avatar — Circle 이니셜 (회사명 첫 2자), 배경색 = hash(id) % palette
3. SentimentBar — Canvas 수평 누적 바, 감정별 색상
4. SentimentBadge — 아이콘 + 텍스트 + 배경 틴트
5. StatementBubble — 채팅 버블 스타일, 고객=왼쪽 회색, 우리팀=오른쪽 파란색
6. KeywordChip — FilterChip + 카테고리 색상 dot + 빈도 배지
7. EmptyState — 큰 아이콘 + 메시지 + 재시도 버튼
8. ErrorState — 에러 아이콘 + 메시지 + 재시도

---

## Phase 5: UI Screens + Feature Extensions
마일스톤 M-35 ~ M-44를 순서대로 실행한다.
각 마일스톤 완료 시 RALPH_BACKLOG.md의 해당 항목을 `- [x]`로 업데이트하라.
같은 에러 3회 반복 시 `- [SKIP]`으로 표시하고 다음 마일스톤으로 진행하라.

**전제**: Phase 1~4(M-01~M-34)가 완료된 상태.
존재하는 ViewModel들:
- `ui/customer/CustomerListViewModel`
- `ui/card/CardNewsListViewModel`, `ui/card/CardDetailViewModel`
- `ui/search/SearchViewModel`
- `ui/navigation/NavGraph.kt` (skeleton)
- `ui/theme/`: Color, Type, Theme

**중요**: M-35~M-38은 **Tier 2 TDD** (test-alongside). UI 코드 먼저 작성 가능,
단 마일스톤 완료 전 androidTest 에 해당 UI 테스트가 존재해야 한다.
M-39~M-43은 **Tier 1 TDD** (test-first). 테스트 파일 먼저.

---

## Compose UI 공통 패턴

모든 Screen Composable:
```kotlin
@Composable
fun XxxScreen(
    viewModel: XxxViewModel = hiltViewModel(),
    onNavigate: (route: String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    when (val state = uiState) {
        is XxxUiState.Loading -> LoadingIndicator()
        is XxxUiState.Data -> XxxContent(data = state.items, onNavigate = onNavigate)
        is XxxUiState.Empty -> EmptyState(message = "표시할 데이터가 없습니다")
        is XxxUiState.Error -> ErrorState(message = state.message, onRetry = { viewModel.refresh() })
    }
}
```

공통 컴포넌트 (모든 Screen이 공유):
```kotlin
// ui/components/LoadingIndicator.kt
@Composable
fun LoadingIndicator() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

// ui/components/EmptyState.kt
@Composable
fun EmptyState(message: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(text = message, style = MaterialTheme.typography.bodyLarge)
    }
}

// ui/components/ErrorState.kt
@Composable
fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = message, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onRetry) { Text("다시 시도") }
    }
}
```

---

### M-35: CustomerListScreen + CustomerCard + 공통 컴포넌트

**목표**: UI 파일 생성 + 컴파일 성공 (androidTest는 M-44에서 추가)

**파일 산출물**:
- `app/src/main/java/com/ralphthon/app/ui/customer/CustomerListScreen.kt`
- `app/src/main/java/com/ralphthon/app/ui/components/CustomerCard.kt`
- `app/src/main/java/com/ralphthon/app/ui/components/Avatar.kt`
- `app/src/main/java/com/ralphthon/app/ui/components/ShimmerLoading.kt`
- `app/src/main/java/com/ralphthon/app/ui/components/EmptyState.kt`
- `app/src/main/java/com/ralphthon/app/ui/components/ErrorState.kt`
- `app/src/main/java/com/ralphthon/app/ui/components/LoadingIndicator.kt`

**CustomerListScreen** (데모 품질):
- `Scaffold` + `TopAppBar` + `BottomNavigation` (3 tabs: 고객, 검색, 업로드)
- `SwipeRefresh` 또는 `pullRefresh` modifier로 Pull-to-refresh 지원
- 로딩 상태: shimmer skeleton (실제 CardItem 형태의 placeholder 3개)
- 데이터 상태: `LazyColumn` + `CustomerCard`
- Empty/Error 상태: 큰 아이콘 + 메시지 + 재시도 버튼

**Bottom Navigation** (3 tabs):
```kotlin
NavigationBar {
    NavigationBarItem(
        selected = currentRoute == Screen.CustomerList.route,
        onClick = { navController.navigate(Screen.CustomerList.route) },
        icon = { Icon(Icons.Default.People, contentDescription = "고객목록") },
        label = { Text("고객") }
    )
    NavigationBarItem(
        selected = currentRoute == Screen.Search.route,
        onClick = { navController.navigate(Screen.Search.route) },
        icon = { Icon(Icons.Default.Search, contentDescription = "검색") },
        label = { Text("검색") }
    )
    NavigationBarItem(
        selected = currentRoute == Screen.Upload.route,
        onClick = { navController.navigate(Screen.Upload.route) },
        icon = { Icon(Icons.Default.Upload, contentDescription = "업로드") },
        label = { Text("업로드") }
    )
}
```

**CustomerCard** (ElevatedCard + Avatar + 활동 dot):
```kotlin
@Composable
fun CustomerCard(customer: Customer, onClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Avatar(name = customer.company, id = customer.id)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = customer.name, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.width(8.dp))
                    ActivityDot(lastInteractionAt = customer.lastInteractionAt)
                }
                Text(text = customer.company, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Row {
                    Text(text = "카드 ${customer.cardCount}개", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = "대화 ${customer.totalConversations}회", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
```

**Avatar** (Circle + 이니셜 + hash 배경색):
```kotlin
@Composable
fun Avatar(name: String, id: Long, size: Dp = 44.dp) {
    val initials = name.take(2).uppercase()
    val palette = listOf(Color(0xFF1976D2), Color(0xFF388E3C), Color(0xFF7B1FA2),
        Color(0xFFD32F2F), Color(0xFF0288D1), Color(0xFFF57C00))
    val bgColor = palette[(id % palette.size).toInt()]
    Box(
        modifier = Modifier.size(size).clip(CircleShape).background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Text(text = initials, color = Color.White, style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold)
    }
}
```

**ActivityDot** (green/yellow/red 기준: 7일/30일):
```kotlin
@Composable
fun ActivityDot(lastInteractionAt: String) {
    val color = /* parse date, green < 7days, yellow < 30days, red >= 30days */ Color(0xFF4CAF50)
    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(color))
}
```

**ShimmerLoading** (accompanist placeholder):
```kotlin
@Composable
fun ShimmerCardItem() {
    ElevatedCard(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)) {
        Row(modifier = Modifier.padding(16.dp)) {
            Box(modifier = Modifier.size(44.dp).clip(CircleShape)
                .placeholder(visible = true, highlight = PlaceholderHighlight.shimmer()))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Box(modifier = Modifier.fillMaxWidth(0.4f).height(16.dp)
                    .placeholder(visible = true, highlight = PlaceholderHighlight.shimmer()))
                Spacer(modifier = Modifier.height(4.dp))
                Box(modifier = Modifier.fillMaxWidth(0.6f).height(12.dp)
                    .placeholder(visible = true, highlight = PlaceholderHighlight.shimmer()))
            }
        }
    }
}
```

**검증**: `./gradlew.bat compileDebugKotlin --no-daemon 2>&1 | tail -3`
완료 후 RALPH_BACKLOG.md M-35 체크.
`git add app/src/main/java/com/ralphthon/app/ui/`
`git commit -m "feat(M-35): CustomerListScreen + Avatar + Shimmer + BottomNav"`
`git push origin master`

---

### M-36: CardNewsListScreen + ContextCardItem + FilterChipRow

**목표**: UI 파일 생성 + 컴파일 성공

**파일 산출물**:
- `app/src/main/java/com/ralphthon/app/ui/card/CardNewsListScreen.kt`
- `app/src/main/java/com/ralphthon/app/ui/card/ContextCardItem.kt`
- `app/src/main/java/com/ralphthon/app/ui/card/FilterChipRow.kt`
- `app/src/main/java/com/ralphthon/app/ui/components/SentimentBadge.kt`
- `app/src/main/java/com/ralphthon/app/ui/components/SentimentBar.kt`
- `app/src/main/java/com/ralphthon/app/ui/components/KeywordChip.kt`

**CardNewsListScreen**: LazyColumn + FilterChipRow + 각 ContextCardItem + shimmer pagination
**FilterChipRow**: `LazyRow` + `FilterChip` (ConversationType 각각)

**ContextCardItem** (데모 품질):
- `ElevatedCard` + 왼쪽 4dp 색상 accent bar (dominant sentiment 색상)
- Header: 대화 타입 이모지/아이콘 + 제목
- Body: 요약 텍스트 (2줄 제한, overflow = Ellipsis)
- `SentimentBar`: Canvas 수평 누적 바 (감정 분포)
- `KeywordChip` 행: 카테고리 색상 dot + 키워드명 + 빈도 배지
- Footer: 날짜 + 대화 길이(분)

```kotlin
@Composable
fun ContextCardItem(card: ContextCard, onClick: () -> Unit) {
    val dominantColor = sentimentColor(card.sentiment)
    ElevatedCard(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp)
    ) {
        Row {
            // 왼쪽 색상 accent bar
            Box(modifier = Modifier.width(4.dp).fillMaxHeight().background(dominantColor))
            Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = conversationTypeEmoji(card.conversationType),
                        style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = card.title, style = MaterialTheme.typography.titleMedium,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = card.summary, style = MaterialTheme.typography.bodySmall,
                    maxLines = 2, overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(8.dp))
                SentimentBadge(sentiment = card.sentiment)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    items(card.keywords.take(4)) { keyword ->
                        KeywordChip(keyword = keyword, onClick = {})
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Row {
                    Text(text = card.createdAt, style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline)
                }
            }
        }
    }
}
```

**SentimentBar** (Canvas 수평 누적 바):
```kotlin
@Composable
fun SentimentBar(statements: List<KeyStatement>, modifier: Modifier = Modifier) {
    if (statements.isEmpty()) return
    val counts = Sentiment.values().associateWith { s -> statements.count { it.sentiment == s } }
    val total = statements.size.toFloat()
    Canvas(modifier = modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))) {
        var offsetX = 0f
        Sentiment.values().forEach { sentiment ->
            val fraction = (counts[sentiment] ?: 0) / total
            val width = size.width * fraction
            drawRect(color = sentimentColorValue(sentiment), topLeft = Offset(offsetX, 0f),
                size = Size(width, size.height))
            offsetX += width
        }
    }
}
```

**SentimentBadge** (아이콘 + 텍스트 + 배경 틴트):
```kotlin
@Composable
fun SentimentBadge(sentiment: Sentiment) {
    val (color, label, icon) = when (sentiment) {
        Sentiment.POSITIVE -> Triple(SentimentPositive, "긍정", Icons.Default.ThumbUp)
        Sentiment.NEGATIVE -> Triple(SentimentNegative, "부정", Icons.Default.ThumbDown)
        Sentiment.COMMITMENT -> Triple(SentimentCommitment, "약속", Icons.Default.CheckCircle)
        Sentiment.CONCERN -> Triple(SentimentConcern, "우려", Icons.Default.Warning)
        Sentiment.QUESTION -> Triple(SentimentQuestion, "질문", Icons.Default.HelpOutline)
        Sentiment.NEUTRAL -> Triple(SentimentNeutral, "중립", Icons.Default.Remove)
    }
    Surface(color = color.copy(alpha = 0.12f), shape = RoundedCornerShape(12.dp)) {
        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = null, tint = color,
                modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = label, color = color, style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold)
        }
    }
}
```

**KeywordChip** (FilterChip + 카테고리 dot + 빈도 배지):
```kotlin
@Composable
fun KeywordChip(keyword: Keyword, onClick: () -> Unit) {
    val categoryColor = when (keyword.category) {
        KeywordCategory.TECHNOLOGY -> CategoryTechnology
        KeywordCategory.BUSINESS -> CategoryBusiness
        KeywordCategory.PRODUCT -> CategoryProduct
        KeywordCategory.COMPETITOR -> CategoryCompetitor
        KeywordCategory.GENERAL -> CategoryGeneral
    }
    FilterChip(
        selected = false,
        onClick = onClick,
        label = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(categoryColor))
                Spacer(modifier = Modifier.width(4.dp))
                Text(keyword.term, style = MaterialTheme.typography.labelSmall)
                Spacer(modifier = Modifier.width(4.dp))
                Text("${keyword.frequency}", style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline)
            }
        }
    )
}
```

**검증**: `./gradlew.bat compileDebugKotlin --no-daemon 2>&1 | tail -3`
완료 후 RALPH_BACKLOG.md M-36 체크.
`git add app/src/main/java/com/ralphthon/app/ui/`
`git commit -m "feat(M-36): CardNewsListScreen + SentimentBar + KeywordChip"`
`git push origin master`

---

### M-37: CardDetailScreen + KnowledgePanel + StatementBubble

**목표**: UI 파일 생성 + 컴파일 성공

**파일 산출물**:
- `app/src/main/java/com/ralphthon/app/ui/card/CardDetailScreen.kt`
- `app/src/main/java/com/ralphthon/app/ui/card/KnowledgePanel.kt`
- `app/src/main/java/com/ralphthon/app/ui/components/StatementBubble.kt`

**CardDetailScreen** (데모 품질):
- `uiState` + `knowledgeState` 두 개 StateFlow 동시 구독
- **Gradient header card**: `Brush.verticalGradient(listOf(Color(0xFF1565C0), Color(0xFF1976D2)))`, white text
  - 제목, 고객명, 날짜, 대화 타입 아이콘 표시
- **Sentiment distribution**: `SentimentBar` (Canvas 누적 바 + 퍼센트 레이블)
- **Timeline view**: 세로 선 + dots + `StatementBubble` (채팅 스타일)
- **Keywords**: `KeywordChip` 행 — 탭 시 `ModalBottomSheet`로 KnowledgePanel 표시

```kotlin
@Composable
fun CardDetailScreen(
    cardId: Long,
    onNavigateBack: () -> Unit = {},
    viewModel: CardDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val knowledgeState by viewModel.knowledgeState.collectAsStateWithLifecycle()
    var showKnowledgeSheet by remember { mutableStateOf(false) }

    when (val state = uiState) {
        is CardDetailUiState.Loading -> LoadingIndicator()
        is CardDetailUiState.Data -> {
            val card = state.card
            LazyColumn {
                item {
                    // Gradient header
                    Box(
                        modifier = Modifier.fillMaxWidth().height(180.dp)
                            .background(Brush.verticalGradient(
                                listOf(Color(0xFF1565C0), Color(0xFF1976D2))))
                            .padding(20.dp)
                    ) {
                        Column {
                            Text(card.title, style = MaterialTheme.typography.headlineSmall,
                                color = Color.White, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(card.customerName, style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.85f))
                            Text(card.createdAt, style = MaterialTheme.typography.bodySmall,
                                color = Color.White.copy(alpha = 0.7f))
                        }
                    }
                }
                item {
                    // Sentiment distribution
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("감정 분포", style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        SentimentBar(statements = card.statements,
                            modifier = Modifier.fillMaxWidth().height(10.dp))
                    }
                }
                item {
                    // Keywords
                    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                        Text("키워드", style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(card.keywords) { keyword ->
                                KeywordChip(keyword = keyword, onClick = {
                                    viewModel.loadKnowledge(keyword.id)
                                    showKnowledgeSheet = true
                                })
                            }
                        }
                    }
                }
                // Timeline statements
                itemsIndexed(card.statements) { index, statement ->
                    StatementBubble(statement = statement, index = index,
                        isLast = index == card.statements.lastIndex)
                }
            }
            if (showKnowledgeSheet) {
                ModalBottomSheet(onDismissRequest = { showKnowledgeSheet = false }) {
                    KnowledgePanel(knowledgeState = knowledgeState,
                        onClose = { showKnowledgeSheet = false })
                }
            }
        }
        is CardDetailUiState.Error -> ErrorState(message = state.message,
            onRetry = { viewModel.loadCard(cardId) })
    }
}
```

**StatementBubble** (채팅 버블: 고객=왼쪽 회색, 우리팀=오른쪽 파란색):
```kotlin
@Composable
fun StatementBubble(statement: KeyStatement, index: Int, isLast: Boolean) {
    val isCustomer = statement.speaker.contains("고객", ignoreCase = true) ||
        statement.speaker.contains("customer", ignoreCase = true)
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = if (isCustomer) Arrangement.Start else Arrangement.End
    ) {
        if (!isCustomer) Spacer(modifier = Modifier.weight(1f))
        Column(horizontalAlignment = if (isCustomer) Alignment.Start else Alignment.End) {
            Text(text = statement.speaker, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.outline)
            Surface(
                shape = RoundedCornerShape(
                    topStart = if (isCustomer) 4.dp else 16.dp,
                    topEnd = if (isCustomer) 16.dp else 4.dp,
                    bottomStart = 16.dp, bottomEnd = 16.dp
                ),
                color = if (isCustomer) Color(0xFFEEEEEE) else Color(0xFF1976D2)
            ) {
                Text(
                    text = statement.text,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        .widthIn(max = 280.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isCustomer) Color(0xFF212121) else Color.White
                )
            }
            SentimentBadge(sentiment = statement.sentiment)
        }
        if (isCustomer) Spacer(modifier = Modifier.weight(1f))
    }
}
```

**KnowledgePanel** (ModalBottomSheet 내용 — 기사 + 컨텍스트 설명 + 관련 키워드):
```kotlin
@Composable
fun KnowledgePanel(knowledgeState: KnowledgeUiState, onClose: () -> Unit) {
    when (val state = knowledgeState) {
        is KnowledgeUiState.Idle -> Unit
        is KnowledgeUiState.Loading -> LoadingIndicator()
        is KnowledgeUiState.Data -> {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically) {
                    Text(text = state.result.keywordTerm,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold)
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "닫기")
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                LazyColumn {
                    items(state.result.articles) { article ->
                        Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(article.title, style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(article.content, style = MaterialTheme.typography.bodySmall,
                                    maxLines = 3, overflow = TextOverflow.Ellipsis)
                                if (article.contextualExplanation.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    // 노란색 하이라이트 컨텍스트 설명
                                    Surface(color = Color(0xFFFFF9C4),
                                        shape = RoundedCornerShape(4.dp)) {
                                        Text(article.contextualExplanation,
                                            modifier = Modifier.padding(8.dp),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = Color(0xFF5D4037))
                                    }
                                }
                                if (article.relatedKeywords.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        items(article.relatedKeywords) { kw ->
                                            AssistChip(onClick = {},
                                                label = { Text(kw, style = MaterialTheme.typography.labelSmall) })
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        is KnowledgeUiState.Error -> ErrorState(message = state.message, onRetry = {})
    }
}
```

**검증**: `./gradlew.bat compileDebugKotlin --no-daemon 2>&1 | tail -3`
완료 후 RALPH_BACKLOG.md M-37 체크.
`git add app/src/main/java/com/ralphthon/app/ui/`
`git commit -m "feat(M-37): CardDetailScreen + StatementBubble + KnowledgePanel"`
`git push origin master`

---

### M-38: SearchScreen + UploadScreen

**목표**: UI 파일 2개 생성 + 컴파일 성공

**파일 산출물**:
- `app/src/main/java/com/ralphthon/app/ui/search/SearchScreen.kt`
- `app/src/main/java/com/ralphthon/app/ui/upload/UploadScreen.kt`

**SearchScreen** (데모 품질):
- `SearchBar` (Material3) 또는 `OutlinedTextField` + 검색 아이콘
- 최근 검색어 chips (LazyRow) — 탭 시 검색창 자동입력
- 필터 드롭다운: ConversationType, 날짜 범위
- 검색 결과: LazyColumn + ContextCardItem 재사용 + **쿼리 하이라이트**
  - 검색어와 일치하는 텍스트 부분을 `SpanStyle(background = Color(0xFFFFEB3B))` 로 강조
- Empty/Loading/Error 상태 처리

```kotlin
@Composable
fun SearchScreen(
    viewModel: SearchViewModel = hiltViewModel(),
    onCardClick: (Long) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        // 검색 바
        OutlinedTextField(
            value = query,
            onValueChange = { query = it; viewModel.search(it) },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            placeholder = { Text("키워드, 고객명, 주제 검색...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (query.isNotEmpty()) IconButton(onClick = { query = ""; viewModel.clearSearch() }) {
                    Icon(Icons.Default.Clear, contentDescription = "지우기")
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )
        // 최근 검색어
        if (query.isEmpty()) {
            Text("최근 검색", modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.outline)
            LazyRow(modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(viewModel.recentSearches) { recent ->
                    AssistChip(onClick = { query = recent; viewModel.search(recent) },
                        label = { Text(recent) },
                        leadingIcon = { Icon(Icons.Default.History, contentDescription = null,
                            modifier = Modifier.size(16.dp)) })
                }
            }
        }
        // 검색 결과
        when (val state = uiState) {
            is SearchUiState.Loading -> LoadingIndicator()
            is SearchUiState.Data -> LazyColumn {
                items(state.results.cards, key = { it.id }) { card ->
                    ContextCardItem(card = card, onClick = { onCardClick(card.id) })
                }
            }
            is SearchUiState.Empty -> EmptyState(message = "\"$query\" 검색 결과가 없습니다")
            is SearchUiState.Error -> ErrorState(message = state.message,
                onRetry = { viewModel.search(query) })
            else -> Unit
        }
    }
}
```

**UploadScreen** (데모 품질):
- 고객 선택 드롭다운 (`ExposedDropdownMenuBox`)
- 대화 유형 선택: `SegmentedButton` (MEETING / CALL / EMAIL / DEMO / SUPPORT)
- 파일 드롭존: 점선 테두리 Box + 파일 아이콘 + "파일을 선택하거나 여기에 드래그하세요"
- 노트 입력: `OutlinedTextField` (여러 줄)
- 업로드 버튼 + 진행 상태 (ProgressIndicator 또는 LinearProgressIndicator)
- 성공 상태: 체크 아이콘 + "카드 N개가 생성되었습니다" + "카드 보러가기" 버튼

```kotlin
@Composable
fun UploadScreen(
    viewModel: UploadViewModel = hiltViewModel(),
    onNavigateToCards: (Long) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedType by remember { mutableStateOf(ConversationType.MEETING) }
    var notes by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Text("대화 업로드", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(24.dp))

        // 대화 유형 선택 (Segmented Button 스타일)
        Text("대화 유형", style = MaterialTheme.typography.labelLarge)
        Spacer(modifier = Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(ConversationType.values().toList()) { type ->
                FilterChip(
                    selected = selectedType == type,
                    onClick = { selectedType = type },
                    label = { Text(type.name) }
                )
            }
        }
        Spacer(modifier = Modifier.height(20.dp))

        // 파일 드롭존 (점선 테두리)
        Box(
            modifier = Modifier.fillMaxWidth().height(140.dp)
                .border(2.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    RoundedCornerShape(12.dp), PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f) /* dashed */),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.UploadFile, contentDescription = null,
                    modifier = Modifier.size(40.dp), tint = MaterialTheme.colorScheme.outline)
                Spacer(modifier = Modifier.height(8.dp))
                Text("파일을 선택하거나 여기에 드래그하세요",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline)
                Text("MP3, WAV, M4A, TXT 지원",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline)
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        // 노트 입력
        OutlinedTextField(
            value = notes, onValueChange = { notes = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("추가 메모 (선택사항)") },
            minLines = 3, maxLines = 5
        )
        Spacer(modifier = Modifier.height(24.dp))

        // 상태별 UI
        when (val state = uiState) {
            is UploadUiState.Idle -> {
                Button(onClick = { viewModel.upload(customerId = 0L, type = selectedType.name, notes = notes.ifBlank { null }) },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(12.dp)) {
                    Text("업로드", style = MaterialTheme.typography.titleMedium)
                }
            }
            is UploadUiState.Uploading -> {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                Text("업로드 중...", modifier = Modifier.align(Alignment.CenterHorizontally),
                    color = MaterialTheme.colorScheme.outline)
            }
            is UploadUiState.Success -> {
                Column(horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null,
                        tint = SentimentPositive, modifier = Modifier.size(56.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("카드 ${state.cardsGenerated}개가 생성되었습니다",
                        style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(onClick = { /* onNavigateToCards */ },
                        modifier = Modifier.fillMaxWidth()) {
                        Text("카드 보러가기")
                    }
                }
            }
            is UploadUiState.Error -> ErrorState(message = state.message,
                onRetry = { viewModel.resetState() })
        }
    }
}
```

**주의**: UploadViewModel은 M-42에서 구현. 이 단계에서는 hiltViewModel() 참조만,
컴파일이 안 되면 임시로 파라미터 제거하고 상태 없이 정적 UI만 렌더링.

**검증**: `./gradlew.bat compileDebugKotlin --no-daemon 2>&1 | tail -3`
완료 후 RALPH_BACKLOG.md M-38 체크.
`git add app/src/main/java/com/ralphthon/app/ui/`
`git commit -m "feat(M-38): SearchScreen + UploadScreen demo quality"`
`git push origin master`

---

### M-39: Feature Extension: FavoritesRepository + ToggleFavoriteUseCase (15 tests)

**목표**: 15개 테스트 PASS — Tier 1 TDD (테스트 먼저)

**스펙 읽기**: `competition-kit/test-specs/feature-extension-tests.md` 의 "§ 1 Favorites" 섹션

**파일들**:
- `app/src/main/java/com/ralphthon/app/data/repository/FavoritesRepositoryImpl.kt`
- `app/src/main/java/com/ralphthon/app/domain/repository/FavoritesRepository.kt`
- `app/src/main/java/com/ralphthon/app/domain/usecase/ToggleFavoriteUseCase.kt`
- `app/src/test/java/com/ralphthon/app/domain/usecase/ToggleFavoriteUseCaseTest.kt` (8 tests)
- `app/src/test/java/com/ralphthon/app/data/repository/FavoritesRepositoryTest.kt` (7 tests)

**FavoritesRepository** (DataStore 기반):
```kotlin
interface FavoritesRepository {
    suspend fun toggleFavorite(cardId: Long): Result<Boolean>
    suspend fun isFavorite(cardId: Long): Result<Boolean>
    suspend fun getFavoriteIds(): Result<Set<Long>>
}
```

**FavoritesRepositoryImpl** (DataStore Preferences):
```kotlin
class FavoritesRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : FavoritesRepository {
    private val FAVORITES_KEY = stringSetPreferencesKey("favorites")

    override suspend fun toggleFavorite(cardId: Long): Result<Boolean> = runCatching {
        var isFavorite = false
        dataStore.edit { prefs ->
            val current = prefs[FAVORITES_KEY]?.map { it.toLong() }?.toMutableSet() ?: mutableSetOf()
            isFavorite = if (current.contains(cardId)) {
                current.remove(cardId)
                false
            } else {
                current.add(cardId)
                true
            }
            prefs[FAVORITES_KEY] = current.map { it.toString() }.toSet()
        }
        isFavorite
    }
}
```

**ToggleFavoriteUseCase 8개 테스트**:
1. `should_returnTrue_when_cardAddedToFavorites`
2. `should_returnFalse_when_cardRemovedFromFavorites`
3. `should_returnFailure_when_cardIdNegative`
4. `should_callRepository_when_invoked`
5. `should_propagateError_when_repositoryFails`
6. `should_returnCurrentFavoriteStatus_when_checked`
7. `should_toggleFavorite_when_calledMultipleTimes`
8. `should_useIoDispatcher_when_callingRepository`

**FavoritesRepositoryTest 7개 테스트**: DataStore mock으로 toggle/isFavorite/getAll 검증

**검증**: `./gradlew.bat test --tests "*.ToggleFavoriteUseCaseTest" --tests "*.FavoritesRepositoryTest" --no-daemon 2>&1 | tail -5`
완료 후 RALPH_BACKLOG.md M-39 체크.
커밋: `git commit -m "test(FEAT-001): FavoritesRepository + ToggleFavoriteUseCase 15 tests"`
`git push origin master`

---

### M-40: Feature Extension: SortCardsUseCase (10 tests)

**목표**: 10개 테스트 PASS — Tier 1 TDD

**스펙 읽기**: `competition-kit/test-specs/feature-extension-tests.md` 의 "§ 2 Sort" 섹션

**파일들**:
- `app/src/test/java/com/ralphthon/app/domain/usecase/SortCardsUseCaseTest.kt`
- `app/src/main/java/com/ralphthon/app/domain/usecase/SortCardsUseCase.kt`

**SortCardsUseCase** (클라이언트 사이드 정렬, 네트워크 호출 없음):
```kotlin
enum class SortOrder { DATE_DESC, DATE_ASC, SENTIMENT, TITLE_ASC }

class SortCardsUseCase @Inject constructor() {
    operator fun invoke(cards: List<ContextCard>, sortOrder: SortOrder): List<ContextCard> =
        when (sortOrder) {
            SortOrder.DATE_DESC -> cards.sortedByDescending { it.createdAt }
            SortOrder.DATE_ASC -> cards.sortedBy { it.createdAt }
            SortOrder.SENTIMENT -> cards.sortedBy { it.sentiment.ordinal }
            SortOrder.TITLE_ASC -> cards.sortedBy { it.title }
        }
}
```

**10개 테스트 커버리지**:
1. `should_sortByDateDesc_when_dateDescOrderRequested`
2. `should_sortByDateAsc_when_dateAscOrderRequested`
3. `should_sortBySentiment_when_sentimentOrderRequested`
4. `should_sortByTitle_when_titleAscOrderRequested`
5. `should_returnEmptyList_when_inputIsEmpty`
6. `should_returnSingleItem_when_singleItemInput`
7. `should_preserveAllItems_when_sorted`
8. `should_maintainStableSort_when_sameValues`
9. `should_returnOriginalOrder_when_allDatesEqual`
10. `should_returnCorrectCount_when_sorted`

**검증**: `./gradlew.bat test --tests "*.SortCardsUseCaseTest" --no-daemon 2>&1 | tail -5`
완료 후 RALPH_BACKLOG.md M-40 체크.
커밋: `git commit -m "test(FEAT-002): SortCardsUseCase 10 tests"`
`git push origin master`

---

### M-41: Feature Extension: SearchHistoryRepository (20 tests)

**목표**: 20개 테스트 PASS — Tier 1 TDD

**스펙 읽기**: `competition-kit/test-specs/feature-extension-tests.md` 의 "§ 3 SearchHistory" 섹션

**파일들**:
- `app/src/main/java/com/ralphthon/app/domain/repository/SearchHistoryRepository.kt`
- `app/src/main/java/com/ralphthon/app/data/repository/SearchHistoryRepositoryImpl.kt`
- `app/src/main/java/com/ralphthon/app/ui/search/SearchHistoryViewModel.kt`
- `app/src/test/java/com/ralphthon/app/data/repository/SearchHistoryRepositoryTest.kt` (10 tests)
- `app/src/test/java/com/ralphthon/app/ui/search/SearchHistoryViewModelTest.kt` (10 tests)

**SearchHistoryRepository**:
```kotlin
interface SearchHistoryRepository {
    suspend fun addSearch(query: String): Result<Unit>
    suspend fun getHistory(): Result<List<String>>
    suspend fun clearHistory(): Result<Unit>
    suspend fun removeItem(query: String): Result<Unit>
}
```

**제약**: 최대 10개 항목, 초과 시 가장 오래된 항목 제거. DataStore 기반.

**SearchHistoryRepositoryTest 10개**:
- 항목 추가, 조회, 삭제, 전체 삭제
- 최대 10개 제한 검증
- 중복 추가 시 최신으로 이동 검증
- 빈 히스토리 조회 검증

**SearchHistoryViewModelTest 10개**:
- init 시 히스토리 로드
- 검색어 추가 시 ViewModel 상태 업데이트
- 항목 삭제
- 전체 삭제
- 에러 상태 처리

**검증**: `./gradlew.bat test --tests "*.SearchHistoryRepositoryTest" --tests "*.SearchHistoryViewModelTest" --no-daemon 2>&1 | tail -5`
완료 후 RALPH_BACKLOG.md M-41 체크.
커밋: `git commit -m "test(FEAT-003): SearchHistoryRepository 20 tests"`
`git push origin master`

---

### M-42: Feature Extension: UploadViewModel + Progress (15 tests)

**목표**: 15개 테스트 PASS — Tier 1 TDD

**스펙 읽기**: `competition-kit/test-specs/feature-extension-tests.md` 의 "§ 4 Upload Progress" 섹션

**파일들**:
- `app/src/test/java/com/ralphthon/app/ui/upload/UploadViewModelTest.kt`
- `app/src/main/java/com/ralphthon/app/ui/upload/UploadViewModel.kt`

**UploadViewModel**:
```kotlin
sealed class UploadUiState {
    object Idle : UploadUiState()
    object Uploading : UploadUiState()
    data class Success(val cardsGenerated: Int) : UploadUiState()
    data class Error(val message: String) : UploadUiState()
}

@HiltViewModel
class UploadViewModel @Inject constructor(
    private val uploadUseCase: UploadConversationUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow<UploadUiState>(UploadUiState.Idle)
    val uiState: StateFlow<UploadUiState> = _uiState.asStateFlow()

    fun upload(customerId: Long, type: String, notes: String?) {
        viewModelScope.launch {
            _uiState.value = UploadUiState.Uploading
            uploadUseCase(customerId, type, notes = notes).fold(
                onSuccess = { _uiState.value = UploadUiState.Success(it.cardsGenerated) },
                onFailure = { _uiState.value = UploadUiState.Error(it.message ?: "업로드 실패") }
            )
        }
    }

    fun resetState() { _uiState.value = UploadUiState.Idle }
}
```

**15개 테스트 커버리지**:
1. `should_emitIdle_when_initialized`
2. `should_emitUploading_when_uploadStarts`
3. `should_emitSuccess_when_uploadSucceeds`
4. `should_showCardsGenerated_when_uploadSucceeds`
5. `should_emitError_when_uploadFails`
6. `should_emitError_when_validationFails`
7. `should_transitionIdleToUploadingToSuccess`
8. `should_transitionIdleToUploadingToError`
9. `should_resetToIdle_when_resetCalled`
10. `should_callUseCase_when_uploadInvoked`
11. `should_emitError_when_networkException`
12. `should_preserveErrorMessage_when_exceptionHasMessage`
13. `should_showDefaultError_when_noMessage`
14. `should_allowRetry_when_afterError` (reset + upload 재시도)
15. `should_notAllowConcurrentUpload_when_alreadyUploading` (선택)

**검증**: `./gradlew.bat test --tests "*.UploadViewModelTest" --no-daemon 2>&1 | tail -5`
완료 후 RALPH_BACKLOG.md M-42 체크.
커밋: `git commit -m "test(FEAT-004): UploadViewModel progress 15 tests"`
`git push origin master`

---

### M-43: Feature Extension: ThemeRepository + Dark Mode (20 tests)

**목표**: 20개 테스트 PASS — Tier 1 TDD

**스펙 읽기**: `competition-kit/test-specs/feature-extension-tests.md` 의 "§ 5 Dark Mode" 섹션

**파일들**:
- `app/src/main/java/com/ralphthon/app/domain/repository/ThemeRepository.kt`
- `app/src/main/java/com/ralphthon/app/data/repository/ThemeRepositoryImpl.kt`
- `app/src/test/java/com/ralphthon/app/data/repository/ThemeRepositoryTest.kt` (10 tests)
- `app/src/test/java/com/ralphthon/app/ui/theme/ThemeTest.kt` (10 tests)

**ThemeRepository**:
```kotlin
interface ThemeRepository {
    suspend fun isDarkMode(): Result<Boolean>
    suspend fun setDarkMode(enabled: Boolean): Result<Unit>
    fun observeDarkMode(): Flow<Boolean>
}
```

**ThemeRepositoryImpl**: DataStore Preferences 기반

**ThemeRepositoryTest 10개**:
- isDarkMode 기본값 false
- setDarkMode(true) → isDarkMode() == true
- setDarkMode(false) → isDarkMode() == false
- observeDarkMode Flow 방출 검증
- DataStore 에러 처리

**ThemeTest 10개** (ViewModel 또는 pure logic):
- 테마 전환 상태 검증
- CompositionLocal 값 검증 (isSystemInDarkTheme() mock)
- 다크모드 on/off 시 색상 변경 검증 (순수 로직)

**검증**: `./gradlew.bat test --tests "*.ThemeRepositoryTest" --tests "*.ThemeTest" --no-daemon 2>&1 | tail -5`
완료 후 RALPH_BACKLOG.md M-43 체크.
커밋: `git commit -m "test(FEAT-005): ThemeRepository dark mode 20 tests"`
`git push origin master`

---

### M-44: Feature Extension UI Tests 70 tests (androidTest)

**목표**: 70개 Compose UI 테스트 PASS

**스펙 읽기**: `competition-kit/test-specs/feature-extension-tests.md` 의 "§ 1-3 UI 부분" 섹션

**테스트 위치**: `app/src/androidTest/java/com/ralphthon/app/`

**파일들**:
- `ui/feature/FavoritesUiTest.kt` (15 tests)
- `ui/feature/SortUiTest.kt` (15 tests)
- `ui/feature/SearchHistoryUiTest.kt` (20 tests)
- `ui/feature/ScreenComposeTest.kt` (20 tests)

**Compose Testing 패턴** (JUnit 4 + AndroidJUnit4):
```kotlin
@RunWith(AndroidJUnit4::class)
class ScreenComposeTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun should_showCustomerList_when_dataLoaded() {
        composeTestRule.setContent {
            CustomerCard(customer = Customer.withDefaults(name = "홍길동", company = "삼성"), onClick = {})
        }
        composeTestRule.onNodeWithText("홍길동").assertIsDisplayed()
        composeTestRule.onNodeWithText("삼성").assertIsDisplayed()
    }
}
```

**Semantic assertion 사용** (onNodeWithText, onNodeWithContentDescription, onNodeWithTag):
- FavoritesUiTest: 즐겨찾기 아이콘 표시/해제 검증
- SortUiTest: 정렬 메뉴 표시, 정렬 후 순서 검증
- SearchHistoryUiTest: 히스토리 항목 표시, 탭 시 검색창 자동입력 검증
- ScreenComposeTest: 각 Screen의 기본 렌더링, Empty/Loading/Error 상태 UI 검증

**androidTest에서는 JUnit 4 사용**:
```kotlin
import org.junit.Test
import org.junit.runner.RunWith
import androidx.test.ext.junit.runners.AndroidJUnit4
```

**검증**: 이 테스트들은 기기/에뮬레이터가 필요하므로 실제 실행 대신 컴파일 검증:
`./gradlew.bat compileDebugAndroidTestKotlin --no-daemon 2>&1 | tail -5`
완료 후 RALPH_BACKLOG.md M-44 체크.
커밋: `git commit -m "test(FEAT-006): UI feature tests 70"`
`git push origin master`

---

## Phase 5 완료 처리

모든 M-35 ~ M-44가 완료되면:
1. `./gradlew.bat test --no-daemon 2>&1 | tail -5` → 누적 테스트 수 확인 (목표: 616+)
2. `git add app/src/`
3. `git commit -m "feat: Phase 5 UI screens + features complete (150 tests)"`
4. `git push origin master`
5. 세션 종료
