package ir.kitgroup.distributor.core.utils.datastore

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore("user_prefs")
val Context.settingsDataStore by preferencesDataStore(name = "settings_prefs")

@Singleton
class MainPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        val KEY_PRS_GUID = stringPreferencesKey("key_prsGuid")
        val KEY_NAME = stringPreferencesKey("key_Name")
        val KEY_IS_LOGGED = booleanPreferencesKey("key_is_login")
        val KEY_BASE_URL = stringPreferencesKey("base_url")
    }

    val prsGuid: Flow<String?> = context.dataStore.data
        .map { it[KEY_PRS_GUID] }

    val name: Flow<String?> = context.dataStore.data
        .map { it[KEY_NAME] }

    val isLoggedIn: Flow<Boolean?> = context.dataStore.data
        .map { it[KEY_IS_LOGGED] }

    val baseUrlFlow: Flow<String?> = context.settingsDataStore.data
        .map { it[KEY_BASE_URL] }


    suspend fun saveDistributorInfo(
        prsGuid: String, name: String
    ) {
        context.dataStore.edit { prefs ->
            prefs[KEY_PRS_GUID] = prsGuid
            prefs[KEY_NAME] = name
            prefs[KEY_IS_LOGGED] = true
        }
    }

    suspend fun clearUserInfo() {
        context.dataStore.edit { it.clear() }
    }

    suspend fun saveBaseUrl(baseUrl: String) {
        context.settingsDataStore.edit {
            it[KEY_BASE_URL] = baseUrl
        }
    }

    suspend fun getBaseUrl(): String {
        return baseUrlFlow.first() ?: "http://default/api/Android/"
    }
}
