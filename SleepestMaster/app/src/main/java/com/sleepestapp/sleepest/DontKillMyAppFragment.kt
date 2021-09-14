package com.sleepestapp.sleepest

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import dev.doubledot.doki.views.DokiContentView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.sleepestapp.sleepest.databinding.FragmentDontKillMyAppBinding
import com.sleepestapp.sleepest.databinding.FragmentSleepBinding
import com.sleepestapp.sleepest.storage.DataStoreRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

class DontKillMyAppFragment : DialogFragment() {

    /**
     * Get actual context
     */
    private val actualContext: Context by lazy {
        requireActivity().applicationContext }

    private val dataStoreRepository by lazy {
        (actualContext as MainApplication).dataStoreRepository}

    private lateinit var binding: FragmentDontKillMyAppBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        binding = FragmentDontKillMyAppBinding.inflate(inflater, container, false)

        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dokiCustomView = View.inflate(context, R.layout.fragment_dont_kill_my_app, null)
        dokiCustomView?.findViewById<DokiContentView?>(R.id.doki_content)?.let {
            it.setButtonsVisibility(false)
            it.loadContent()
        }

        binding.tvNoticeDontKillMyApp.text = getString(R.string.notice_dont_kill_my_app)

        binding.tvDescriptionDontKillMyApp.text = getString(R.string.description_dont_kill_my_app)

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