package com.ralphthon.app.ui.customer

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ralphthon.app.domain.model.ActionItem
import com.ralphthon.app.domain.model.ActionItemStatus
import com.ralphthon.app.domain.model.PredictedQuestion
import com.ralphthon.app.domain.model.PriceCommitment
import com.ralphthon.app.ui.components.ActionItemChecklist
import com.ralphthon.app.ui.components.PredictedQuestionCard
import com.ralphthon.app.ui.components.PriceHistoryList
import com.ralphthon.app.ui.components.SentimentDot
import com.ralphthon.app.ui.components.sentimentColor
import com.ralphthon.app.ui.theme.SentimentNegative
import com.ralphthon.app.ui.theme.SentimentPositive

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerBriefBottomSheet(
    customerId: Long,
    onDismiss: () -> Unit,
    onNavigateToDetail: () -> Unit,
    viewModel: CustomerBriefViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    androidx.compose.runtime.LaunchedEffect(customerId) {
        viewModel.loadBrief(customerId)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        when (val state = uiState) {
            is CustomerBriefUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is CustomerBriefUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            is CustomerBriefUiState.Data -> {
                BriefContent(
                    state = state,
                    onTogglePredictions = { viewModel.togglePredictions() },
                    onTogglePriceHistory = { viewModel.togglePriceHistory() },
                    onToggleActionItems = { viewModel.toggleActionItems() },
                    onNavigateToDetail = onNavigateToDetail
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BriefContent(
    state: CustomerBriefUiState.Data,
    onTogglePredictions: () -> Unit,
    onTogglePriceHistory: () -> Unit,
    onToggleActionItems: () -> Unit,
    onNavigateToDetail: () -> Unit
) {
    val brief = state.brief
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = brief.customer.companyName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            SentimentDot(sentiment = brief.overallSentiment, size = 14.dp)
        }
        Spacer(modifier = Modifier.height(16.dp))

        // Section 1: Last conversation summary
        SectionTitle(title = "마지막 대화 요약")
        Spacer(modifier = Modifier.height(8.dp))

        brief.lastCustomerMeetingSummary?.let { summary ->
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "고객 미팅",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        brief.lastInternalMeetingSummary?.let { summary ->
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color(0xFF7B1FA2).copy(alpha = 0.1f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "사내 회의",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF7B1FA2)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Section 2: Predicted questions
        ExpandableSection(
            title = "예상 질문",
            badge = "${brief.predictedQuestions.size}",
            isExpanded = state.isPredictionsExpanded,
            onToggle = onTogglePredictions
        ) {
            brief.predictedQuestions.forEach { pq ->
                PredictedQuestionCard(pq = pq)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Section 3: Price history
        ExpandableSection(
            title = "가격 히스토리",
            badge = "${brief.priceHistory.size}",
            isExpanded = state.isPriceHistoryExpanded,
            onToggle = onTogglePriceHistory
        ) {
            PriceHistoryList(priceCommitments = brief.priceHistory)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Section 4: Open action items
        ExpandableSection(
            title = "미완료 액션",
            badge = "${brief.openActionItemsCount}",
            isExpanded = state.isActionItemsExpanded,
            onToggle = onToggleActionItems
        ) {
            ActionItemChecklist(actionItems = brief.recentActionItems)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onNavigateToDetail,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("상세 보기")
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun ExpandableSection(
    title: String,
    badge: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Badge(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Text(badge, modifier = Modifier.padding(horizontal = 4.dp))
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                contentDescription = if (isExpanded) "접기" else "펼치기"
            )
        }
        AnimatedVisibility(visible = isExpanded) {
            Column(modifier = Modifier.padding(start = 8.dp)) {
                content()
            }
        }
    }
}

