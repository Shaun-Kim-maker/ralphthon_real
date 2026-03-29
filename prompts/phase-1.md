RALPH_BACKLOG.md를 읽고 첫 번째 미완료(- [ ]) 마일스톤을 찾아서 실행하라.
이미 완료된(- [x]) 마일스톤은 건너뛴다.
이 Phase의 마일스톤이 모두 완료되면 세션을 종료한다.

세션 시작 체크:
1. RALPH_BACKLOG.md 읽기
2. git log --oneline -3
3. ./gradlew.bat compileDebugKotlin --no-daemon 2>&1 | tail -3

---

## Phase 1: Architecture Foundation
마일스톤 M-01 ~ M-08을 순서대로 실행한다.
각 마일스톤 완료 시 RALPH_BACKLOG.md의 해당 항목을 `- [x]`로 업데이트하라.
같은 에러 3회 반복 시 `- [SKIP]`으로 표시하고 다음 마일스톤으로 진행하라.

---

### M-01: Gradle 프로젝트 스캐폴딩

**목표**: 빌드 시스템 설정 완료 (BUILD SUCCESSFUL)

**파일 산출물**:
- `build.gradle.kts` (project-level)
- `app/build.gradle.kts` (app-level)
- `settings.gradle.kts`
- `gradle/libs.versions.toml`

**libs.versions.toml 의존성 (정확한 버전 사용)**:
```toml
[versions]
kotlin = "1.9.22"
compose-bom = "2024.02.00"
compose-compiler = "1.5.10"
hilt = "2.50"
retrofit = "2.9.0"
okhttp = "4.12.0"
gson = "2.10.1"
junit5 = "5.10.1"
mockk = "1.13.8"
coroutines-test = "1.7.3"
mockwebserver = "4.12.0"
turbine = "1.0.0"
mannodermaus-plugin = "1.10.0.0"
agp = "8.2.2"
coil = "2.5.0"
accompanist = "0.34.0"
core-ktx = "1.12.0"
lifecycle = "2.7.0"
activity-compose = "1.8.2"
navigation-compose = "2.7.6"
hilt-navigation-compose = "1.1.0"
datastore = "1.0.0"

[libraries]
# Kotlin
kotlin-stdlib = { module = "org.jetbrains.kotlin:kotlin-stdlib", version.ref = "kotlin" }
# Compose
compose-bom = { module = "androidx.compose:compose-bom", version.ref = "compose-bom" }
compose-ui = { module = "androidx.compose.ui:ui" }
compose-ui-tooling = { module = "androidx.compose.ui:ui-tooling" }
compose-ui-tooling-preview = { module = "androidx.compose.ui:ui-tooling-preview" }
compose-material3 = { module = "androidx.compose.material3:material3" }
compose-icons-extended = { module = "androidx.compose.material:material-icons-extended" }
# AndroidX
core-ktx = { module = "androidx.core:core-ktx", version.ref = "core-ktx" }
lifecycle-runtime-ktx = { module = "androidx.lifecycle:lifecycle-runtime-ktx", version.ref = "lifecycle" }
lifecycle-viewmodel-compose = { module = "androidx.lifecycle:lifecycle-viewmodel-compose", version.ref = "lifecycle" }
activity-compose = { module = "androidx.activity:activity-compose", version.ref = "activity-compose" }
navigation-compose = { module = "androidx.navigation:navigation-compose", version.ref = "navigation-compose" }
# Hilt
hilt-android = { module = "com.google.dagger:hilt-android", version.ref = "hilt" }
hilt-compiler = { module = "com.google.dagger:hilt-android-compiler", version.ref = "hilt" }
hilt-navigation-compose = { module = "androidx.hilt:hilt-navigation-compose", version.ref = "hilt-navigation-compose" }
# Network
retrofit = { module = "com.squareup.retrofit2:retrofit", version.ref = "retrofit" }
retrofit-gson = { module = "com.squareup.retrofit2:converter-gson", version.ref = "retrofit" }
okhttp = { module = "com.squareup.okhttp3:okhttp", version.ref = "okhttp" }
okhttp-logging = { module = "com.squareup.okhttp3:logging-interceptor", version.ref = "okhttp" }
gson = { module = "com.google.code.gson:gson", version.ref = "gson" }
# DataStore
datastore-preferences = { module = "androidx.datastore:datastore-preferences", version.ref = "datastore" }
# Test
junit5-api = { module = "org.junit.jupiter:junit-jupiter-api", version.ref = "junit5" }
junit5-engine = { module = "org.junit.jupiter:junit-jupiter-engine", version.ref = "junit5" }
junit5-params = { module = "org.junit.jupiter:junit-jupiter-params", version.ref = "junit5" }
mockk = { module = "io.mockk:mockk", version.ref = "mockk" }
coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "coroutines-test" }
mockwebserver = { module = "com.squareup.okhttp3:mockwebserver", version.ref = "mockwebserver" }
turbine = { module = "app.cash.turbine:turbine", version.ref = "turbine" }
# Image Loading
coil-compose = { module = "io.coil-kt:coil-compose", version.ref = "coil" }
accompanist-placeholder = { module = "com.google.accompanist:accompanist-placeholder-material3", version.ref = "accompanist" }
# Android Test
compose-ui-test-junit4 = { module = "androidx.compose.ui:ui-test-junit4" }
compose-ui-test-manifest = { module = "androidx.compose.ui:ui-test-manifest" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
mannodermaus = { id = "de.mannodermaus.android-junit5", version.ref = "mannodermaus-plugin" }
```

**project build.gradle.kts**:
```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.hilt) apply false
}
```

**app/build.gradle.kts** 핵심 설정:
- `applicationId = "com.ralphthon.app"`
- `compileSdk = 35`, `targetSdk = 35`, `minSdk = 26`
- `composeOptions { kotlinCompilerExtensionVersion = "1.5.10" }`
- `buildFeatures { compose = true; buildConfig = true }`
- `buildConfigField("String", "BASE_URL", "\"https://api.ralphthon.com/\"")` in defaultConfig
- kapt for Hilt
- mannodermaus junit5 plugin 적용

**settings.gradle.kts**:
```kotlin
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "ralphthon"
include(":app")
```

**검증**: `./gradlew.bat assembleDebug --no-daemon 2>&1 | tail -5` → BUILD SUCCESSFUL
완료 후 RALPH_BACKLOG.md M-01 체크.
`git add gradle/ build.gradle.kts app/build.gradle.kts settings.gradle.kts`
`git commit -m "feat(M-01): Gradle scaffolding BUILD SUCCESSFUL"`
`git push origin master`

---

### M-02: Domain 모델 7개

**목표**: 7개 data class 생성, 컴파일 성공

**파일 위치**: `app/src/main/java/com/ralphthon/app/domain/model/`

**파일별 내용**:

`Customer.kt`:
```kotlin
data class Customer(
    val id: Long,
    val name: String,
    val company: String,
    val cardCount: Int,
    val totalConversations: Int,
    val lastInteractionAt: String,
    val contacts: List<Contact> = emptyList()
) {
    companion object {
        fun withDefaults(
            id: Long = 0L,
            name: String = "",
            company: String = "",
            cardCount: Int = 0,
            totalConversations: Int = 0,
            lastInteractionAt: String = "",
            contacts: List<Contact> = emptyList()
        ) = Customer(id, name, company, cardCount, totalConversations, lastInteractionAt, contacts)
    }
}
```

`Contact.kt`:
```kotlin
data class Contact(
    val id: Long,
    val name: String,
    val email: String,
    val phone: String,
    val position: String
)
```

`Conversation.kt`:
```kotlin
data class Conversation(
    val id: Long,
    val type: ConversationType,
    val date: String,
    val duration: Int,
    val summary: String
)
```

`ContextCard.kt`:
```kotlin
data class ContextCard(
    val id: Long,
    val customerId: Long,
    val customerName: String,
    val title: String,
    val summary: String,
    val topic: String,
    val conversationType: ConversationType,
    val sentiment: Sentiment,
    val createdAt: String,
    val statements: List<KeyStatement> = emptyList(),
    val keywords: List<Keyword> = emptyList()
) {
    companion object {
        fun withDefaults(
            id: Long = 0L,
            customerId: Long = 0L,
            customerName: String = "",
            title: String = "",
            summary: String = "",
            topic: String = "",
            conversationType: ConversationType = ConversationType.MEETING,
            sentiment: Sentiment = Sentiment.NEUTRAL,
            createdAt: String = "",
            statements: List<KeyStatement> = emptyList(),
            keywords: List<Keyword> = emptyList()
        ) = ContextCard(id, customerId, customerName, title, summary, topic, conversationType, sentiment, createdAt, statements, keywords)
    }
}
```

`KeyStatement.kt`:
```kotlin
data class KeyStatement(
    val id: Long,
    val text: String,
    val speaker: String,
    val timestampInSeconds: Int,
    val sentiment: Sentiment
) {
    companion object {
        fun withDefaults(
            id: Long = 0L,
            text: String = "",
            speaker: String = "",
            timestampInSeconds: Int = 0,
            sentiment: Sentiment = Sentiment.NEUTRAL
        ) = KeyStatement(id, text, speaker, timestampInSeconds, sentiment)
    }
}
```

`Keyword.kt`:
```kotlin
data class Keyword(
    val id: Long,
    val term: String,
    val category: KeywordCategory,
    val frequency: Int
) {
    companion object {
        fun withDefaults(
            id: Long = 0L,
            term: String = "",
            category: KeywordCategory = KeywordCategory.GENERAL,
            frequency: Int = 0
        ) = Keyword(id, term, category, frequency)
    }
}
```

`KnowledgeArticle.kt`:
```kotlin
data class KnowledgeArticle(
    val id: Long,
    val title: String,
    val content: String,
    val source: String,
    val contextualExplanation: String,
    val relatedKeywords: List<String> = emptyList()
) {
    companion object {
        fun withDefaults(
            id: Long = 0L,
            title: String = "",
            content: String = "",
            source: String = "",
            contextualExplanation: String = "",
            relatedKeywords: List<String> = emptyList()
        ) = KnowledgeArticle(id, title, content, source, contextualExplanation, relatedKeywords)
    }
}
```

**검증**: `./gradlew.bat compileDebugKotlin --no-daemon 2>&1 | tail -3` → BUILD SUCCESSFUL
완료 후 RALPH_BACKLOG.md M-02 체크.
`git add app/src/main/java/com/ralphthon/app/domain/model/`
`git commit -m "feat(M-02): Domain models 7 files"`
`git push origin master`

---

### M-03: Enum 3개 + SearchResult

**목표**: 3개 enum, 1개 data class 생성, 컴파일 성공

**파일 위치**: `app/src/main/java/com/ralphthon/app/domain/model/`

`ConversationType.kt`:
```kotlin
enum class ConversationType {
    MEETING, CALL, EMAIL, DEMO, SUPPORT
}
```

`Sentiment.kt`:
```kotlin
enum class Sentiment {
    POSITIVE, NEGATIVE, NEUTRAL, COMMITMENT, CONCERN, QUESTION;

    companion object {
        fun fromString(value: String): Sentiment =
            values().firstOrNull { it.name.equals(value, ignoreCase = true) } ?: NEUTRAL
    }
}
```

`KeywordCategory.kt`:
```kotlin
enum class KeywordCategory {
    TECHNOLOGY, BUSINESS, PRODUCT, COMPETITOR, GENERAL;

    companion object {
        fun fromString(value: String): KeywordCategory =
            values().firstOrNull { it.name.equals(value, ignoreCase = true) } ?: GENERAL
    }
}
```

`SearchResult.kt`:
```kotlin
data class SearchResult(
    val cards: List<ContextCard>,
    val totalCount: Int,
    val hasMore: Boolean,
    val query: String
)
```

**검증**: `./gradlew.bat compileDebugKotlin --no-daemon 2>&1 | tail -3`
완료 후 RALPH_BACKLOG.md M-03 체크.
`git add app/src/main/java/com/ralphthon/app/domain/model/`
`git commit -m "feat(M-03): Enums 3 + SearchResult"`
`git push origin master`

---

### M-04: ModelValidationTest 16 tests (TDD)

**목표**: 16개 테스트 PASS

**스펙 읽기**: `competition-kit/test-specs/domain-tests.md` 의 "§ 14 Model Validation" 섹션 읽기 (offset 사용해서 해당 부분만)

**파일 산출물**: `app/src/test/java/com/ralphthon/app/domain/model/ModelValidationTest.kt`

**테스트 파일 먼저 작성 (Tier 1 TDD)**:
- `@ExtendWith(MockitoExtension::class)` 또는 JUnit 5 순수 사용
- 테스트 메서드명 규칙: `should_[expectedBehavior]_when_[condition]`
- Customer.withDefaults()가 올바른 기본값을 갖는지 검증
- ContextCard.withDefaults() 기본값 검증
- KeyStatement.withDefaults() 기본값 검증
- Keyword.withDefaults() 기본값 검증
- KnowledgeArticle.withDefaults() 기본값 검증
- Sentiment.fromString() 유효/무효 입력 검증
- KeywordCategory.fromString() 유효/무효 입력 검증
- null/빈 문자열 입력 처리 검증

**JUnit 5 임포트** (mannodermaus 사용):
```kotlin
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.*
```

**검증**: `./gradlew.bat test --tests "*.ModelValidationTest" --no-daemon 2>&1 | tail -5`
완료 후 RALPH_BACKLOG.md M-04 체크.
커밋: `git commit -m "test(M-04): ModelValidationTest 16 tests"`
`git push origin master`

---

### M-05: ModelEqualityTest 10 tests (TDD)

**목표**: 10개 테스트 PASS

**스펙 읽기**: `competition-kit/test-specs/domain-tests.md` 의 "§ 15 Model Equality" 섹션

**파일 산출물**: `app/src/test/java/com/ralphthon/app/domain/model/ModelEqualityTest.kt`

**테스트 내용**:
- data class equals() 동작 검증 (동일 값 → equal)
- data class copy() 동작 검증
- hashCode 일관성 검증
- List 포함 여부 검증 (Customer.contacts)
- ContextCard statements/keywords 포함 검증
- Enum 비교 검증

**검증**: `./gradlew.bat test --tests "*.ModelEqualityTest" --no-daemon 2>&1 | tail -5`
완료 후 RALPH_BACKLOG.md M-05 체크.
커밋: `git commit -m "test(M-05): ModelEqualityTest 10 tests"`
`git push origin master`

---

### M-06: DTO 클래스 정의

**목표**: 6개 DTO 파일 생성, 컴파일 성공

**파일 위치**: `app/src/main/java/com/ralphthon/app/data/dto/`

`CustomerDtos.kt`:
```kotlin
data class CustomerSummaryDto(
    val id: Long,
    val name: String,
    val company: String,
    val card_count: Int,
    val total_conversations: Int,
    val last_interaction_at: String
)

data class CustomerDetailDto(
    val id: Long,
    val name: String,
    val company: String,
    val card_count: Int,
    val total_conversations: Int,
    val last_interaction_at: String,
    val contacts: List<ContactDto> = emptyList()
)

data class ContactDto(
    val id: Long,
    val name: String,
    val email: String,
    val phone: String,
    val position: String
)

data class CustomerListResponse(
    val customers: List<CustomerSummaryDto>,
    val total_count: Int,
    val has_more: Boolean
)
```

`CardDtos.kt`:
```kotlin
data class CardSummaryDto(
    val id: Long,
    val customer_id: Long,
    val customer_name: String,
    val title: String,
    val summary: String,
    val topic: String,
    val conversation_type: String,
    val sentiment: String,
    val created_at: String
)

data class CardDetailDto(
    val id: Long,
    val customer_id: Long,
    val customer_name: String,
    val title: String,
    val summary: String,
    val topic: String,
    val conversation_type: String,
    val sentiment: String,
    val created_at: String,
    val statements: List<KeyStatementDto> = emptyList(),
    val keywords: List<KeywordDto> = emptyList()
)

data class KeyStatementDto(
    val id: Long,
    val text: String,
    val speaker: String,
    val timestamp_in_seconds: Int,
    val sentiment: String
)

data class KeywordDto(
    val id: Long,
    val term: String,
    val category: String,
    val frequency: Int
)

data class CardListResponseDto(
    val cards: List<CardSummaryDto>,
    val total_count: Int,
    val has_more: Boolean,
    val page: Int,
    val size: Int
)
```

`KnowledgeDtos.kt`:
```kotlin
data class KnowledgeResponseDto(
    val keyword_id: Long,
    val keyword_term: String,
    val articles: List<ArticleDto>
)

data class ArticleDto(
    val id: Long,
    val title: String,
    val content: String,
    val source: String,
    val contextual_explanation: String,
    val related_keywords: List<String> = emptyList()
)
```

`SearchDtos.kt`:
```kotlin
data class SearchResponseDto(
    val cards: List<CardSummaryDto>,
    val total_count: Int,
    val has_more: Boolean,
    val query: String
)
```

`UploadDtos.kt`:
```kotlin
data class UploadRequestDto(
    val customer_id: Long,
    val type: String,
    val notes: String? = null
)

data class UploadResponseDto(
    val conversation_id: Long,
    val cards_generated: Int,
    val status: String
)
```

`ErrorResponse.kt`:
```kotlin
data class ErrorResponse(
    val code: String,
    val message: String,
    val details: Map<String, String> = emptyMap()
)
```

**검증**: `./gradlew.bat compileDebugKotlin --no-daemon 2>&1 | tail -3`
완료 후 RALPH_BACKLOG.md M-06 체크.
`git add app/src/main/java/com/ralphthon/app/data/dto/`
`git commit -m "feat(M-06): DTO classes 6 files"`
`git push origin master`

---

### M-07: DI AppModule

**목표**: Hilt AppModule 생성, 컴파일 성공

**파일 산출물**: `app/src/main/java/com/ralphthon/app/di/AppModule.kt`

```kotlin
package com.ralphthon.app.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.ralphthon.app.BuildConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class MainDispatcher

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder().create()

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        return OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @MainDispatcher
    fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main
}
```

**검증**: `./gradlew.bat compileDebugKotlin --no-daemon 2>&1 | tail -3`
완료 후 RALPH_BACKLOG.md M-07 체크.
`git add app/src/main/java/com/ralphthon/app/di/`
`git commit -m "feat(M-07): DI AppModule"`
`git push origin master`

---

### M-08: Theme + NavGraph skeleton + strings.xml

**목표**: UI 기반 파일 생성, 컴파일 성공

**파일 산출물**:
- `app/src/main/java/com/ralphthon/app/ui/theme/Color.kt`
- `app/src/main/java/com/ralphthon/app/ui/theme/Type.kt`
- `app/src/main/java/com/ralphthon/app/ui/theme/Theme.kt`
- `app/src/main/java/com/ralphthon/app/ui/navigation/NavGraph.kt`
- `app/src/main/res/values/strings.xml`

**Color.kt** (professional blue/gray palette):
```kotlin
val PrimaryBlue = Color(0xFF1E3A5F)
val SecondaryBlue = Color(0xFF2D6A9F)
val AccentBlue = Color(0xFF4A90D9)
val BackgroundLight = Color(0xFFF5F7FA)
val SurfaceLight = Color(0xFFFFFFFF)
val OnPrimary = Color(0xFFFFFFFF)
val OnBackground = Color(0xFF1A1A2E)
val CardBackground = Color(0xFFFFFFFF)
val DividerColor = Color(0xFFE0E6ED)
val ErrorRed = Color(0xFFD32F2F)
val SuccessGreen = Color(0xFF2E7D32)
val NeutralGray = Color(0xFF757575)

// 감정 색상
val SentimentPositive = Color(0xFF4CAF50)
val SentimentNegative = Color(0xFFF44336)
val SentimentNeutral = Color(0xFF9E9E9E)
val SentimentCommitment = Color(0xFF2196F3)
val SentimentConcern = Color(0xFFFF9800)
val SentimentQuestion = Color(0xFF9C27B0)

// 카테고리 색상
val CategoryTechnology = Color(0xFF1976D2)
val CategoryBusiness = Color(0xFF388E3C)
val CategoryProduct = Color(0xFF7B1FA2)
val CategoryCompetitor = Color(0xFFD32F2F)
val CategoryGeneral = Color(0xFF757575)
```

**NavGraph.kt** (5개 route skeleton):
```kotlin
sealed class Screen(val route: String) {
    object CustomerList : Screen("customerList")
    object CardNewsList : Screen("cardNewsList/{customerId}") {
        fun createRoute(customerId: Long) = "cardNewsList/$customerId"
    }
    object CardDetail : Screen("cardDetail/{cardId}") {
        fun createRoute(cardId: Long) = "cardDetail/$cardId"
    }
    object Search : Screen("search")
    object Upload : Screen("upload")
}

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Screen.CustomerList.route) {
        composable(Screen.CustomerList.route) { /* placeholder */ }
        composable(Screen.CardNewsList.route) { /* placeholder */ }
        composable(Screen.CardDetail.route) { /* placeholder */ }
        composable(Screen.Search.route) { /* placeholder */ }
        composable(Screen.Upload.route) { /* placeholder */ }
    }
}
```

**strings.xml**:
```xml
<resources>
    <string name="app_name">Ralphthon</string>
    <string name="customers">고객 목록</string>
    <string name="cards">컨텍스트 카드</string>
    <string name="search">검색</string>
    <string name="upload">업로드</string>
    <string name="loading">로딩 중...</string>
    <string name="error_network">네트워크 오류가 발생했습니다</string>
    <string name="error_not_found">데이터를 찾을 수 없습니다</string>
    <string name="empty_state">표시할 데이터가 없습니다</string>
    <string name="retry">다시 시도</string>
</resources>
```

**검증**: `./gradlew.bat compileDebugKotlin --no-daemon 2>&1 | tail -3`
완료 후 RALPH_BACKLOG.md M-08 체크.
`git add app/src/main/java/com/ralphthon/app/ui/theme/ app/src/main/java/com/ralphthon/app/ui/navigation/ app/src/main/res/values/strings.xml`
`git commit -m "feat(M-08): Theme + NavGraph skeleton + strings"`
`git push origin master`

---

## Phase 1 완료 처리

모든 M-01 ~ M-08이 완료되면:
1. `git add app/src build.gradle.kts app/build.gradle.kts settings.gradle.kts gradle/`
2. `git commit -m "feat: Phase 1 architecture foundation (26 tests)"`
3. `git push`
4. 세션 종료
