package com.doitstudio.sleepest_master.ui.settings

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.provider.Settings
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.content.ContextCompat
import androidx.databinding.Observable
import androidx.databinding.ObservableArrayMap
import androidx.databinding.ObservableField
import androidx.databinding.ObservableMap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import com.doitstudio.sleepest_master.MainApplication
import com.doitstudio.sleepest_master.R
import com.doitstudio.sleepest_master.alarmclock.AlarmClockReceiver
import com.doitstudio.sleepest_master.model.data.AlarmClockReceiverUsage
import com.doitstudio.sleepest_master.model.data.credits.CreditsSites
import com.doitstudio.sleepest_master.storage.DataStoreRepository
import com.doitstudio.sleepest_master.storage.DatabaseRepository
import com.doitstudio.sleepest_master.util.PermissionsUtil
import com.doitstudio.sleepest_master.util.SmileySelectorUtil
import com.doitstudio.sleepest_master.util.TimeConverterUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.util.*

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

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
                dataStoreRepository.updateAutoDarkModeAckn(true)
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
                dataStoreRepository.updateAutoDarkModeAckn(true)
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


    val showAlarmActiv = ObservableField(true)
    val showActualWakeUpPoint = ObservableField(true)
    val showActualSleepTime = ObservableField(true)
    val showDetailedSleepTime = ObservableField(true)
    val showSleepState = ObservableField(true)
    fun bannerSettingsToggled(view: View) {

        scope.launch {
            when (view.tag.toString()) {
                "show_alarm_active" -> showAlarmActiv.get()
                    ?.let { dataStoreRepository.updateBannerShowAlarmActiv(it) }
                "show_actual_wakeup" -> showActualWakeUpPoint.get()
                    ?.let { dataStoreRepository.updateBannerShowActualWakeUpPoint(it) }
                "show_actual_sleep_time" -> showActualSleepTime.get()
                    ?.let { dataStoreRepository.updateBannerShowActualSleepTime(it) }
                "show_actual_sleep_state" -> showSleepState.get()
                    ?.let { dataStoreRepository.updateBannerShowSleepState(it) }
            }
        }
    }

    // endregion

    // region Help

    fun onHelpClicked(view: View) {

        when (view.tag.toString()) {
            "tutorial" -> "nksnklas"
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
            PermissionsUtil.isActivityRecognitionPermissionGranted(context)
        )

        dailyPermission.set(
            PermissionsUtil.isActivityRecognitionPermissionGranted(context)
        )

        storagePermission.set(
            PermissionsUtil.isNotificationPolicyAccessGranted(context)
        )

        overlayPermission.set(PermissionsUtil.isOverlayPermissionGranted(context))

    }


    // endregion

    // region Credits

    fun onCreditsClicked(view: View) {
        when (view.tag.toString()) {
            "flaticon" -> "asd"

        }
    }

    val authorsText = ObservableField("")

    // endregion

    // region Data

    val removeButtonText = ObservableField(context.getString(R.string.settings_delete_all_data))

    fun onDataClicked(view: View) {
        when(view.tag.toString()){
            "export" -> {

            }
            "remove" -> {
                TransitionManager.beginDelayedTransition(transitionsContainer);

                removeExpand.set(removeExpand.get() != true)
                removeButtonText.set(if (removeExpand.get() == false)
                    context.getString(R.string.settings_delete_all_data) else context.getString(
                                    R.string.settings_return))
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
            darkMode.set(settingsParams.designDarkMode)
            autoDarkMode.set(settingsParams.designAutoDarkMode)
            showDarkModeSetting.set(if (settingsParams.designAutoDarkMode) View.GONE else View.VISIBLE)

            showAlarmActiv.set(settingsParams.bannerShowAlarmActiv)
            showActualWakeUpPoint.set(settingsParams.bannerShowActualWakeUpPoint)
            showActualSleepTime.set(settingsParams.bannerShowActualSleepTime)
            showSleepState.set(settingsParams.bannerShowSleepState)

        }



        checkPermissions()
    }

    //region animation

    lateinit var transitionsContainer : ViewGroup
    lateinit var animatedTopView : MotionLayout

    //endregion
}