package com.ralphthon.app.ui.card

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ralphthon.app.domain.model.ActionItem
import com.ralphthon.app.domain.model.ActionItemStatus
import com.ralphthon.app.domain.model.ConversationType
import com.ralphthon.app.domain.model.KeyStatement
import com.ralphthon.app.domain.model.KnowledgeArticle
import com.ralphthon.app.domain.model.PredictedQuestion
import com.ralphthon.app.domain.model.PriceCommitment
import com.ralphthon.app.ui.components.ErrorStateView
import com.ralphthon.app.ui.components.KeywordChip
import com.ralphthon.app.ui.components.SentimentDot
import com.ralphthon.app.ui.components.ShimmerLoadingList
import com.ralphthon.app.ui.components.sentimentColorByScore
import com.ralphthon.app.ui.theme.SentimentNegative
import com.ralphthon.app.ui.theme.SentimentPositive

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CardDetailScreen(
    onBack: () -> Unit,
    viewModel: CardDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("카드 상세") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                }
            }
        )

        when (val state = uiState) {
            is CardDetailUiState.Loading -> {
                ShimmerLoadingList(count = 3)
            }
            is CardDetailUiState.Error -> {
                ErrorStateView(
                    message = state.message,
                    onRetry = { viewModel.retry() }
                )
            }
            is CardDetailUiState.Data -> {
                CardDetailContent(
                    state = state,
                    onTogglePrice = { viewModel.togglePricePanel() },
                    onToggleAction = { viewModel.toggleActionPanel() },
                    onTogglePrediction = { viewModel.togglePredictionPanel() },
                    onToggleKnowledge = { viewModel.toggleKnowledgePanel() }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CardDetailContent(
    state: CardDetailUiState.Data,
    onTogglePrice: () -> Unit,
    onToggleAction: () -> Unit,
    onTogglePrediction: () -> Unit,
    onToggleKnowledge: () -> Unit
) {
    val card = state.card
    val accentColor = sentimentColorByScore(card.sentimentScore)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Gradient header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(accentColor.copy(alpha = 0.15f), Color.Transparent)
                    )
                )
                .padding(16.dp)
        ) {
            Column {
                Text(
                    text = card.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = card.date,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FilterChip(
                        selected = true,
                        onClick = {},
                        label = {
                            Text(
                                text = if (card.conversationType == ConversationType.CUSTOMER_MEETING) "고객 미팅" else "사내 회의",
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = accentColor.copy(alpha = 0.2f),
                            selectedLabelColor = accentColor
                        )
                    )
                    SentimentDot(sentiment = card.sentiment, size = 12.dp)
                    Text(
                        text = "${(card.sentimentScore * 100).toInt()}%",
                        style = MaterialTheme.typography.labelMedium,
                        color = accentColor
                    )
                }
            }
        }

        // Summary
        Text(
            text = card.summary,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(16.dp)
        )

        // Key Statements - always visible
        if (card.keyStatements.isNotEmpty()) {
            Text(
                text = "핵심 발언",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            card.keyStatements.forEach { stmt ->
                StatementTimelineItem(stmt)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Price/Conditions Panel
        ExpandableDetailPanel(
            title = "가격/조건",
            count = card.priceCommitments.size,
            isExpanded = state.isPriceExpanded,
            onToggle = onTogglePrice
        ) {
            card.priceCommitments.forEach { pc ->
                PriceDetailItem(pc)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Action Items Panel
        ExpandableDetailPanel(
            title = "액션 아이템",
            count = card.actionItems.size,
            isExpanded = state.isActionExpanded,
            onToggle = onToggleAction
        ) {
            card.actionItems.forEach { ai ->
                ActionDetailItem(ai)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Predicted Questions Panel
        ExpandableDetailPanel(
            title = "예상 질문",
            count = card.predictedQuestions.size,
            isExpanded = state.isPredictionExpanded,
            onToggle = onTogglePrediction
        ) {
            card.predictedQuestions.forEach { pq ->
                PredictionDetailItem(pq)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Knowledge Panel
        ExpandableDetailPanel(
            title = "관련 지식",
            count = state.additionalKnowledge.size,
            isExpanded = state.isKnowledgeExpanded,
            onToggle = onToggleKnowledge
        ) {
            state.additionalKnowledge.forEach { ka ->
                KnowledgeDetailItem(ka)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Keywords at bottom
        if (card.keywords.isNotEmpty()) {
            Text(
                text = "키워드",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            FlowRow(
                modifier = Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                card.keywords.forEach { kw ->
                    KeywordChip(keyword = kw)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ExpandableDetailPanel(
    title: String,
    count: Int,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    if (count == 0) return
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${count}건",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        }
        AnimatedVisibility(visible = isExpanded) {
            Column { content() }
        }
    }
}

@Composable
private fun StatementTimelineItem(stmt: KeyStatement) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        // Timeline dot + line
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(32.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stmt.speaker.take(1),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stmt.speaker,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                        .clickable { /* Mock: 녹음파일 해당 위치로 이동 */ }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "녹음 위치로 이동",
                        modifier = Modifier.size(12.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = stmt.timestamp,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                SentimentDot(sentiment = stmt.sentiment, size = 8.dp)
                if (stmt.isImportant) {
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "중요",
                        modifier = Modifier.size(14.dp),
                        tint = Color(0xFFFFC107)
                    )
                }
            }
            Text(
                text = stmt.text,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun PriceDetailItem(pc: PriceCommitment) {
    ElevatedCard(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${String.format("%,.0f", pc.amount)} ${pc.currency}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = pc.condition,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = pc.mentionedAt,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ActionDetailItem(item: ActionItem) {
    ElevatedCard(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Text(
                        text = item.assignee,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    item.dueDate?.let { date ->
                        Text(
                            text = date,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            FilterChip(
                selected = item.status == ActionItemStatus.DONE,
                onClick = {},
                label = {
                    Text(
                        text = if (item.status == ActionItemStatus.OPEN) "진행중" else "완료",
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = if (item.status == ActionItemStatus.OPEN)
                        SentimentNegative.copy(alpha = 0.1f) else SentimentPositive.copy(alpha = 0.1f),
                    labelColor = if (item.status == ActionItemStatus.OPEN)
                        SentimentNegative else SentimentPositive,
                    selectedContainerColor = SentimentPositive.copy(alpha = 0.1f),
                    selectedLabelColor = SentimentPositive
                )
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PredictionDetailItem(pq: PredictedQuestion) {
    ElevatedCard(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = "Q: ${pq.question}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "A: ${pq.suggestedAnswer}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("신뢰도", style = MaterialTheme.typography.labelSmall)
                Spacer(modifier = Modifier.width(8.dp))
                LinearProgressIndicator(
                    progress = { pq.confidence },
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("${(pq.confidence * 100).toInt()}%", style = MaterialTheme.typography.labelSmall)
            }
            if (pq.relatedKnowledge.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    pq.relatedKnowledge.forEach { k ->
                        FilterChip(
                            selected = false,
                            onClick = {},
                            label = { Text(k, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun KnowledgeDetailItem(article: KnowledgeArticle) {
    ElevatedCard(
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = false,
                    onClick = {},
                    label = { Text(article.category, style = MaterialTheme.typography.labelSmall) }
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = article.content,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("관련도", style = MaterialTheme.typography.labelSmall)
                Spacer(modifier = Modifier.width(8.dp))
                LinearProgressIndicator(
                    progress = { article.relevanceScore },
                    modifier = Modifier
                        .weight(1f)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "${(article.relevanceScore * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
