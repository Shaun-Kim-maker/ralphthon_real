package com.ralphthon.app.data.repository

import javax.inject.Inject
import javax.inject.Singleton

interface ThemeRepository {
    suspend fun isDarkMode(): Boolean
    suspend fun setDarkMode(enabled: Boolean)
}

@Singleton
class ThemeRepositoryImpl @Inject constructor() : ThemeRepository {
    private var darkMode = false
    override suspend fun isDarkMode(): Boolean = darkMode
    override suspend fun setDarkMode(enabled: Boolean) { darkMode = enabled }
}
