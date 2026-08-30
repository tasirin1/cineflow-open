package com.cineflow.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.cineflow.app.ui.home.HomeFragment
import com.cineflow.app.util.AppLogger
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppLogger.d(TAG, "onCreate: membuka halaman utama")
        setContentView(R.layout.activity_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        if (savedInstanceState == null) {
            AppLogger.d(TAG, "onCreate: memuat HomeFragment awal")
            loadFragment(HomeFragment())
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    AppLogger.d(TAG, "Navigasi ke Home")
                    loadFragment(HomeFragment())
                    true
                }
                R.id.nav_download -> {
                    AppLogger.d(TAG, "Navigasi ke Unduhan (belum ada fragment)")
                    true
                }
                R.id.nav_account -> {
                    AppLogger.d(TAG, "Navigasi ke Akun (belum ada fragment)")
                    true
                }
                else -> false
            }
        }
    }

    override fun onStart() {
        super.onStart()
        AppLogger.d(TAG, "onStart")
    }

    override fun onResume() {
        super.onResume()
        AppLogger.d(TAG, "onResume")
    }

    override fun onDestroy() {
        AppLogger.d(TAG, "onDestroy")
        super.onDestroy()
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
