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

class LaunchActivity : AppCompatActivity() {

    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, 2800)
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }
}
