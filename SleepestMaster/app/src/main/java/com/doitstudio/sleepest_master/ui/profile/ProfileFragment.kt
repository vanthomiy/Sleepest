package com.doitstudio.sleepest_master.ui.profile

import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.doitstudio.sleepest_master.MainApplication
import com.doitstudio.sleepest_master.R
import com.doitstudio.sleepest_master.alarmclock.AlarmClockAudio
import com.doitstudio.sleepest_master.alarmclock.AlarmClockReceiver
import com.doitstudio.sleepest_master.sleepcalculation.SleepCalculationHandler
import com.kevalpatel.ringtonepicker.RingtonePickerDialog
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*


class ProfileFragment : Fragment() {

    private lateinit var profileViewModel: ProfileViewModel
    private val actualContext: Context by lazy {requireActivity().applicationContext}

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        profileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_profile, container, false)

        //region Test

        var pref = actualContext.getSharedPreferences("AlarmChanged", 0)
        val textAlarm = """
            Last Alarm changed: ${pref.getInt("hour", 0)}:${pref.getInt("minute", 0)},${pref.getInt(
            "actualWakeup",
            0
        )},${pref.getInt("alarmUse", 0)}
            
            """.trimIndent()
        pref = actualContext.getSharedPreferences("StartService", 0)
        val textStartService = """
            Last service start: ${pref.getInt("hour", 0)}:${pref.getInt("minute", 0)}
            
            """.trimIndent()
        pref = actualContext.getSharedPreferences("StopService", 0)
        val textStopService = """
            Last service stop: ${pref.getInt("hour", 0)}:${pref.getInt("minute", 0)}
            
            """.trimIndent()
        pref = actualContext.getSharedPreferences("Workmanager", 0)
        val textLastWorkmanager = """
            Last workmanager call: ${pref.getInt("hour", 0)}:${pref.getInt("minute", 0)}
            
            """.trimIndent()
        pref = actualContext.getSharedPreferences("WorkmanagerCalculation", 0)
        val textLastWorkmanagerCalculation = """
            Last workmanagerCalc call: ${pref.getInt("hour", 0)}:${pref.getInt("minute", 0)}
            
            """.trimIndent()
        pref = actualContext.getSharedPreferences("AlarmClock", 0)
        val textCalc1 = """
            Alarmclock: ${pref.getInt("hour", 0)}:${pref.getInt("minute", 0)}
            
            """.trimIndent()
        pref = actualContext.getSharedPreferences("AlarmSet", 0)
        val textCalc2 = """
            AlarmSet: ${pref.getInt("hour", 0)}:${pref.getInt("minute", 0)},${pref.getInt(
            "hour1",
            0
        )}:${pref.getInt("minute1", 0)},${
            pref.getInt("actualWakeup", 0)}
            
            """.trimIndent()
        pref = actualContext.getSharedPreferences("AlarmReceiver", 0)
        val textAlarmReceiver = """
            AlarmReceiver: ${pref.getInt("hour", 0)}:${pref.getInt("minute", 0)},${pref.getInt(
            "intent",
            0
        )}
            
            """.trimIndent()
        pref = actualContext.getSharedPreferences("SleepTime", 0)
        val textSleepTime= """
            SleepTime: ${pref.getInt("sleeptime", 0)}
            
            """.trimIndent()
        pref = actualContext.getSharedPreferences("StopException", 0)
        val textStopException = """
            Exc.: ${pref.getString("exception", "XX")}
            
            """.trimIndent()
        pref = actualContext.getSharedPreferences("AlarmReceiver1", 0)
        val textAlarmReceiver1 = """
            AlarmReceiver1: ${pref.getString("usage", "XX")},${pref.getInt("day", 0)},${pref.getInt(
            "hour",
            0
        )},${pref.getInt("minute", 0)}
            
            """.trimIndent()

        var textGesamt = textAlarm + textStartService + textStopService + textLastWorkmanager + textLastWorkmanagerCalculation + textCalc1 + textCalc2 + textAlarmReceiver + textSleepTime + textStopException + textAlarmReceiver1



        val text: TextView = root.findViewById(R.id.text_dashboard)
        text.setText(textGesamt)

        val btn : Button = root.findViewById(R.id.btnStartForegroundTest)
        btn.setOnClickListener() {
            //startOrStopForegroundService(Actions.START, actualContext);
            /*val startForegroundIntent = Intent(context, ForegroundActivity::class.java)
            startForegroundIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startForegroundIntent.putExtra("intent", 1)
            startActivity(startForegroundIntent)*/

            //selectRingTone()
            //val calendar = Calendar.getInstance()
            export()

            /*AlarmClockReceiver.startAlarmManager(
                calendar.get(Calendar.DAY_OF_WEEK), calendar.get(
                    Calendar.HOUR_OF_DAY
                ), calendar.get(Calendar.MINUTE) + 2, actualContext, 1
            )*/

            //val calendar = Calendar.getInstance()
        

            //AlarmClockReceiver.startAlarmManager(calendar.get(Calendar.DAY_OF_WEEK), calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE) + 2, actualContext, 1)

            //Toast.makeText(actualContext, "Gut gemacht, die App wird jetzt zerstört", Toast.LENGTH_LONG).show()
        }

        //endregion

        return root
    }

    private fun selectRingTone() {

        //check if audio volume is 0
        val audioManager = actualContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        if (audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) <= 0) {
            Toast.makeText(actualContext, "Increase volume to hear sounds", Toast.LENGTH_LONG).show()
        }

        val ringtonePickerBuilder = RingtonePickerDialog.Builder(actualContext, parentFragmentManager)
            .setTitle("Select your ringtone")
            .displayDefaultRingtone(true)
            .setPositiveButtonText("Set")
            .setCancelButtonText("Cancel")
            .setPlaySampleWhileSelection(true)
            .setListener { ringtoneName, ringtoneUri -> Toast.makeText(actualContext, ringtoneUri.toString(), Toast.LENGTH_LONG).show()}

        ringtonePickerBuilder.addRingtoneType(RingtonePickerDialog.Builder.TYPE_ALARM)
        ringtonePickerBuilder.show()
    }


    // region export

    var sleepOutClassifyOutputExportBed = ""

    private val repository by lazy { (actualContext as MainApplication).dataBaseRepository }

    private fun dataPrep() = runBlocking{
        val data = repository.allSleepApiRawData.first()
        data.forEach {

            val time = it.timestampSeconds.toLong() * 1000; // wokraround to change format

            val instantNow = Instant.now()
            val date = millisToDateTime(time)
            sleepOutClassifyOutputExportBed += "${date.toLocalDate()};${date.hour}:${date.minute};${it.confidence};${(it.light)};${(it.motion)};0\n"
            // Just display values that are shorter than 24Hours away
        }
    }

    fun export(){

        val handler = SleepCalculationHandler(actualContext)

        handler.defineUserWakeup()

        /*dataPrep()

        var switchExportFile =  "Datum;Uhrzeit;Schlaf;Licht;Bewegung;Wahre Zeiten"

        val splitOut  = sleepOutClassifyOutputExportBed.split("\n")

        splitOut.reversed().forEach {
            switchExportFile += "${it}\n";
        }

        val shareIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, switchExportFile)
            type = "text/csv"
        }

        startActivity(Intent.createChooser(shareIntent, "Export data"))*/
    }

    private fun millisToStringDateTime(millis: Long) : String {
        // define once somewhere in order to reuse it
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        // JVM representation of a millisecond epoch absolute instant
        val instant = Instant.ofEpochMilli(millis)

        // Adding the timezone information to be able to format it (change accordingly)
        val date = LocalDateTime.ofInstant(instant, ZoneId.systemDefault())//.plusHours(1) //reudiger workaround haha
        return formatter.format(date) // 10/12/2019 06:35:45
    }

    private fun millisToDateTime(millis: Long) : LocalDateTime {
        // define once somewhere in order to reuse it
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        // JVM representation of a millisecond epoch absolute instant
        val instant = Instant.ofEpochMilli(millis)

        // Adding the timezone information to be able to format it (change accordingly)
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault())//.plusHours(1) //reudiger workaround haha
    }

    //endregion

}


