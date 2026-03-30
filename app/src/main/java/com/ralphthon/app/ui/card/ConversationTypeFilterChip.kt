package com.ralphthon.app.ui.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.ralphthon.app.domain.model.ConversationType

@Composable
fun ConversationTypeFilterChip(
    selectedType: ConversationType?,
    onTypeSelected: (ConversationType?) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
            selected = selectedType == null,
            onClick = { onTypeSelected(null) },
            label = { Text("전체", style = MaterialTheme.typography.labelSmall) }
        )
        FilterChip(
            selected = selectedType == ConversationType.CUSTOMER_MEETING,
            onClick = { onTypeSelected(ConversationType.CUSTOMER_MEETING) },
            label = { Text("고객 미팅", style = MaterialTheme.typography.labelSmall) }
        )
        FilterChip(
            selected = selectedType == ConversationType.INTERNAL_MEETING,
            onClick = { onTypeSelected(ConversationType.INTERNAL_MEETING) },
            label = { Text("사내 회의", style = MaterialTheme.typography.labelSmall) }
        )
    }
}
