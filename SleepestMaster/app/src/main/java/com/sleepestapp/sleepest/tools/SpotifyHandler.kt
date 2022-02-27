package com.sleepestapp.sleepest.tools

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.sleepestapp.sleepest.model.data.Constants
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.client.Subscription
import com.spotify.protocol.types.PlayerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlin.coroutines.Continuation
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class SpotifyHandler() {

    private val CLIENTID = "d1a260dcd07f454ca47f10f09f88be21"
    private val REDIRECTURI = "http://localhost:8888/callback"
    val scope: CoroutineScope = MainScope()
    private var playerStateSubscription: Subscription<PlayerState>? = null
    private var appContext : Context? = null
    private var status : String? = null

    suspend fun connect(applicationContext: Context)  {
        disconnect()
        try {
            SpotifyClient.spotifyAppRemote = connectToAppRemote(true, applicationContext)
        } catch (error: Throwable) {
            disconnect()
            throw error
        }

    }

    fun disconnect() {
        playerStateSubscription = cancelAndResetSubscription(playerStateSubscription)
        if (SpotifyClient.spotifyAppRemote != null) {
            SpotifyAppRemote.disconnect(SpotifyClient.spotifyAppRemote)
        }
    }

    fun isConnected() : Boolean? {
        return SpotifyClient.spotifyAppRemote?.isConnected
    }

    fun onPlayClicked() {

        SpotifyClient.spotifyAppRemote?.let {
            it.playerApi
                .playerState
                .setResultCallback { playerState ->
                    if (playerState.isPaused) {
                        it.playerApi
                            .resume()
                    } else {
                        it.playerApi
                            .pause()
                    }
                }
        }
    }

    fun onSkipPreviousButtonClicked() {
        SpotifyClient.spotifyAppRemote
            ?.playerApi
            ?.skipPrevious()
            ?.setResultCallback {  }
            ?.setErrorCallback {

            }
    }

    fun onSkipNextButtonClicked() {
        SpotifyClient.spotifyAppRemote
            ?.playerApi
            ?.skipNext()
            ?.setResultCallback {  }
            ?.setErrorCallback {

            }
    }

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
                        subscribeToPlayerState(applicationContext)
                    }

                    override fun onFailure(error: Throwable) {
                        cont.resumeWithException(error)
                        Toast.makeText(applicationContext, error.message, Toast.LENGTH_LONG).show()
                    }
                })
        }

    private fun subscribeToPlayerState(applicationContext: Context) {

        appContext = applicationContext
        playerStateSubscription = cancelAndResetSubscription(playerStateSubscription)

        playerStateSubscription = SpotifyClient.spotifyAppRemote
            ?.playerApi
            ?.subscribeToPlayerState()
            ?.setEventCallback(playerStateEventCallback)
            ?.setLifecycleCallback(
                object : Subscription.LifecycleCallback {
                    override fun onStart() {
                        val a = 0
                    }

                    override fun onStop() {
                        val b = 0
                    }
                })
            ?.setErrorCallback {
                val b = 1
            } as? Subscription<PlayerState>
    }

    private fun <T : Any?> cancelAndResetSubscription(subscription: Subscription<T>?): Subscription<T>? {
        return subscription?.let {
            if (!it.isCanceled) {
                it.cancel()
            }
            null
        }
    }

    private val playerStateEventCallback = Subscription.EventCallback<PlayerState> { playerState ->
        val intent = Intent(Constants.SPOTIFY_BROADCAST_RECEIVER_INTENT)
        intent.putExtra("PlayPauseStatus", !playerState.isPaused)
        appContext?.let { LocalBroadcastManager.getInstance(it).sendBroadcast(intent) }
    }

}