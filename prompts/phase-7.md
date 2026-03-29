RALPH_BACKLOG.md를 읽고 첫 번째 미완료(- [ ]) 마일스톤을 찾아서 실행하라.
이미 완료된(- [x]) 마일스톤은 건너뛴다.
이 Phase의 마일스톤이 모두 완료되면 세션을 종료한다.

세션 시작 체크:
1. RALPH_BACKLOG.md 읽기
2. git log --oneline -3
3. ./gradlew.bat compileDebugKotlin --no-daemon 2>&1 | tail -3

---

## Phase 7: Integration + Wiring + Final Verification
마일스톤 M-53 ~ M-58을 순서대로 실행한다.
각 마일스톤 완료 시 RALPH_BACKLOG.md의 해당 항목을 `- [x]`로 업데이트하라.
같은 에러 3회 반복 시 `- [SKIP]`으로 표시하고 다음 마일스톤으로 진행하라.

**전제**: Phase 1~6(M-01~M-52)가 완료된 상태.
이 Phase는 다음을 완성한다:
- Repository 구현체 4개 생성
- DI DataModule 생성 (Hilt bindings)
- NavGraph를 실제 Screen에 연결
- 동시성 테스트, 통합 테스트 추가
- 전체 최종 검증

---

### M-53: Repository Implementations 4개

**목표**: 4개 Repository 구현체 생성 + 컴파일 성공

**파일 위치**: `app/src/main/java/com/ralphthon/app/data/repository/`

**CustomerRepositoryImpl.kt**:
```kotlin
package com.ralphthon.app.data.repository

import com.ralphthon.app.data.api.CustomerApiClient
import com.ralphthon.app.data.mapper.toDomain
import com.ralphthon.app.domain.model.Customer
import com.ralphthon.app.domain.repository.CustomerRepository
import javax.inject.Inject

class CustomerRepositoryImpl @Inject constructor(
    private val apiClient: CustomerApiClient
) : CustomerRepository {

    override suspend fun getCustomers(): Result<List<Customer>> =
        apiClient.getCustomers()

    override suspend fun getCustomerById(id: Long): Result<Customer> =
        apiClient.getCustomerById(id)
}
```

**CardRepositoryImpl.kt**:
```kotlin
class CardRepositoryImpl @Inject constructor(
    private val apiClient: CardApiClient
) : CardRepository {

    override suspend fun getCardsByCustomer(
        customerId: Long, page: Int, size: Int
    ): Result<CardListResult> = apiClient.getCardsByCustomer(customerId, page, size)

    override suspend fun getCardById(cardId: Long): Result<ContextCard> =
        apiClient.getCardById(cardId)

    override suspend fun searchCards(
        query: String, customerId: Long?, dateFrom: String?, dateTo: String?,
        page: Int, size: Int
    ): Result<CardListResult> = apiClient.searchCards(query, customerId, dateFrom, dateTo, page, size)
}
```

**KnowledgeRepositoryImpl.kt**:
```kotlin
class KnowledgeRepositoryImpl @Inject constructor(
    private val apiClient: KnowledgeApiClient
) : KnowledgeRepository {
    override suspend fun getKnowledge(keywordId: Long): Result<KnowledgeResult> =
        apiClient.getKnowledge(keywordId)
}
```

**UploadRepositoryImpl.kt**:
```kotlin
class UploadRepositoryImpl @Inject constructor(
    private val apiClient: UploadApiClient
) : UploadRepository {
    override suspend fun uploadConversation(
        customerId: Long, type: String, audioFile: File?,
        transcriptFile: File?, notes: String?
    ): Result<UploadResult> = apiClient.upload(customerId, type, audioFile, transcriptFile, notes)
}
```

**UploadApiClient에 `upload()` 메서드가 없으면 추가**:
```kotlin
suspend fun upload(
    customerId: Long, type: String,
    audioFile: File?, transcriptFile: File?, notes: String?
): Result<UploadResult> = runCatching {
    val customerIdBody = customerId.toString().toRequestBody("text/plain".toMediaType())
    val typeBody = type.toRequestBody("text/plain".toMediaType())
    val audioPart = audioFile?.let { MultipartBody.Part.createFormData("audio", it.name, it.asRequestBody()) }
    val transcriptPart = transcriptFile?.let { MultipartBody.Part.createFormData("transcript", it.name, it.asRequestBody()) }
    val notesBody = notes?.toRequestBody("text/plain".toMediaType())
    val response = service.uploadConversation(customerIdBody, typeBody, audioPart, transcriptPart, notesBody)
    if (response.isSuccessful) {
        val body = response.body() ?: throw NetworkException("Empty response")
        UploadResult(conversationId = body.conversation_id, cardsGenerated = body.cards_generated)
    } else {
        throw NetworkException("HTTP ${response.code()}")
    }
}
```

**검증**: `./gradlew.bat compileDebugKotlin --no-daemon 2>&1 | tail -3`
완료 후 RALPH_BACKLOG.md M-53 체크.
`git add app/src/main/java/com/ralphthon/app/data/repository/`
`git commit -m "feat(M-53): Repository implementations 4 files"`
`git push origin master`

---

### M-54: DI DataModule

**목표**: Hilt bindings for Repository interfaces → Implementations

**파일 산출물**: `app/src/main/java/com/ralphthon/app/di/DataModule.kt`

```kotlin
package com.ralphthon.app.di

import com.ralphthon.app.data.api.CardApiService
import com.ralphthon.app.data.api.CustomerApiService
import com.ralphthon.app.data.api.KnowledgeApiService
import com.ralphthon.app.data.api.UploadApiService
import com.ralphthon.app.data.repository.*
import com.ralphthon.app.domain.repository.*
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds
    @Singleton
    abstract fun bindCustomerRepository(impl: CustomerRepositoryImpl): CustomerRepository

    @Binds
    @Singleton
    abstract fun bindCardRepository(impl: CardRepositoryImpl): CardRepository

    @Binds
    @Singleton
    abstract fun bindKnowledgeRepository(impl: KnowledgeRepositoryImpl): KnowledgeRepository

    @Binds
    @Singleton
    abstract fun bindUploadRepository(impl: UploadRepositoryImpl): UploadRepository

    @Binds
    @Singleton
    abstract fun bindFavoritesRepository(impl: FavoritesRepositoryImpl): FavoritesRepository

    @Binds
    @Singleton
    abstract fun bindSearchHistoryRepository(impl: SearchHistoryRepositoryImpl): SearchHistoryRepository

    @Binds
    @Singleton
    abstract fun bindThemeRepository(impl: ThemeRepositoryImpl): ThemeRepository

    companion object {
        @Provides
        @Singleton
        fun provideCustomerApiService(retrofit: Retrofit): CustomerApiService =
            retrofit.create(CustomerApiService::class.java)

        @Provides
        @Singleton
        fun provideCardApiService(retrofit: Retrofit): CardApiService =
            retrofit.create(CardApiService::class.java)

        @Provides
        @Singleton
        fun provideKnowledgeApiService(retrofit: Retrofit): KnowledgeApiService =
            retrofit.create(KnowledgeApiService::class.java)

        @Provides
        @Singleton
        fun provideUploadApiService(retrofit: Retrofit): UploadApiService =
            retrofit.create(UploadApiService::class.java)
    }
}
```

**DataStore 주입 추가** (AppModule에 또는 DataModule companion에):
```kotlin
@Provides
@Singleton
fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
    PreferenceDataStoreFactory.create { context.preferencesDataStoreFile("user_prefs") }
```

`AppModule.kt`에 `@ApplicationContext context: Context` 파라미터 추가 필요 시 수정.

**검증**: `./gradlew.bat compileDebugKotlin --no-daemon 2>&1 | tail -3`
완료 후 RALPH_BACKLOG.md M-54 체크.
`git add app/src/main/java/com/ralphthon/app/di/`
`git commit -m "feat(M-54): DI DataModule Hilt bindings"`
`git push origin master`

---

### M-55: NavGraph Wiring + MainActivity

**목표**: NavGraph를 실제 Screen에 연결, `./gradlew.bat assembleDebug` BUILD SUCCESSFUL

**파일 수정**:
- `app/src/main/java/com/ralphthon/app/ui/navigation/NavGraph.kt` (업데이트)
- `app/src/main/java/com/ralphthon/app/MainActivity.kt` (업데이트)

**NavGraph.kt** (실제 Screen 연결):
```kotlin
@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.CustomerList.route) {
        composable(Screen.CustomerList.route) {
            CustomerListScreen(
                onCustomerClick = { customerId ->
                    navController.navigate(Screen.CardNewsList.createRoute(customerId))
                }
            )
        }
        composable(
            route = Screen.CardNewsList.route,
            arguments = listOf(navArgument("customerId") { type = NavType.LongType })
        ) { backStackEntry ->
            val customerId = backStackEntry.arguments?.getLong("customerId") ?: return@composable
            CardNewsListScreen(
                customerId = customerId,
                onCardClick = { cardId ->
                    navController.navigate(Screen.CardDetail.createRoute(cardId))
                },
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(
            route = Screen.CardDetail.route,
            arguments = listOf(navArgument("cardId") { type = NavType.LongType })
        ) { backStackEntry ->
            val cardId = backStackEntry.arguments?.getLong("cardId") ?: return@composable
            CardDetailScreen(
                cardId = cardId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Screen.Search.route) {
            SearchScreen(
                onCardClick = { cardId ->
                    navController.navigate(Screen.CardDetail.createRoute(cardId))
                }
            )
        }
        composable(Screen.Upload.route) {
            UploadScreen()
        }
    }
}
```

**MainActivity.kt**:
```kotlin
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RalphthonTheme {
                val navController = rememberNavController()
                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = false,
                                onClick = { navController.navigate(Screen.CustomerList.route) },
                                icon = { Icon(Icons.Default.People, contentDescription = "고객") },
                                label = { Text("고객") }
                            )
                            NavigationBarItem(
                                selected = false,
                                onClick = { navController.navigate(Screen.Search.route) },
                                icon = { Icon(Icons.Default.Search, contentDescription = "검색") },
                                label = { Text("검색") }
                            )
                            NavigationBarItem(
                                selected = false,
                                onClick = { navController.navigate(Screen.Upload.route) },
                                icon = { Icon(Icons.Default.Upload, contentDescription = "업로드") },
                                label = { Text("업로드") }
                            )
                        }
                    }
                ) { paddingValues ->
                    Box(modifier = Modifier.padding(paddingValues)) {
                        NavGraph(navController = navController)
                    }
                }
            }
        }
    }
}
```

**Screen Composable 시그니처 조정**:
NavGraph에서 customerId를 파라미터로 받는 Screen들은 ViewModel에 SavedStateHandle로 전달하거나 파라미터로 직접 받도록 수정.
CardNewsListViewModel이 SavedStateHandle 기반이 아니라면 customerId 파라미터를 받도록 수정.

### Bottom Navigation 구현
- BottomNavigation with 3 items: 고객목록, 검색, 업로드
- 각 탭에 적절한 Material Icon
- 현재 탭 하이라이트 (primary color)
- NavGraph에서 startDestination = customerList

```kotlin
// MainActivity.kt Scaffold bottomBar
val navBackStackEntry by navController.currentBackStackEntryAsState()
val currentRoute = navBackStackEntry?.destination?.route

Scaffold(
    bottomBar = {
        NavigationBar {
            NavigationBarItem(
                selected = currentRoute == Screen.CustomerList.route,
                onClick = {
                    navController.navigate(Screen.CustomerList.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(Icons.Default.People, contentDescription = "고객목록") },
                label = { Text("고객") }
            )
            NavigationBarItem(
                selected = currentRoute == Screen.Search.route,
                onClick = {
                    navController.navigate(Screen.Search.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(Icons.Default.Search, contentDescription = "검색") },
                label = { Text("검색") }
            )
            NavigationBarItem(
                selected = currentRoute == Screen.Upload.route,
                onClick = {
                    navController.navigate(Screen.Upload.route) {
                        popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(Icons.Default.Upload, contentDescription = "업로드") },
                label = { Text("업로드") }
            )
        }
    }
) { paddingValues ->
    Box(modifier = Modifier.padding(paddingValues)) {
        NavGraph(navController = navController)
    }
}
```

**검증**: `./gradlew.bat assembleDebug --no-daemon 2>&1 | tail -5` → BUILD SUCCESSFUL 필수
완료 후 RALPH_BACKLOG.md M-55 체크.
`git add app/src/main/java/com/ralphthon/app/ui/navigation/ app/src/main/java/com/ralphthon/app/MainActivity.kt`
`git commit -m "feat(M-55): NavGraph wiring + BottomNavigation 3 tabs"`
`git push origin master`

---

### M-56: Concurrency Tests 80 tests

**목표**: 80개 테스트 PASS

**스펙 읽기**: `competition-kit/test-specs/concurrency-tests.md` 의 "§ 1-5" 섹션

**테스트 파일들**:
- `app/src/test/java/com/ralphthon/app/ConcurrencyTest.kt` (30 tests)
- `app/src/test/java/com/ralphthon/app/data/api/ConcurrentApiTest.kt` (25 tests)
- `app/src/test/java/com/ralphthon/app/ui/viewmodel/ConcurrentViewModelTest.kt` (25 tests)

**ConcurrencyTest.kt (30개)** — 일반 동시성:
```kotlin
@Test
fun should_handleConcurrentRequests_when_multipleCalled() = runTest {
    val results = (1..10).map { id ->
        async { useCase(id.toLong()) }
    }.awaitAll()
    assertEquals(10, results.filter { it.isSuccess }.size)
}

@Test
fun should_cancelCoroutine_when_jobCancelled() = runTest {
    var wasExecuted = false
    val job = launch {
        delay(1000)
        wasExecuted = true
    }
    job.cancel()
    advanceTimeBy(2000)
    assertFalse(wasExecuted)
}
```
- 동시 API 요청 10개: 결과 수 검증
- Job 취소 시 코루틴 중단 검증
- 디스패처 전환 (IO → Main) 검증
- StateFlow 동시 다중 구독자 검증
- viewModelScope 취소 시 pending 코루틴 정리 검증

**ConcurrentApiTest.kt (25개)** — API 동시 호출:
- MockWebServer에 동시 요청 처리
- race condition 없음 검증
- 응답 순서 독립성 검증

**ConcurrentViewModelTest.kt (25개)** — ViewModel 동시성:
```kotlin
@Test
fun should_notHaveRaceCondition_when_refreshCalledMultipleTimes() = runTest {
    repeat(5) { viewModel.refresh() }
    advanceUntilIdle()
    // 최종 상태는 Data 또는 Error (중간 Loading은 허용)
    assertFalse(viewModel.uiState.value is CustomerListUiState.Loading)
}
```
- 동시 refresh() 호출 시 최종 상태 일관성
- loadMore() 중복 호출 방지 검증
- 취소 후 재시작 검증

**검증**: `./gradlew.bat test --tests "*.ConcurrencyTest" --tests "*.ConcurrentApiTest" --tests "*.ConcurrentViewModelTest" --no-daemon 2>&1 | tail -5`
완료 후 RALPH_BACKLOG.md M-56 체크.
커밋: `git commit -m "test(CONC-001): Concurrency tests 80"`
`git push origin master`

---

### M-57: Repository Integration Tests 20 tests

**목표**: 20개 테스트 PASS

**스펙 읽기**: `competition-kit/test-specs/domain-tests.md` 의 "§ 10-12 Repository Integration" 섹션

**테스트 파일들**:
- `app/src/test/java/com/ralphthon/app/data/repository/CustomerRepositoryImplTest.kt` (5 tests)
- `app/src/test/java/com/ralphthon/app/data/repository/CardRepositoryImplTest.kt` (6 tests)
- `app/src/test/java/com/ralphthon/app/data/repository/KnowledgeRepositoryImplTest.kt` (5 tests)
- `app/src/test/java/com/ralphthon/app/data/repository/UploadRepositoryImplTest.kt` (4 tests)

**통합 테스트 패턴** (MockWebServer + 실제 ApiClient + 실제 Mapper + Repository Impl):
```kotlin
class CustomerRepositoryImplTest {
    private lateinit var mockWebServer: MockWebServer
    private lateinit var repository: CustomerRepositoryImpl

    @BeforeEach
    fun setUp() {
        mockWebServer = MockWebServer()
        mockWebServer.start()
        val retrofit = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        val apiService = retrofit.create(CustomerApiService::class.java)
        val apiClient = CustomerApiClient(apiService)
        repository = CustomerRepositoryImpl(apiClient)
    }

    @Test
    fun should_returnMappedDomainObjects_when_apiSucceeds() = runTest {
        mockWebServer.enqueue(MockResponse()
            .setBody("""{"customers":[{"id":1,"name":"홍길동","company":"삼성","card_count":5,"total_conversations":10,"last_interaction_at":"2024-01-01"}],"total_count":1,"has_more":false}""")
            .setResponseCode(200))
        val result = repository.getCustomers()
        assertTrue(result.isSuccess)
        val customers = result.getOrNull()!!
        assertEquals(1, customers.size)
        assertEquals("홍길동", customers[0].name)
        assertEquals("삼성", customers[0].company)
    }
}
```

**각 Repository 5개 테스트**:
1. API 성공 → 도메인 객체 매핑 검증
2. API 404 → NotFoundException 전파 검증
3. API 500 → NetworkException 전파 검증
4. 빈 응답 → 빈 리스트 반환 검증
5. 특수 케이스 (페이지네이션, 필터 등)

**검증**: `./gradlew.bat test --tests "*RepositoryImplTest" --no-daemon 2>&1 | tail -5`
완료 후 RALPH_BACKLOG.md M-57 체크.
커밋: `git commit -m "test(INT-001): Repository integration tests 20"`
`git push origin master`

---

### M-58: FINAL VERIFICATION

**목표**: 전체 테스트 1000개 이상 PASS, assembleDebug BUILD SUCCESSFUL

**단계별 실행**:

**단계 1: 전체 unit test 실행**
```
./gradlew.bat test --no-daemon 2>&1 | tail -10
```
- "BUILD SUCCESSFUL" 확인
- `X tests completed, 0 failed` 확인
- 실패 테스트 있으면 즉시 수정 후 재실행

**단계 2: XML 리포트에서 테스트 수 확인**
```
./gradlew.bat test --no-daemon 2>&1 | grep "tests"
```
목표: 총 1000개 이상

XML 리포트 위치: `app/build/reports/tests/testDebugUnitTest/`

**단계 3: Debug APK 빌드**
```
./gradlew.bat assembleDebug --no-daemon 2>&1 | tail -5
```
"BUILD SUCCESSFUL" + APK 파일 생성 확인:
`app/build/outputs/apk/debug/app-debug.apk`

**단계 4: RALPH_BACKLOG.md 최종 업데이트**
- 모든 M-01 ~ M-58 항목이 `- [x]`로 표시되어 있는지 확인
- 테스트 수, 빌드 상태 Summary 섹션에 기록:
  ```
  ## Final Results
  - 총 테스트 수: XXXX
  - 빌드 상태: GREEN
  - APK 생성: ✓
  - 완료 시각: YYYY-MM-DD HH:MM
  ```

**단계 5: 최종 커밋 + 푸시**
```
git add app/src RALPH_BACKLOG.md
git commit -m "feat: Phase 7 final verification - all tests pass"
git push
```

**완료 조건**:
- BUILD SUCCESSFUL (assembleDebug)
- 테스트 총 수 >= 1000 (0 failed)
- RALPH_BACKLOG.md 모든 항목 `- [x]`
- git push 성공

완료 후 세션 종료.

---

## Phase 7 완료 = 전체 프로젝트 완료

축하합니다! 랄프톤 프로젝트가 완료되었습니다.

**최종 통계 요약**:
| Phase | 마일스톤 | 테스트 수 |
|-------|---------|---------|
| 1. Architecture | M-01~M-08 | 26 |
| 2. Data Layer | M-09~M-16 | 76 |
| 3. Domain UseCases | M-17~M-24 | 132 |
| 4. ViewModels | M-25~M-34 | 232 |
| 5. UI + Features | M-35~M-44 | 150 |
| 6. Deep Testing | M-45~M-52 | 365 |
| 7. Integration | M-53~M-58 | 100 |
| **합계** | **58개** | **1,081개** |
