package com.doitstudio.sleepest_master.ui.profile

import android.app.Application
import android.transition.TransitionManager
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.appcompat.widget.AppCompatImageView
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.core.widget.NestedScrollView
import androidx.databinding.ObservableArrayList
import androidx.databinding.ObservableField
import androidx.lifecycle.AndroidViewModel
import com.doitstudio.sleepest_master.MainApplication
import com.doitstudio.sleepest_master.storage.DataStoreRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalTime
import kotlin.math.abs

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private fun getStringXml(id: Int): String {
        return getApplication<Application>().resources.getString(id)
    }

    //region binding values


    private val scope: CoroutineScope = MainScope()
    private val context by lazy { getApplication<Application>().applicationContext }
    private val dataStoreRepository: DataStoreRepository by lazy {
        (context as MainApplication).dataStoreRepository
    }

    fun onAppStatusClick(view: View) {

    }

    fun onAppFunctionClick(view: View) {

    }

    fun onReportProblemClick(view: View) {

    }

    fun onRateAppClick(view: View) {

    }

    // region Design
    val darkMode = ObservableField(true)
    fun darkModeToggled(view: View) {
        scope.launch {
            darkMode.get()?.let {
                dataStoreRepository.updateAutoSleepTime(it)
                //darkMode.set(!it)
            }
        }
    }

    val languageSelections = ObservableArrayList<String>()
    val selectedLanguage = ObservableField(0)
    fun onLanguageChanged(
            parent: AdapterView<*>?,
            selectedItemView: View,
            language: Int,
            id: Long
    ){
        scope.launch {
            dataStoreRepository.updateStandardMobilePosition(language)
        }

    }

    // endregion

    // region Help

    fun onHelpClicked(view: View) {
        when(view.tag.toString()){
            "tutorial" -> "asd"
            "importantSettings" -> "asd"
        }
    }

    // endregion

    // region About us

    fun onAboutUsClicked(view: View) {
        updateExpandChanged(view.tag.toString(), true)
        when(view.tag.toString()){
            "improvement" -> "asd"
            "rate" -> "asd"
            "error" -> "asd"
        }
    }

    // endregion

    // region Permissions

    val activityPermission = ObservableField(false)
    val alarmPermission = ObservableField(false)
    val storagePermission = ObservableField(false)

    fun onPermissionClicked(view: View) {
        updateExpandChanged(view.tag.toString(), true)
        when(view.tag.toString()){
            "dailyActivity" -> "asd"
            "sleepActivity" -> "asd"
            "storage" -> "asd"
        }
    }

    // endregion

    // region Data

    fun onDataClicked(view: View) {
        updateExpandChanged(view.tag.toString(), true)
        when(view.tag.toString()){
            "export" -> "asd"
            "remove" -> "asd"
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
            var sleepParams = dataStoreRepository.sleepParameterFlow.first()
            val time = LocalTime.ofSecondOfDay(sleepParams.normalSleepTime.toLong())

            languageSelections.addAll(arrayListOf<String>("Deutsch", "Englisch"))
        }
    }

    //region animation

    lateinit var transitionsContainer : ViewGroup
    lateinit var transitionsContainerTop : ViewGroup
    lateinit var animatedTopView : MotionLayout
    lateinit var imageMoonView : AppCompatImageView

    fun onShowTips(view: View){
        animateTop(true)

    }

    var lastScroll = 0
    var lastScrollDelta = 0
    var progress = 0f
    var newProgress = 0f
    fun onScrollChanged(v: NestedScrollView, l: Int, t: Int, oldl: Int, oldt: Int) {
        //Log.d(TAG, "scroll changed: " + this.getTop() + " "+t);
        val scrollY: Int = v.scrollY // For ScrollView hprizontal use getScrollX()
        val b = l
        val c = t
        val d  = oldl
        //TransitionManager.beginDelayedTransition(transitionsContainerTop);

        newProgress = (1f / 500f) * scrollY
        animatedTopView.progress = newProgress

        if(abs(progress - newProgress) > 0.25 ) {
            progress = newProgress
        }

        lastScroll = scrollY
    }

    var lastMotionEvent : Int = MotionEvent.ACTION_UP


    val pictureScale = ObservableField(1.0f)


    private fun animateTop(expand: Boolean){


        if(expand)
        {
            pictureScale.set(0.25f)
        }
        else
        {
            pictureScale.set(1f)
        }
    }


    //endregion
}