package com.ralphthon.app.ui.search

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ralphthon.app.domain.model.SearchResult
import com.ralphthon.app.ui.components.EmptyStateView
import com.ralphthon.app.ui.components.ErrorStateView
import com.ralphthon.app.ui.components.ShimmerLoadingList
import com.ralphthon.app.ui.theme.AccentBlue
import com.ralphthon.app.ui.theme.AccentGreen
import com.ralphthon.app.ui.theme.AccentYellow
import com.ralphthon.app.ui.theme.Primary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    onResultClick: (Long) -> Unit = {},
    viewModel: SearchViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val recentSearches by viewModel.recentSearches.collectAsStateWithLifecycle()
    var query by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        SearchBar(
            inputField = {
                SearchBarDefaults.InputField(
                    query = query,
                    onQueryChange = {
                        query = it
                        viewModel.search(it)
                    },
                    onSearch = {
                        isSearchActive = false
                        viewModel.search(it)
                    },
                    expanded = isSearchActive,
                    onExpandedChange = { isSearchActive = it },
                    placeholder = { Text("키워드, 발언, 가격 검색") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "검색") },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = {
                                query = ""
                                viewModel.clearSearch()
                            }) {
                                Icon(Icons.Default.Clear, contentDescription = "지우기")
                            }
                        }
                    }
                )
            },
            expanded = isSearchActive,
            onExpandedChange = { isSearchActive = it },
            modifier = Modifier.fillMaxWidth()
        ) {
            // Search suggestions when active
            if (recentSearches.isNotEmpty()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "최근 검색",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    recentSearches.forEach { recent ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    query = recent
                                    viewModel.search(recent)
                                    isSearchActive = false
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 12.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = recent,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { viewModel.removeRecentSearch(recent) }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "삭제",
                                    modifier = Modifier.height(16.dp).width(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Recent search chips when idle
        if (uiState is SearchUiState.Idle && recentSearches.isNotEmpty()) {
            Text(
                text = "최근 검색어",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                recentSearches.forEach { recent ->
                    FilterChip(
                        selected = false,
                        onClick = {
                            query = recent
                            viewModel.search(recent)
                        },
                        label = { Text(recent) }
                    )
                }
            }
        }

        when (val state = uiState) {
            is SearchUiState.Idle -> { /* shown above */ }
            is SearchUiState.Loading -> {
                ShimmerLoadingList(count = 3)
            }
            is SearchUiState.Data -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    items(state.results, key = { it.id }) { result ->
                        SearchResultCard(
                            result = result,
                            query = state.query,
                            onClick = { onResultClick(result.sourceId) }
                        )
                    }
                }
            }
            is SearchUiState.Empty -> {
                EmptyStateView(
                    message = "'${state.query}'에 대한 검색 결과가 없습니다",
                    icon = Icons.Default.Search
                )
            }
            is SearchUiState.Error -> {
                ErrorStateView(message = state.message)
            }
        }
    }
}

@Composable
private fun SearchResultCard(
    result: SearchResult,
    query: String,
    onClick: () -> Unit
) {
    val typeBadgeColor = when (result.type) {
        "statement" -> AccentBlue
        "price" -> AccentYellow
        "keyword" -> AccentGreen
        else -> Primary
    }
    val typeLabel = when (result.type) {
        "statement" -> "발언"
        "price" -> "가격"
        "keyword" -> "키워드"
        else -> result.type
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(typeBadgeColor.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = typeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = typeBadgeColor
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = result.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            HighlightedText(
                text = result.snippet,
                query = query
            )
        }
    }
}

@Composable
private fun HighlightedText(
    text: String,
    query: String
) {
    val annotated = buildAnnotatedString {
        val lowerText = text.lowercase()
        val lowerQuery = query.lowercase()
        var start = 0
        var index = lowerText.indexOf(lowerQuery, start)
        while (index >= 0) {
            append(text.substring(start, index))
            withStyle(
                SpanStyle(
                    fontWeight = FontWeight.Bold,
                    color = Primary,
                    background = Primary.copy(alpha = 0.1f)
                )
            ) {
                append(text.substring(index, index + query.length))
            }
            start = index + query.length
            index = lowerText.indexOf(lowerQuery, start)
        }
        append(text.substring(start))
    }
    Text(
        text = annotated,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
