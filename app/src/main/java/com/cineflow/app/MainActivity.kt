package com.cineflow.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.cineflow.app.ui.account.AccountFragment
import com.cineflow.app.ui.downloads.DownloadsFragment
import com.cineflow.app.ui.home.HomeFragment
import com.cineflow.app.util.AppLogger
import com.google.android.material.bottomnavigation.BottomNavigationView
class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    private var lastSelectedId: Int = R.id.nav_home

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
            val id = item.itemId
            if (id == lastSelectedId) return@setOnItemSelectedListener true
            when (id) {
                R.id.nav_home -> {
                    AppLogger.d(TAG, "Navigasi ke Home")
                    loadFragment(HomeFragment())
                }
                R.id.nav_download -> {
                    AppLogger.d(TAG, "Navigasi ke Unduhan")
                    loadFragment(DownloadsFragment())
                }
                R.id.nav_account -> {
                    AppLogger.d(TAG, "Navigasi ke Akun")
                    loadFragment(AccountFragment())
                }
                else -> return@setOnItemSelectedListener false
            }
            lastSelectedId = id
            true
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
