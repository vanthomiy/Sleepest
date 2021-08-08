package com.doitstudio.sleepest_master

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.doitstudio.sleepest_master.databinding.SplashScreenBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

class SplashScreen : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

       /* binding = SplashScreenBinding.inflate(layoutInflater)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.splash_screen)

        Handler().postDelayed({
            val intent = Intent(
                this,
                MainActivity::class.java
            )
            startActivity(intent)
            finish()
        }, SPLASH_SCREEN.toLong())*/

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        // Send user to MainActivity as soon as this activity loads
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        // remove this activity from the stack
        finish()
    }

    companion object {
        private const val SPLASH_SCREEN = 2500
    }
}