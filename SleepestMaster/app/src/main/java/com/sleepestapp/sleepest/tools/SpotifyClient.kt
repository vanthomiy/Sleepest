package com.sleepestapp.sleepest.tools

import android.util.Log
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.android.appremote.api.error.SpotifyDisconnectedException

object SpotifyClient {
    var spotifyAppRemote: SpotifyAppRemote? = null
}