package com.ralphthon.app.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.ralphthon.app.domain.model.Keyword
import com.ralphthon.app.domain.model.KeywordCategory
import com.ralphthon.app.ui.theme.AccentBlue
import com.ralphthon.app.ui.theme.AccentGreen
import com.ralphthon.app.ui.theme.AccentRed
import com.ralphthon.app.ui.theme.AccentYellow
import com.ralphthon.app.ui.theme.Secondary

fun keywordCategoryColor(category: KeywordCategory): Color = when (category) {
    KeywordCategory.PRODUCT -> AccentBlue
    KeywordCategory.PRICE -> AccentRed
    KeywordCategory.COMPETITOR -> AccentYellow
    KeywordCategory.TECHNICAL -> AccentGreen
    KeywordCategory.BUSINESS -> Color(0xFF7B1FA2)
    KeywordCategory.GENERAL -> Secondary
}

@Composable
fun KeywordChip(
    keyword: Keyword,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val chipColor = keywordCategoryColor(keyword.category)
    FilterChip(
        selected = false,
        onClick = { onClick?.invoke() },
        label = {
            Text(
                text = keyword.text,
                style = MaterialTheme.typography.labelSmall
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            containerColor = chipColor.copy(alpha = 0.1f),
            labelColor = chipColor
        ),
        border = FilterChipDefaults.filterChipBorder(
            borderColor = chipColor.copy(alpha = 0.3f),
            enabled = true,
            selected = false
        ),
        modifier = modifier.padding(end = 4.dp, bottom = 4.dp)
    )
}
