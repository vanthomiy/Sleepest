package com.doitstudio.sleepest_master.model.data.export

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import android.widget.Toast
import com.doitstudio.sleepest_master.model.data.LightConditions
import com.doitstudio.sleepest_master.storage.DatabaseRepository
import com.doitstudio.sleepest_master.storage.db.SleepApiRawDataEntity
import com.doitstudio.sleepest_master.storage.db.UserSleepSessionEntity
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

object ImportUtil {

    suspend fun getLoadFileFromUri(uri: Uri?, actualContext : Context, dataBaseRepository : DatabaseRepository){

        uri?.let {
            val importJson = readTextFromUri(it, actualContext)

            var data = mutableListOf<UserSleepExportData>()
            try {
                var gson = Gson()

                data.addAll(
                    gson.fromJson(importJson, Array<UserSleepExportData>::class.java).asList()
                )
            } catch (ex: Exception) {

                Toast.makeText(actualContext, "Wrong data format", Toast.LENGTH_SHORT).show()
                return@let

            }

            try {

                var sessions = mutableListOf<UserSleepSessionEntity>()
                var sleepApiRawDataEntity = mutableListOf<SleepApiRawDataEntity>()

                data.forEach { session ->

                    sessions.add(
                        UserSleepSessionEntity(
                            session.id,
                            session.mobilePosition,
                            session.lightConditions?:LightConditions.UNIDENTIFIED,
                            session.sleepTimes,
                            session.userSleepRating,
                            session.userCalculationRating
                        )
                    )

                    sleepApiRawDataEntity.addAll(session.sleepApiRawData)
                }

                dataBaseRepository.insertSleepApiRawData(sleepApiRawDataEntity)
                dataBaseRepository.insertUserSleepSessions(sessions)

                Toast.makeText(actualContext, "Successful imported data", Toast.LENGTH_SHORT).show()


            } catch (ex: Exception) {
                Toast.makeText(actualContext, "Cant write to database", Toast.LENGTH_SHORT).show()
                return@let
            } finally {
            }
        }

    }


    suspend fun getLoadFileFromIntent(data: Intent?, actualContext : Context, dataBaseRepository : DatabaseRepository){

        (data?.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri)?.let {
            getLoadFileFromUri(it,actualContext, dataBaseRepository)
        }

    }


    private fun readTextFromUri(uri: Uri, actualContext: Context): String {

        val contentResolver: ContentResolver = actualContext.contentResolver
        val stringBuilder = StringBuilder()
        contentResolver.openInputStream(uri)?.use { inputStream ->
            BufferedReader(InputStreamReader(inputStream)).use { reader ->
                var line: String? = reader.readLine()
                while (line != null) {
                    stringBuilder.append(line)
                    line = reader.readLine()
                }
            }
        }
        return stringBuilder.toString()
    }



}