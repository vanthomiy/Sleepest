package com.sleepestapp.sleepest.tools

import android.app.Application
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.sleepestapp.sleepest.MainApplication
import com.sleepestapp.sleepest.storage.DataStoreRepository
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class SpotifyHandler() {

    private val CLIENTID = "d1a260dcd07f454ca47f10f09f88be21"
    private val REDIRECTURI = "http://localhost:8888/callback"
    val scope: CoroutineScope = MainScope()

    suspend fun connect(applicationContext: Context)  {
        disconnect()
            try {
                SpotifyClient.spotifyAppRemote = connectToAppRemote(true, applicationContext)
                //onConnected()
            } catch (error: Throwable) {
                disconnect()
                throw error
            }

    }

    fun disconnect() {
        if (SpotifyClient.spotifyAppRemote != null) {
            SpotifyAppRemote.disconnect(SpotifyClient.spotifyAppRemote)
        }
    }

    fun isConnected() : Boolean? {
        return SpotifyClient.spotifyAppRemote?.isConnected
    }

    /*fun isPlaying() : Boolean {
        SpotifyClient.spotifyAppRemote?.let {
            it.playerApi
                .playerState
                .setResultCallback { playerState ->
                    if (playerState.isPaused) {

                        it.playerApi
                            .resume()
                            .setResultCallback { logMessage(getString(R.string.command_feedback, "play")) }
                            .setErrorCallback(errorCallback)


                    } else {
                        it.playerApi
                            .pause()
                            .setResultCallback { logMessage(getString(R.string.command_feedback, "pause")) }
                            .setErrorCallback(errorCallback)
                    }
                }
            return true
        } ?: run {
            return false
        }
    }*/

    fun stopPlayer() {
        SpotifyClient.spotifyAppRemote?.playerApi?.pause()
    }

    private suspend fun connectToAppRemote(showAuthView: Boolean, applicationContext: Context): SpotifyAppRemote? =
        suspendCoroutine { cont: Continuation<SpotifyAppRemote> ->
            SpotifyAppRemote.connect(
                applicationContext,
                ConnectionParams.Builder(CLIENTID)
                    .setRedirectUri(REDIRECTURI)
                    .showAuthView(showAuthView)
                    .build(),
                object : Connector.ConnectionListener {
                    override fun onConnected(spotifyAppRemote: SpotifyAppRemote) {
                        cont.resume(spotifyAppRemote)
                    }

                    override fun onFailure(error: Throwable) {
                        cont.resumeWithException(error)
                        Toast.makeText(applicationContext, error.message, Toast.LENGTH_LONG).show()
                    }
                })
        }
}