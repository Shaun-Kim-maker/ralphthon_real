package com.ralphthon.app.ui.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ralphthon.app.domain.model.ActionItem
import com.ralphthon.app.domain.model.ActionItemStatus
import com.ralphthon.app.ui.theme.SentimentNegative
import com.ralphthon.app.ui.theme.SentimentPositive

@Composable
fun ActionItemSection(actionItems: List<ActionItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        actionItems.forEach { item ->
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
    }
}
