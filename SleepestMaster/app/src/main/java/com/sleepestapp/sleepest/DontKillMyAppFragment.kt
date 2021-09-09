package com.sleepestapp.sleepest

import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import dev.doubledot.doki.views.DokiContentView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.sleepestapp.sleepest.storage.DataStoreRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

class DontKillMyAppFragment : DialogFragment() {

    private lateinit var dataStoreRepository : DataStoreRepository

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dokiCustomView = View.inflate(context, R.layout.fragment_dont_kill_my_app, null)
        dokiCustomView?.findViewById<DokiContentView?>(R.id.doki_content)?.let {
            it.setButtonsVisibility(false)
            it.loadContent()
        }

        dataStoreRepository = (requireContext().applicationContext as MainApplication).dataStoreRepository



        val tvNoticeDontKillMyApp : TextView = dokiCustomView.findViewById(R.id.tvNoticeDontKillMyApp)
        tvNoticeDontKillMyApp.setText(getString(R.string.notice_dont_kill_my_app))


        val tvDescriptionDontKillMyApp : TextView = dokiCustomView.findViewById(R.id.tvDescriptionDontKillMyApp)
        tvDescriptionDontKillMyApp.setText(getString(R.string.description_dont_kill_my_app))

        return MaterialDialog(requireContext()).show {
            customView(view = dokiCustomView)
            positiveButton(R.string.doki_close) {
                dataStoreRepository.updateEnergyOptionsShownJob(true)
                dismiss()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        //dataStoreRepository.updateEnergyOptionsShownJob(true)
    }

    fun show(context: FragmentActivity, tag: String = DOKI_DIALOG_TAG) {
        show(context.supportFragmentManager, tag)
    }

    companion object {
        private const val DOKI_DIALOG_TAG = "doki_dialog"
        fun show(context: FragmentActivity, tag: String = DOKI_DIALOG_TAG) {
           DontKillMyAppFragment().show(context, tag)
        }
    }
}