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
    private var direction: Int? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (infoId == null) {
            infoId = arguments?.getInt("infoId")
        }

        if (info == null) {
            info = arguments?.getInt("info")
        }

        if (direction == null) {
            direction = arguments?.getInt("direction")
        }
    }

    override fun onInflate(context: Context, attrs: AttributeSet, savedInstanceState: Bundle?) {
        super.onInflate(context, attrs, savedInstanceState)
        if (context != null && attrs != null && infoId == null && info == null) {
            val styledAttributes = context.obtainStyledAttributes(attrs, R.styleable.InfoFragment_MembersInjector)
            infoId = styledAttributes.getInt(R.styleable.InfoFragment_MembersInjector_infoId, 0)
            info = styledAttributes.getInt(R.styleable.InfoFragment_MembersInjector_info, 0)
            direction = styledAttributes.getInt(R.styleable.InfoFragment_MembersInjector_direction, 0)
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

        var actualStlye = direction?.let { InfoEntityStlye.getById(it) }

        infoEntity?.forEach{ info ->
            val transactions = childFragmentManager.beginTransaction()
            //transactions.setCustomAnimations(R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit);

            val infoFragment = actualStlye?.let { InfoEntityFragment(view?.context, info, it) }
            if (infoFragment != null) {
                transactions?.add(binding.lLInfo.id, infoFragment)?.commit()
            }

            actualStlye = if(info.textHeader == null)
            {
                if (actualStlye == InfoEntityStlye.PICTURE_LEFT) InfoEntityStlye.PICTURE_RIGHT else
                    InfoEntityStlye.PICTURE_LEFT
            }
            else{
                if (actualStlye == InfoEntityStlye.PICTURE_BOTTOM) InfoEntityStlye.PICTURE_TOP else
                    InfoEntityStlye.PICTURE_BOTTOM
            }
        }

    }

}


