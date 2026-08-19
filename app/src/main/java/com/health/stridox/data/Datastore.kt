package com.health.stridox.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import okio.Path.Companion.toPath

fun createDataStore(context: Context): DataStore<Preferences> {
    return PreferenceDataStoreFactory.createWithPath(
        produceFile = { context.filesDir.resolve("stridox.preferences_pb").absolutePath.toPath() },
        scope = CoroutineScope(Dispatchers.IO),
        corruptionHandler = ReplaceFileCorruptionHandler {
            emptyPreferences()
        }
    )
}
