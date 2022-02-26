package com.sleepestapp.sleepest.storage.datastorage

import androidx.datastore.core.DataStore
import com.sleepestapp.sleepest.BackgroundService
import com.sleepestapp.sleepest.Spotify
import com.sleepestapp.sleepest.Tutorial
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import java.io.IOException

const val SPOTIFY_STATUS_NAME = "spotify_status"

class SpotifyStatus(private val dataStore: DataStore<Spotify>) {

    suspend fun loadDefault(){
        dataStore.updateData  {
                obj ->
            obj.toBuilder()
                .build()
        }
    }

    val spotifyData: Flow<Spotify> = dataStore.data
        .catch { exception->
            if(exception is IOException){
                emit(Spotify.getDefaultInstance())
            }else{
                throw exception
            }
        }

    suspend fun updateSpotifyEnabled(isEnabled:Boolean){
        dataStore.updateData{preference->
            preference.toBuilder().setSpotifyEnabled(isEnabled).build()
        }
    }

    suspend fun updateSpotifyConnected(isConnected:Boolean){
        dataStore.updateData{preference->
            preference.toBuilder().setSpotifyConnected(isConnected).build()
        }
    }

    suspend fun updateSpotifyPlaying(isPlaying:Boolean){
        dataStore.updateData{preference->
            preference.toBuilder().setSpotifyIsPlaying(isPlaying).build()
        }
    }

}