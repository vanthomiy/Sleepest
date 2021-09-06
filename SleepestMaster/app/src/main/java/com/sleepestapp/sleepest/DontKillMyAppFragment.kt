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

class DontKillMyAppFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dokiCustomView = View.inflate(context,
            com.sleepestapp.sleepest.R.layout.fragment_dont_kill_my_app, null)
        dokiCustomView?.findViewById<DokiContentView?>(com.sleepestapp.sleepest.R.id.doki_content)?.let {
            it.setButtonsVisibility(false)
            it.loadContent()
        }

        val tvNoticeDontKillMyApp : TextView = dokiCustomView.findViewById(com.sleepestapp.sleepest.R.id.tvNoticeDontKillMyApp)
        tvNoticeDontKillMyApp.setText(getString(com.sleepestapp.sleepest.R.string.notice_dont_kill_my_app))


        val tvDescriptionDontKillMyApp : TextView = dokiCustomView.findViewById(com.sleepestapp.sleepest.R.id.tvDescriptionDontKillMyApp)
        tvDescriptionDontKillMyApp.setText(getString(com.sleepestapp.sleepest.R.string.description_dont_kill_my_app))

        return MaterialDialog(requireContext()).show {
            customView(view = dokiCustomView)
            positiveButton(com.sleepestapp.sleepest.R.string.doki_close) {
                dismiss()
            }
        }
    }

    fun show(context: FragmentActivity, tag: String = com.sleepestapp.sleepest.DontKillMyAppFragment.Companion.DOKI_DIALOG_TAG) {
        show(context.supportFragmentManager, tag)
    }

    companion object {
        private const val DOKI_DIALOG_TAG = "doki_dialog"
        fun show(context: FragmentActivity, tag: String = com.sleepestapp.sleepest.DontKillMyAppFragment.Companion.DOKI_DIALOG_TAG) {
            com.sleepestapp.sleepest.DontKillMyAppFragment()
                .show(context, tag)
        }
    }
}