package com.buddy.ultra.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.buddy.ultra.security.SecurityManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

enum class OrbState {
    IDLE,
    LISTENING,
    PROCESSING,
    THINKING,
    SPEAKING,
    EXECUTING,
    ERROR,
    OFFLINE
}

@Singleton
class SpeechEngine @Inject constructor(
    @ApplicationContext private val context: Context,
    private val securityManager: SecurityManager
) : RecognitionListener, TextToSpeech.OnInitListener {

    private var recognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var ttsReady = false

    private val _orbState = MutableStateFlow(OrbState.IDLE)
    val orbState: StateFlow<OrbState> = _orbState

    var onSpeechCaptured: ((String) -> Unit)? = null

    init {
        initRecognizer()
        tts = TextToSpeech(context, this)
    }

    fun initRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            recognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(this@SpeechEngine)
            }
        }
    }

    fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-IN")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        recognizer?.startListening(intent)
        _orbState.value = OrbState.LISTENING
    }

    fun stopListening() {
        recognizer?.stopListening()
        _orbState.value = OrbState.IDLE
    }

    fun setOrbState(state: OrbState) {
        _orbState.value = state
    }

    fun speak(text: String, onFinished: (() -> Unit)? = null) {
        if (!ttsReady) return
        _orbState.value = OrbState.SPEAKING
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) {
                _orbState.value = OrbState.IDLE
                onFinished?.invoke()
            }
            override fun onError(utteranceId: String?) {
                _orbState.value = OrbState.IDLE
            }
        })
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "BUDDY_TTS")
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.language = Locale("en", "IN")
            ttsReady = true
        }
    }

    override fun onResults(results: Bundle?) {
        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (!matches.isNullOrEmpty()) {
            val captured = matches[0]
            val wake = securityManager.getWakeWord()
            val clean = if (captured.lowercase().contains(wake)) {
                captured.lowercase().replace(wake, "").trim()
            } else {
                captured
            }
            _orbState.value = OrbState.PROCESSING
            onSpeechCaptured?.invoke(if (clean.isBlank()) captured else clean)
        } else {
            _orbState.value = OrbState.IDLE
        }
    }

    override fun onError(error: Int) {
        _orbState.value = OrbState.IDLE
    }

    override fun onReadyForSpeech(params: Bundle?) {}
    override fun onBeginningOfSpeech() {}
    override fun onRmsChanged(rmsdB: Float) {}
    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onEndOfSpeech() {}
    override fun onPartialResults(partialResults: Bundle?) {}
    override fun onEvent(eventType: Int, params: Bundle?) {}
}
