package com.sleepestapp.sleepest

import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.sleepestapp.sleepest.onboarding.OnboardingActivity
import com.sleepestapp.sleepest.storage.DataStoreRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class SplashScreen : AppCompatActivity() {

    private val dataStoreRepository: DataStoreRepository by lazy {
        (applicationContext as MainApplication).dataStoreRepository
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        @Suppress("DEPRECATION")
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        // Send user to MainActivity or OnboardActivity as soon as this activity loads
        // remove this activity from the stack

        lifecycleScope.launch {
            if (dataStoreRepository.tutorialStatusFlow.first().tutorialCompleted) {
                startMain()
            } else {
                startTutorial()
            }
        }

    }

    private fun startMain() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun startTutorial() {
        val intent = Intent(this, OnboardingActivity::class.java)
        startActivity(intent)
        finish()
    }

}