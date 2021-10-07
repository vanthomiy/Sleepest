package com.sleepestapp.sleepest

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import dev.doubledot.doki.views.DokiContentView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView

class DontKillMyAppFragment : DialogFragment() {

    /**
     * Get actual context
     */
    private val actualContext: Context by lazy {
        requireActivity().applicationContext }

    /**
     * Get repo of datastore
     */
    private val dataStoreRepository by lazy {
        (actualContext as MainApplication).dataStoreRepository}

    /**
     * Initiate the dialog
     */
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dokiCustomView = View.inflate(context, R.layout.fragment_dont_kill_my_app, null)
        dokiCustomView?.findViewById<DokiContentView?>(R.id.doki_content)?.let {
            it.setButtonsVisibility(false)
            it.loadContent()
        }
        //Set the topic text
        val tvNoticeDontKillMyApp : TextView = dokiCustomView.findViewById(R.id.tvNoticeDontKillMyApp)
        tvNoticeDontKillMyApp.text = getString(R.string.notice_dont_kill_my_app)

        //Set the text for the user to let him know what to do
        val tvDescriptionDontKillMyApp : TextView = dokiCustomView.findViewById(R.id.tvDescriptionDontKillMyApp)
        tvDescriptionDontKillMyApp.text = getString(R.string.description_dont_kill_my_app)

        //return a new dialog window
        return MaterialDialog(requireContext()).show {
            customView(view = dokiCustomView)

            //Set the close button
            positiveButton(R.string.doki_close) {
                dataStoreRepository.updateEnergyOptionsShownJob(true)
                dismiss()
            }
        }
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