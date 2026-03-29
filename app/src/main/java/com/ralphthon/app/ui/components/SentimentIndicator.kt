package com.ralphthon.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ralphthon.app.domain.model.Sentiment
import com.ralphthon.app.ui.theme.SentimentNegative
import com.ralphthon.app.ui.theme.SentimentNeutral
import com.ralphthon.app.ui.theme.SentimentPositive

fun sentimentColor(sentiment: Sentiment): Color = when (sentiment) {
    Sentiment.POSITIVE -> SentimentPositive
    Sentiment.NEGATIVE -> SentimentNegative
    Sentiment.NEUTRAL -> SentimentNeutral
}

fun sentimentColorByScore(score: Float): Color = when {
    score >= 0.6f -> SentimentPositive
    score <= 0.4f -> SentimentNegative
    else -> SentimentNeutral
}

@Composable
fun SentimentDot(
    sentiment: Sentiment,
    size: Dp = 10.dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(sentimentColor(sentiment))
    )
}

@Composable
fun SentimentMiniBar(
    score: Float,
    modifier: Modifier = Modifier
) {
    val barColor = sentimentColorByScore(score)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(4.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(Color.LightGray.copy(alpha = 0.3f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(score.coerceIn(0f, 1f))
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(barColor)
        )
    }
}
