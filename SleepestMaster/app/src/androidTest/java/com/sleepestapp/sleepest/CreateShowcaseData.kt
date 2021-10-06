package com.sleepestapp.sleepest

import android.content.Context
import androidx.test.platform.app.InstrumentationRegistry
import com.sleepestapp.sleepest.model.data.ActivityOnDay
import com.sleepestapp.sleepest.model.data.LightConditions
import com.sleepestapp.sleepest.model.data.MobilePosition
import com.sleepestapp.sleepest.model.data.SleepState
import com.sleepestapp.sleepest.sleepcalculation.model.SleepTimes
import com.sleepestapp.sleepest.storage.DataStoreRepository
import com.sleepestapp.sleepest.storage.DatabaseRepository
import com.sleepestapp.sleepest.storage.db.SleepApiRawDataEntity
import com.sleepestapp.sleepest.storage.db.SleepDatabase
import com.sleepestapp.sleepest.storage.db.UserSleepSessionEntity
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.util.stream.IntStream.range

class CreateShowcaseData {

    private lateinit var context: Context

    private lateinit var sleepDatabaseRepository: DatabaseRepository

    private val dbDatabase by lazy {
        SleepDatabase.getDatabase(context)
    }

    private val sleepStoreRepository by lazy {
        DataStoreRepository.getRepo(context)
    }

    @Before
    fun init() {
        context = InstrumentationRegistry.getInstrumentation().targetContext


        sleepDatabaseRepository = DatabaseRepository.getRepo(
            dbDatabase.sleepApiRawDataDao(),
            dbDatabase.userSleepSessionDao(),
            dbDatabase.alarmDao(),
            dbDatabase.activityApiRawDataDao()
        )
    }

    /**
     * Create showcase data for x months and by different positions and light conditions
     */
    @Test
    fun createShowCaseData() = runBlocking {

        // remove all old sleep data and sessions
        sleepDatabaseRepository.deleteAllUserSleepSessions()
        sleepDatabaseRepository.deleteSleepApiRawData()

        val actualDate = LocalDate.now().minusDays(1)
        val sessionsToFill = 3 * 30 // 3 months
        val sleepFailsPercentage = 10 // 10 out of 100

        val sleepSessionMinAmount = 20
        val sleepSessionMaxAmount = 40

        val sleepStartTimeEarly = LocalTime.of(20,0)
        val sleepStartTimeLate = LocalTime.of(23,30)

        val sleepDurationMax = 9 * 60 // min
        val sleepDurationMin = 5 * 60 // min

        val positionInBedPercentage = 75 // 80 out of 100 in bed
        val awakeInSleepTime = 10 // 10 out of 100 is awake
        val lightSleepPhaseWeight = 10 // weight out of all
        val deepSleepPhaseWeight = 4 // weight out of all
        val remSleepPhaseWeight = 2 // weight out of all

        for (i in 0..sessionsToFill) {

            val usedDate = actualDate.minusDays(i.toLong())

            // for the session
            val isSleepFailure = (0..100).random() < sleepFailsPercentage
            val sleepSessionAmount = (sleepSessionMinAmount..sleepSessionMaxAmount).random()
            val sleepStartTime = LocalTime.ofSecondOfDay((sleepStartTimeEarly.toSecondOfDay()..sleepStartTimeLate.toSecondOfDay()).random()
                .toLong())
            val actualDateTime = usedDate.atTime(sleepStartTime)
            val actualTimeStamp = actualDateTime.atZone(ZoneOffset.systemDefault()).toEpochSecond().toInt()
            val sleepDuration = (sleepDurationMin..sleepDurationMax).random()
            val inBed = if ((0..100).random() < positionInBedPercentage)
                MobilePosition.INBED
            else
                MobilePosition.ONTABLE

            val sleepDataList = createSleepApiRawData(
                actualTimeStamp,
                sleepSessionAmount,
                sleepDuration,
                awakeInSleepTime,
                inBed,
                lightSleepPhaseWeight,
                deepSleepPhaseWeight,
                remSleepPhaseWeight
            )

            sleepDatabaseRepository.insertSleepApiRawData(sleepDataList)

            val id = UserSleepSessionEntity.getIdByDateTimeWithTimeZoneLive(
                sleepStoreRepository,
                actualDateTime
            )
            val userSleepSession = sleepDatabaseRepository.getOrCreateSleepSessionById(id)

            userSleepSession.lightConditions = LightConditions.LIGHT
            userSleepSession.mobilePosition = inBed
            userSleepSession.sleepTimes.possibleSleepTimeStart = sleepStoreRepository.getSleepTimeStart()
            userSleepSession.sleepTimes.possibleSleepTimeEnd = sleepStoreRepository.getSleepTimeEnd()
            userSleepSession.sleepTimes.sleepTimeStart = actualTimeStamp
            userSleepSession.sleepTimes.sleepTimeEnd = actualTimeStamp + (sleepDuration * 60)
            userSleepSession.sleepTimes.sleepDuration = SleepApiRawDataEntity.getSleepTime(sleepDataList)
            userSleepSession.sleepTimes.awakeTime = SleepApiRawDataEntity.getAwakeTime(sleepDataList)
            userSleepSession.sleepTimes.lightSleepDuration = SleepApiRawDataEntity.getSleepTimeByState(sleepDataList, SleepState.LIGHT)
            userSleepSession.sleepTimes.deepSleepDuration = SleepApiRawDataEntity.getSleepTimeByState(sleepDataList, SleepState.DEEP)
            userSleepSession.sleepTimes.remSleepDuration = SleepApiRawDataEntity.getSleepTimeByState(sleepDataList, SleepState.REM)

            userSleepSession.userSleepRating.activityOnDay = ActivityOnDay.values()[(0..5).random().toInt()]

            sleepDatabaseRepository.insertUserSleepSession(userSleepSession)

        }
    }

    private fun createSleepApiRawData(startTimeStamp:Int, count:Int, duration:Int, awakeInSleepTime:Int,
                                      mobilePosition: MobilePosition, light:Int = 0,
                                      deep:Int = 0, rem:Int = 0) : List<SleepApiRawDataEntity> {
        val dataPoints = mutableListOf<SleepApiRawDataEntity>()

        for (i in 0..duration step duration / count) {

            val isAwake = (0..100).random() < awakeInSleepTime

            val sleepState = when {
                isAwake -> SleepState.AWAKE
                mobilePosition == MobilePosition.ONTABLE -> SleepState.SLEEPING
                else -> {
                    val sleepStateNumber = (0..10).random()

                    when {
                        sleepStateNumber < rem -> SleepState.REM
                        sleepStateNumber < deep -> SleepState.DEEP
                        else -> SleepState.LIGHT
                    }
                }
            }

            val data = SleepApiRawDataEntity(
                startTimeStamp + (i * 60),
                10,
                1,
                1,
                sleepState,
                sleepState,
            )

            dataPoints.add(data)
        }

        return dataPoints
    }
}