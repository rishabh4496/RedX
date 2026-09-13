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

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _speechRate = MutableStateFlow(1.0f)
    val speechRate: StateFlow<Float> = _speechRate.asStateFlow()

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                isInitialized = true
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _isSpeaking.value = true
                    }

                    override fun onDone(utteranceId: String?) {
                        _isSpeaking.value = false
                    }

                    @Suppress("OVERRIDE_DEPRECATION")
                    override fun onError(utteranceId: String?) {
                        _isSpeaking.value = false
                    }
                })
            }
        }
    }

    fun speak(text: String) {
        if (!isInitialized || text.isBlank()) return
        stop()
        tts?.setSpeechRate(_speechRate.value)
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "REDX_POST_SPEECH")
        _isSpeaking.value = true
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
