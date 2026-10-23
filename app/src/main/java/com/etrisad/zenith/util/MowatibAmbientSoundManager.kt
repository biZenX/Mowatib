package com.etrisad.zenith.util

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.media.AudioAttributes
import android.media.MediaPlayer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AmbientSound(
    val id: String,
    val nameEn: String,
    val nameAr: String,
    val assetFileName: String
)

object MowatibAmbientSoundManager {
    val AVAILABLE_SOUNDS = listOf(
        AmbientSound("none", "Silent", "صامت", ""),
        AmbientSound("rain", "Gentle Rain", "مطر هادئ", "audio/rain.mp3"),
        AmbientSound("forest", "Forest", "أصوات الغابة", "audio/forest.mp3"),
        AmbientSound("water", "Water Stream", "خرير الماء", "audio/water_stream.mp3"),
        AmbientSound("fireplace", "Fireplace", "موقد دافئ", "audio/fireplace.mp3"),
        AmbientSound("ocean", "Ocean Waves", "أمواج البحر", "audio/ocean.mp3"),
        AmbientSound("brown_noise", "Brown Noise", "الضوضاء البنية", "audio/brown_noise.mp3"),
        AmbientSound("quran", "Quran Recitation", "تلاوة قرآنية هادئة", "audio/quran_anam.mp3")
    )

    private var mediaPlayer: MediaPlayer? = null
    private val _currentSound = MutableStateFlow<AmbientSound>(AVAILABLE_SOUNDS[0])
    val currentSound: StateFlow<AmbientSound> = _currentSound.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _volume = MutableStateFlow(0.7f)
    val volume: StateFlow<Float> = _volume.asStateFlow()

    fun playSound(context: Context, sound: AmbientSound) {
        if (sound.id == "none" || sound.assetFileName.isEmpty()) {
            stop()
            _currentSound.value = AVAILABLE_SOUNDS[0]
            return
        }

        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            mediaPlayer = null

            val afd: AssetFileDescriptor = context.assets.openFd(sound.assetFileName)
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                afd.close()
                isLooping = true
                val vol = _volume.value
                setVolume(vol, vol)
                prepare()
                start()
            }
            _currentSound.value = sound
            _isPlaying.value = true
        } catch (e: Exception) {
            e.printStackTrace()
            _isPlaying.value = false
        }
    }

    fun togglePlayPause(context: Context) {
        val player = mediaPlayer
        if (player != null && player.isPlaying) {
            player.pause()
            _isPlaying.value = false
        } else if (player != null) {
            player.start()
            _isPlaying.value = true
        } else if (_currentSound.value.id != "none") {
            playSound(context, _currentSound.value)
        }
    }

    fun setVolume(vol: Float) {
        val clamped = vol.coerceIn(0f, 1f)
        _volume.value = clamped
        try {
            mediaPlayer?.setVolume(clamped, clamped)
        } catch (_: Exception) {}
    }

    fun stop() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (_: Exception) {}
        mediaPlayer = null
        _isPlaying.value = false
    }
}
