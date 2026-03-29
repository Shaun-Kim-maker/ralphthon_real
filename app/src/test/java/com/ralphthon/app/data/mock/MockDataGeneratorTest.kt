package com.ralphthon.app.data.mock

import com.ralphthon.app.domain.model.ConversationType
import com.ralphthon.app.domain.model.Sentiment
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MockDataGeneratorTest {

    @Test
    fun should_generate10Customers_when_called() {
        val customers = MockDataGenerator.generateCustomers()
        assertEquals(10, customers.size)
    }

    @Test
    fun should_generate200Conversations_when_called() {
        val conversations = MockDataGenerator.generateConversations()
        assertTrue(conversations.size in 190..210, "Expected ~200 conversations, got ${conversations.size}")
    }

    @Test
    fun should_haveUniqueCustomerIds_when_generated() {
        val customers = MockDataGenerator.generateCustomers()
        val ids = customers.map { it.id }
        assertEquals(ids.size, ids.distinct().size)
    }

    @Test
    fun should_haveUniqueConversationIds_when_generated() {
        val conversations = MockDataGenerator.generateConversations()
        val ids = conversations.map { it.id }
        assertEquals(ids.size, ids.distinct().size)
    }

    @Test
    fun should_haveBothConversationTypes_when_generated() {
        val conversations = MockDataGenerator.generateConversations()
        val types = conversations.map { it.type }.toSet()
        assertTrue(types.contains(ConversationType.CUSTOMER_MEETING))
        assertTrue(types.contains(ConversationType.INTERNAL_MEETING))
    }

    @Test
    fun should_haveAllSentiments_when_generated() {
        val conversations = MockDataGenerator.generateConversations()
        val sentiments = conversations.map { it.sentiment }.toSet()
        assertTrue(sentiments.contains(Sentiment.POSITIVE))
        assertTrue(sentiments.contains(Sentiment.NEGATIVE))
        assertTrue(sentiments.contains(Sentiment.NEUTRAL))
    }

    @Test
    fun should_haveKeywords_when_conversationGenerated() {
        val conversations = MockDataGenerator.generateConversations()
        assertTrue(conversations.all { it.keywords.isNotEmpty() })
    }

    @Test
    fun should_haveKeyStatements_when_conversationGenerated() {
        val conversations = MockDataGenerator.generateConversations()
        assertTrue(conversations.all { it.keyStatements.isNotEmpty() })
    }

    @Test
    fun should_havePriceCommitments_when_someConversations() {
        val conversations = MockDataGenerator.generateConversations()
        assertTrue(conversations.any { it.priceCommitments.isNotEmpty() })
    }

    @Test
    fun should_haveActionItems_when_someConversations() {
        val conversations = MockDataGenerator.generateConversations()
        assertTrue(conversations.any { it.actionItems.isNotEmpty() })
    }

    @Test
    fun should_havePredictedQuestions_when_someConversations() {
        val conversations = MockDataGenerator.generateConversations()
        assertTrue(conversations.any { it.predictedQuestions.isNotEmpty() })
    }

    @Test
    fun should_returnConversations_when_filteredByCustomerId() {
        val customers = MockDataGenerator.generateCustomers()
        val firstCustomer = customers.first()
        val filtered = MockDataGenerator.getConversationsByCustomerId(firstCustomer.id)
        assertTrue(filtered.isNotEmpty())
        assertTrue(filtered.all { it.customerId == firstCustomer.id })
    }

    @Test
    fun should_returnCards_when_filteredByCustomerId() {
        val customers = MockDataGenerator.generateCustomers()
        val firstCustomer = customers.first()
        val cards = MockDataGenerator.getCardsByCustomerId(firstCustomer.id)
        assertTrue(cards.isNotEmpty())
        assertTrue(cards.all { it.customerId == firstCustomer.id })
    }

    @Test
    fun should_returnResults_when_searchCalled() {
        val results = MockDataGenerator.search("가격")
        assertTrue(results.isNotEmpty())
    }

    @Test
    fun should_generateContextCards_when_called() {
        val conversations = MockDataGenerator.generateConversations()
        val cards = MockDataGenerator.generateContextCards()
        assertEquals(conversations.size, cards.size)
    }
}
