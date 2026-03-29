package com.ralphthon.app.ui.card

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.List
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ralphthon.app.domain.model.ContextCard
import com.ralphthon.app.domain.model.ConversationType
import com.ralphthon.app.ui.components.EmptyStateView
import com.ralphthon.app.ui.components.ErrorStateView
import com.ralphthon.app.ui.components.KeywordChip
import com.ralphthon.app.ui.components.SentimentMiniBar
import com.ralphthon.app.ui.components.ShimmerLoadingList
import com.ralphthon.app.ui.components.sentimentColorByScore

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CardNewsListScreen(
    onCardClick: (Long) -> Unit,
    onBack: () -> Unit,
    viewModel: CardNewsListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val event by viewModel.event.collectAsStateWithLifecycle()
    event?.let {
        when (it) {
            is CardNewsListEvent.NavigateToDetail -> {
                onCardClick(it.cardId)
                viewModel.onEventConsumed()
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("카드뉴스") },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "뒤로")
                }
            }
        )

        // Filter chips
        val currentFilter = (uiState as? CardNewsListUiState.Data)?.currentFilter
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = currentFilter == null,
                onClick = { viewModel.filterByType(null) },
                label = { Text("전체") }
            )
            FilterChip(
                selected = currentFilter == ConversationType.CUSTOMER_MEETING,
                onClick = { viewModel.filterByType(ConversationType.CUSTOMER_MEETING) },
                label = { Text("고객 미팅") }
            )
            FilterChip(
                selected = currentFilter == ConversationType.INTERNAL_MEETING,
                onClick = { viewModel.filterByType(ConversationType.INTERNAL_MEETING) },
                label = { Text("사내 회의") }
            )
        }

        when (val state = uiState) {
            is CardNewsListUiState.Loading -> {
                ShimmerLoadingList(count = 3)
            }
            is CardNewsListUiState.Data -> {
                val listState = rememberLazyListState()
                val reachedEnd by remember {
                    derivedStateOf {
                        val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()
                        lastVisible != null && lastVisible.index >= listState.layoutInfo.totalItemsCount - 2
                    }
                }
                LaunchedEffect(reachedEnd) {
                    if (reachedEnd && state.hasMore) {
                        viewModel.loadNextPage()
                    }
                }

                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(4.dp)) }
                    items(state.cards, key = { it.id }) { card ->
                        CardNewsItem(
                            card = card,
                            onClick = { viewModel.onCardClick(card.id) }
                        )
                    }
                    if (state.isLoadingMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(modifier = Modifier.height(24.dp).width(24.dp))
                            }
                        }
                    }
                    item { Spacer(modifier = Modifier.height(4.dp)) }
                }
            }
            is CardNewsListUiState.Empty -> {
                EmptyStateView(
                    message = "카드뉴스가 없습니다",
                    icon = Icons.Default.List,
                    onRetry = { viewModel.loadCards() }
                )
            }
            is CardNewsListUiState.Error -> {
                ErrorStateView(
                    message = state.message,
                    onRetry = { viewModel.loadCards() }
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CardNewsItem(
    card: ContextCard,
    onClick: () -> Unit
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.height(IntrinsicSize.Min)) {
            // Left accent bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(sentimentColorByScore(card.sentimentScore))
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = card.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = card.date,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = card.summary,
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                SentimentMiniBar(score = card.sentimentScore)
                Spacer(modifier = Modifier.height(8.dp))
                if (card.keywords.isNotEmpty()) {
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        card.keywords.take(5).forEach { keyword ->
                            KeywordChip(keyword = keyword)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                }
                Row {
                    Text(
                        text = "발언 ${card.keyStatements.size}건",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "가격 ${card.priceCommitments.size}건",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
