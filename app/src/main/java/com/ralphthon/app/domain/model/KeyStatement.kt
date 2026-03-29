package com.ralphthon.app.domain.model

data class KeyStatement(
    val id: Long,
    val speaker: String,
    val text: String,
    val timestamp: String,
    val sentiment: Sentiment,
    val isImportant: Boolean
) {
    companion object {
        fun withDefaults(
            id: Long = 0L,
            speaker: String = "",
            text: String = "",
            timestamp: String = "00:00",
            timestampSeconds: Int = -1,
            sentiment: Sentiment = Sentiment.NEUTRAL,
            isImportant: Boolean = false
        ): KeyStatement {
            val resolvedTimestamp = if (timestampSeconds < 0) "00:00" else timestamp
            return KeyStatement(
                id = id,
                speaker = speaker,
                text = text,
                timestamp = resolvedTimestamp,
                sentiment = sentiment,
                isImportant = isImportant
            )
        }
    }
}
