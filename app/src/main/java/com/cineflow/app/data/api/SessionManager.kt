package com.cineflow.app.data.api

import android.content.Context
import android.os.Build
import android.util.Log
import com.cineflow.app.data.model.*
import com.cineflow.app.util.AppLogger
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
    const val preferencesName = PREFS_NAME

    private const val KEY_ACCESS_TOKEN = "access_token"
    private const val KEY_TOKEN_TYPE = "token_type"
    private const val KEY_EXPIRES_AT = "expires_at_epoch_ms"
    private const val KEY_REFRESH_TOKEN = "refresh_token"

    private var authApi: ApiService? = null

    fun init(context: Context) {
        AppLogger.d(TAG, "init: membangun authApi client, baseUrl=${ApiClient.BASE_URL}")
        authApi = Retrofit.Builder()
            .baseUrl(ApiClient.BASE_URL)
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
        AppLogger.d(TAG, "init: authApi siap")
    }

    fun isTokenValid(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val token = prefs.getString(KEY_ACCESS_TOKEN, null)
        val expiresAt = prefs.getLong(KEY_EXPIRES_AT, 0L)
        val valid = !token.isNullOrEmpty() && expiresAt > System.currentTimeMillis()
        AppLogger.d(
            TAG,
            "isTokenValid: token=${token?.take(12)}..., expiresAt=$expiresAt, now=${System.currentTimeMillis()}, valid=$valid"
        )
        return valid
    }

    fun getToken(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val token = prefs.getString(KEY_ACCESS_TOKEN, null)
        val expiresAt = prefs.getLong(KEY_EXPIRES_AT, 0L)
        if (!token.isNullOrEmpty() && expiresAt > System.currentTimeMillis()) {
            AppLogger.d(TAG, "getToken: token valid (${token.take(12)}...)")
            return token
        }
        AppLogger.w(TAG, "getToken: token tidak valid/expired")
        return null
    }

    fun getAppInstanceId(context: Context): String {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        var id = prefs.getString("app_instance_id", null)
        if (id == null) {
            id = UUID.randomUUID().toString()
            prefs.edit().putString("app_instance_id", id).apply()
            AppLogger.i(TAG, "app_instance_id baru dibuat: $id")
        } else {
            AppLogger.d(TAG, "app_instance_id eksisting: ${id.take(12)}...")
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
            AppLogger.d(TAG, "getNonce: request appInstanceId=${request.appInstanceId.take(12)}..., versionCode=${request.appVersionCode}")
            val response = authApi?.getNonce(request)
            val body = response?.body()
            if (response?.isSuccessful == true && body?.isSuccess == true && body.data != null) {
                AppLogger.d(TAG, "getNonce: sukses, nonce=${body.data!!.nonce.take(12)}...")
                return@withContext body.data!!.nonce
            }
            val errBody = response?.errorBody()?.string()
            AppLogger.logApiError(TAG, "api/app/auth/nonce", response?.code() ?: -1, errBody)
            AppLogger.e(TAG, "getNonce gagal: isSuccess=${body?.isSuccess}, message=${body?.message}")
            return@withContext null
        } catch (e: Exception) {
            AppLogger.e(TAG, "getNonce exception", e)
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
            AppLogger.d(TAG, "loginWithGoogle: idToken=${idToken.take(20)}..., nonce=${nonce.take(12)}..., device=${request.manufacturer} ${request.model}")

            val response = authApi?.loginWithGoogle(request)
            val body = response?.body()
            if (response?.isSuccessful == true && body?.isSuccess == true && body.data != null) {
                val loginData = body.data!!
                val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

                val tokenInfo = loginData.tokenInfo
                val accessToken = tokenInfo?.accessToken
                if (accessToken.isNullOrEmpty()) {
                    AppLogger.e(TAG, "loginWithGoogle: sukses tapi token_info kosong / tidak ada access_token di response (tokenInfo=${tokenInfo != null})")
                    return@withContext false
                }

                val expiresIn = tokenInfo.expiresInSeconds
                val refreshToken = tokenInfo.refreshToken
                val tokenType = tokenInfo.tokenType
                val expiresAt = System.currentTimeMillis() + (expiresIn * 1000)

                prefs.edit()
                    .putString(KEY_ACCESS_TOKEN, accessToken)
                    .putString(KEY_TOKEN_TYPE, tokenType)
                    .putLong(KEY_EXPIRES_AT, expiresAt)
                    .putString(KEY_REFRESH_TOKEN, refreshToken)
                    .apply()

                AppLogger.d(TAG, "loginWithGoogle: SUKSES, token=${accessToken.take(12)}..., expiresIn=$expiresIn s, hasRefresh=${!refreshToken.isNullOrEmpty()}")
                return@withContext true
            }
            val errBody = response?.errorBody()?.string()
            AppLogger.logApiError(TAG, "api/app/auth/login/google-account", response?.code() ?: -1, errBody)
            AppLogger.e(TAG, "loginWithGoogle gagal: isSuccess=${body?.isSuccess}, message=${body?.message}")
            return@withContext false
        } catch (e: Exception) {
            AppLogger.e(TAG, "loginWithGoogle exception", e)
            return@withContext false
        }
    }

    fun logout(context: Context) {
        AppLogger.i(TAG, "logout: menghapus semua data sesi")
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit().clear().apply()
    }

    // ==================== Device Pairing (login tanpa Google SHA-1) ====================

    /**
     * Mulai device pairing. Mengembalikan data link (user_code, verification_uri).
     * User harus membuka verification_uri di browser (perangkat lain) lalu masukkan user_code.
     */
    suspend fun startDeviceLink(context: Context): DeviceLinkStartResponseData? = withContext(Dispatchers.IO) {
        try {
            val request = DeviceLinkStartRequest(
                appInstanceId = getAppInstanceId(context),
                packageName = context.packageName,
                appVersionCode = 12,
                deviceType = "android_mobile",
                uiMode = "phone",
                manufacturer = Build.MANUFACTURER ?: "",
                brand = Build.BRAND ?: "",
                model = Build.MODEL ?: "",
                androidSdkInt = Build.VERSION.SDK_INT,
                androidRelease = Build.VERSION.RELEASE ?: "",
                locale = Locale.getDefault().toLanguageTag()
            )
            AppLogger.d(TAG, "startDeviceLink: memulai pairing appInstanceId=${request.appInstanceId.take(12)}...")
            val response = authApi?.startDeviceLink(request)
            val body = response?.body()
            if (response?.isSuccessful == true && body?.isSuccess == true && body.data != null) {
                val data = body.data!!
                AppLogger.d(TAG, "startDeviceLink: user_code=${data.userCode}, uri=${data.verificationUriComplete}")
                return@withContext data
            }
            val err = response?.errorBody()?.string()
            AppLogger.logApiError(TAG, "api/app/auth/device/pairing", response?.code() ?: -1, err)
            return@withContext null
        } catch (e: Exception) {
            AppLogger.e(TAG, "startDeviceLink exception", e)
            return@withContext null
        }
    }

    /**
     * Polling status device link. Saat user sudah approve (is_authenticated=true),
     * grant_token tersedia dan bisa ditukar lewat [exchangeDeviceLink].
     */
    suspend fun pollDeviceLinkStatus(context: Context, deviceCode: String): DeviceLinkStatusData? = withContext(Dispatchers.IO) {
        try {
            val appInstanceId = getAppInstanceId(context)
            AppLogger.d(TAG, "pollDeviceLinkStatus: cek status device=$deviceCode")
            val response = authApi?.getDeviceLinkStatus(deviceCode, appInstanceId)
            val body = response?.body()
            if (response?.isSuccessful == true && body?.isSuccess == true && body.data != null) {
                val data = body.data!!
                AppLogger.d(TAG, "pollDeviceLinkStatus: status=${data.status}, authenticated=${data.isAuthenticated}")
                return@withContext data
            }
            val err = response?.errorBody()?.string()
            AppLogger.logApiError(TAG, "api/app/auth/device/status", response?.code() ?: -1, err)
            return@withContext null
        } catch (e: Exception) {
            AppLogger.e(TAG, "pollDeviceLinkStatus exception", e)
            return@withContext null
        }
    }

    /**
     * Tukar grant_token (setelah user approve di browser) menjadi access_token.
     * Simpan access_token ke SharedPreferences seperti login Google.
     */
    suspend fun exchangeDeviceLink(context: Context, deviceCode: String, grantToken: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = DeviceLinkExchangeRequest(
                deviceCode = deviceCode,
                appInstanceId = getAppInstanceId(context),
                grantToken = grantToken
            )
            AppLogger.d(TAG, "exchangeDeviceLink: tukar grant_token -> access_token")
            val response = authApi?.exchangeDeviceLink(request)
            val body = response?.body()
            if (response?.isSuccessful == true && body?.isSuccess == true && body.data != null) {
                val exchangeData = body.data!!
                val tokenInfo = exchangeData.tokenInfo
                val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                val accessToken = tokenInfo?.accessToken
                if (accessToken.isNullOrEmpty()) {
                    AppLogger.e(TAG, "exchangeDeviceLink: token_info kosong / tidak ada access_token di respons (tokenInfo=${tokenInfo != null})")
                    return@withContext false
                }
                val expiresIn = tokenInfo.expiresInSeconds
                val refreshToken = tokenInfo.refreshToken
                val tokenType = tokenInfo.tokenType
                prefs.edit()
                    .putString(KEY_ACCESS_TOKEN, accessToken)
                    .putString(KEY_TOKEN_TYPE, tokenType)
                    .putLong(KEY_EXPIRES_AT, System.currentTimeMillis() + (expiresIn * 1000))
                    .putString(KEY_REFRESH_TOKEN, refreshToken)
                    .apply()
                AppLogger.d(TAG, "exchangeDeviceLink: SUKSES, token=${accessToken.take(12)}..., expiresIn=$expiresIn s, hasRefresh=${!refreshToken.isNullOrEmpty()}")
                return@withContext true
            }
            val err = response?.errorBody()?.string()
            AppLogger.logApiError(TAG, "api/app/auth/device/exchange", response?.code() ?: -1, err)
            return@withContext false
        } catch (e: Exception) {
            AppLogger.e(TAG, "exchangeDeviceLink exception", e)
            return@withContext false
        }
    }
}
