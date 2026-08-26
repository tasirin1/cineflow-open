package com.cineflow.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cineflow.app.data.api.SessionManager
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
        setContentView(R.layout.activity_login)

        btnGoogleSignIn = findViewById(R.id.btn_google_sign_in)
        btnDeviceLink = findViewById(R.id.btn_device_link)
        progressBar = findViewById(R.id.progress_login)
        tvStatus = findViewById(R.id.tv_login_status)

        // Check if already logged in
        if (SessionManager.isTokenValid(this)) {
            goToMain()
            return
        }

        // Configure Google Sign-In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(WEB_CLIENT_ID)
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)

        btnGoogleSignIn.setOnClickListener {
            val signInIntent = googleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN)
        }

        btnDeviceLink.setOnClickListener {
            startDeviceLink()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account?.idToken
                if (idToken != null) {
                    handleGoogleSignIn(idToken)
                } else {
                    Toast.makeText(this, "Gagal mendapatkan token Google", Toast.LENGTH_SHORT).show()
                }
            } catch (e: ApiException) {
                Log.e(TAG, "Google sign-in failed: code=${e.statusCode}", e)
                Toast.makeText(this, "Login Google gagal (${e.statusCode})", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleGoogleSignIn(idToken: String) {
        showLoading("Memproses login Google...")

        lifecycleScope.launch {
            // Step 1: Get nonce
            val nonce = SessionManager.getNonce(this@LoginActivity)
            if (nonce == null) {
                hideLoading()
                Toast.makeText(this@LoginActivity, "Gagal memuat nonce", Toast.LENGTH_SHORT).show()
                return@launch
            }

            // Step 2: Login with Google
            val success = SessionManager.loginWithGoogle(this@LoginActivity, idToken, nonce)
            if (success) {
                goToMain()
            } else {
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
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
