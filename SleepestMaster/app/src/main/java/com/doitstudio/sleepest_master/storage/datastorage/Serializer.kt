package com.doitstudio.sleepest_master.storage.datastorage

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.doitstudio.sleepest_master.LiveUserSleepActivity
import com.doitstudio.sleepest_master.SleepApiData
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

/*
class AlarmSerializer() : Serializer<Alarm> {

    override fun readFrom(input: InputStream): Alarm {
        try {
            return Alarm.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override fun writeTo(t: Alarm, output: OutputStream) {
        t.writeTo(output)
    }

    override val defaultValue: Alarm = Alarm.newBuilder()
            .setSleepDuration(28800)
            .setWakeupEarly(21600)
            .setWakeupLate(32400)
            .build()
}
 */

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