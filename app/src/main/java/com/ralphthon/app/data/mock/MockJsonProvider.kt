package com.ralphthon.app.data.mock

import com.google.gson.Gson
import com.google.gson.GsonBuilder

object MockJsonProvider {

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    fun customersJson(): String {
        return gson.toJson(MockDataGenerator.generateCustomers())
    }

    fun customerJson(id: Long): String {
        val customer = MockDataGenerator.getCustomerById(id)
        return gson.toJson(customer)
    }

    fun cardsJson(customerId: Long): String {
        return gson.toJson(MockDataGenerator.getCardsByCustomerId(customerId))
    }

    fun cardJson(id: Long): String {
        val card = MockDataGenerator.getCardById(id)
        return gson.toJson(card)
    }

    fun searchJson(query: String): String {
        return gson.toJson(MockDataGenerator.search(query))
    }
}
