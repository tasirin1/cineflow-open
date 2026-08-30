package com.cineflow.app

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.LinearInterpolator
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.cineflow.app.data.api.SessionManager
import com.cineflow.app.util.AppLogger

class LaunchActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "LaunchActivity"
    }

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppLogger.d(TAG, "onCreate: memulai splash screen")
        setContentView(R.layout.activity_launch)

        val progressValue = findViewById<TextView>(R.id.tv_launch_progress_value)
        val progressBar = findViewById<ProgressBar>(R.id.progress_launch)
        val footerText = findViewById<TextView>(R.id.tv_launch_footer)

        val animator = ValueAnimator.ofInt(0, 100)
        animator.duration = 2500
        animator.interpolator = LinearInterpolator()
        animator.addUpdateListener { animation ->
            val value = animation.animatedValue as Int
            progressBar.progress = value
            progressValue.text = "$value%"
            when {
                value < 30 -> footerText.text = "Persiapan aplikasi..."
                value < 60 -> footerText.text = "Memuat sumber data..."
                value < 90 -> footerText.text = "Hampir selesai..."
                else -> footerText.text = "Siap!"
            }
        }
        animator.start()

        handler.postDelayed({
            val tokenValid = SessionManager.isTokenValid(this)
            AppLogger.d(TAG, "Navigasi: token valid? $tokenValid")
            if (tokenValid) {
                AppLogger.d(TAG, "Token valid -> buka MainActivity")
                startActivity(Intent(this, MainActivity::class.java))
            } else {
                AppLogger.d(TAG, "Token tidak ada/expired -> buka LoginActivity")
                startActivity(Intent(this, LoginActivity::class.java))
            }
            finish()
        }, 2800)
    }

    override fun onDestroy() {
        AppLogger.d(TAG, "onDestroy: splash ditutup")
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}
