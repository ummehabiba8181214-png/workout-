package com.example.ui

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.Exercise
import com.example.data.Phase
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExerciseTimerDialog(
    exercise: Exercise,
    phase: Phase,
    onDismiss: () -> Unit,
    onCompleteExercise: () -> Unit
) {
    val context = LocalContext.current
    val initialSec = remember(exercise) { exercise.getSuggestedTimerSeconds() }

    var totalSeconds by remember { mutableIntStateOf(initialSec) }
    var remainingSeconds by remember { mutableIntStateOf(initialSec) }
    var isRunning by remember { mutableStateOf(false) }
    var isFinished by remember { mutableStateOf(false) }
    var autoMarkComplete by remember { mutableStateOf(true) }

    // Audio tone generator
    val toneGenerator = remember {
        try {
            ToneGenerator(AudioManager.STREAM_MUSIC, 85)
        } catch (e: Exception) {
            null
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            try {
                toneGenerator?.release()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }

    // Helper functions for audio/vibration feedback
    fun playTickSound() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 120)
        } catch (_: Exception) {}
    }

    fun playFinishSound() {
        try {
            toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 500)
        } catch (_: Exception) {}
    }

    fun triggerVibration(patternMs: Long = 100) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(patternMs, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(patternMs)
                }
            }
        } catch (_: Exception) {}
    }

    // Timer coroutine loop
    LaunchedEffect(isRunning, remainingSeconds) {
        if (isRunning && remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds -= 1

            // Cues for final seconds
            if (remainingSeconds in 1..3) {
                playTickSound()
                triggerVibration(80)
            } else if (remainingSeconds == 0) {
                isRunning = false
                isFinished = true
                playFinishSound()
                triggerVibration(400)

                if (autoMarkComplete) {
                    onCompleteExercise()
                }
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .testTag("exercise_timer_dialog"),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with Close Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(phase.lightColor),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Timer,
                                contentDescription = "Timer",
                                tint = phase.primaryColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Countdown Timer",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = phase.primaryColor
                            )
                            Text(
                                text = exercise.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("timer_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color(0xFF64748B)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Target Reps / Notes
                Text(
                    text = "Target: ${exercise.reps}",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B),
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Circular Progress Gauge & Timer Display
                val progressFraction = if (totalSeconds > 0) {
                    remainingSeconds.toFloat() / totalSeconds.toFloat()
                } else 0f

                val animatedProgress by animateFloatAsState(
                    targetValue = progressFraction,
                    animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                    label = "timerProgress"
                )

                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .testTag("timer_circular_gauge"),
                    contentAlignment = Alignment.Center
                ) {
                    // Gauge ring canvas
                    val gaugeColor = when {
                        isFinished -> Color(0xFF1A7A4A)
                        remainingSeconds in 1..5 && isRunning -> Color(0xFFE11D48)
                        else -> phase.primaryColor
                    }

                    Canvas(modifier = Modifier.size(180.dp)) {
                        // Background track
                        drawCircle(
                            color = Color(0xFFF1F5F9),
                            style = Stroke(width = 14.dp.toPx())
                        )
                        // Active Progress Arc
                        drawArc(
                            color = gaugeColor,
                            startAngle = -90f,
                            sweepAngle = 360f * animatedProgress,
                            useCenter = false,
                            style = Stroke(width = 14.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        // Formatted Digital Time MM:SS
                        val mins = remainingSeconds / 60
                        val secs = remainingSeconds % 60
                        val timeFormatted = String.format("%02d:%02d", mins, secs)

                        Text(
                            text = timeFormatted,
                            fontSize = 38.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (remainingSeconds in 1..5 && isRunning) Color(0xFFE11D48) else Color(0xFF1E293B)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Status subtitle
                        val statusText = when {
                            isFinished -> "🎉 Hold Complete!"
                            isRunning && remainingSeconds <= 5 -> "⚡ Final Seconds!"
                            isRunning -> "🫁 Hold Steady..."
                            remainingSeconds < totalSeconds -> "⏸️ Paused"
                            else -> "⏱️ Ready to Start"
                        }

                        Text(
                            text = statusText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = if (isFinished) Color(0xFF1A7A4A) else Color(0xFF64748B)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Time Adjuster Controls (-10s, -5s, +5s, +10s)
                if (!isRunning) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                val newTotal = (totalSeconds - 10).coerceAtLeast(5)
                                totalSeconds = newTotal
                                remainingSeconds = newTotal
                                isFinished = false
                            },
                            modifier = Modifier.testTag("timer_minus_10")
                        ) {
                            Text("-10s", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = phase.primaryColor)
                        }

                        IconButton(
                            onClick = {
                                val newTotal = (totalSeconds - 5).coerceAtLeast(5)
                                totalSeconds = newTotal
                                remainingSeconds = newTotal
                                isFinished = false
                            },
                            modifier = Modifier.testTag("timer_minus_5")
                        ) {
                            Icon(imageVector = Icons.Default.Remove, contentDescription = "-5s", tint = phase.primaryColor)
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "${totalSeconds}s",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF334155)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = {
                                val newTotal = totalSeconds + 5
                                totalSeconds = newTotal
                                remainingSeconds = newTotal
                                isFinished = false
                            },
                            modifier = Modifier.testTag("timer_plus_5")
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = "+5s", tint = phase.primaryColor)
                        }

                        IconButton(
                            onClick = {
                                val newTotal = totalSeconds + 10
                                totalSeconds = newTotal
                                remainingSeconds = newTotal
                                isFinished = false
                            },
                            modifier = Modifier.testTag("timer_plus_10")
                        ) {
                            Text("+10s", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = phase.primaryColor)
                        }
                    }

                    // Preset Chips
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(15, 20, 30, 45, 60, 120, 300).forEach { presetSec ->
                            val label = if (presetSec >= 60) "${presetSec / 60}m" else "${presetSec}s"
                            val isSelected = totalSeconds == presetSec

                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 3.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isSelected) phase.primaryColor else phase.lightColor)
                                    .clickable {
                                        totalSeconds = presetSec
                                        remainingSeconds = presetSec
                                        isFinished = false
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                    .testTag("preset_chip_$presetSec"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else phase.primaryColor
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Action Controls: Start / Pause / Reset
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isRunning) {
                        Button(
                            onClick = { isRunning = false },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("timer_pause_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE2E8F0))
                        ) {
                            Icon(imageVector = Icons.Default.Pause, contentDescription = "Pause", tint = Color(0xFF1E293B))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Pause", color = Color(0xFF1E293B), fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = {
                                if (remainingSeconds == 0) {
                                    remainingSeconds = totalSeconds
                                    isFinished = false
                                }
                                isRunning = true
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("timer_start_button"),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = phase.primaryColor)
                        ) {
                            Icon(
                                imageVector = if (remainingSeconds < totalSeconds && remainingSeconds > 0) Icons.Default.PlayArrow else Icons.Default.PlayArrow,
                                contentDescription = "Start",
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (remainingSeconds < totalSeconds && remainingSeconds > 0) "Resume" else "Start Timer",
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            isRunning = false
                            remainingSeconds = totalSeconds
                            isFinished = false
                        },
                        modifier = Modifier
                            .height(50.dp)
                            .testTag("timer_reset_button"),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFCBD5E1))
                    ) {
                        Icon(imageVector = Icons.Default.Refresh, contentDescription = "Reset", tint = Color(0xFF64748B))
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Auto-mark completed Checkbox
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { autoMarkComplete = !autoMarkComplete }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = autoMarkComplete,
                        onCheckedChange = { autoMarkComplete = it },
                        colors = CheckboxDefaults.colors(checkedColor = phase.primaryColor),
                        modifier = Modifier.testTag("auto_mark_checkbox")
                    )
                    Text(
                        text = "Mark exercise complete when timer finishes",
                        fontSize = 12.sp,
                        color = Color(0xFF475569)
                    )
                }

                if (isFinished) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = {
                            onCompleteExercise()
                            onDismiss()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("timer_finish_done_button"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1A7A4A))
                    ) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "Done", tint = Color.White)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Complete Exercise", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
