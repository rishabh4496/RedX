package com.example.redx.util

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class PostSpeechManager(context: Context) {

    private var tts: TextToSpeech? = null
    private var isInitialized = false
    @Volatile private var lastUtteranceId: String? = null
    // Utterance ids carry a generation so late callbacks from a superseded session
    // (e.g. onStop after stop() + speak()) can't flip the new session's state.
    @Volatile private var generation = 0

    private fun isCurrent(utteranceId: String?): Boolean =
        utteranceId != null && utteranceId.startsWith("REDX_${generation}_")

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _speechRate = MutableStateFlow(1.0f)
    val speechRate: StateFlow<Float> = _speechRate.asStateFlow()

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                // Prefer the device language; fall back to US English only if unsupported.
                val engine = tts
                val result = engine?.setLanguage(Locale.getDefault())
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    engine?.setLanguage(Locale.US)
                }
                isInitialized = true
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        // Only the last queued chunk ends the session.
                        if (utteranceId == lastUtteranceId) _isSpeaking.value = false
                    }

                    override fun onStop(utteranceId: String?, interrupted: Boolean) {
                        if (isCurrent(utteranceId)) _isSpeaking.value = false
                    }

                    @Suppress("OVERRIDE_DEPRECATION")
                    override fun onError(utteranceId: String?) {
                        if (isCurrent(utteranceId)) _isSpeaking.value = false
                    }
                })
            }
        }
    }

    fun speak(text: String) {
        if (!isInitialized || text.isBlank()) return
        stop()
        val engine = tts ?: return
        // The engine rejects input over getMaxSpeechInputLength(), so long posts are queued
        // as several chunks instead of failing silently.
        val limit = minOf(TextToSpeech.getMaxSpeechInputLength() - 100, SpeechChunker.DEFAULT_MAX_LENGTH)
        val chunks = SpeechChunker.chunk(text, limit.coerceAtLeast(500))
        if (chunks.isEmpty()) return
        engine.setSpeechRate(_speechRate.value)
        val gen = ++generation
        lastUtteranceId = "REDX_${gen}_${chunks.lastIndex}"
        var started = false
        chunks.forEachIndexed { index, chunk ->
            val mode = if (index == 0) TextToSpeech.QUEUE_FLUSH else TextToSpeech.QUEUE_ADD
            val status = engine.speak(chunk, mode, null, "REDX_${gen}_$index")
            if (status == TextToSpeech.SUCCESS) started = true
        }
        // Only claim to be speaking if the engine actually accepted something.
        _isSpeaking.value = started
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun setRate(rate: Float) {
        _speechRate.value = rate
        tts?.setSpeechRate(rate)
    }

    fun cycleRate() {
        val nextRate = when (_speechRate.value) {
            1.0f -> 1.25f
            1.25f -> 1.5f
            1.5f -> 0.85f
            else -> 1.0f
        }
        setRate(nextRate)
    }

    fun shutdown() {
        stop()
        tts?.shutdown()
        tts = null
    }
}
