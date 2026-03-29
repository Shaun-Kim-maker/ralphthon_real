package com.ralphthon.app.data.mock

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import com.ralphthon.app.data.dto.ActionItemDto
import com.ralphthon.app.data.dto.CardDto
import com.ralphthon.app.data.dto.ConversationDto
import com.ralphthon.app.data.dto.CustomerDto
import com.ralphthon.app.data.dto.PredictedQuestionDto
import com.ralphthon.app.data.dto.PriceCommitmentDto
import com.ralphthon.app.domain.model.Customer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class MockDataSerializationTest {

    private val gson = Gson()

    // Test 1
    @Test
    fun should_serializeCustomers_when_toJson() {
        val customers = MockDataGenerator.generateCustomers()
        val json = gson.toJson(customers)
        assertTrue(json.isNotEmpty())
        assertTrue(json.startsWith("["))
    }

    // Test 2
    @Test
    fun should_deserializeCustomers_when_fromJson() {
        val json = MockJsonProvider.customersJson()
        val type = object : TypeToken<List<CustomerDto>>() {}.type
        val customers: List<CustomerDto> = gson.fromJson(json, type)
        assertEquals(10, customers.size)
    }

    // Test 3
    @Test
    fun should_preserveCustomerId_when_roundTrip() {
        val original = MockDataGenerator.generateCustomers()
        val json = gson.toJson(original)
        val type = object : TypeToken<List<Customer>>() {}.type
        val deserialized: List<Customer> = gson.fromJson(json, type)
        assertEquals(original.size, deserialized.size)
        for (i in original.indices) {
            assertEquals(original[i].id, deserialized[i].id)
        }
    }

    // Test 4
    @Test
    fun should_preserveCompanyName_when_roundTrip() {
        val original = MockDataGenerator.generateCustomers()
        val json = gson.toJson(original)
        val type = object : TypeToken<List<Customer>>() {}.type
        val deserialized: List<Customer> = gson.fromJson(json, type)
        for (i in original.indices) {
            assertEquals(original[i].companyName, deserialized[i].companyName)
        }
    }

    // Test 5
    @Test
    fun should_serializeConversations_when_toJson() {
        val conversations = MockDataGenerator.generateConversations()
        val json = gson.toJson(conversations)
        assertTrue(json.isNotEmpty())
        assertTrue(json.startsWith("["))
    }

    // Test 6
    @Test
    fun should_deserializeConversations_when_fromJson() {
        val conversations = MockDataGenerator.generateConversations()
        val json = gson.toJson(conversations)
        val type = object : TypeToken<List<ConversationDto>>() {}.type
        val deserialized: List<ConversationDto> = gson.fromJson(json, type)
        assertTrue(deserialized.size >= 200)
    }

    // Test 7
    @Test
    fun should_preserveConversationType_when_roundTrip() {
        val conversations = MockDataGenerator.generateConversations()
        val json = gson.toJson(conversations)
        val jsonArray = JsonParser.parseString(json).asJsonArray
        val firstConv = jsonArray[0].asJsonObject
        val typeField = firstConv.get("type").asString
        assertNotNull(typeField)
        assertTrue(typeField == "CUSTOMER_MEETING" || typeField == "INTERNAL_MEETING")
    }

    // Test 8
    @Test
    fun should_serializeCards_when_toJson() {
        val cards = MockDataGenerator.generateContextCards()
        val json = gson.toJson(cards)
        assertTrue(json.isNotEmpty())
        assertTrue(json.startsWith("["))
    }

    // Test 9
    @Test
    fun should_deserializeCards_when_fromJson() {
        val cards = MockDataGenerator.generateContextCards()
        val json = gson.toJson(cards)
        val type = object : TypeToken<List<CardDto>>() {}.type
        val deserialized: List<CardDto> = gson.fromJson(json, type)
        assertEquals(cards.size, deserialized.size)
    }

    // Test 10
    @Test
    fun should_preserveSentiment_when_roundTrip() {
        val cards = MockDataGenerator.generateContextCards()
        val json = gson.toJson(cards)
        val jsonArray = JsonParser.parseString(json).asJsonArray
        val firstCard = jsonArray[0].asJsonObject
        val sentimentField = firstCard.get("sentiment").asString
        assertNotNull(sentimentField)
        assertTrue(sentimentField == "POSITIVE" || sentimentField == "NEUTRAL" || sentimentField == "NEGATIVE")
    }

    // Test 11
    @Test
    fun should_serializeKeywords_when_embedded() {
        val cards = MockDataGenerator.generateContextCards()
        val json = gson.toJson(cards)
        val jsonArray = JsonParser.parseString(json).asJsonArray
        val firstCard = jsonArray[0].asJsonObject
        assertTrue(firstCard.has("keywords"))
        val keywords = firstCard.get("keywords").asJsonArray
        assertTrue(keywords.size() > 0)
    }

    // Test 12
    @Test
    fun should_deserializeKeywords_when_fromCardJson() {
        val cards = MockDataGenerator.generateContextCards()
        val json = gson.toJson(cards)
        val type = object : TypeToken<List<CardDto>>() {}.type
        val deserialized: List<CardDto> = gson.fromJson(json, type)
        val firstCard = deserialized[0]
        assertNotNull(firstCard.keywords)
        assertTrue(firstCard.keywords.isNotEmpty())
    }

    // Test 13
    @Test
    fun should_serializePriceCommitments_when_embedded() {
        val cards = MockDataGenerator.generateContextCards()
        val json = gson.toJson(cards)
        val jsonArray = JsonParser.parseString(json).asJsonArray
        var foundWithPriceCommitments = false
        for (element in jsonArray) {
            val card = element.asJsonObject
            if (card.has("priceCommitments")) {
                val priceCommitments = card.get("priceCommitments").asJsonArray
                if (priceCommitments.size() > 0) {
                    foundWithPriceCommitments = true
                    break
                }
            }
        }
        assertTrue(foundWithPriceCommitments)
    }

    // Test 14
    @Test
    fun should_deserializePriceCommitments_when_fromJson() {
        val cards = MockDataGenerator.generateContextCards()
        val json = gson.toJson(cards)
        val jsonArray = JsonParser.parseString(json).asJsonArray
        var foundPriceCommitment: JsonObject? = null
        for (element in jsonArray) {
            val card = element.asJsonObject
            val priceCommitments = card.get("priceCommitments").asJsonArray
            if (priceCommitments.size() > 0) {
                foundPriceCommitment = priceCommitments[0].asJsonObject
                break
            }
        }
        assertNotNull(foundPriceCommitment)
        assertTrue(foundPriceCommitment!!.has("amount"))
        assertTrue(foundPriceCommitment.has("currency"))
    }

    // Test 15
    @Test
    fun should_serializeActionItems_when_embedded() {
        val cards = MockDataGenerator.generateContextCards()
        val json = gson.toJson(cards)
        val jsonArray = JsonParser.parseString(json).asJsonArray
        var foundWithActionItems = false
        for (element in jsonArray) {
            val card = element.asJsonObject
            if (card.has("actionItems")) {
                val actionItems = card.get("actionItems").asJsonArray
                if (actionItems.size() > 0) {
                    foundWithActionItems = true
                    break
                }
            }
        }
        assertTrue(foundWithActionItems)
    }

    // Test 16
    @Test
    fun should_deserializeActionItems_when_fromJson() {
        val cards = MockDataGenerator.generateContextCards()
        val json = gson.toJson(cards)
        val jsonArray = JsonParser.parseString(json).asJsonArray
        var foundActionItem: JsonObject? = null
        for (element in jsonArray) {
            val card = element.asJsonObject
            val actionItems = card.get("actionItems").asJsonArray
            if (actionItems.size() > 0) {
                foundActionItem = actionItems[0].asJsonObject
                break
            }
        }
        assertNotNull(foundActionItem)
        assertTrue(foundActionItem!!.has("status"))
    }

    // Test 17
    @Test
    fun should_serializePredictedQuestions_when_embedded() {
        val cards = MockDataGenerator.generateContextCards()
        val json = gson.toJson(cards)
        val jsonArray = JsonParser.parseString(json).asJsonArray
        var foundWithPredictedQuestions = false
        for (element in jsonArray) {
            val card = element.asJsonObject
            if (card.has("predictedQuestions")) {
                val predictedQuestions = card.get("predictedQuestions").asJsonArray
                if (predictedQuestions.size() > 0) {
                    foundWithPredictedQuestions = true
                    break
                }
            }
        }
        assertTrue(foundWithPredictedQuestions)
    }

    // Test 18
    @Test
    fun should_deserializePredictedQuestions_when_fromJson() {
        val cards = MockDataGenerator.generateContextCards()
        val json = gson.toJson(cards)
        val jsonArray = JsonParser.parseString(json).asJsonArray
        var foundQuestion: JsonObject? = null
        for (element in jsonArray) {
            val card = element.asJsonObject
            val predictedQuestions = card.get("predictedQuestions").asJsonArray
            if (predictedQuestions.size() > 0) {
                foundQuestion = predictedQuestions[0].asJsonObject
                break
            }
        }
        assertNotNull(foundQuestion)
        assertTrue(foundQuestion!!.has("confidence"))
        val confidence = foundQuestion.get("confidence").asFloat
        assertTrue(confidence in 0.0f..1.0f)
    }

    // Test 19
    @Test
    fun should_handleNullFields_when_customerContactNameNull() {
        // Verify null contactName serializes/deserializes correctly using CustomerDto round-trip
        val nullContactCustomer = CustomerDto(
            id = 99L,
            companyName = "테스트회사",
            contactName = null,
            industry = "IT",
            lastInteractionDate = "2026-03-29",
            totalConversations = 0,
            summary = null
        )
        val json = gson.toJson(nullContactCustomer)
        val roundTripped: CustomerDto = gson.fromJson(json, CustomerDto::class.java)
        assertNull(roundTripped.contactName)
        assertNull(roundTripped.summary)
        assertEquals("테스트회사", roundTripped.companyName)
        assertEquals(99L, roundTripped.id)
    }

    // Test 20
    @Test
    fun should_handleConversationTypeEnum_when_deserialized() {
        val conversations = MockDataGenerator.generateConversations()
        val json = gson.toJson(conversations)
        val jsonArray = JsonParser.parseString(json).asJsonArray
        val typeValues = mutableSetOf<String>()
        for (element in jsonArray) {
            val conv = element.asJsonObject
            typeValues.add(conv.get("type").asString)
        }
        assertTrue(typeValues.contains("CUSTOMER_MEETING"))
        assertTrue(typeValues.contains("INTERNAL_MEETING"))
    }
}
