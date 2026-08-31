package com.cineflow.app.ui.account

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.cineflow.app.LoginActivity
import com.cineflow.app.R
import com.cineflow.app.data.api.ApiClient
import com.cineflow.app.data.api.SessionManager
import com.cineflow.app.util.AppLogger
import kotlinx.coroutines.launch

class AccountFragment : Fragment() {

    companion object {
        private const val TAG = "AccountFragment"
    }

    private lateinit var cardInfo: View
    private lateinit var tvName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvStatus: TextView
    private lateinit var btnLogin: Button
    private lateinit var btnLogout: TextView
    private lateinit var tvNotLoggedIn: TextView
    private lateinit var progressLoading: ProgressBar
    private lateinit var tvError: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cardInfo = view.findViewById(R.id.card_info)
        tvName = view.findViewById(R.id.tv_name)
        tvEmail = view.findViewById(R.id.tv_email)
        tvStatus = view.findViewById(R.id.tv_status)
        btnLogin = view.findViewById(R.id.btn_login)
        btnLogout = view.findViewById(R.id.btn_logout)
        tvNotLoggedIn = view.findViewById(R.id.tv_not_logged_in)
        progressLoading = view.findViewById(R.id.progress_loading)
        tvError = view.findViewById(R.id.tv_error)

        btnLogin.setOnClickListener { openLogin() }
        btnLogout.setOnClickListener { logout() }

        loadState()
    }

    private fun loadState() {
        val context = requireContext()
        if (!SessionManager.isTokenValid(context)) {
            AppLogger.d(TAG, "loadState: belum login")
            showNotLoggedIn()
            return
        }

        val user = currentUser
        if (user != null) {
            showUser(user)
            return
        }

        fetchCurrentUser()
    }

    private val currentUser: CurrentUser?
        get() {
            val prefs = requireContext().getSharedPreferences(
                SessionManager.preferencesName, android.content.Context.MODE_PRIVATE
            )
            val name = prefs.getString("user_name", null) ?: return null
            val email = prefs.getString("user_email", null)
            val premium = prefs.getBoolean("user_premium", false)
            return CurrentUser(name, email, premium)
        }

    private data class CurrentUser(
        val name: String,
        val email: String?,
        val premium: Boolean
    )

    private fun fetchCurrentUser() {
        progressLoading.visibility = View.VISIBLE
        cardInfo.visibility = View.GONE
        tvError.visibility = View.GONE
        tvNotLoggedIn.visibility = View.GONE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = ApiClient.api.getCurrentUser()
                val body = response.body()
                if (response.isSuccessful && body?.isSuccess == true && body.data != null) {
                    val data = body.data!!
                    saveUser(data.name, data.email, data.isPremium)
                    showUser(CurrentUser(data.name, data.email, data.isPremium))
                } else {
                    AppLogger.w(TAG, "fetchCurrentUser gagal: ${response.code()}")
                    showError("Gagal memuat akun (${response.code()})")
                }
            } catch (e: Exception) {
                AppLogger.e(TAG, "fetchCurrentUser exception", e)
                showError("Error: ${e.message}")
            }
        }
    }

    private fun saveUser(name: String, email: String?, premium: Boolean) {
        requireContext().getSharedPreferences(
            SessionManager.preferencesName, android.content.Context.MODE_PRIVATE
        ).edit()
            .putString("user_name", name)
            .putString("user_email", email)
            .putBoolean("user_premium", premium)
            .apply()
    }

    private fun showUser(user: CurrentUser) {
        progressLoading.visibility = View.GONE
        tvError.visibility = View.GONE
        tvNotLoggedIn.visibility = View.GONE
        cardInfo.visibility = View.VISIBLE

        tvName.text = user.name
        tvEmail.text = if (user.email.isNullOrBlank()) "—" else user.email
        tvStatus.text = if (user.premium) "PREMIUM" else "GRATIS"

        btnLogout.visibility = View.VISIBLE
        btnLogin.visibility = View.GONE
    }

    private fun showNotLoggedIn() {
        progressLoading.visibility = View.GONE
        cardInfo.visibility = View.GONE
        tvError.visibility = View.GONE
        tvNotLoggedIn.visibility = View.VISIBLE
        btnLogin.visibility = View.VISIBLE
        btnLogout.visibility = View.GONE
    }

    private fun showError(message: String) {
        progressLoading.visibility = View.GONE
        cardInfo.visibility = View.GONE
        tvNotLoggedIn.visibility = View.GONE
        tvError.text = message
        tvError.visibility = View.VISIBLE
        btnLogout.visibility = View.VISIBLE
    }

    private fun openLogin() {
        AppLogger.d(TAG, "openLogin: navigasi ke LoginActivity")
        startActivity(Intent(requireContext(), LoginActivity::class.java))
        requireActivity().finish()
    }

    private fun logout() {
        AppLogger.i(TAG, "logout: hapus sesi lalu kembali ke LoginActivity")
        SessionManager.logout(requireContext())
        val intent = Intent(requireContext(), LoginActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        startActivity(intent)
        requireActivity().finish()
    }
}
