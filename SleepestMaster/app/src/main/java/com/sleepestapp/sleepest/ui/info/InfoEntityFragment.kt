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

class InfoEntityFragment(val applicationContext: Context, private val infoEntity: InfoEntity, private val style: InfoEntityStlye) : Fragment() {

    private lateinit var binding: FragmentInfoEntityBinding
    private val viewModel by lazy { ViewModelProvider(this).get(InfoEntityViewModel::class.java) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentInfoEntityBinding.inflate(inflater, container, false)
        binding.infoViewModel = viewModel

        if(style == InfoEntityStlye.PICTURE_LEFT || style == InfoEntityStlye.PICTURE_TOP){
            viewModel.layoutFormat.set(
                LayoutDirection.LTR)
        }
        else if(style == InfoEntityStlye.PICTURE_RIGHT || style == InfoEntityStlye.PICTURE_BOTTOM) {
            viewModel.layoutFormat.set(
                LayoutDirection.RTL)
        }

        if(style == InfoEntityStlye.PICTURE_LEFT || style == InfoEntityStlye.PICTURE_RIGHT){
            viewModel.orientation.set(
                LinearLayout.HORIZONTAL)
        }
        else if(style == InfoEntityStlye.PICTURE_BOTTOM || style == InfoEntityStlye.PICTURE_TOP) {
            viewModel.orientation.set(
                LinearLayout.VERTICAL)
        }

        if (infoEntity.textHeader != null && infoEntity.textHeader != "") {
            viewModel.headerVisible.set(View.VISIBLE)
            viewModel.textHeader.set(infoEntity.textHeader)
        }

        if (infoEntity.textDescription != null && infoEntity.textDescription != "") {
            viewModel.descrriptionVisible.set(View.VISIBLE)
            viewModel.textDescription.set(infoEntity.textDescription)
        }

        if (infoEntity.lottie != null) {
            viewModel.lottieVisible.set(View.VISIBLE)
            binding.lottieAnim.setAnimation(infoEntity.lottie)
        }

        if (infoEntity.image != null) {
            viewModel.imageVisible.set(View.VISIBLE)
            binding.imageView.setImageDrawable(resources.getDrawable(infoEntity.image))
        }


        return binding.root
    }

}


