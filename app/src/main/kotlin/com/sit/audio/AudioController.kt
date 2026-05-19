package com.sit.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Build
import com.sit.R
import com.sit.domain.AudioTrack

/**
 * Plays the chase sound during SPRINTING intervals and ducks other audio
 * (Spotify, podcasts, …) via AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK. Outside of
 * sprints the player is stopped and focus is abandoned so background audio
 * restores to full volume.
 */
class AudioController(private val context: Context) {

    private val audioManager: AudioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private val attributes: AudioAttributes = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_MEDIA)
        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
        .build()

    private var player: MediaPlayer? = null
    private var focusRequest: AudioFocusRequest? = null

    fun playSprint(track: AudioTrack) {
        stopSprint()
        if (!requestFocus()) return
        val resId = trackResource(track)
        player = MediaPlayer.create(context, resId)?.apply {
            isLooping = true
            setAudioAttributes(attributes)
            setOnErrorListener { _, _, _ -> true }
            start()
        }
    }

    fun stopSprint() {
        player?.let {
            try {
                if (it.isPlaying) it.stop()
            } catch (_: IllegalStateException) {
                // Player not in a state we can stop; release anyway.
            }
            it.release()
        }
        player = null
        abandonFocus()
    }

    fun release() {
        stopSprint()
    }

    private fun requestFocus(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val req = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(attributes)
                .setWillPauseWhenDucked(false)
                .build()
            focusRequest = req
            audioManager.requestAudioFocus(req) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                null,
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK,
            ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    private fun abandonFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
            focusRequest = null
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(null)
        }
    }

    private fun trackResource(track: AudioTrack): Int = when (track) {
        AudioTrack.DOG_BARKING -> R.raw.bark_loop
        AudioTrack.HORROR_CHASE -> R.raw.horror_chase
        AudioTrack.STANDARD_BEEP -> R.raw.standard_beep
    }
}
