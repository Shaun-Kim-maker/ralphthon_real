package com.ralphthon.app.ui.theme

import androidx.compose.ui.graphics.Color

// Primary
val Primary = Color(0xFF1A73E8)
val OnPrimary = Color(0xFFFFFFFF)
val PrimaryContainer = Color(0xFFD3E3FD)
val OnPrimaryContainer = Color(0xFF041E49)

// Secondary
val Secondary = Color(0xFF5F6368)
val OnSecondary = Color(0xFFFFFFFF)
val SecondaryContainer = Color(0xFFE8EAED)
val OnSecondaryContainer = Color(0xFF1F1F1F)

// Background/Surface
val Background = Color(0xFFF5F7FA)
val OnBackground = Color(0xFF1F1F1F)
val Surface = Color(0xFFFFFFFF)
val OnSurface = Color(0xFF1F1F1F)
val SurfaceVariant = Color(0xFFEEF2F7)

// Sentiment Colors
val SentimentPositive = Color(0xFF34A853)
val SentimentNegative = Color(0xFFEA4335)
val SentimentNeutral = Color(0xFFFBBC04)
val SentimentCommitment = Color(0xFF2196F3)
val SentimentConcern = Color(0xFFFF9800)
val SentimentQuestion = Color(0xFF9C27B0)

// Error
val Error = Color(0xFFEA4335)
val OnError = Color(0xFFFFFFFF)

// Accent
val AccentBlue = Color(0xFF4285F4)
val AccentGreen = Color(0xFF34A853)
val AccentYellow = Color(0xFFFBBC04)
val AccentRed = Color(0xFFEA4335)

// Avatar Palette (8-color hash)
val AvatarColors = listOf(
    Color(0xFFE53935), // Red
    Color(0xFF8E24AA), // Purple
    Color(0xFF1E88E5), // Blue
    Color(0xFF00897B), // Teal
    Color(0xFF43A047), // Green
    Color(0xFFFB8C00), // Orange
    Color(0xFF6D4C41), // Brown
    Color(0xFF546E7A), // Blue Gray
)

// Activity Status Colors
val StatusRecent = Color(0xFF4CAF50)    // Green <7 days
val StatusWarning = Color(0xFFFFC107)   // Yellow 7-30 days
val StatusDanger = Color(0xFFF44336)    // Red >30 days
