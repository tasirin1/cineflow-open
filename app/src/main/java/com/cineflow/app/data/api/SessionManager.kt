package com.cineflow.app.data.api

import android.content.Context
import android.os.Build
import android.util.Log
import com.cineflow.app.data.model.AppSessionRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.UUID

object SessionManager {

    private const val TAG = "SessionManager"
    private const val PREFS_NAME = "cineflow_session"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_TOKEN_TYPE = "token_type"
    private const val KEY_EXPIRES_AT = "expires_at_epoch_ms"
    private const val KEY_SESSION_ID = "session_id"

    private var sessionApi: ApiService? = null

    fun init(context: Context) {
        sessionApi = Retrofit.Builder()
            .baseUrl(ApiClient.BASE_URL)
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    fun getToken(): String? {
        val prefs = SessionClient.appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val token = prefs?.getString(KEY_ACCESS_TOKEN, null)
        val expiresAt = prefs?.getLong(KEY_EXPIRES_AT, 0L) ?: 0L
        if (token != null && expiresAt > System.currentTimeMillis()) {
            return token
        }
        return null
    }

    fun getAuthorizationHeader(): String? {
        val token = getToken() ?: return null
        val prefs = SessionClient.appContext?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val tokenType = prefs?.getString(KEY_TOKEN_TYPE, "Bearer") ?: "Bearer"
        return "$tokenType $token"
    }

    suspend fun ensureSession(context: Context): Boolean = withContext(Dispatchers.IO) {
        val existingToken = getToken()
        if (existingToken != null) {
            Log.d(TAG, "Using existing session token")
            return@withContext true
        }

        try {
            Log.d(TAG, "Creating new session...")
            val request = AppSessionRequest(
                appInstanceId = UUID.randomUUID().toString(),
                packageName = context.packageName,
                appVersionName = "0.2.7",
                appVersionCode = 11,
                apkSha256 = null,
                signerSha256 = null,
                deviceType = "android_mobile",
                uiMode = if ((context.resources.configuration.uiMode and
                    android.content.res.Configuration.UI_MODE_TYPE_TELEVISION) != 0) "tv" else "phone",
                manufacturer = Build.MANUFACTURER ?: "",
                brand = Build.BRAND ?: "",
                model = Build.MODEL ?: "",
                androidSdkInt = Build.VERSION.SDK_INT,
                androidRelease = Build.VERSION.RELEASE ?: "",
                locale = java.util.Locale.getDefault().toLanguageTag(),
                requestedAtEpochMs = System.currentTimeMillis()
            )

            val response = sessionApi?.createSession(request)
            val body = response?.body()

            if (response?.isSuccessful == true && body?.isSuccess == true && body.data != null) {
                val sessionData = body.data!!
                val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

                val accessToken = sessionData.accessToken
                if (accessToken.isNullOrEmpty()) {
                    Log.e(TAG, "Session created but no access token returned")
                    return@withContext false
                }

                val expiresAt = sessionData.expiresAtEpochMs
                    ?: ((sessionData.expiresInSeconds ?: 300L) * 1000) + System.currentTimeMillis()

                prefs.edit()
                    .putString(KEY_ACCESS_TOKEN, accessToken)
                    .putString(KEY_TOKEN_TYPE, sessionData.tokenType ?: "Bearer")
                    .putLong(KEY_EXPIRES_AT, expiresAt)
                    .putString(KEY_SESSION_ID, sessionData.sessionId)
                    .apply()

                Log.d(TAG, "Session created successfully")
                return@withContext true
            } else {
                Log.e(TAG, "Session creation failed: ${response?.code()} ${body?.message}")
                return@withContext false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Session creation error", e)
            return@withContext false
        }
    }
}

object SessionClient {
    var appContext: Context? = null
        private set

    fun init(context: Context) {
        appContext = context.applicationContext
    }
}
