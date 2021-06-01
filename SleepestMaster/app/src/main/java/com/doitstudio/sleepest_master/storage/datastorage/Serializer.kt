package com.doitstudio.sleepest_master.storage.datastorage

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.doitstudio.sleepest_master.*
import com.doitstudio.sleepest_master.model.data.MobilePosition
import com.doitstudio.sleepest_master.model.data.MobileUseFrequency
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

class SleepApiDataSerializer() : Serializer<SleepApiData> {

    override fun readFrom(input: InputStream): SleepApiData {
        try {
            return SleepApiData.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override fun writeTo(t: SleepApiData, output: OutputStream) {
        t.writeTo(output)
    }

    override val defaultValue: SleepApiData = SleepApiData.getDefaultInstance()
}


class ActivityApiDataSerializer() : Serializer<ActivityApiData> {

    override fun readFrom(input: InputStream): ActivityApiData {
        try {
            return ActivityApiData.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override fun writeTo(t: ActivityApiData, output: OutputStream) {
        t.writeTo(output)
    }

    override val defaultValue: ActivityApiData = ActivityApiData.getDefaultInstance()
}


class LiveUserSleepActivitySerializer() : Serializer<LiveUserSleepActivity> {

    override fun readFrom(input: InputStream): LiveUserSleepActivity {
        try {
            return LiveUserSleepActivity.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override fun writeTo(t: LiveUserSleepActivity, output: OutputStream) {
        t.writeTo(output)
    }

    override val defaultValue: LiveUserSleepActivity = LiveUserSleepActivity.getDefaultInstance()
}

class SleepParameterSerializer() : Serializer<SleepParameters> {

    override fun readFrom(input: InputStream): SleepParameters {
        try {
            return SleepParameters.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override fun writeTo(t: SleepParameters, output: OutputStream) {
        t.writeTo(output)
    }

    override val defaultValue: SleepParameters = SleepParameters.newBuilder()
        .setStandardMobilePosition(MobilePosition.UNIDENTIFIED.ordinal)
        .setMobileUseFrequency(MobileUseFrequency.getValue(MobileUseFrequency.NONE))
        .setNormalSleepTime(32400)
        .setSleepTimeStart(80000)
        .setSleepTimeEnd(40000)
        .build()
}

class BackgroundServiceSerializer() : Serializer<BackgroundService> {

    override fun readFrom(input: InputStream): BackgroundService {
        try {
            return BackgroundService.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override fun writeTo(t: BackgroundService, output: OutputStream) {
        t.writeTo(output)
    }

    override val defaultValue: BackgroundService = BackgroundService.newBuilder()
        .setIsForegroundActive(false)
        .setIsBackgroundActive(false)
        .build()
}