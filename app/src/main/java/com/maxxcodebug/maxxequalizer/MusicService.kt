package com.maxxcodebug.maxxequalizer

import android.app.*
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.IBinder

/**
 * Playback state + listener bus. Same-process, so Activities talk to this
 * directly (no AIDL/Messenger needed) — matches this codebase's plain
 * singleton/helper pattern rather than LiveData/ViewModel.
 */
object PlaybackState {
    var currentTitle: String? = null
    var currentArtist: String? = null
    var isPlaying: Boolean = false
    var duration: Int = 0

    interface Listener {
        fun onStateChanged()
        fun onProgress(position: Int)
    }

    private val listeners = mutableListOf<Listener>()

    fun addListener(l: Listener) { listeners.add(l) }
    fun removeListener(l: Listener) { listeners.remove(l) }
    fun notifyChanged() { listeners.forEach { it.onStateChanged() } }
    fun notifyProgress(position: Int) { listeners.forEach { it.onProgress(position) } }
}

class MusicService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())
    private val channelId = "maxxequalizer_music_playback"

    companion object {
        const val ACTION_PLAY = "com.maxxcodebug.maxxequalizer.action.PLAY"
        const val ACTION_TOGGLE = "com.maxxcodebug.maxxequalizer.action.TOGGLE"
        const val ACTION_SEEK = "com.maxxcodebug.maxxequalizer.action.SEEK"
        const val EXTRA_URI = "uri"
        const val EXTRA_TITLE = "title"
        const val EXTRA_ARTIST = "artist"
        const val EXTRA_POSITION = "position"

        fun playSong(context: android.content.Context, uri: Uri, title: String, artist: String) {
            val intent = Intent(context, MusicService::class.java).apply {
                action = ACTION_PLAY
                putExtra(EXTRA_URI, uri.toString())
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_ARTIST, artist)
            }
            if (Build.VERSION.SDK_INT >= 26) context.startForegroundService(intent)
            else context.startService(intent)
        }

        fun togglePlayPause(context: android.content.Context) {
            context.startService(Intent(context, MusicService::class.java).apply { action = ACTION_TOGGLE })
        }

        fun seekTo(context: android.content.Context, position: Int) {
            context.startService(Intent(context, MusicService::class.java).apply {
                action = ACTION_SEEK
                putExtra(EXTRA_POSITION, position)
            })
        }

        fun currentPosition(): Int = instance?.mediaPlayer?.currentPosition ?: 0

        private var instance: MusicService? = null
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> {
                val uriString = intent.getStringExtra(EXTRA_URI) ?: return START_NOT_STICKY
                val title = intent.getStringExtra(EXTRA_TITLE) ?: "Unknown"
                val artist = intent.getStringExtra(EXTRA_ARTIST) ?: "Unknown artist"
                playInternal(Uri.parse(uriString), title, artist)
            }
            ACTION_TOGGLE -> toggleInternal()
            ACTION_SEEK -> {
                val pos = intent.getIntExtra(EXTRA_POSITION, 0)
                mediaPlayer?.seekTo(pos)
            }
        }
        return START_STICKY
    }

    private fun playInternal(uri: Uri, title: String, artist: String) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            setDataSource(this@MusicService, uri)
            setOnPreparedListener {
                start()
                PlaybackState.duration = duration
                PlaybackState.isPlaying = true
                PlaybackState.notifyChanged()
                startProgressUpdates()
            }
            setOnCompletionListener {
                PlaybackState.isPlaying = false
                PlaybackState.notifyChanged()
            }
            prepareAsync()
        }
        PlaybackState.currentTitle = title
        PlaybackState.currentArtist = artist
        startForeground(1, buildNotification(title, artist))
    }

    private fun toggleInternal() {
        val mp = mediaPlayer ?: return
        if (mp.isPlaying) {
            mp.pause()
            PlaybackState.isPlaying = false
        } else {
            mp.start()
            PlaybackState.isPlaying = true
            startProgressUpdates()
        }
        PlaybackState.notifyChanged()
        startForeground(1, buildNotification(PlaybackState.currentTitle ?: "", PlaybackState.currentArtist ?: ""))
    }

    private fun startProgressUpdates() {
        handler.post(object : Runnable {
            override fun run() {
                val mp = mediaPlayer ?: return
                if (mp.isPlaying) {
                    PlaybackState.notifyProgress(mp.currentPosition)
                    handler.postDelayed(this, 500)
                }
            }
        })
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= 26) {
            val channel = NotificationChannel(channelId, "Music playback", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    private fun buildNotification(title: String, artist: String): Notification {
        return NotificationCompatBuilder(this, channelId, title, artist)
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
        instance = null
    }
}

/** Small helper so we don't need to pull in androidx.core notification builder imports separately. */
private fun NotificationCompatBuilder(
    context: android.content.Context,
    channelId: String,
    title: String,
    artist: String
): Notification {
    return androidx.core.app.NotificationCompat.Builder(context, channelId)
        .setContentTitle(title)
        .setContentText(artist)
        .setSmallIcon(R.drawable.ic_nav_equalizer)
        .setOngoing(true)
        .build()
}
