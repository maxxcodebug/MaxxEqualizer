package com.maxxcodebug.maxxequalizer

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.maxxcodebug.maxxequalizer.ui.AmoledThemeHelper
import com.maxxcodebug.maxxequalizer.ui.BottomNavHelper
import com.maxxcodebug.maxxequalizer.ui.NavScreen
import com.maxxcodebug.maxxequalizer.state.EqPreferencesManager

data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val duration: Long,
    val uri: Uri
)

class MusicActivity : AppCompatActivity(), PlaybackState.Listener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SongAdapter
    private lateinit var nowPlayingBar: View
    private lateinit var nowPlayingTitle: TextView
    private lateinit var nowPlayingArtist: TextView
    private lateinit var playPauseButton: ImageButton
    private lateinit var seekBar: SeekBar

    private var currentSong: Song? = null
    private val songs = mutableListOf<Song>()
    private val handler = android.os.Handler(android.os.Looper.getMainLooper())

    private val requestPermissionLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) loadSongs() else {
            Toast.makeText(this, "Music permission denied — can't load local songs", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AmoledThemeHelper.applyIfNeeded(this)
        setContentView(R.layout.activity_music)

        findViewById<eightbitlab.com.blurview.BlurView>(R.id.blurView)?.let { blurView ->
            val blurTarget = findViewById<eightbitlab.com.blurview.BlurTarget>(R.id.contentBlurTarget)
            blurView.setupWith(blurTarget).setBlurRadius(18f)
            blurView.clipToOutline = true
        }

        recyclerView = findViewById(R.id.songRecyclerView)
        nowPlayingBar = findViewById(R.id.nowPlayingBar)
        nowPlayingTitle = findViewById(R.id.nowPlayingTitle)
        nowPlayingArtist = findViewById(R.id.nowPlayingArtist)
        playPauseButton = findViewById(R.id.playPauseButton)
        seekBar = findViewById(R.id.seekBar)

        adapter = SongAdapter(songs) { song -> playSong(song) }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        playPauseButton.setOnClickListener { togglePlayPause() }

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) mediaPlayer?.seekTo(progress)
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        nowPlayingBar.visibility = View.GONE

        BottomNavHelper.setup(this, NavScreen.MUSIC, EqPreferencesManager(this))

        checkPermissionAndLoad()

        if (PlaybackState.currentTitle != null) {
            nowPlayingBar.visibility = View.VISIBLE
            nowPlayingTitle.text = PlaybackState.currentTitle
            nowPlayingArtist.text = PlaybackState.currentArtist
            nowPlayingBar.setOnClickListener {
                startActivity(Intent(this, NowPlayingActivity::class.java))
            }
        }
    }

    override fun onStart() {
        super.onStart()
        PlaybackState.addListener(this)
    }

    override fun onStop() {
        super.onStop()
        PlaybackState.removeListener(this)
    }

    override fun onStateChanged() {
        runOnUiThread {
            playPauseButton.setImageResource(
                if (PlaybackState.isPlaying) R.drawable.ic_nav_power else R.drawable.ic_nav_equalizer
            )
        }
    }

    override fun onProgress(position: Int) {
        runOnUiThread { seekBar.progress = position }
    }

    private fun checkPermissionAndLoad() {
        val permission = if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO
        else Manifest.permission.READ_EXTERNAL_STORAGE

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            loadSongs()
        } else {
            requestPermissionLauncher.launch(permission)
        }
    }

    private fun loadSongs() {
        songs.clear()
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} ASC"

        contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null, sortOrder
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idCol)
                val uri = Uri.withAppendedPath(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id.toString())
                songs.add(
                    Song(
                        id = id,
                        title = cursor.getString(titleCol) ?: "Unknown",
                        artist = cursor.getString(artistCol) ?: "Unknown artist",
                        duration = cursor.getLong(durationCol),
                        uri = uri
                    )
                )
            }
        }
        adapter.notifyDataSetChanged()

        if (songs.isEmpty()) {
            Toast.makeText(this, "No local songs found on this device", Toast.LENGTH_SHORT).show()
        }
    }

    private fun playSong(song: Song) {
        MusicService.playSong(this, song.uri, song.title, song.artist)
        currentSong = song
        nowPlayingBar.visibility = View.VISIBLE
        nowPlayingTitle.text = song.title
        nowPlayingArtist.text = song.artist
        nowPlayingBar.setOnClickListener {
            startActivity(Intent(this, NowPlayingActivity::class.java))
        }
    }

    private fun togglePlayPause() {
        MusicService.togglePlayPause(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}

class SongAdapter(
    private val songs: List<Song>,
    private val onClick: (Song) -> Unit
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    class SongViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.songItemTitle)
        val artist: TextView = view.findViewById(R.id.songItemArtist)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_song, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        holder.title.text = song.title
        holder.artist.text = song.artist
        holder.itemView.setOnClickListener { onClick(song) }
    }

    override fun getItemCount() = songs.size
}
