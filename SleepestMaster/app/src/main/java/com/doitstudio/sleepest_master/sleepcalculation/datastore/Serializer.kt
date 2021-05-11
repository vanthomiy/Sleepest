package com.doitstudio.sleepest_master.sleepcalculation.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.doitstudio.sleepest_master.LiveUserSleepActivity
import com.doitstudio.sleepest_master.SleepApiData
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

