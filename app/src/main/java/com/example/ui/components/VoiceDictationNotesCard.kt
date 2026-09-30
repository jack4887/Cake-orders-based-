package com.example.ui.components

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import java.util.Locale

enum class DictationNoteTag(val label: String, val prefix: String) {
    DESIGN_SPEC("Design Spec", "[Design Spec] "),
    CLIENT_FEEDBACK("Client Feedback", "[Client Feedback] "),
    PIPING_DETAIL("Piping & Florals", "[Piping/Florals] "),
    ALLERGY_NOTE("Dietary Alert", "[Dietary] "),
    PLAIN("Plain Text", "")
}

object VoiceDictationFormatter {
    fun appendDictatedSegment(
        existingNotes: String,
        spokenTranscript: String,
        tag: DictationNoteTag
    ): String {
        val cleanSpoken = spokenTranscript.trim()
        if (cleanSpoken.isEmpty()) return existingNotes

        val capitalized = cleanSpoken.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString()
        }
        val punctuated = if (capitalized.endsWith(".") ||
            capitalized.endsWith("!") ||
            capitalized.endsWith("?")
        ) {
            capitalized
        } else {
            "$capitalized."
        }

        val formattedLine = "${tag.prefix}$punctuated"
        val cleanExisting = existingNotes.trim()
        return if (cleanExisting.isEmpty()) {
            formattedLine
        } else {
            "$cleanExisting\n$formattedLine"
        }
    }

    fun mapSpeechErrorToMessage(errorCode: Int): String {
        return when (errorCode) {
            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error. Please check your microphone."
            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission is required for voice dictation."
            SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
                "Network connection issue while transcribing speech."
            SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Try speaking closer to the microphone."
            SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Speech recognizer is busy. Tap Stop and try again."
            SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected. Tap Dictate when ready to speak."
            else -> "Voice dictation paused (code $errorCode). Tap Dictate to try again."
        }
    }
}

@Composable
fun VoiceDictationNotesCard(
    notesText: String,
    onNotesChange: (String) -> Unit,
    title: String = "Voice-to-Text Design & Client Feedback Notes",
    subtitle: String = "Hands-free dictation for cake design requirements, piping details, or client feedback while working at the bench.",
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTag by remember { mutableStateOf(DictationNoteTag.DESIGN_SPEC) }
    var isListening by remember { mutableStateOf(false) }
    var livePartialText by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isErrorStatus by remember { mutableStateOf(false) }

    val speechRecognizer = remember(context) {
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            SpeechRecognizer.createSpeechRecognizer(context)
        } else {
            null
        }
    }

    // Fallback Intent Launcher for devices without direct SpeechRecognizer service binding
    val recognizerIntentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isListening = false
        if (result.resultCode == Activity.RESULT_OK) {
            val matches = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val bestMatch = matches?.firstOrNull().orEmpty()
            if (bestMatch.isNotBlank()) {
                val updated = VoiceDictationFormatter.appendDictatedSegment(
                    existingNotes = notesText,
                    spokenTranscript = bestMatch,
                    tag = selectedTag
                )
                onNotesChange(updated)
                statusMessage = "Transcribed: “$bestMatch”"
                isErrorStatus = false
            }
        }
    }

    fun startSpeechCapture() {
        statusMessage = null
        isErrorStatus = false
        livePartialText = ""

        val recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(
                RecognizerIntent.EXTRA_PROMPT,
                "Speak cake design requirements or client feedback…"
            )
        }

        if (speechRecognizer != null) {
            try {
                isListening = true
                speechRecognizer.startListening(recognizerIntent)
            } catch (e: Exception) {
                isListening = false
                statusMessage = "Unable to start speech recognizer: ${e.localizedMessage}"
                isErrorStatus = true
            }
        } else {
            try {
                recognizerIntentLauncher.launch(recognizerIntent)
            } catch (_: Exception) {
                isListening = false
                statusMessage = "Speech recognition service is not installed on this device."
                isErrorStatus = true
            }
        }
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startSpeechCapture()
        } else {
            isListening = false
            statusMessage = "Microphone permission was denied. Enable microphone access to dictate notes hands-free."
            isErrorStatus = true
        }
    }

    DisposableEffect(speechRecognizer, selectedTag, notesText) {
        val listener = object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                isListening = true
                statusMessage = "Listening… Speak design specs or client feedback now."
                isErrorStatus = false
            }

            override fun onBeginningOfSpeech() {
                isListening = true
            }

            override fun onRmsChanged(rmsdB: Float) = Unit

            override fun onBufferReceived(buffer: ByteArray?) = Unit

            override fun onEndOfSpeech() {
                isListening = false
            }

            override fun onError(error: Int) {
                isListening = false
                livePartialText = ""
                statusMessage = VoiceDictationFormatter.mapSpeechErrorToMessage(error)
                isErrorStatus = true
            }

            override fun onResults(results: Bundle?) {
                isListening = false
                livePartialText = ""
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val bestMatch = matches?.firstOrNull().orEmpty()
                if (bestMatch.isNotBlank()) {
                    val updated = VoiceDictationFormatter.appendDictatedSegment(
                        existingNotes = notesText,
                        spokenTranscript = bestMatch,
                        tag = selectedTag
                    )
                    onNotesChange(updated)
                    statusMessage = "Added voice note: “$bestMatch”"
                    isErrorStatus = false
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val partial = partialResults
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                    .orEmpty()
                if (partial.isNotBlank()) {
                    livePartialText = partial
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) = Unit
        }

        speechRecognizer?.setRecognitionListener(listener)

        onDispose {
            try {
                speechRecognizer?.cancel()
            } catch (_: Exception) {
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                speechRecognizer?.destroy()
            } catch (_: Exception) {
            }
        }
    }

    val pulseTransition = rememberInfiniteTransition(label = "mic_pulse")
    val pulseScale by pulseTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isListening) 1.16f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(550),
            repeatMode = RepeatMode.Reverse
        ),
        label = "mic_pulse_scale"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("voice_dictation_notes_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Header Row + Dictate Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (isListening) Icons.Default.GraphicEq else Icons.Default.Mic,
                            contentDescription = null,
                            tint = if (isListening) {
                                MaterialTheme.colorScheme.error
                            } else {
                                MaterialTheme.colorScheme.primary
                            },
                            modifier = Modifier
                                .size(18.dp)
                                .scale(if (isListening) pulseScale else 1f)
                        )
                        Text(
                            text = "Voice Dictation",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = {
                        if (isListening) {
                            try {
                                speechRecognizer?.stopListening()
                            } catch (_: Exception) {
                            }
                            isListening = false
                            statusMessage = "Voice dictation stopped."
                            isErrorStatus = false
                        } else {
                            val hasPerm = ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED
                            if (hasPerm) {
                                startSpeechCapture()
                            } else {
                                audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isListening) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.primary
                        }
                    ),
                    shape = RoundedCornerShape(50),
                    modifier = Modifier.testTag("voice_dictate_toggle_button")
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = if (isListening) "Stop dictation" else "Start voice dictation",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isListening) "Stop Listening" else "Dictate Note")
                }
            }

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Note Category Prefix Selector
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                DictationNoteTag.entries.forEach { tag ->
                    FilterChip(
                        selected = selectedTag == tag,
                        onClick = { selectedTag = tag },
                        label = { Text(tag.label, style = MaterialTheme.typography.labelSmall) },
                        modifier = Modifier.testTag("dictation_tag_${tag.name.lowercase()}")
                    )
                }
            }

            // Live Streaming Partial Transcript Banner while speaking
            AnimatedVisibility(visible = isListening || livePartialText.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("voice_live_partial_banner")
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.error)
                        )
                        Text(
                            text = if (livePartialText.isNotBlank()) {
                                "Transcribing: “$livePartialText…”"
                            } else {
                                "Listening for cake design notes or client feedback…"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            fontStyle = FontStyle.Italic,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // Status / Feedback Banner
            if (!statusMessage.isNullOrBlank() && !isListening) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isErrorStatus) {
                        MaterialTheme.colorScheme.errorContainer
                    } else {
                        MaterialTheme.colorScheme.secondaryContainer
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("voice_dictation_status_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = if (isErrorStatus) Icons.Default.MicOff else Icons.Default.Mic,
                                contentDescription = null,
                                tint = if (isErrorStatus) {
                                    MaterialTheme.colorScheme.onErrorContainer
                                } else {
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                },
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = statusMessage.orEmpty(),
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isErrorStatus) {
                                    MaterialTheme.colorScheme.onErrorContainer
                                } else {
                                    MaterialTheme.colorScheme.onSecondaryContainer
                                }
                            )
                        }
                        IconButton(
                            onClick = { statusMessage = null },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Dismiss status",
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            // Editable Design Requirements & Client Feedback TextField
            OutlinedTextField(
                value = notesText,
                onValueChange = onNotesChange,
                label = { Text("Design Requirements, Piping & Client Feedback") },
                placeholder = {
                    Text("Tap 'Dictate Note' to speak hands-free or type design specifications…")
                },
                minLines = 3,
                trailingIcon = {
                    if (notesText.isNotEmpty()) {
                        IconButton(onClick = { onNotesChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear design notes"
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("voice_design_notes_input")
            )
        }
    }
}
