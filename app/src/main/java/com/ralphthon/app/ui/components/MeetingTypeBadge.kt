package com.ralphthon.app.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ralphthon.app.domain.model.ConversationType

@Composable
fun MeetingTypeBadge(
    conversationType: ConversationType,
    modifier: Modifier = Modifier
) {
    val (color, label) = when (conversationType) {
        ConversationType.CUSTOMER_MEETING -> Color(0xFF0288D1) to "고객 미팅"
        ConversationType.INTERNAL_MEETING -> Color(0xFFF57C00) to "사내 회의"
    }
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Text(
            text = label,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
