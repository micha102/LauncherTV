package org.cosh.launchertv

import android.os.Bundle
import android.os.Build
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import org.cosh.launchertv.fragments.ApplicationFragment

class Launcher : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setFullScreen()
        setContentView(R.layout.activity_launcher)

        supportFragmentManager.beginTransaction()
            .replace(R.id.container, ApplicationFragment.newInstance(), ApplicationFragment.TAG)
            .commit()
    }

    override fun onResume() {
        super.onResume()
        setFullScreen()
    }

    private fun setFullScreen() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                // For versions above KitKat (API 19), use immersive fullscreen
                val decorView = window.decorView
                val uiOptions = (
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION  // Hide nav bar
                                or View.SYSTEM_UI_FLAG_FULLSCREEN      // Hide status bar
                                or View.SYSTEM_UI_FLAG_IMMERSIVE       // Enable immersive mode
                                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY // Stick the immersive mode even after interactions
                        )

                decorView.systemUiVisibility = uiOptions
            } else {
                // For older versions, fallback to standard fullscreen
                window.setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}