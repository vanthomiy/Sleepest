package com.sleepestapp.sleepest.ui.settings

import android.app.Application
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import com.sleepestapp.sleepest.MainApplication
import com.sleepestapp.sleepest.R
import com.sleepestapp.sleepest.model.data.Websites
import com.sleepestapp.sleepest.storage.DataStoreRepository
import com.sleepestapp.sleepest.storage.DatabaseRepository
import com.sleepestapp.sleepest.util.PermissionsUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    //region Init


    private val scope: CoroutineScope = MainScope()
    private val actualContext by lazy { getApplication<Application>().applicationContext }
    public val dataStoreRepository: DataStoreRepository by lazy {
        (actualContext as MainApplication).dataStoreRepository
    }

    public val dataBaseRepository: DatabaseRepository by lazy {
        (actualContext as MainApplication).dataBaseRepository
    }

    //endregion


    //region binding values


    // region Design
    val darkMode = ObservableField(true)

    /**
     * Dark mode toggled handler
     */
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

    /**
     * Auto dark mode toggled handler
     */
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

    /**
     * Banner settings changed by the user
     */
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

    // region About us

    /**
     * About us clicked
     */
    fun onAboutUsClicked(view: View) {
        when (view.tag.toString()) {
            "improvement" -> {
                val intent = Intent(Intent.ACTION_SENDTO)
                intent.data = Uri.parse("mailto: sleepestapp@gmail.com")
                val packageInfo = actualContext.packageManager.getPackageInfo(actualContext.packageName, 0)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(Intent.EXTRA_EMAIL, "Improvement")
                intent.putExtra(Intent.EXTRA_SUBJECT, "Version: " + packageInfo.versionName)

                actualContext.startActivity(intent)

            }
            "rate" -> {
                val uri = Uri.parse("market://details?id=" + actualContext.packageName)
                val myAppLinkToMarket = Intent(Intent.ACTION_VIEW, uri)
                myAppLinkToMarket.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    actualContext.startActivity(myAppLinkToMarket)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(actualContext, " unable to find market app", Toast.LENGTH_LONG).show()
                }
            }
            "error" -> {
                val intent = Intent(Intent.ACTION_SENDTO)
                intent.data = Uri.parse("mailto: sleepestapp@gmail.com")
                val packageInfo = actualContext.packageManager.getPackageInfo(actualContext.packageName, 0)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(Intent.EXTRA_EMAIL, "Error")
                intent.putExtra(Intent.EXTRA_SUBJECT, "Version: " + packageInfo.versionName)

                actualContext.startActivity(intent)

            }
            "PRIVACY_POLICE" -> {
                val websiteUrl = Websites.getWebsite(view.tag as Websites)

                actualContext.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(websiteUrl)))
            }
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



    /**
     * Show permission info for each permission
     */
    fun showPermissionInfo(permission: String){
        TransitionManager.beginDelayedTransition(transitionsContainer);

        activityPermissionDescription.set(if (permission == "sleepActivity") if (activityPermissionDescription.get() != View.VISIBLE) View.VISIBLE else View.GONE else View.GONE)
        dailyPermissionDescription.set(if (permission == "dailyActivity") if (dailyPermissionDescription.get() != View.VISIBLE) View.VISIBLE else View.GONE else View.GONE)
        storagePermissionDescription.set(if (permission == "storage") if (storagePermissionDescription.get() != View.VISIBLE) View.VISIBLE else View.GONE else View.GONE)
        overlayPermissionDescription.set(if (permission == "overlay") if (overlayPermissionDescription.get() != View.VISIBLE) View.VISIBLE else View.GONE else View.GONE)
    }

    /**
     * Check if permissions are granted
     */
    fun checkPermissions(){

        activityPermission.set(
            PermissionsUtil.isActivityRecognitionPermissionGranted(actualContext)
        )

        dailyPermission.set(
            PermissionsUtil.isActivityRecognitionPermissionGranted(actualContext)
        )

        storagePermission.set(
            PermissionsUtil.isNotificationPolicyAccessGranted(actualContext)
        )

        overlayPermission.set(PermissionsUtil.isOverlayPermissionGranted(actualContext))

    }


    // endregion

    // region Data

    val removeButtonText = ObservableField(actualContext.getString(R.string.settings_delete_all_data))

    /**
     * Remove data clicked
     */
    fun onDataClicked(view: View) {
        when(view.tag.toString()){
            "remove" -> {
                TransitionManager.beginDelayedTransition(transitionsContainer);

                removeExpand.set(removeExpand.get() != true)
                removeButtonText.set(if (removeExpand.get() == false)
                    actualContext.getString(R.string.settings_delete_all_data) else actualContext.getString(
                                    R.string.settings_return))
            }
            "removeAckn" -> {

                scope.launch {

                    dataBaseRepository.deleteAllAlarms()
                    dataBaseRepository.deleteActivityApiRawData()
                    dataBaseRepository.deleteSleepApiRawData()
                    dataBaseRepository.deleteAllUserSleepSessions()

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

    /**
     * Expand a topic is clicked
     */
    fun onExpandClicked(view: View) {
        updateExpandChanged(view.tag.toString(), true)
    }

    /**
     * Expand the actual topic and hide all other topics
     */
    private fun updateExpandChanged(value: String, toggle: Boolean = false) {

        TransitionManager.beginDelayedTransition(transitionsContainer);

        actualExpand.set(if (actualExpand.get() == value.toIntOrNull()) -1 else value.toIntOrNull())
        removeExpand.set(if (actualExpand.get() == 4) removeExpand.get() else false)

    }

    //endregion

    init {

        /**
         * Loads all the init values from the datastore and passes the values to the bindings
         */
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