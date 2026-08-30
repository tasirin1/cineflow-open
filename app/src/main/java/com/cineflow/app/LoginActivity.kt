package com.cineflow.app

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cineflow.app.data.api.SessionManager
import com.cineflow.app.util.AppLogger
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppLogger.d(TAG, "onCreate: membuka halaman login")
        setContentView(R.layout.activity_login)

        btnGoogleSignIn = findViewById(R.id.btn_google_sign_in)
        btnDeviceLink = findViewById(R.id.btn_device_link)
        progressBar = findViewById(R.id.progress_login)
        tvStatus = findViewById(R.id.tv_login_status)

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
            AppLogger.d(TAG, "Tombol Device Pairing ditekan (belum diimplementasikan)")
            startDeviceLink()
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

    private fun startDeviceLink() {
        showLoading("Menunggu device pairing...")
        // TODO: Implement device pairing flow
        hideLoading()
        Toast.makeText(this, "Device pairing belum tersedia", Toast.LENGTH_SHORT).show()
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
