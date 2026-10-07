package com.example.ui.chiko

import android.content.Context
import android.media.AudioAttributes
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

/**
 * AI CFO Voice Emotion profiles for natural, expressive, noise-free speech.
 */
enum class CfoVoiceEmotion(
    val title: String,
    val subtitle: String,
    val pitch: Float,
    val speechRate: Float,
    val emoji: String,
    val color: Color
) {
    CONFIDENT_CFO(
        title = "Confident CFO",
        subtitle = "Crisp, authoritative & visionary",
        pitch = 1.0f,
        speechRate = 1.05f,
        emoji = "💼",
        color = Color(0xFF10B981)
    ),
    EMPATHETIC_COACH(
        title = "Empathetic Coach",
        subtitle = "Warm, encouraging & supportive",
        pitch = 0.95f,
        speechRate = 0.92f,
        emoji = "🤝",
        color = Color(0xFF3B82F6)
    ),
    CALM_ADVISOR(
        title = "Calm Advisor",
        subtitle = "Steady, balanced & analytical",
        pitch = 1.0f,
        speechRate = 0.98f,
        emoji = "🧘",
        color = Color(0xFF8B5CF6)
    ),
    DYNAMIC_ALPHA(
        title = "Dynamic Alpha",
        subtitle = "Energetic, fast-paced & bullish",
        pitch = 1.12f,
        speechRate = 1.15f,
        emoji = "⚡",
        color = Color(0xFFF59E0B)
    )
}

/**
 * Manages clean, noise-free speech playback with emotional voice modulation,
 * track progress tracking, and Play / Pause / Replay controls.
 */
class CfoAudioPlayerController(
    private val context: Context,
    private val tts: TextToSpeech?
) {
    var activeMessageId by mutableStateOf<String?>(null)
    var isPlaying by mutableStateOf(false)
    var isPaused by mutableStateOf(false)
    var currentEmotion by mutableStateOf(CfoVoiceEmotion.CONFIDENT_CFO)
    var playbackSpeed by mutableFloatStateOf(1.0f)
    var trackProgress by mutableFloatStateOf(0f)

    private var currentCleanText: String = ""
    private var utteranceId: String = ""

    init {
        configureAudioAttributes()
        setupProgressListener()
    }

    private fun configureAudioAttributes() {
        tts?.language = Locale.US
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
            .build()
        tts?.setAudioAttributes(audioAttributes)
    }

    private fun setupProgressListener() {
        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(id: String?) {
                if (id == utteranceId) {
                    isPlaying = true
                    isPaused = false
                }
            }

            override fun onDone(id: String?) {
                if (id == utteranceId) {
                    isPlaying = false
                    isPaused = false
                    trackProgress = 1f
                }
            }

            @Deprecated("Deprecated in Java")
            override fun onError(id: String?) {
                if (id == utteranceId) {
                    isPlaying = false
                    isPaused = false
                }
            }

            override fun onRangeStart(id: String?, start: Int, end: Int, frame: Int) {
                if (id == utteranceId && currentCleanText.isNotEmpty()) {
                    trackProgress = (end.toFloat() / currentCleanText.length.toFloat()).coerceIn(0f, 1f)
                }
            }
        })
    }

    fun playTrack(messageId: String, rawText: String, emotion: CfoVoiceEmotion = currentEmotion) {
        if (tts == null) {
            Toast.makeText(context, "Voice engine initializing...", Toast.LENGTH_SHORT).show()
            return
        }

        currentEmotion = emotion
        val cleanSpeech = cleanTextForNoiseFreeSpeech(rawText)
        currentCleanText = cleanSpeech
        utteranceId = "chiko_${messageId}_${System.currentTimeMillis()}"
        activeMessageId = messageId

        // Apply emotional pitch and speed
        tts.setPitch(emotion.pitch)
        tts.setSpeechRate(emotion.speechRate * playbackSpeed)

        val params = Bundle()
        tts.speak(cleanSpeech, TextToSpeech.QUEUE_FLUSH, params, utteranceId)
        isPlaying = true
        isPaused = false
        trackProgress = 0.05f
    }

    fun pauseTrack() {
        if (isPlaying) {
            tts?.stop()
            isPlaying = false
            isPaused = true
        }
    }

    fun resumeTrack(messageId: String, rawText: String) {
        playTrack(messageId, rawText, currentEmotion)
    }

    fun replayTrack(messageId: String, rawText: String) {
        tts?.stop()
        trackProgress = 0f
        playTrack(messageId, rawText, currentEmotion)
    }

    fun stopTrack() {
        tts?.stop()
        isPlaying = false
        isPaused = false
        trackProgress = 0f
        activeMessageId = null
    }

    fun setEmotion(emotion: CfoVoiceEmotion, messageId: String?, rawText: String?) {
        currentEmotion = emotion
        if (isPlaying && messageId != null && rawText != null) {
            playTrack(messageId, rawText, emotion)
        }
    }

    fun cycleSpeed() {
        playbackSpeed = when (playbackSpeed) {
            1.0f -> 1.25f
            1.25f -> 1.5f
            1.5f -> 0.85f
            else -> 1.0f
        }
        if (isPlaying && activeMessageId != null && currentCleanText.isNotEmpty()) {
            tts?.setSpeechRate(currentEmotion.speechRate * playbackSpeed)
        }
    }
}

/**
 * Strips all noisy markdown symbols, table delimiters, brackets, and emojis,
 * and expands financial shorthand so the TTS voice engine speaks in pristine,
 * crystal-clear, noise-free English.
 */
fun cleanTextForNoiseFreeSpeech(text: String): String {
    var cleaned = text

    // 1. Convert Markdown tables to fluent spoken sentences instead of reciting pipes and dashes
    cleaned = cleaned.replace(Regex("""\|[^\n]+\|""")) { match ->
        val row = match.value
        if (row.contains("---")) {
            ""
        } else {
            val cells = row.split("|").map { it.trim() }.filter { it.isNotEmpty() }
            if (cells.size >= 2) {
                "${cells.joinToString(", ")}. "
            } else {
                ""
            }
        }
    }

    // 2. Expand Financial Shorthand
    cleaned = cleaned
        .replace("₹", " rupees ")
        .replace("$", " dollars ")
        .replace("YoY", " year over year ")
        .replace("QoQ", " quarter over quarter ")
        .replace("SIP", " S I P ")
        .replace("CAGR", " compound annual growth rate ")
        .replace("ETF", " E T F ")
        .replace("p.a.", " per annum ")
        .replace("EPFO", " E P F O ")
        .replace("ESI", " E S I ")

    // 3. Remove Markdown syntax symbols that cause noise
    cleaned = cleaned
        .replace(Regex("""\*\*"""), "")
        .replace(Regex("""\b#+\s*"""), "")
        .replace(Regex("""[*_`~>\[\]]"""), " ")
        .replace(Regex("""\(http[^\)]+\)"""), "")

    // 4. Remove emojis and unusual Unicode characters
    cleaned = cleaned.replace(
        Regex("""[\uD83C-\uDBFF\uDC00-\uDFFF\u2600-\u27BF⚠️📈💼📉🎯✅•⚡🔥💡🎉]"""),
        ""
    )

    // 5. Clean whitespace & repeated punctuation
    cleaned = cleaned
        .replace(Regex("""\s+"""), " ")
        .replace(Regex(""",\s*,"""), ",")
        .replace(Regex("""\.\s*\."""), ".")
        .trim()

    return cleaned
}

/**
 * Interactive Audio Track Bar with Play, Pause, Resume, Replay, animated equalizer sound waves,
 * emotion selection chips, and speed toggles.
 */
@Composable
fun CfoAudioTrackBar(
    messageId: String,
    rawText: String,
    controller: CfoAudioPlayerController,
    modifier: Modifier = Modifier
) {
    val isThisTrackActive = controller.activeMessageId == messageId
    val isPlaying = isThisTrackActive && controller.isPlaying
    val isPaused = isThisTrackActive && controller.isPaused
    var showEmotionMenu by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Play / Pause / Resume / Replay Buttons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(controller.currentEmotion.color, controller.currentEmotion.color.copy(alpha = 0.8f))
                                )
                            )
                            .clickable {
                                when {
                                    isPlaying -> controller.pauseTrack()
                                    isPaused -> controller.resumeTrack(messageId, rawText)
                                    else -> controller.playTrack(messageId, rawText)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Replay Button
                    IconButton(
                        onClick = { controller.replayTrack(messageId, rawText) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Replay,
                            contentDescription = "Replay Track",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // Equalizer Sound Wave Animation
                    if (isPlaying) {
                        SoundWaveEqualizer(color = controller.currentEmotion.color)
                    } else {
                        Text(
                            text = if (isPaused) "Paused" else "AI Voice Output",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Emotion & Speed Controls
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Emotion Preset Chip
                    Box {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = controller.currentEmotion.color.copy(alpha = 0.15f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { showEmotionMenu = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = controller.currentEmotion.emoji,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = controller.currentEmotion.title,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    fontWeight = FontWeight.Bold,
                                    color = controller.currentEmotion.color
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showEmotionMenu,
                            onDismissRequest = { showEmotionMenu = false }
                        ) {
                            CfoVoiceEmotion.values().forEach { emotion ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(emotion.emoji, fontSize = 14.sp)
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(emotion.title, fontWeight = FontWeight.Bold)
                                            }
                                            Text(
                                                emotion.subtitle,
                                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                                                color = MaterialTheme.colorScheme.outline
                                            )
                                        }
                                    },
                                    onClick = {
                                        controller.setEmotion(emotion, messageId, rawText)
                                        showEmotionMenu = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Speed Toggle
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { controller.cycleSpeed() }
                    ) {
                        Text(
                            text = "${controller.playbackSpeed}x",
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Track Progress Bar
            if (isThisTrackActive && (isPlaying || isPaused)) {
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { controller.trackProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = controller.currentEmotion.color,
                    trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )
            }
        }
    }
}

/**
 * Animated 5-bar Sound Wave Equalizer giving a futuristic, studio-grade visual cue.
 */
@Composable
private fun SoundWaveEqualizer(color: Color) {
    val transition = rememberInfiniteTransition(label = "equalizer")

    val h1 by transition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(tween(350, easing = LinearEasing), RepeatMode.Reverse),
        label = "h1"
    )
    val h2 by transition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(tween(420, easing = LinearEasing), RepeatMode.Reverse),
        label = "h2"
    )
    val h3 by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(tween(280, easing = LinearEasing), RepeatMode.Reverse),
        label = "h3"
    )
    val h4 by transition.animateFloat(
        initialValue = 0.9f,
        targetValue = 0.4f,
        animationSpec = infiniteRepeatable(tween(390, easing = LinearEasing), RepeatMode.Reverse),
        label = "h4"
    )
    val h5 by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(tween(310, easing = LinearEasing), RepeatMode.Reverse),
        label = "h5"
    )

    Row(
        modifier = Modifier
            .height(16.dp)
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(h1, h2, h3, h4, h5).forEach { fraction ->
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .fillMaxHeight(fraction)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
        }
    }
}
