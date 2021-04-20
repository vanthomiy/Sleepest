package com.doitstudio.sleepest_master.storage.datastorage

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.doitstudio.sleepest_master.Alarm
import com.doitstudio.sleepest_master.LiveUserSleepActivity
import com.doitstudio.sleepest_master.SleepApiData
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

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

    override val defaultValue: Alarm = Alarm.getDefaultInstance()
}
