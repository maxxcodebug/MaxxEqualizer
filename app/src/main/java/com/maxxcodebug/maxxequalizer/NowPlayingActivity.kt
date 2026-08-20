package com.maxxcodebug.maxxequalizer

import android.os.Bundle
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.maxxcodebug.maxxequalizer.ui.AmoledThemeHelper

class NowPlayingActivity : AppCompatActivity(), PlaybackState.Listener {

    private lateinit var titleView: TextView
    private lateinit var artistView: TextView
    private lateinit var playPauseButton: ImageButton
    private lateinit var seekBar: SeekBar
    private var userSeeking = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AmoledThemeHelper.applyIfNeeded(this)
        setContentView(R.layout.activity_now_playing)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)

        titleView = findViewById(R.id.nowPlayingBigTitle)
        artistView = findViewById(R.id.nowPlayingBigArtist)
        playPauseButton = findViewById(R.id.nowPlayingBigPlayPause)
        seekBar = findViewById(R.id.nowPlayingBigSeekBar)

        findViewById<ImageButton>(R.id.nowPlayingCollapseButton).setOnClickListener { finish() }

        playPauseButton.setOnClickListener {
            MusicService.togglePlayPause(this)
        }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {}
            override fun onStartTrackingTouch(sb: SeekBar?) { userSeeking = true }
            override fun onStopTrackingTouch(sb: SeekBar?) {
                userSeeking = false
                MusicService.seekTo(this@NowPlayingActivity, sb?.progress ?: 0)
            }
        })

        renderState()
        seekBar.max = if (PlaybackState.duration > 0) PlaybackState.duration else 1
    }

    override fun onStart() {
        super.onStart()
        PlaybackState.addListener(this)
    }

    override fun onStop() {
        super.onStop()
        PlaybackState.removeListener(this)
    }

    private fun renderState() {
        titleView.text = PlaybackState.currentTitle ?: "Nothing playing"
        artistView.text = PlaybackState.currentArtist ?: ""
        playPauseButton.setImageResource(
            if (PlaybackState.isPlaying) R.drawable.ic_nav_power else R.drawable.ic_nav_equalizer
        )
        seekBar.max = if (PlaybackState.duration > 0) PlaybackState.duration else 1
    }

    override fun onStateChanged() {
        runOnUiThread { renderState() }
    }

    override fun onProgress(position: Int) {
        if (!userSeeking) runOnUiThread { seekBar.progress = position }
    }
}
