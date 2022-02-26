package com.sleepestapp.sleepest.storage.datastorage

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.sleepestapp.sleepest.*
import com.sleepestapp.sleepest.model.data.MobilePosition
import com.sleepestapp.sleepest.model.data.MobileUseFrequency
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

@Suppress("BlockingMethodInNonBlockingContext")
object SleepApiDataSerializer : Serializer<SleepApiData> {

    override suspend fun readFrom(input: InputStream): SleepApiData {
        try {
            return SleepApiData.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(
        t: SleepApiData,
        output: OutputStream
    ): Unit = t.writeTo(output)

    override val defaultValue: SleepApiData = SleepApiData.getDefaultInstance()
}

@Suppress("BlockingMethodInNonBlockingContext")
object ActivityApiDataSerializer : Serializer<ActivityApiData> {

    override suspend fun readFrom(input: InputStream): ActivityApiData {
        try {
            return ActivityApiData.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(
        t: ActivityApiData,
        output: OutputStream
    ): Unit = t.writeTo(output)

    override val defaultValue: ActivityApiData = ActivityApiData.getDefaultInstance()
}

@Suppress("BlockingMethodInNonBlockingContext")
object SettingsDataSerializer : Serializer<SettingsData> {

    override suspend fun readFrom(input: InputStream): SettingsData {
        try {
            return SettingsData.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(
        t: SettingsData,
        output: OutputStream
    ): Unit = t.writeTo(output)

    override val defaultValue: SettingsData = SettingsData.newBuilder()
        .setBannerShowActualSleepTime(true)
        .setBannerShowActualWakeUpPoint(true)
        .setBannerShowSleepState(true)
        .setBannerShowAlarmActiv(true)
        .setDesignAutoDarkMode(true)
        .build()
}


@Suppress("BlockingMethodInNonBlockingContext")
object LiveUserSleepActivitySerializer : Serializer<LiveUserSleepActivity> {

    override suspend fun readFrom(input: InputStream): LiveUserSleepActivity {
        try {
            return LiveUserSleepActivity.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(
        t: LiveUserSleepActivity,
        output: OutputStream
    ): Unit = t.writeTo(output)

    override val defaultValue: LiveUserSleepActivity = LiveUserSleepActivity.getDefaultInstance()
}

@Suppress("BlockingMethodInNonBlockingContext")
object SleepParameterSerializer : Serializer<SleepParameters> {

    override suspend fun readFrom(input: InputStream): SleepParameters {
        try {
            return SleepParameters.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(
        t: SleepParameters,
        output: OutputStream
    ): Unit = t.writeTo(output)

    override val defaultValue: SleepParameters = SleepParameters.newBuilder()
        .setStandardMobilePosition(MobilePosition.UNIDENTIFIED.ordinal)
        .setMobileUseFrequency(MobileUseFrequency.getValue(MobileUseFrequency.NONE))
        .setSleepDuration(32400)
        .setSleepTimeStart(72000)
        .setSleepTimeEnd(36000)
        .setStandardLightCondition(2)
        .setStandardLightConditionOverLastWeek(0)
        .setStandardMobilePosition(2)
        .setStandardMobilePositionOverLastWeek(0)
        .setUserActivityTracking(true)
        .build()
}

@Suppress("BlockingMethodInNonBlockingContext")
object AlarmParameterSerializer : Serializer<AlarmParameters> {

    override suspend fun readFrom(input: InputStream): AlarmParameters {
        try {
            return AlarmParameters.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(
        t: AlarmParameters,
        output: OutputStream
    ): Unit = t.writeTo(output)

    override val defaultValue: AlarmParameters = AlarmParameters.newBuilder()
        .setAlarmTone("null")
        .setAlarmArt(0)
        .build()
}

@Suppress("BlockingMethodInNonBlockingContext")
object BackgroundServiceSerializer : Serializer<BackgroundService> {

    override suspend fun readFrom(input: InputStream): BackgroundService {
        try {
            return BackgroundService.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(
        t: BackgroundService,
        output: OutputStream
    ): Unit = t.writeTo(output)

    override val defaultValue: BackgroundService = BackgroundService.newBuilder()
        .setIsForegroundActive(false)
        .setIsBackgroundActive(false)
        .build()
}

@Suppress("BlockingMethodInNonBlockingContext")
object TutorialStatusSerializer : Serializer<Tutorial> {

    override suspend fun readFrom(input: InputStream): Tutorial {
        try {
            return Tutorial.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(
        t: Tutorial,
        output: OutputStream
    ): Unit = t.writeTo(output)

    override val defaultValue: Tutorial = Tutorial.newBuilder()
        .setEnergyOptionsShown(false)
        .setTutorialCompleted(false)
        .build()
}

@Suppress("BlockingMethodInNonBlockingContext")
object SpotifyStatusSerializer : Serializer<Spotify> {

    override suspend fun readFrom(input: InputStream): Spotify {
        try {
            return Spotify.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(
        t: Spotify,
        output: OutputStream
    ): Unit = t.writeTo(output)

    override val defaultValue: Spotify = Spotify.newBuilder()
        .setSpotifyIsPlaying(false)
        .setSpotifyEnabled(false)
        .setSpotifyConnected(false)
        .build()
}