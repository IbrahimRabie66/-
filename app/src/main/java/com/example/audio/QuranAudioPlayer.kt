package com.example.audio

import android.media.AudioAttributes
import android.media.MediaPlayer
import com.example.data.model.AyahItem
import com.example.data.model.Reciter
import com.example.data.model.SUPPORTED_RECITERS
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AudioPlaybackState(
    val selectedReciter: Reciter = SUPPORTED_RECITERS.first(),
    val isPlaying: Boolean = false,
    val isBuffering: Boolean = false,
    val currentSurahNumber: Int = 1,
    val currentSurahName: String = "الفاتحة",
    val currentAyahNumber: Int = 1,
    val currentAyahText: String = "",
    val isFullSurahMode: Boolean = false,
    val statusLabel: String = "جاهز للتلاوة الصوتية",
    val errorMessage: String? = null
)

class QuranAudioPlayer {

    private var mediaPlayer: MediaPlayer? = null
    private var currentPlaylist: List<AyahItem> = emptyList()
    private var currentIndexInPlaylist: Int = 0

    private val _state = MutableStateFlow(AudioPlaybackState())
    val state: StateFlow<AudioPlaybackState> = _state.asStateFlow()

    fun selectReciter(reciter: Reciter) {
        val wasPlaying = _state.value.isPlaying
        val currentSurah = _state.value.currentSurahNumber
        val currentAyah = _state.value.currentAyahNumber
        val fullSurahMode = _state.value.isFullSurahMode

        _state.update {
            it.copy(
                selectedReciter = reciter,
                statusLabel = "تم اختيار ${reciter.arabicName}",
                errorMessage = null
            )
        }

        if (wasPlaying) {
            if (fullSurahMode) {
                playFullSurah(currentSurah, _state.value.currentSurahName)
            } else {
                val targetAyah = currentPlaylist.find {
                    it.surahNumber == currentSurah && it.ayahNumber == currentAyah
                }
                if (targetAyah != null) {
                    playAyahFromSurah(targetAyah, currentPlaylist)
                }
            }
        }
    }

    fun playAyahFromSurah(ayah: AyahItem, surahAyahs: List<AyahItem>) {
        currentPlaylist = surahAyahs
        currentIndexInPlaylist = surahAyahs.indexOfFirst { it.ayahNumber == ayah.ayahNumber }.coerceAtLeast(0)

        val reciter = _state.value.selectedReciter
        val audioUrl = reciter.getAyahAudioUrl(ayah.surahNumber, ayah.ayahNumber)

        _state.update {
            it.copy(
                isBuffering = true,
                isPlaying = false,
                isFullSurahMode = false,
                currentSurahNumber = ayah.surahNumber,
                currentSurahName = ayah.surahName,
                currentAyahNumber = ayah.ayahNumber,
                currentAyahText = ayah.textUthmani,
                statusLabel = "جاري تحميل تلاوة الآية (${ayah.ayahNumber}) بصوت ${reciter.arabicName}...",
                errorMessage = null
            )
        }

        startMediaPlayer(
            url = audioUrl,
            onPreparedActiveLabel = "يتلو الآن: سورة ${ayah.surahName} • الآية ${ayah.ayahNumber} (${reciter.arabicName})",
            onCompletion = {
                // Auto-advance to the next Ayah in the Surah
                if (currentIndexInPlaylist + 1 < currentPlaylist.size) {
                    currentIndexInPlaylist += 1
                    val nextAyah = currentPlaylist[currentIndexInPlaylist]
                    playAyahFromSurah(nextAyah, currentPlaylist)
                } else {
                    _state.update {
                        it.copy(
                            isPlaying = false,
                            isBuffering = false,
                            statusLabel = "اكتملت تلاوة سورة ${ayah.surahName} بصوت ${reciter.arabicName}"
                        )
                    }
                }
            }
        )
    }

    fun playFullSurah(surahNumber: Int, surahName: String) {
        val reciter = _state.value.selectedReciter
        val audioUrl = reciter.getFullSurahAudioUrl(surahNumber)

        _state.update {
            it.copy(
                isBuffering = true,
                isPlaying = false,
                isFullSurahMode = true,
                currentSurahNumber = surahNumber,
                currentSurahName = surahName,
                currentAyahNumber = 1,
                currentAyahText = "تلاوة كاملة لسورة $surahName",
                statusLabel = "جاري تشغيل سورة $surahName كاملة بصوت ${reciter.arabicName}...",
                errorMessage = null
            )
        }

        startMediaPlayer(
            url = audioUrl,
            onPreparedActiveLabel = "تلاوة سورة $surahName كاملة • ${reciter.arabicName}",
            onCompletion = {
                _state.update {
                    it.copy(
                        isPlaying = false,
                        isBuffering = false,
                        statusLabel = "انتهت تلاوة سورة $surahName"
                    )
                }
            }
        )
    }

    fun togglePlayPause(fallbackAyahs: List<AyahItem>) {
        val mp = mediaPlayer
        if (mp != null) {
            try {
                if (mp.isPlaying) {
                    mp.pause()
                    _state.update {
                        it.copy(
                            isPlaying = false,
                            statusLabel = "متوقف مؤقتاً • سورة ${it.currentSurahName} (${it.currentAyahNumber})"
                        )
                    }
                    return
                } else if (!_state.value.isBuffering) {
                    mp.start()
                    _state.update {
                        it.copy(
                            isPlaying = true,
                            statusLabel = "يتلو الآن: سورة ${it.currentSurahName} • ${it.selectedReciter.arabicName}"
                        )
                    }
                    return
                }
            } catch (_: Exception) {
                // Re-initialize below
            }
        }

        if (fallbackAyahs.isNotEmpty()) {
            val target = fallbackAyahs.find { it.ayahNumber == _state.value.currentAyahNumber } ?: fallbackAyahs.first()
            playAyahFromSurah(target, fallbackAyahs)
        } else {
            playFullSurah(_state.value.currentSurahNumber, _state.value.currentSurahName)
        }
    }

    fun skipToNextAyah() {
        if (currentPlaylist.isNotEmpty() && currentIndexInPlaylist + 1 < currentPlaylist.size) {
            currentIndexInPlaylist += 1
            playAyahFromSurah(currentPlaylist[currentIndexInPlaylist], currentPlaylist)
        }
    }

    fun skipToPreviousAyah() {
        if (currentPlaylist.isNotEmpty() && currentIndexInPlaylist - 1 >= 0) {
            currentIndexInPlaylist -= 1
            playAyahFromSurah(currentPlaylist[currentIndexInPlaylist], currentPlaylist)
        }
    }

    fun stopPlayback() {
        releasePlayer()
        _state.update {
            it.copy(
                isPlaying = false,
                isBuffering = false,
                statusLabel = "تم إيقاف التلاوة"
            )
        }
    }

    private fun startMediaPlayer(
        url: String,
        onPreparedActiveLabel: String,
        onCompletion: () -> Unit
    ) {
        releasePlayer()
        try {
            val mp = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(url)
                setOnPreparedListener { player ->
                    player.start()
                    _state.update {
                        it.copy(
                            isBuffering = false,
                            isPlaying = true,
                            statusLabel = onPreparedActiveLabel,
                            errorMessage = null
                        )
                    }
                }
                setOnCompletionListener {
                    onCompletion()
                }
                setOnErrorListener { _, _, _ ->
                    _state.update {
                        it.copy(
                            isBuffering = false,
                            isPlaying = false,
                            statusLabel = "تعذر الاتصال بخادم الصوتيات حالياً",
                            errorMessage = "تأكد من الاتصال بالإنترنت للاستماع لتلاوة ${it.selectedReciter.arabicName}"
                        )
                    }
                    true
                }
                prepareAsync()
            }
            mediaPlayer = mp
        } catch (e: Exception) {
            _state.update {
                it.copy(
                    isBuffering = false,
                    isPlaying = false,
                    statusLabel = "تعذر تشغيل الملف الصوتي",
                    errorMessage = e.localizedMessage
                )
            }
        }
    }

    fun releasePlayer() {
        try {
            mediaPlayer?.stop()
        } catch (_: Exception) {
        }
        try {
            mediaPlayer?.release()
        } catch (_: Exception) {
        }
        mediaPlayer = null
    }
}
