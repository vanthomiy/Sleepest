package com.doitstudio.sleepest_master

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.doitstudio.sleepest_master.databinding.SplashScreenBinding

class SplashScreen : AppCompatActivity() {

    private lateinit var binding: SplashScreenBinding

    var top: Animation? = null
    var bottom: Animation? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = SplashScreenBinding.inflate(layoutInflater)

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.splash_screen)

        top = AnimationUtils.loadAnimation(this, R.anim.top)
        bottom = AnimationUtils.loadAnimation(this, R.anim.bottom)

        binding.imageView.animation = top
        binding.textView3.animation = bottom
        binding.tvS1.animation = bottom
        binding.tvL1.animation = bottom
        binding.tvE1.animation = bottom
        binding.tvE2.animation = bottom
        binding.tvP1.animation = bottom
        binding.tvE3.animation = bottom
        binding.tvS2.animation = bottom
        binding.tvT1.animation = bottom

        Handler().postDelayed({
            val intent = Intent(
                this,
                MainActivity::class.java
            )
            startActivity(intent)
            finish()
        }, SPLASH_SCREEN.toLong())
    }

    companion object {
        private const val SPLASH_SCREEN = 2500
    }
}