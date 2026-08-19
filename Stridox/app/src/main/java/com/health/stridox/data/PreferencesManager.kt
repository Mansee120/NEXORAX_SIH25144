@file:OptIn(ExperimentalCoroutinesApi::class)

package com.health.stridox.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


@OptIn(ExperimentalUuidApi::class)
class PreferencesImpl(private val dataStore: DataStore<Preferences>) :
    com.health.stridox.domain.Preferences {

    private object Keys {
        val deviceId = stringPreferencesKey("device_id")
        val login = stringPreferencesKey("login")
        val loginBoolean = booleanPreferencesKey("loginBoolean")
        val movingTimeSeconds = intPreferencesKey("moving_time_seconds")
        val steps = intPreferencesKey("steps")
        val points = intPreferencesKey("points")
        val language = stringPreferencesKey("language")
        val isDarkTheme = booleanPreferencesKey("is_dark_theme")
        val themeMode = stringPreferencesKey("theme_mode")
    }

    override suspend fun <T> set(key: String, value: T) {
        dataStore.edit { pref ->
            when (value) {
                is Int -> pref[intPreferencesKey(key)] = value
                is Long -> pref[longPreferencesKey(key)] = value
                is Double -> pref[doublePreferencesKey(key)] = value
                is Float -> pref[floatPreferencesKey(key)] = value
                is String -> pref[stringPreferencesKey(key)] = value
                is Boolean -> pref[booleanPreferencesKey(key)] = value
                else -> throw IllegalArgumentException("Unsupported type")
            }
        }
    }

    override fun <T> get(key: String, defaultValue: T): Flow<T> =
        dataStore.data.map { pref ->
            val result = when (defaultValue) {
                is Int -> pref[intPreferencesKey(key)] ?: defaultValue
                is Long -> pref[longPreferencesKey(key)] ?: defaultValue
                is Double -> pref[doublePreferencesKey(key)] ?: defaultValue
                is Float -> pref[floatPreferencesKey(key)] ?: defaultValue
                is String -> pref[stringPreferencesKey(key)] ?: defaultValue
                is Boolean -> pref[booleanPreferencesKey(key)] ?: defaultValue
                else -> throw IllegalArgumentException("Unsupported type")
            }
            @Suppress("UNCHECKED_CAST") result as T
        }

    override suspend fun <T> getOnce(key: String, defaultValue: T): T =
        get(key, defaultValue).first()

    override suspend fun getDeviceId(): String {
        val stored = dataStore.data.map { it[Keys.deviceId] }.first()
        if (stored != null) return stored

        val newId = Uuid.random().toString()
        set("device_id", newId)
        return newId
    }

    override suspend fun getLoginResponse(): LoginUser? {
        return dataStore.data
            .map { it[Keys.login] }
            .map { json -> json?.let { Json.decodeFromString<LoginUser>(it) } }
            .firstOrNull()
    }

    override fun getLoginResponseFlow(): Flow<LoginUser?> {
        return dataStore.data
            .map { it[Keys.login] }
            .map { json -> json?.let { Json.decodeFromString<LoginUser>(it) } }
    }

    override suspend fun islogin(): Boolean {
        return dataStore.data
            .map { it[Keys.loginBoolean] }
            .firstOrNull() ?: false
    }

    override suspend fun login(loginResponse: LoginUser) {
        val json = Json.encodeToString(loginResponse)
        dataStore.edit { it[Keys.login] = json }
        dataStore.edit { it[Keys.loginBoolean] = true }
    }

    override suspend fun logout() {
        dataStore.edit { it[Keys.loginBoolean] = false }
    }

    override suspend fun saveMovingTime(timeSeconds: Int) {
        dataStore.edit { it[Keys.movingTimeSeconds] = timeSeconds }
    }

    override suspend fun getMovingTime(): Int {
        return dataStore.data.map { it[Keys.movingTimeSeconds] ?: 0 }.first()
    }

    override suspend fun saveSteps(steps: Int) {
        dataStore.edit { it[Keys.steps] = steps }
    }

    override suspend fun getSteps(): Int {
        return dataStore.data.map { it[Keys.steps] ?: 0 }.first()
    }

    override suspend fun savePoints(points: Int) {
        dataStore.edit { it[Keys.points] = points }
    }

    override suspend fun getPoints(): Int {
        return dataStore.data.map { it[Keys.points] ?: 0 }.first()
    }

    override suspend fun saveLanguage(lang: String) {
        dataStore.edit { it[Keys.language] = lang }
    }

    override fun getLanguage(): Flow<String> {
        return dataStore.data.map { it[Keys.language] ?: "en" }
    }

    override suspend fun saveThemeMode(mode: String) {
        dataStore.edit { it[Keys.themeMode] = mode }
    }

    override fun getThemeMode(): Flow<String> {
        return dataStore.data.map { it[Keys.themeMode] ?: "system" }
    }

    override suspend fun saveTheme(isDark: Boolean) {
        // Fallback or legacy support if needed
        dataStore.edit { it[Keys.isDarkTheme] = isDark }
        // Sync with new system if needed? Or just ignore
    }

    override fun isDarkTheme(): Flow<Boolean> {
        return dataStore.data.map {
            it[Keys.isDarkTheme] ?: false
        }
    }
}
