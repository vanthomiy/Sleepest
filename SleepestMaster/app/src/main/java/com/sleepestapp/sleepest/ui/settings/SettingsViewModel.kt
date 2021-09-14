package com.sleepestapp.sleepest.ui.settings
import android.transition.TransitionManager
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sleepestapp.sleepest.storage.DataStoreRepository
import com.sleepestapp.sleepest.storage.DatabaseRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch


class SettingsViewModel(
    val dataStoreRepository: DataStoreRepository,
    val dataBaseRepository: DatabaseRepository
) : ViewModel() {

    //region binding values


    // region Design
    val darkMode = MutableLiveData(true)

    /**
     * Dark mode toggled handler
     */
    fun darkModeToggled(view: View) {
        viewModelScope.launch {
            darkMode.value?.let {
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

    val autoDarkMode = MutableLiveData(true)
    val showDarkModeSetting = MutableLiveData(View.GONE)

    /**
     * Auto dark mode toggled handler
     */
    fun autoDarkModeToggled(view: View) {
        TransitionManager.beginDelayedTransition(transitionsContainer);

        viewModelScope.launch {
            autoDarkMode.value?.let {
                dataStoreRepository.updateAutoDarkMode(it)
                dataStoreRepository.updateAutoDarkModeAckn(true)
            }
        }

        autoDarkMode.value?.let { auto ->
            showDarkModeSetting.value = (if (auto) View.GONE else View.VISIBLE)
            AppCompatDelegate
                    .setDefaultNightMode(if (auto)
                        AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM else
                        darkMode.value.let { mode ->
                            if (mode == true)
                                AppCompatDelegate.MODE_NIGHT_YES else
                                AppCompatDelegate.MODE_NIGHT_NO
                        })

        }
    }


    val showAlarmActiv = MutableLiveData(true)
    val showActualWakeUpPoint = MutableLiveData(true)
    val showActualSleepTime = MutableLiveData(true)
    val showDetailedSleepTime = MutableLiveData(true)
    val showSleepState = MutableLiveData(true)

    /**
     * Banner settings changed by the user
     */
    fun bannerSettingsToggled(view: View) {

        viewModelScope.launch {
            when (view.tag.toString()) {
                "show_alarm_active" -> showAlarmActiv.value
                    ?.let { dataStoreRepository.updateBannerShowAlarmActiv(it) }
                "show_actual_wakeup" -> showActualWakeUpPoint.value
                    ?.let { dataStoreRepository.updateBannerShowActualWakeUpPoint(it) }
                "show_actual_sleep_time" -> showActualSleepTime.value
                    ?.let { dataStoreRepository.updateBannerShowActualSleepTime(it) }
                "show_actual_sleep_state" -> showSleepState.value
                    ?.let { dataStoreRepository.updateBannerShowSleepState(it) }
            }
        }
    }

    // endregion

    // region About us


    val aboutUsSelection = MutableLiveData("")

    /**
     * About us clicked
     */
    fun onAboutUsClicked(view: View) {
        aboutUsSelection.value = view.tag.toString()
    }

    // endregion

    // region Permissions

    val activityPermission = MutableLiveData(false)
    val dailyPermission = MutableLiveData(false)
    val storagePermission = MutableLiveData(false)
    val overlayPermission = MutableLiveData(false)

    val activityPermissionDescription = MutableLiveData(View.GONE)
    val dailyPermissionDescription = MutableLiveData(View.GONE)
    val storagePermissionDescription = MutableLiveData(View.GONE)
    val overlayPermissionDescription = MutableLiveData(View.GONE)



    /**
     * Show permission info for each permission
     */
    fun showPermissionInfo(permission: String){
        TransitionManager.beginDelayedTransition(transitionsContainer);

        activityPermissionDescription.value = (if (permission == "sleepActivity") if (activityPermissionDescription.value != View.VISIBLE) View.VISIBLE else View.GONE else View.GONE)
        dailyPermissionDescription.value = (if (permission == "dailyActivity") if (dailyPermissionDescription.value != View.VISIBLE) View.VISIBLE else View.GONE else View.GONE)
        storagePermissionDescription.value = (if (permission == "storage") if (storagePermissionDescription.value != View.VISIBLE) View.VISIBLE else View.GONE else View.GONE)
        overlayPermissionDescription.value = (if (permission == "overlay") if (overlayPermissionDescription.value != View.VISIBLE) View.VISIBLE else View.GONE else View.GONE)
    }


    // endregion

    // region Data

    var removeTextNormal = ""
    var removeTextSpecific = ""

    val removeButtonText = MutableLiveData(removeTextNormal)

    /**
     * Remove data clicked
     */
    fun onDataClicked(view: View) {
        when(view.tag.toString()){
            "remove" -> {
                TransitionManager.beginDelayedTransition(transitionsContainer);

                removeExpand.value = (removeExpand.value != true)
                removeButtonText.value = (if (removeExpand.value == false)
                    removeTextNormal else removeTextSpecific)
            }
            "removeAckn" -> {

                viewModelScope.launch {

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

    val actualExpand = MutableLiveData(-1)
    val goneState = MutableLiveData(View.GONE)
    val visibleState = MutableLiveData(View.VISIBLE)
    val removeExpand = MutableLiveData(false)

    val normalRotationState = MutableLiveData(0)
    val rotatedState = MutableLiveData(180)

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

        actualExpand.value = (if (actualExpand.value == value.toIntOrNull()) -1 else value.toIntOrNull())
        removeExpand.value = (if (actualExpand.value == 4) removeExpand.value else false)

    }

    //endregion

    init {

        /**
         * Loads all the init values from the datastore and passes the values to the bindings
         */
        viewModelScope.launch {

            var settingsParams = dataStoreRepository.settingsDataFlow.first()
            darkMode.value = (settingsParams.designDarkMode)
            autoDarkMode.value = (settingsParams.designAutoDarkMode)
            showDarkModeSetting.value = (if (settingsParams.designAutoDarkMode) View.GONE else View.VISIBLE)

            showAlarmActiv.value = (settingsParams.bannerShowAlarmActiv)
            showActualWakeUpPoint.value = (settingsParams.bannerShowActualWakeUpPoint)
            showActualSleepTime.value = (settingsParams.bannerShowActualSleepTime)
            showSleepState.value = (settingsParams.bannerShowSleepState)

        }
    }

    //region animation

    lateinit var transitionsContainer : ViewGroup
    lateinit var animatedTopView : MotionLayout

    //endregion
}