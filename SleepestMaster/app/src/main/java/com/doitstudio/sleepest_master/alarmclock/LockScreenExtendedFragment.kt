package com.doitstudio.sleepest_master.alarmclock


import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.View.TEXT_ALIGNMENT_CENTER
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.doitstudio.sleepest_master.DontKillMyAppFragment
import com.doitstudio.sleepest_master.MainApplication
import com.doitstudio.sleepest_master.R
import com.doitstudio.sleepest_master.databinding.ActivityLockScreenAlarmBinding
import com.doitstudio.sleepest_master.databinding.ActivityLockScreenAlarmExtendedBinding
import com.doitstudio.sleepest_master.databinding.FragmentSettingsBinding
import com.doitstudio.sleepest_master.googleapi.SleepHandler
import com.doitstudio.sleepest_master.model.data.Constants
import com.doitstudio.sleepest_master.model.data.Websites
import com.doitstudio.sleepest_master.model.data.credits.CreditsSites
import com.doitstudio.sleepest_master.model.data.export.ImportUtil
import com.doitstudio.sleepest_master.model.data.export.UserSleepExportData
import com.doitstudio.sleepest_master.storage.DataStoreRepository
import com.doitstudio.sleepest_master.storage.DatabaseRepository
import com.doitstudio.sleepest_master.util.IconAnimatorUtil.isDarkThemeOn
import com.doitstudio.sleepest_master.util.SmileySelectorUtil
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.*


class LockScreenExtendedFragment : Fragment() {

    private lateinit var binding: ActivityLockScreenAlarmExtendedBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = ActivityLockScreenAlarmExtendedBinding.inflate(inflater, container, false)

        return binding.root

    }
}


