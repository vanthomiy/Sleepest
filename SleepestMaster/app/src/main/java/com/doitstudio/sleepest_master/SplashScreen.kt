package com.doitstudio.sleepest_master

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import com.doitstudio.sleepest_master.onboarding.OnboardingActivity

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
        // remove this activity from the stack
        if (notFirstAppStart()) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            val editor = getSharedPreferences("FirstAppStart", MODE_PRIVATE).edit()
            editor.putBoolean("started", true)
            editor.apply()
            val intent = Intent(this, OnboardingActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun notFirstAppStart(): Boolean {
        val sharedPreferences = getSharedPreferences("FirstAppStart", MODE_PRIVATE)
        return sharedPreferences.getBoolean("started", false)
    }

    companion object {
        private const val SPLASH_SCREEN = 2500
    }
}