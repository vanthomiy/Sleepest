package com.doitstudio.sleepest_master.ui.info

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.doitstudio.sleepest_master.databinding.FragmentInfoEntityBinding
import com.doitstudio.sleepest_master.model.data.InfoEntityStlye

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
        viewModel.transitionsContainer = (binding.rLEntityLayer)

        if (infoEntity.textHeader != null) {
            viewModel.headerVisible.set(View.VISIBLE)
            viewModel.textHeader.set(infoEntity.textHeader)
        }

        if (infoEntity.textDescription != null) {
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


