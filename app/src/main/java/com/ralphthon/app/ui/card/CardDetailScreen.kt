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
import com.ralphthon.app.ui.components.PredictedQuestionCard
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
            StatementTimeline(statements = card.keyStatements)
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Price/Conditions Panel
        ExpandableDetailPanel(
            title = "가격/조건",
            count = card.priceCommitments.size,
            isExpanded = state.isPriceExpanded,
            onToggle = onTogglePrice
        ) {
            PriceCommitmentSection(priceCommitments = card.priceCommitments)
        }

        // Action Items Panel
        ExpandableDetailPanel(
            title = "액션 아이템",
            count = card.actionItems.size,
            isExpanded = state.isActionExpanded,
            onToggle = onToggleAction
        ) {
            ActionItemSection(actionItems = card.actionItems)
        }

        // Predicted Questions Panel
        ExpandableDetailPanel(
            title = "예상 질문",
            count = card.predictedQuestions.size,
            isExpanded = state.isPredictionExpanded,
            onToggle = onTogglePrediction
        ) {
            card.predictedQuestions.forEach { pq ->
                PredictedQuestionCard(pq = pq, detailed = true)
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
            KnowledgePanel(articles = state.additionalKnowledge)
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

