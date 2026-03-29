package com.ralphthon.app.ui.upload

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
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
import com.ralphthon.app.domain.model.ConversationType
import com.ralphthon.app.ui.theme.SentimentNegative
import com.ralphthon.app.ui.theme.SentimentPositive

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadScreen(
    viewModel: UploadViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val formState by viewModel.formState.collectAsStateWithLifecycle()
    var customerDropdownExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "대화 업로드",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        // Customer selector
        ExposedDropdownMenuBox(
            expanded = customerDropdownExpanded,
            onExpandedChange = { customerDropdownExpanded = it }
        ) {
            OutlinedTextField(
                value = formState.selectedCustomer?.companyName ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("고객 선택") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerDropdownExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
            )
            ExposedDropdownMenu(
                expanded = customerDropdownExpanded,
                onDismissRequest = { customerDropdownExpanded = false }
            ) {
                formState.customers.forEach { customer ->
                    DropdownMenuItem(
                        text = { Text(customer.companyName) },
                        onClick = {
                            viewModel.selectCustomer(customer)
                            customerDropdownExpanded = false
                        }
                    )
                }
            }
        }

        // Conversation type toggle
        Text(
            text = "대화 유형",
            style = MaterialTheme.typography.labelLarge
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = formState.conversationType == ConversationType.CUSTOMER_MEETING,
                onClick = { viewModel.setConversationType(ConversationType.CUSTOMER_MEETING) },
                label = { Text("고객 미팅") },
                leadingIcon = if (formState.conversationType == ConversationType.CUSTOMER_MEETING) {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                } else null
            )
            FilterChip(
                selected = formState.conversationType == ConversationType.INTERNAL_MEETING,
                onClick = { viewModel.setConversationType(ConversationType.INTERNAL_MEETING) },
                label = { Text("사내 회의") },
                leadingIcon = if (formState.conversationType == ConversationType.INTERNAL_MEETING) {
                    { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                } else null
            )
        }

        // Title
        OutlinedTextField(
            value = formState.title,
            onValueChange = { viewModel.setTitle(it) },
            label = { Text("제목") },
            placeholder = { Text("대화 제목을 입력하세요") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // File selector
        ElevatedCard(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (formState.fileName != null) {
                    Text(
                        text = formState.fileName ?: "",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                OutlinedButton(
                    onClick = {
                        viewModel.setFile("/mock/path/recording.m4a", "recording.m4a")
                    }
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.size(4.dp))
                    Text("파일 선택")
                }
            }
        }

        // Upload button
        val isFormComplete = formState.selectedCustomer != null
                && formState.title.isNotBlank()
                && formState.filePath != null

        Button(
            onClick = { viewModel.upload() },
            enabled = isFormComplete && uiState !is UploadUiState.Uploading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState is UploadUiState.Uploading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text("업로드 중...")
            } else {
                Text("업로드")
            }
        }

        // Status messages
        when (val state = uiState) {
            is UploadUiState.Success -> {
                ElevatedCard(
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = null,
                            tint = SentimentPositive
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(
                            text = state.message,
                            color = SentimentPositive,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { viewModel.resetForm() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("새 업로드")
                }
            }
            is UploadUiState.Error -> {
                ElevatedCard(
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = state.message,
                        color = SentimentNegative,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            else -> {}
        }
    }
}
