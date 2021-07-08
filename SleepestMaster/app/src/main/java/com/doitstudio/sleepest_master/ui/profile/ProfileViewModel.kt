package com.doitstudio.sleepest_master.ui.profile

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import com.doitstudio.sleepest_master.MainActivity
import com.doitstudio.sleepest_master.MainApplication
import com.doitstudio.sleepest_master.R
import com.doitstudio.sleepest_master.storage.DataStoreRepository
import com.doitstudio.sleepest_master.storage.DatabaseRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.*

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    //region binding values


    private val scope: CoroutineScope = MainScope()
    private val context by lazy { getApplication<Application>().applicationContext }
    private val dataStoreRepository: DataStoreRepository by lazy {
        (context as MainApplication).dataStoreRepository
    }

    private val dataBaseRepository: DatabaseRepository by lazy {
        (context as MainApplication).dataBaseRepository
    }

    // region Design
    val darkMode = ObservableField(true)
    fun darkModeToggled(view: View) {
        scope.launch {
            darkMode.get()?.let {
                dataStoreRepository.updateDarkMode(it)
                AppCompatDelegate
                        .setDefaultNightMode(
                            if (it)
                                AppCompatDelegate.MODE_NIGHT_YES else
                                AppCompatDelegate.MODE_NIGHT_NO
                        );
            }
        }
    }

    val autoDarkMode = ObservableField(true)
    val showDarkModeSetting = ObservableField(View.GONE)
    fun autoDarkModeToggled(view: View) {
        TransitionManager.beginDelayedTransition(transitionsContainer);

        scope.launch {
            autoDarkMode.get()?.let {
                dataStoreRepository.updateAutoDarkMode(it)
            }
        }

        autoDarkMode.get()?.let { auto ->
            showDarkModeSetting.set(if (auto) View.GONE else View.VISIBLE)
            AppCompatDelegate
                    .setDefaultNightMode(if (auto)
                        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM else
                        darkMode.get().let { mode ->
                            if (mode == true)
                                AppCompatDelegate.MODE_NIGHT_YES else
                                AppCompatDelegate.MODE_NIGHT_NO
                        })

        }
    }

    val languageSelections = ObservableArrayList<String>()
    val selectedLanguage = ObservableField(0)


    // endregion

    // region Help

    fun onHelpClicked(view: View) {
        when (view.tag.toString()) {
            "tutorial" -> "asd"
            "importantSettings" -> "saas"
        }
    }

    // endregion

    // region About us

    fun onAboutUsClicked(view: View) {
        updateExpandChanged(view.tag.toString(), true)
        when (view.tag.toString()) {
            "improvement" -> "asd"
            "rate" -> "asd"
            "error" -> "asd"
        }
    }

    // endregion

    // region Permissions

    val activityPermission = ObservableField(false)
    val dailyPermission = ObservableField(false)
    val storagePermission = ObservableField(false)
    val overlayPermission = ObservableField(false)

    val activityPermissionDescription = ObservableField(View.GONE)
    val dailyPermissionDescription = ObservableField(View.GONE)
    val storagePermissionDescription = ObservableField(View.GONE)
    val overlayPermissionDescription = ObservableField(View.GONE)


    fun showPermissionInfo(permission: String){
        TransitionManager.beginDelayedTransition(transitionsContainer);

        activityPermissionDescription.set(if (permission == "sleepActivity") if (activityPermissionDescription.get() != View.VISIBLE) View.VISIBLE else View.GONE else View.GONE)
        dailyPermissionDescription.set(if (permission == "dailyActivity") if (dailyPermissionDescription.get() != View.VISIBLE) View.VISIBLE else View.GONE else View.GONE)
        storagePermissionDescription.set(if (permission == "storage") if (storagePermissionDescription.get() != View.VISIBLE) View.VISIBLE else View.GONE else View.GONE)
        overlayPermissionDescription.set(if (permission == "overlay") if (overlayPermissionDescription.get() != View.VISIBLE) View.VISIBLE else View.GONE else View.GONE)
    }

    fun checkPermissions(){

        activityPermission.set(
            PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACTIVITY_RECOGNITION
            )
        )

        dailyPermission.set(
            PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACTIVITY_RECOGNITION
            )
        )

        storagePermission.set(
            PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ANSWER_PHONE_CALLS
            )
        )

        overlayPermission.set(Settings.canDrawOverlays(context))

    }


    // endregion

    // region Data

    val removeButtonText = ObservableField("delete all data")

    fun onDataClicked(view: View) {
        when(view.tag.toString()){
            "export" -> {

            }
            "remove" -> {
                TransitionManager.beginDelayedTransition(transitionsContainer);

                removeExpand.set(removeExpand.get() != true)
                removeButtonText.set(if (removeExpand.get() == true) "delete all data" else "return")
            }
            "removeAckn" -> {


                scope.launch {

                    dataBaseRepository.deleteAllAlarms()
                    dataBaseRepository.deleteActivityApiRawData()
                    dataBaseRepository.deleteSleepApiRawData()
                    dataBaseRepository.deleteUserSleepSession()

                    dataStoreRepository.deleteAllData()
                }
            }
        }
    }

    // endregion

    val actualExpand = ObservableField(-1)
    val goneState = ObservableField(View.GONE)
    val visibleState = ObservableField(View.VISIBLE)
    val removeExpand = ObservableField(false)

    val normalRotationState = ObservableField(0)
    val rotatedState = ObservableField(180)


    fun onExpandClicked(view: View) {
        updateExpandChanged(view.tag.toString(), true)
    }

    private fun updateExpandChanged(value: String, toggle: Boolean = false) {

        TransitionManager.beginDelayedTransition(transitionsContainer);

        actualExpand.set(if (actualExpand.get() == value.toIntOrNull()) -1 else value.toIntOrNull())
        removeExpand.set(if (actualExpand.get() == 4) removeExpand.get() else false)

    }



    //endregion

    init {

        scope.launch {

            var settingsParams = dataStoreRepository.settingsDataFlow.first()
            languageSelections.addAll(arrayListOf<String>("Deutsch", "Englisch"))

            darkMode.set(settingsParams.designDarkMode)
            autoDarkMode.set(settingsParams.designAutoDarkMode)
            showDarkModeSetting.set(if (settingsParams.designAutoDarkMode) View.GONE else View.VISIBLE)
            selectedLanguage.set(settingsParams.designLanguage)
        }

        checkPermissions()
    }

    //region animation

    lateinit var transitionsContainer : ViewGroup
    lateinit var animatedTopView : MotionLayout

    //endregion
}