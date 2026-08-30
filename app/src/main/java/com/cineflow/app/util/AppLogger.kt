package com.cineflow.app.util

import android.util.Log
import org.json.JSONObject

/**
 * AppLogger — helper untuk logging yang konsisten dan mudah difilter di Logcat.
 *
 * Filter di Logcat pakai tag: "CineFlow" atau nama class (LaunchActivity, LoginActivity, dsb).
 */
object AppLogger {

    private const val PREFIX = "CineFlow"

    fun d(tag: String, message: String) = Log.d("$PREFIX/$tag", message)
    fun i(tag: String, message: String) = Log.i("$PREFIX/$tag", message)
    fun w(tag: String, message: String) = Log.w("$PREFIX/$tag", message)

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (throwable != null) {
            Log.e("$PREFIX/$tag", message, throwable)
        } else {
            Log.e("$PREFIX/$tag", message)
        }
    }

    /**
     * Log respons HTTP secara lengkap: code, message, dan body error.
     */
    fun logApiError(tag: String, endpoint: String, code: Int, errorBody: String?) {
        val sb = StringBuilder()
        sb.append("API error '$endpoint': code=$code")
        if (!errorBody.isNullOrEmpty()) {
            sb.append(" body=").append(errorBody)
        }
        Log.e("$PREFIX/$tag", sb.toString())
    }

    /**
     * Parse dan log pesan error dari body BaseResponse JSON kalau tersedia.
     */
    fun logApiResponse(tag: String, endpoint: String, code: Int, isSuccess: Boolean, message: String?) {
        val status = if (isSuccess) "OK" else "FAIL"
        Log.d("$PREFIX/$tag", "API '$endpoint' -> $status (code=$code, message=$message)")
    }
}
