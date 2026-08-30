package com.cineflow.app

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cineflow.app.data.api.SessionManager
import com.cineflow.app.data.model.DeviceLinkStartResponseData
import com.cineflow.app.util.AppLogger
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "LoginActivity"
        private const val RC_GOOGLE_SIGN_IN = 9001
        private const val WEB_CLIENT_ID = "592012033889-rf8gtl65o0j1ol0ar6vh1rfkp21catnu.apps.googleusercontent.com"
    }

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var btnGoogleSignIn: Button
    private lateinit var btnDeviceLink: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var tvStatus: TextView
    private lateinit var tvCopyDebug: TextView
    private var deviceLinkJob: Job? = null
    private var processingExchange = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppLogger.d(TAG, "onCreate: membuka halaman login")
        setContentView(R.layout.activity_login)

        btnGoogleSignIn = findViewById(R.id.btn_google_sign_in)
        btnDeviceLink = findViewById(R.id.btn_device_link)
        progressBar = findViewById(R.id.progress_login)
        tvStatus = findViewById(R.id.tv_login_status)
        tvCopyDebug = findViewById(R.id.tv_copy_debug)

        // Check if already logged in
        if (SessionManager.isTokenValid(this)) {
            AppLogger.d(TAG, "Token sudah valid dari onCreate -> langsung ke MainActivity")
            goToMain()
            return
        }

        // Configure Google Sign-In
        AppLogger.d(TAG, "Mengkonfigurasi Google Sign-In dengan clientId=${WEB_CLIENT_ID.take(20)}...")
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(WEB_CLIENT_ID)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        btnGoogleSignIn.setOnClickListener {
            AppLogger.d(TAG, "Tombol Google Sign-In ditekan")
            val signInIntent = googleSignInClient.signInIntent
            AppLogger.d(TAG, "Meluncurkan intent Google Sign-In (RC=$RC_GOOGLE_SIGN_IN)")
            startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN)
        }

        btnDeviceLink.setOnClickListener {
            AppLogger.d(TAG, "Tombol Device Pairing ditekan")
            startDeviceLinkFlow()
        }

        tvCopyDebug.setOnClickListener {
            AppLogger.d(TAG, "Tombol salin info debug ditekan")
            copyDebugLog()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        AppLogger.d(TAG, "onActivityResult: requestCode=$requestCode resultCode=$resultCode data=${if (data == null) "null" else "ada"}")

        if (requestCode == RC_GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                AppLogger.d(TAG, "Google account: email=${account?.email}, idToken=${if (idToken != null) "${idToken.take(20)}..." else "NULL!"}")
                if (idToken != null) {
                    handleGoogleSignIn(idToken)
                } else {
                    AppLogger.e(TAG, "account.idToken = NULL — kemungkinan SHA-1 tidak terdaftar di Google Cloud Console, atau clientId salah jenis (Android vs Web)")
                    Toast.makeText(this, "Gagal mendapatkan token Google (idToken null)", Toast.LENGTH_SHORT).show()
                }
            } catch (e: ApiException) {
                AppLogger.e(TAG, "Google sign-in gagal: statusCode=${e.statusCode} (2=sign-in dibatalkan, 12500=error internal/konfigurasi)", e)
                Toast.makeText(this, "Login Google gagal (${e.statusCode})", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                AppLogger.e(TAG, "Google sign-in exception tidak terduga", e)
                Toast.makeText(this, "Login Google gagal", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleGoogleSignIn(idToken: String) {
        AppLogger.d(TAG, "handleGoogleSignIn: memulai alur nonce + login")
        showLoading("Memproses login Google...")

        lifecycleScope.launch {
            // Step 1: Get nonce
            AppLogger.d(TAG, "Step 1: minta nonce dari server...")
            val nonce = SessionManager.getNonce(this@LoginActivity)
            if (nonce == null) {
                AppLogger.e(TAG, "Step 1 GAGAL: nonce null")
                hideLoading()
                Toast.makeText(this@LoginActivity, "Gagal memuat nonce", Toast.LENGTH_SHORT).show()
                return@launch
            }
            AppLogger.d(TAG, "Step 1 SUKSES: nonce=${nonce.take(12)}...")

            // Step 2: Login with Google
            AppLogger.d(TAG, "Step 2: kirim idToken + nonce ke server...")
            val success = SessionManager.loginWithGoogle(this@LoginActivity, idToken, nonce)
            if (success) {
                AppLogger.d(TAG, "Step 2 SUKSES: login berhasil -> MainActivity")
                goToMain()
            } else {
                AppLogger.e(TAG, "Step 2 GAGAL: loginWithGoogle mengembalikan false")
                hideLoading()
                Toast.makeText(this@LoginActivity, "Login gagal. Coba lagi.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun startDeviceLinkFlow() {
        showLoading("Menghubungkan ke server...")

        lifecycleScope.launch {
            val data = SessionManager.startDeviceLink(this@LoginActivity)
            if (data == null) {
                AppLogger.e(TAG, "startDeviceLinkFlow: gagal memulai device pairing")
                hideLoading()
                Toast.makeText(this@LoginActivity, "Gagal membuat sesi. Periksa koneksi internet.", Toast.LENGTH_LONG).show()
                return@launch
            }
            hideLoading()
            showDevicePairingDialog(data)
        }
    }

    private fun showDevicePairingDialog(data: DeviceLinkStartResponseData) {
        val userCode = data.userCode ?: ""
        val uri = data.verificationUriComplete ?: data.verificationUri ?: ""
        val expiresIn = data.expiresInSeconds
        val interval = (data.intervalSeconds).coerceAtLeast(3)

        val message = buildString {
            append("Masukkan kode berikut di situs CineFlow untuk menautkan perangkat ini:\n\n")
            append("KODE: ")
            append(userCode.ifBlank { "-" })
            append("\n\nKode berlaku ")
            append(expiresIn)
            append(" detik.")
            if (uri.isNotBlank()) {
                append("\n\nLink:\n")
                append(uri)
            }
        }

        val builder = AlertDialog.Builder(this)
            .setTitle("Device Pairing")
            .setMessage(message)
            .setNegativeButton("Batal") { _, _ ->
                AppLogger.d(TAG, "Device pairing dibatalkan oleh user")
                deviceLinkJob?.cancel()
                deviceLinkJob = null
            }
            .setNeutralButton("Salin Kode") { _, _ ->
                copyToClipboard(if (userCode.isNotBlank()) userCode else uri, "Kode device pairing")
                Toast.makeText(this, "Kode disalin", Toast.LENGTH_SHORT).show()
            }

        if (uri.isNotBlank()) {
            builder.setPositiveButton("Buka Link") { _, _ ->
                openVerificationUri(uri)
            }
        }

        val dialog = builder.create()
        dialog.setOnDismissListener {
            AppLogger.d(TAG, "Dialog device pairing ditutup, hentikan polling")
            if (processingExchange) return@setOnDismissListener
            deviceLinkJob?.cancel()
            deviceLinkJob = null
        }
        dialog.show()

        // Polling status device link sampai ter-autentikasi atau timeout
        deviceLinkJob?.cancel()
        deviceLinkJob = lifecycleScope.launch {
            val startTime = System.currentTimeMillis()
            val timeoutMs = expiresIn.toLong() * 1000L
            while (System.currentTimeMillis() - startTime < timeoutMs) {
                delay(interval * 1000L)
                if (!dialog.isShowing) break
                val status = SessionManager.pollDeviceLinkStatus(this@LoginActivity, data.deviceCode ?: "")
                if (status?.isAuthenticated == true) {
                    AppLogger.d(TAG, "Device pairing ter-autentikasi: menukar grant_token")
                    processingExchange = true
                    val grantToken = status.grantToken
                    if (grantToken.isNullOrBlank()) {
                        AppLogger.e(TAG, "is_authenticated=true tapi grant_token kosong")
                        processingExchange = false
                        if (dialog.isShowing) dialog.dismiss()
                        Toast.makeText(this@LoginActivity, "Tautan disetujui tapi token tidak ditemukan. Coba lagi.", Toast.LENGTH_LONG).show()
                        deviceLinkJob = null
                        return@launch
                    }
                    showLoading("Memproses login...")
                    val ok = SessionManager.exchangeDeviceLink(this@LoginActivity, data.deviceCode ?: "", grantToken)
                    if (ok && SessionManager.isTokenValid(this@LoginActivity)) {
                        AppLogger.d(TAG, "Device pairing SUKSES -> MainActivity")
                        deviceLinkJob = null
                        goToMain()
                    } else {
                        AppLogger.e(TAG, "Exchange device link gagal")
                        processingExchange = false
                        if (dialog.isShowing) dialog.dismiss()
                        hideLoading()
                        Toast.makeText(this@LoginActivity, "Login gagal. Coba lagi.", Toast.LENGTH_LONG).show()
                    }
                    return@launch
                }
                if (status == null) {
                    AppLogger.w(TAG, "Polling status device link mengembalikan null, lanjut poll")
                }
            }
            if (deviceLinkJob != null) {
                AppLogger.d(TAG, "Polling device link timeout (expires)")
                deviceLinkJob = null
                if (dialog.isShowing) {
                    dialog.dismiss()
                    Toast.makeText(this@LoginActivity, "Kode device pairing kedaluwarsa. Coba lagi.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun openVerificationUri(uri: String) {
        try {
            AppLogger.d(TAG, "Buka link verifikasi di browser: $uri")
            startActivity(Intent(Intent.ACTION_VIEW, android.net.Uri.parse(uri)))
        } catch (e: Exception) {
            AppLogger.e(TAG, "Gagal membuka link verifikasi", e)
            Toast.makeText(this, "Tidak ada aplikasi browser. Buka manual: $uri", Toast.LENGTH_LONG).show()
        }
    }

    private fun copyToClipboard(text: String, label: String) {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
    }

    private fun copyDebugLog() {
        val info = buildString {
            append("CineFlow Debug Info\n")
            append("App instance ID: ")
            append(SessionManager.getAppInstanceId(this@LoginActivity))
            append("\nDevice: ")
            append(android.os.Build.MANUFACTURER)
            append(" ")
            append(android.os.Build.MODEL)
            append("\nAndroid SDK: ")
            append(android.os.Build.VERSION.SDK_INT)
            append("\nToken valid: ")
            append(SessionManager.isTokenValid(this@LoginActivity))
        }
        copyToClipboard(info, "CineFlow Debug Info")
        Toast.makeText(this, "Info debug disalin", Toast.LENGTH_SHORT).show()
    }

    private fun showLoading(message: String) {
        progressBar.visibility = View.VISIBLE
        tvStatus.text = message
        tvStatus.visibility = View.VISIBLE
        btnGoogleSignIn.isEnabled = false
        btnDeviceLink.isEnabled = false
    }

    private fun hideLoading() {
        progressBar.visibility = View.GONE
        tvStatus.visibility = View.GONE
        btnGoogleSignIn.isEnabled = true
        btnDeviceLink.isEnabled = true
    }

    private fun goToMain() {
        AppLogger.d(TAG, "goToMain: navigasi ke MainActivity")
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
