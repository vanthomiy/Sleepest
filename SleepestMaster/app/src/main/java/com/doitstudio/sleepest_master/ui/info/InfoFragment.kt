package com.doitstudio.sleepest_master.ui.info

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.doitstudio.sleepest_master.R
import com.doitstudio.sleepest_master.databinding.AlarmEntityBinding
import com.doitstudio.sleepest_master.databinding.FragmentInfoBinding
import com.doitstudio.sleepest_master.model.data.Info
import com.doitstudio.sleepest_master.model.data.InfoEntityStlye

class InfoFragment : Fragment() {

    private lateinit var binding: FragmentInfoBinding

    // your fragment parameter, a string
    private var infoId: Int? = null
    private var info: Int? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (infoId == null) {
            infoId = arguments?.getInt("infoId")
        }

        if (info == null) {
            info = arguments?.getInt("info")
        }
    }

    override fun onInflate(context: Context, attrs: AttributeSet, savedInstanceState: Bundle?) {
        super.onInflate(context, attrs, savedInstanceState)
        if (context != null && attrs != null && infoId == null && info == null) {
            val styledAttributes = context.obtainStyledAttributes(attrs, R.styleable.InfoFragment_MembersInjector)
            infoId = styledAttributes.getInt(R.styleable.InfoFragment_MembersInjector_infoId, 0)
            info = styledAttributes.getInt(R.styleable.InfoFragment_MembersInjector_info, 0)
            styledAttributes.recycle()
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentInfoBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val infoEntity = infoId?.let { info?.let { Info.getById(it) }?.let { it1 ->
            InfoEntity.getInfo(
                it1, it)
        } }

        var actualStlye = InfoEntityStlye.TYPE1

        infoEntity?.forEach{ info ->
            val transactions = childFragmentManager.beginTransaction()
            //transactions.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);

            val infoFragment = InfoEntityFragment(view?.context, info, actualStlye)
            transactions?.add(binding.lLInfo.id, infoFragment)?.commit()

            actualStlye = if(info.textHeader == null)
            {
                if (actualStlye == InfoEntityStlye.TYPE1) InfoEntityStlye.TYPE2 else
                    InfoEntityStlye.TYPE1
            }
            else{
                if (actualStlye == InfoEntityStlye.TYPE3) InfoEntityStlye.TYPE4 else
                    InfoEntityStlye.TYPE3
            }
        }

    }

}


