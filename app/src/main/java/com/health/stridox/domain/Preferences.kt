package com.health.stridox.domain

import com.health.stridox.data.LoginUser
import kotlinx.coroutines.flow.Flow

interface Preferences {
    suspend fun <T> set(key: String, value: T)
    fun <T> get(key: String, defaultValue: T): Flow<T>
    suspend fun <T> getOnce(key: String, defaultValue: T): T
    suspend fun getDeviceId(): String

    suspend fun login(loginResponse: LoginUser)
    suspend fun getLoginResponse(): LoginUser?
    fun getLoginResponseFlow(): Flow<LoginUser?>
    suspend fun islogin(): Boolean
    suspend fun logout()

    suspend fun saveMovingTime(timeSeconds: Int)
    suspend fun getMovingTime(): Int

    suspend fun saveSteps(steps: Int)
    suspend fun getSteps(): Int

    suspend fun savePoints(points: Int)
    suspend fun getPoints(): Int

    suspend fun saveLanguage(lang: String)
    fun getLanguage(): Flow<String>

    // ThemeMode can be "dark", "light", "system"
    suspend fun saveThemeMode(mode: String)
    fun getThemeMode(): Flow<String>

    @Deprecated("Use saveThemeMode instead")
    suspend fun saveTheme(isDark: Boolean)

    @Deprecated("Use getThemeMode instead")
    fun isDarkTheme(): Flow<Boolean>
}
