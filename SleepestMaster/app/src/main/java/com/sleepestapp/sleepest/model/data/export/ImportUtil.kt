package com.sleepestapp.sleepest.model.data.export

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Parcelable
import android.widget.Toast
import com.sleepestapp.sleepest.R
import com.sleepestapp.sleepest.storage.DatabaseRepository
import com.sleepestapp.sleepest.storage.db.SleepApiRawDataEntity
import com.sleepestapp.sleepest.storage.db.UserSleepSessionEntity
import com.google.gson.Gson
import java.io.BufferedReader
import java.io.InputStreamReader

object ImportUtil {

    /**
     * Read the imported data by uri.
     * Check if its the right data format and then write to the database
     */
    suspend fun getLoadFileFromUri(uri: Uri?, actualContext : Context, dataBaseRepository : DatabaseRepository){

        uri?.let {
            val importJson = readTextFromUri(it, actualContext)

            val data = mutableListOf<UserSleepExportData>()
            try {
                val gson = Gson()

                data.addAll(
                    gson.fromJson(importJson, Array<UserSleepExportData>::class.java).asList()
                )
            } catch (ex: Exception) {

                Toast.makeText(actualContext, actualContext.getString(R.string.settings_import_wrong_format), Toast.LENGTH_SHORT).show()
                return@let

            }

            try {

                val sessions = mutableListOf<UserSleepSessionEntity>()
                val sleepApiRawDataEntity = mutableListOf<SleepApiRawDataEntity>()

                data.forEach { session ->

                    sessions.add(
                        UserSleepSessionEntity(
                            session.id,
                            session.mobilePosition,
                            session.lightConditions,
                            session.sleepTimes,
                            session.userSleepRating,
                            session.userCalculationRating
                        )
                    )

                    sleepApiRawDataEntity.addAll(session.sleepApiRawData)
                }

                dataBaseRepository.insertSleepApiRawData(sleepApiRawDataEntity)
                dataBaseRepository.insertUserSleepSessions(sessions)

                Toast.makeText(actualContext, actualContext.getString(R.string.settings_import_success), Toast.LENGTH_SHORT).show()


            } catch (ex: Exception) {
                Toast.makeText(actualContext, actualContext.getString(R.string.settings_import_cant_write_db), Toast.LENGTH_SHORT).show()
                return@let
            } finally {
            }
        }

    }

    /**
     * Get the uri from the intent and call the [getLoadFileFromUri] function
     */
    suspend fun getLoadFileFromIntent(data: Intent?, actualContext : Context, dataBaseRepository : DatabaseRepository){

        (data?.getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as? Uri)?.let {
            getLoadFileFromUri(it,actualContext, dataBaseRepository)
        }

    }

    /**
     * Reads the actual text out of the uri file with a [BufferedReader]
     */
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