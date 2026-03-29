package com.ralphthon.app.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ralphthon.app.domain.model.Sentiment

@Composable
fun SentimentBadge(
    sentiment: Sentiment,
    modifier: Modifier = Modifier
) {
    val color = sentimentColor(sentiment)
    val label = when (sentiment) {
        Sentiment.POSITIVE -> "긍정"
        Sentiment.NEGATIVE -> "부정"
        Sentiment.NEUTRAL -> "중립"
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
