package com.ralphthon.app.ui.customer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ralphthon.app.domain.model.Customer
import com.ralphthon.app.ui.components.AvatarCircle
import com.ralphthon.app.ui.components.CustomerCard
import com.ralphthon.app.ui.components.EmptyStateView
import com.ralphthon.app.ui.components.ErrorStateView
import com.ralphthon.app.ui.components.ShimmerLoadingList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerListScreen(
    onCustomerClick: (Long) -> Unit,
    viewModel: CustomerListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isRefreshing by remember { mutableStateOf(false) }

    val event by viewModel.event.collectAsStateWithLifecycle()
    event?.let {
        when (it) {
            is CustomerListEvent.NavigateToCards -> {
                onCustomerClick(it.customerId)
                viewModel.onEventConsumed()
            }
            is CustomerListEvent.ShowSnackbar -> {
                viewModel.onEventConsumed()
            }
        }
    }

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = {
            isRefreshing = true
            viewModel.refresh()
            isRefreshing = false
        },
        modifier = Modifier.fillMaxSize()
    ) {
        when (val state = uiState) {
            is CustomerListUiState.Loading -> {
                ShimmerLoadingList(count = 3)
            }
            is CustomerListUiState.Data -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                    items(state.customers, key = { it.id }) { customer ->
                        CustomerCard(
                            customer = customer,
                            onClick = { viewModel.onCustomerClick(customer.id) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                }
            }
            is CustomerListUiState.Empty -> {
                EmptyStateView(
                    message = "등록된 고객이 없습니다",
                    icon = Icons.Default.Person,
                    onRetry = { viewModel.retry() }
                )
            }
            is CustomerListUiState.Error -> {
                ErrorStateView(
                    message = state.message,
                    onRetry = { viewModel.retry() }
                )
            }
        }
    }
}

