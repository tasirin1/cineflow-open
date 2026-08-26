package com.cineflow.app.data.api

import android.content.Context
import android.os.Build
import android.util.Log
import com.cineflow.app.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.Locale
import java.util.UUID

object SessionManager {

    private const val TAG = "SessionManager"
    private const val PREFS_NAME = "cineflow_session"
    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_TOKEN_TYPE = "token_type"
    private const val KEY_EXPIRES_AT = "expires_at_epoch_ms"

    private var authApi: ApiService? = null

    fun init(context: Context) {
        authApi = Retrofit.Builder()
            .baseUrl(ApiClient.BASE_URL)
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    fun isTokenValid(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val token = prefs.getString(KEY_ACCESS_TOKEN, null)
        val expiresAt = prefs.getLong(KEY_EXPIRES_AT, 0L)
        return !token.isNullOrEmpty() && expiresAt > System.currentTimeMillis()
    }

    fun getToken(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val token = prefs.getString(KEY_ACCESS_TOKEN, null)
        val expiresAt = prefs.getLong(KEY_EXPIRES_AT, 0L)
        if (!token.isNullOrEmpty() && expiresAt > System.currentTimeMillis()) {
            return token
        }
        return null
    }

    fun getAppInstanceId(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        var id = prefs.getString("app_instance_id", null)
        if (id == null) {
            id = UUID.randomUUID().toString()
            prefs.edit().putString("app_instance_id", id).apply()
        }
        return id
    }

    suspend fun getNonce(context: Context): String? = withContext(Dispatchers.IO) {
        try {
            val request = AuthNonceRequest(
                appInstanceId = getAppInstanceId(context),
                packageName = context.packageName,
                appVersionCode = 12
            )
            val response = authApi?.getNonce(request)
            val body = response?.body()
            if (response?.isSuccessful == true && body?.isSuccess == true && body.data != null) {
                Log.d(TAG, "Nonce obtained")
                return@withContext body.data!!.nonce
            }
            Log.e(TAG, "Nonce failed: ${response?.code()} ${body?.message}")
            return@withContext null
        } catch (e: Exception) {
            Log.e(TAG, "Nonce error", e)
            return@withContext null
        }
    }

    suspend fun loginWithGoogle(
        context: Context,
        idToken: String,
        nonce: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = GoogleAccountLoginRequest(
                idToken = idToken,
                nonce = nonce,
                appInstanceId = getAppInstanceId(context),
                deviceType = "android_mobile",
                uiMode = "phone",
                manufacturer = Build.MANUFACTURER ?: "",
                brand = Build.BRAND ?: "",
                model = Build.MODEL ?: "",
                androidSdkInt = Build.VERSION.SDK_INT,
                androidRelease = Build.VERSION.RELEASE ?: "",
                locale = Locale.getDefault().toLanguageTag(),
                integrityToken = null
            )

            val response = authApi?.loginWithGoogle(request)
            val body = response?.body()
            if (response?.isSuccessful == true && body?.isSuccess == true && body.data != null) {
                val loginData = body.data!!
                val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

                val accessToken = loginData.accessToken
                if (accessToken.isNullOrEmpty()) {
                    Log.e(TAG, "Login success but no access token")
                    return@withContext false
                }

                val expiresIn = loginData.expiresIn ?: 3600L
                val expiresAt = System.currentTimeMillis() + (expiresIn * 1000)

                prefs.edit()
                    .putString(KEY_ACCESS_TOKEN, accessToken)
                    .putString(KEY_TOKEN_TYPE, "Bearer")
                    .putLong(KEY_EXPIRES_AT, expiresAt)
                    .apply()

                Log.d(TAG, "Google login successful")
                return@withContext true
            }
            Log.e(TAG, "Google login failed: ${response?.code()} ${body?.message}")
            return@withContext false
        } catch (e: Exception) {
            Log.e(TAG, "Google login error", e)
            return@withContext false
        }
    }

    fun logout(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().clear().apply()
    }
}
