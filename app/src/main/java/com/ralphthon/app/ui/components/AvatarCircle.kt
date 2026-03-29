package com.ralphthon.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ralphthon.app.ui.theme.AvatarColors
import kotlin.math.absoluteValue

@Composable
fun AvatarCircle(
    companyName: String,
    industry: String = "",
    size: Dp = 48.dp,
    modifier: Modifier = Modifier,
    customerId: Long = 0
) {
    val initials = companyName.take(2)
    val bgColor = AvatarColors[customerId.hashCode().absoluteValue % AvatarColors.size]

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(bgColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            style = MaterialTheme.typography.titleMedium,
            color = Color.White
        )
    }
}
