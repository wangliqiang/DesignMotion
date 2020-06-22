package com.app.design_motion.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.app.design_motion.R
import com.app.design_motion.common.listener.BottomNavigationViewListener
import com.app.design_motion.common.listener.OnBackPressedListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), BottomNavigationViewListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navView: BottomNavigationView = findViewById(R.id.bottom_navigation)

        val navController = findNavController(R.id.nav_host_fragment)
        // menu should be considered as top level destinations.
        navView.setupWithNavController(navController)
    }

    override fun hideBottomNavigationView() {
        if (bottom_navigation.translationY == 0f)
            bottom_navigation.animate()
                .translationY(bottom_navigation.height.toFloat())
                .setDuration(250)
                .start()
    }

    override fun showBottomNavigationView() {
        if (bottom_navigation.translationY >= bottom_navigation.height.toFloat())
            bottom_navigation.animate()
                .translationY(0f)
                .setDuration(400)
                .start()
    }

    override fun onBackPressed() {
        val fragmentList = supportFragmentManager.fragments
        var proceedToSuper = true
        for (fragment in fragmentList) {
            if (fragment is OnBackPressedListener) {
                proceedToSuper = false
                (fragment as OnBackPressedListener).onBackPressed()
            }
        }
        if (proceedToSuper) super.onBackPressed()
    }
}