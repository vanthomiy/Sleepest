package com.sleepestapp.sleepest.ui.info

import android.content.Context
import android.os.Bundle
import android.util.LayoutDirection
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.sleepestapp.sleepest.databinding.FragmentInfoEntityBinding
import com.sleepestapp.sleepest.model.data.InfoEntityStlye
import com.sleepestapp.sleepest.ui.sleep.SleepFragment

/**
 * An actual Information entity which is child of the [InfoFragment]
 */
class InfoEntityFragment(val applicationContext: Context, private val infoEntity: InfoEntity, private val style: InfoEntityStlye) : Fragment() {

    /**
     * Binding XML Code to Fragment
     */
    private lateinit var binding: FragmentInfoEntityBinding

    /**
     * View model of the [SleepFragment]
     */
    private val viewModel by lazy { ViewModelProvider(this).get(InfoEntityViewModel::class.java) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentInfoEntityBinding.inflate(inflater, container, false)
        binding.infoViewModel = viewModel
        binding.lifecycleOwner = this;

        // Setup the layout for the actual Info Entity
        if(style == InfoEntityStlye.PICTURE_LEFT || style == InfoEntityStlye.PICTURE_TOP){
            viewModel.layoutFormat.value = (
                LayoutDirection.LTR)
        }
        else if(style == InfoEntityStlye.PICTURE_RIGHT || style == InfoEntityStlye.PICTURE_BOTTOM) {
            viewModel.layoutFormat.value = (
                LayoutDirection.RTL)
        }

        if(style == InfoEntityStlye.PICTURE_LEFT || style == InfoEntityStlye.PICTURE_RIGHT){
            viewModel.orientation.value = (
                LinearLayout.HORIZONTAL)
        }
        else if(style == InfoEntityStlye.PICTURE_BOTTOM || style == InfoEntityStlye.PICTURE_TOP) {
            viewModel.orientation.value = (
                LinearLayout.VERTICAL)
        }

        if (infoEntity.textHeader != null && infoEntity.textHeader != "") {
            viewModel.headerVisible.value = (View.VISIBLE)
            viewModel.textHeader.value = (infoEntity.textHeader)
        }

        if (infoEntity.textDescription != null && infoEntity.textDescription != "") {
            viewModel.descrriptionVisible.value = (View.VISIBLE)
            viewModel.textDescription.value = (infoEntity.textDescription)
        }

        if (infoEntity.lottie != null) {
            viewModel.lottieVisible.value = (View.VISIBLE)
            binding.lottieAnim.setAnimation(infoEntity.lottie)
        }

        if (infoEntity.image != null) {
            viewModel.imageVisible.value = (View.VISIBLE)
            binding.imageView.setImageDrawable(resources.getDrawable(infoEntity.image))
        }


        return binding.root
    }

}


