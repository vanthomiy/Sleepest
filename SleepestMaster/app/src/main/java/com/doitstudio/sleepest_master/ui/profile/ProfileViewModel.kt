package com.doitstudio.sleepest_master.ui.profile

import android.Manifest
import android.app.Application
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.provider.Settings
import android.transition.TransitionManager
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.content.ContextCompat
import androidx.core.widget.NestedScrollView
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import com.doitstudio.sleepest_master.DontKillMyAppFragment
import com.doitstudio.sleepest_master.MainApplication
import com.doitstudio.sleepest_master.R
import com.doitstudio.sleepest_master.background.AlarmReceiver
import com.doitstudio.sleepest_master.storage.DataStoreRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.util.*
import kotlin.math.abs

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    //region binding values


    private val scope: CoroutineScope = MainScope()
    private val context by lazy { getApplication<Application>().applicationContext }
    private val dataStoreRepository: DataStoreRepository by lazy {
        (context as MainApplication).dataStoreRepository
    }


    // region Design
    val darkMode = ObservableField(true)
    fun darkModeToggled(view: View) {
        scope.launch {
            darkMode.get()?.let {
                dataStoreRepository.updateDarkMode(it)
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

        autoDarkMode.get()?.let {
            showDarkModeSetting.set(if (it) View.GONE else View.VISIBLE)
        }
    }

    val languageSelections = ObservableArrayList<String>()
    val selectedLanguage = ObservableField(0)
    fun onLanguageChanged(
            parent: AdapterView<*>?,
            selectedItemView: View,
            language: Int,
            id: Long
    ) {
        scope.launch {
            dataStoreRepository.updateLanguage(language)
        }

    }

    // endregion

    // region Help

    fun onHelpClicked(view: View) {
        when (view.tag.toString()) {
            "tutorial" -> "asd"
            "importantSettings" -> "asd"
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


    fun showPermissionInfo(permission:String){
        TransitionManager.beginDelayedTransition(transitionsContainer);

        activityPermissionDescription.set(if(permission == "sleepActivity") if(activityPermissionDescription.get() != View.VISIBLE) View.VISIBLE else View.GONE else View.GONE)
        dailyPermissionDescription.set(if(permission == "dailyActivity") if(dailyPermissionDescription.get() != View.VISIBLE) View.VISIBLE else View.GONE else View.GONE)
        storagePermissionDescription.set(if(permission == "storage") if(storagePermissionDescription.get() != View.VISIBLE) View.VISIBLE else View.GONE else View.GONE)
        overlayPermissionDescription.set(if(permission == "overlay") if(overlayPermissionDescription.get() != View.VISIBLE) View.VISIBLE else View.GONE else View.GONE)
    }

    fun checkPermissions(){

        activityPermission.set(PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACTIVITY_RECOGNITION
        ))

        dailyPermission.set(PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACTIVITY_RECOGNITION
        ))

        storagePermission.set(PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ANSWER_PHONE_CALLS
        ))

        overlayPermission.set(Settings.canDrawOverlays(context))

    }


    // endregion

    // region Data

    fun onDataClicked(view: View) {
        updateExpandChanged(view.tag.toString(), true)
        when(view.tag.toString()){
            "export" -> {

            }
            "remove" -> {

            }
        }
    }

    // endregion

    val designExpand = ObservableField(View.GONE)
    val helpExpand = ObservableField(View.GONE)
    val aboutUsExpand = ObservableField(View.GONE)
    val permissionsExpand = ObservableField(View.GONE)
    val dataExpand = ObservableField(View.GONE)

    val designRotation = ObservableField(0)
    val helpRotation = ObservableField(0)
    val aboutUsRotation = ObservableField(0)
    val permissionsRotation = ObservableField(0)
    val dataRotation = ObservableField(0)


    fun onExpandClicked(view: View) {
        updateExpandChanged(view.tag.toString(), true)
    }

    private fun updateExpandChanged(value: String, toggle: Boolean = false) {

        TransitionManager.beginDelayedTransition(transitionsContainer);

        designExpand.set(if (value == "0" && designExpand.get() == View.GONE) View.VISIBLE else View.GONE)
        helpExpand.set(if (value == "1" && helpExpand.get() == View.GONE) View.VISIBLE else View.GONE)
        aboutUsExpand.set(if (value == "2" && aboutUsExpand.get() == View.GONE) View.VISIBLE else View.GONE)
        permissionsExpand.set(if (value == "3" && permissionsExpand.get() == View.GONE) View.VISIBLE else View.GONE)
        dataExpand.set(if (value == "4" && dataExpand.get() == View.GONE) View.VISIBLE else View.GONE)

        designRotation.set(if (designExpand.get() == View.GONE) 0 else 180)
        helpRotation.set(if (helpExpand.get() == View.GONE) 0 else 180)
        aboutUsRotation.set(if (aboutUsExpand.get() == View.GONE) 0 else 180)
        permissionsRotation.set(if (permissionsExpand.get() == View.GONE) 0 else 180)
        dataRotation.set(if (dataExpand.get() == View.GONE) 0 else 180)

    }


    //endregion

    init {

        scope.launch {
            var settingsParams = dataStoreRepository.settingsDataFlow.first()
            languageSelections.addAll(arrayListOf<String>("Deutsch", "Englisch"))

            darkMode.set(settingsParams.designDarkMode)
            autoDarkMode.set(settingsParams.designAutoDarkMode)
            showDarkModeSetting.set(if(settingsParams.designAutoDarkMode) View.GONE else View.VISIBLE)
            selectedLanguage.set(settingsParams.designLanguage)

        }

        checkPermissions()
    }

    //region animation

    lateinit var transitionsContainer : ViewGroup
    lateinit var animatedTopView : MotionLayout

    //endregion
}