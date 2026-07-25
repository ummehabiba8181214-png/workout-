package com.example.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Exercise
import com.example.data.ExerciseData
import com.example.data.Phase
import com.example.data.Section
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExerciseTrackerScreen(
    viewModel: ExerciseViewModel,
    modifier: Modifier = Modifier
) {
    val activePhaseId by viewModel.activePhaseId.collectAsStateWithLifecycle()
    val activeDay by viewModel.activeDay.collectAsStateWithLifecycle()
    val completionsMap by viewModel.completionsMap.collectAsStateWithLifecycle()
    val startDateMillis by viewModel.startDateMillis.collectAsStateWithLifecycle()

    val phase = ExerciseData.phases.find { it.id == activePhaseId } ?: ExerciseData.phases[0]

    var selectedTab by remember { mutableStateOf(0) } // 0 = Plan, 1 = Summary Dashboard
    var showResetDialog by remember { mutableStateOf(false) }
    var showDayPicker by remember { mutableStateOf(false) }
    var activeTimerData by remember { mutableStateOf<Triple<Exercise, Int, Int>?>(null) }

    val currentDayDateStr = viewModel.getDateForDay(activeDay, "dd/MM/yyyy")

    val context = LocalContext.current
    fun openStartDatePicker() {
        val cal = Calendar.getInstance().apply { timeInMillis = startDateMillis }
        android.app.DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                viewModel.setStartDate(year, month, dayOfMonth)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    // Calculate completion stats for current day
    var totalExercises = 0
    var doneExercises = 0
    phase.sections.forEachIndexed { sIdx, sec ->
        sec.exercises.forEachIndexed { eIdx, _ ->
            totalExercises++
            val key = ExerciseData.makeKey(phase.id, activeDay, sIdx, eIdx)
            if (completionsMap[key] == true) {
                doneExercises++
            }
        }
    }

    val pct = if (totalExercises == 0) 0 else (doneExercises * 100) / totalExercises

    if (showDayPicker) {
        DayPickerModalDialog(
            activeDay = activeDay,
            phase = phase,
            completionsMap = completionsMap,
            currentDayDateStr = currentDayDateStr,
            getDateForDay = { viewModel.getDateForDay(it, "dd/MM") },
            onSelectDay = { viewModel.selectDay(it) },
            onEditStartDate = { openStartDatePicker() },
            onDismiss = { showDayPicker = false }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp,
                modifier = Modifier.testTag("main_navigation_bar")
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.FitnessCenter, contentDescription = "Plan") },
                    label = { Text("Plan", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = phase.primaryColor,
                        selectedTextColor = phase.primaryColor,
                        indicatorColor = phase.lightColor
                    ),
                    modifier = Modifier.testTag("nav_tab_plan")
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Analytics, contentDescription = "Summary") },
                    label = { Text("Summary", fontWeight = FontWeight.Bold, fontSize = 12.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = phase.primaryColor,
                        selectedTextColor = phase.primaryColor,
                        indicatorColor = phase.lightColor
                    ),
                    modifier = Modifier.testTag("nav_tab_summary")
                )
            }
        }
    ) { innerPadding ->
        if (selectedTab == 1) {
            SummaryDashboardScreen(
                completionsMap = completionsMap,
                modifier = Modifier.padding(innerPadding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                // Header Banner Card
                item {
                    HeaderCard(
                        activeDay = activeDay,
                        currentDateStr = currentDayDateStr,
                        phaseColor = phase.primaryColor,
                        onEditStartDate = { openStartDatePicker() }
                    )
                }

                // Phase Selection Tabs
                item {
                    PhaseTabs(
                        activePhaseId = activePhaseId,
                        phases = ExerciseData.phases,
                        onSelectPhase = { viewModel.selectPhase(it) }
                    )
                }

                // Compact Day Selection Bar with Icon
                item {
                    DaySelectionBarCard(
                        activeDay = activeDay,
                        currentDateStr = currentDayDateStr,
                        phase = phase,
                        doneExercises = doneExercises,
                        totalExercises = totalExercises,
                        onOpenDayPicker = { showDayPicker = true },
                        onEditStartDate = { openStartDatePicker() }
                    )
                }

                // Progress Card
                item {
                    ProgressCard(
                        activeDay = activeDay,
                        currentDateStr = currentDayDateStr,
                        level = phase.level,
                        done = doneExercises,
                        total = totalExercises,
                        pct = pct,
                        phaseColor = phase.primaryColor
                    )
                }

                // Exercise Sections
                itemsIndexed(phase.sections) { sectionIdx, section ->
                    SectionCard(
                        phase = phase,
                        sectionIndex = sectionIdx,
                        section = section,
                        activeDay = activeDay,
                        completionsMap = completionsMap,
                        onToggleExercise = { sIdx, eIdx ->
                            viewModel.toggleExercise(sIdx, eIdx)
                        },
                        onOpenTimer = { exercise, sIdx, eIdx ->
                            activeTimerData = Triple(exercise, sIdx, eIdx)
                        }
                    )
                }

                // Warning Box
                item {
                    WarningCard(warnings = ExerciseData.safetyWarnings)
                }

                // Reset Day Button
                item {
                    OutlinedButton(
                        onClick = { showResetDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("reset_day_button"),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(2.dp, phase.primaryColor)
                    ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = "Reset Day",
                        tint = phase.primaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "🔄 Day $activeDay Reset Karein",
                        fontWeight = FontWeight.Bold,
                        color = phase.primaryColor,
                        fontSize = 14.sp
                    )
                }
            }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }
        }
    }

    // Reset Confirmation Dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = {
                Text(
                    text = "Day $activeDay Reset",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text("Kya aap Day $activeDay ke saare completed exercises reset karna chahte hain?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.resetActiveDay()
                        showResetDialog = false
                    }
                ) {
                    Text("Reset", color = Color(0xFFC62828), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Countdown Timer Dialog
    activeTimerData?.let { (exercise, sIdx, eIdx) ->
        val key = ExerciseData.makeKey(phase.id, activeDay, sIdx, eIdx)
        val isDone = completionsMap[key] == true

        ExerciseTimerDialog(
            exercise = exercise,
            phase = phase,
            onDismiss = { activeTimerData = null },
            onCompleteExercise = {
                if (!isDone) {
                    viewModel.toggleExercise(sIdx, eIdx)
                }
            }
        )
    }
}

@Composable
fun HeaderCard(
    activeDay: Int,
    currentDateStr: String,
    phaseColor: Color,
    onEditStartDate: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("header_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = phaseColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🌿 Workout",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.85f),
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "6-Month Recovery Tracker",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(10.dp))

            // Changeable Date Badge
            Surface(
                onClick = onEditStartDate,
                shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(alpha = 0.22f),
                modifier = Modifier.testTag("header_date_chip")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Calendar",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Day $activeDay • $currentDateStr",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit Start Date",
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun PhaseTabs(
    activePhaseId: Int,
    phases: List<Phase>,
    onSelectPhase: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        phases.forEach { p ->
            val isActive = p.id == activePhaseId
            val bgColor by animateColorAsState(
                targetValue = if (isActive) p.primaryColor else Color.White,
                animationSpec = tween(200),
                label = "phaseBg"
            )
            val textColor by animateColorAsState(
                targetValue = if (isActive) Color.White else p.primaryColor,
                animationSpec = tween(200),
                label = "phaseText"
            )

            Card(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onSelectPhase(p.id) }
                    .testTag("phase_tab_${p.id}"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = bgColor),
                elevation = CardDefaults.cardElevation(defaultElevation = if (isActive) 4.dp else 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = p.emoji, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = p.label,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = textColor
                    )
                    Text(
                        text = p.months,
                        fontWeight = FontWeight.Normal,
                        fontSize = 10.sp,
                        color = textColor.copy(alpha = 0.85f)
                    )
                }
            }
        }
    }
}

@Composable
fun DaySelectionBarCard(
    activeDay: Int,
    currentDateStr: String,
    phase: Phase,
    doneExercises: Int,
    totalExercises: Int,
    onOpenDayPicker: () -> Unit,
    onEditStartDate: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onOpenDayPicker() }
            .testTag("day_selection_bar_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(phase.lightColor)
                        .clickable { onEditStartDate() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Day Calendar",
                        tint = phase.primaryColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Day $activeDay",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = phase.lightColor,
                            onClick = onEditStartDate
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(
                                    text = currentDateStr,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = phase.primaryColor
                                )
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = phase.primaryColor,
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$doneExercises of $totalExercises completed today",
                        fontSize = 12.sp,
                        color = Color(0xFF64748B)
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .testTag("open_day_selector_button"),
                color = phase.primaryColor
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Select Day",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "Select Day",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DayPickerModalDialog(
    activeDay: Int,
    phase: Phase,
    completionsMap: Map<String, Boolean>,
    currentDayDateStr: String,
    getDateForDay: (Int) -> String,
    onSelectDay: (Int) -> Unit,
    onEditStartDate: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("close_day_picker_dialog")
            ) {
                Text("Close", fontWeight = FontWeight.Bold, color = phase.primaryColor)
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    onDismiss()
                    onEditStartDate()
                },
                modifier = Modifier.testTag("change_start_date_button")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp), tint = phase.primaryColor)
                    Text("Change Start Date", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = phase.primaryColor)
                }
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = phase.primaryColor
                )
                Text(
                    text = "Select Workout Day",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(
                    onClick = {
                        onDismiss()
                        onEditStartDate()
                    },
                    shape = RoundedCornerShape(10.dp),
                    color = phase.lightColor
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "📅 Current Day $activeDay: $currentDayDateStr",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = phase.primaryColor
                        )
                        Text(
                            text = "Change Date ✏️",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = phase.primaryColor
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(phase.primaryColor)
                        )
                        Text("Active", fontSize = 11.sp, color = Color(0xFF475569))
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(phase.lightColor)
                                .border(1.dp, phase.borderColor, RoundedCornerShape(3.dp))
                        )
                        Text("Completed", fontSize = 11.sp, color = Color(0xFF475569))
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color(0xFFF1F5F9))
                        )
                        Text("Unstarted", fontSize = 11.sp, color = Color(0xFF475569))
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                ) {
                    val scrollState = rememberScrollState()
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(scrollState),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (d in 1..60) {
                            val isActive = d == activeDay
                            val hasData = remember(completionsMap, phase.id, d) {
                                phase.sections.indices.any { sIdx ->
                                    val sec = phase.sections[sIdx]
                                    sec.exercises.indices.any { eIdx ->
                                        completionsMap[ExerciseData.makeKey(phase.id, d, sIdx, eIdx)] == true
                                    }
                                }
                            }

                            val buttonBg = when {
                                isActive -> phase.primaryColor
                                hasData -> phase.lightColor
                                else -> Color(0xFFF1F5F9)
                            }

                            val buttonText = when {
                                isActive -> Color.White
                                hasData -> phase.primaryColor
                                else -> Color(0xFF64748B)
                            }

                            val dayShortDate = getDateForDay(d)

                            Column(
                                modifier = Modifier
                                    .size(width = 44.dp, height = 44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(buttonBg)
                                    .then(
                                        if (hasData && !isActive) Modifier.border(1.dp, phase.borderColor, RoundedCornerShape(10.dp))
                                        else Modifier
                                    )
                                    .clickable {
                                        onSelectDay(d)
                                        onDismiss()
                                    }
                                    .testTag("dialog_day_button_$d"),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "$d",
                                    fontSize = 11.sp,
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                                    color = buttonText
                                )
                                Text(
                                    text = dayShortDate,
                                    fontSize = 8.sp,
                                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (isActive) Color.White.copy(alpha = 0.9f) else Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }
            }
        },
        shape = RoundedCornerShape(20.dp),
        containerColor = Color.White
    )
}

@Composable
fun ProgressCard(
    activeDay: Int,
    currentDateStr: String,
    level: String,
    done: Int,
    total: Int,
    pct: Int,
    phaseColor: Color
) {
    val animatedPct by animateFloatAsState(
        targetValue = pct.toFloat() / 100f,
        animationSpec = tween(300),
        label = "progressPct"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("progress_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Day $activeDay • $currentDateStr — $level",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF333333)
                )
                Text(
                    text = "$done/$total ✅",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = phaseColor
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Progress bar track
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF0F0F0))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedPct.coerceIn(0f, 1f))
                        .height(10.dp)
                        .clip(CircleShape)
                        .background(if (pct == 100) Color(0xFF1A7A4A) else phaseColor)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "$pct% complete",
                    fontSize = 11.sp,
                    color = Color(0xFF888888)
                )
                if (pct == 100) {
                    Text(
                        text = "🎉 Aaj ka din complete!",
                        fontSize = 11.sp,
                        color = Color(0xFF1A7A4A),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun SectionCard(
    phase: Phase,
    sectionIndex: Int,
    section: Section,
    activeDay: Int,
    completionsMap: Map<String, Boolean>,
    onToggleExercise: (Int, Int) -> Unit,
    onOpenTimer: (Exercise, Int, Int) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("section_card_$sectionIndex"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Text(
                text = section.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = phase.primaryColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            // Section Divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(phase.lightColor)
            )

            Spacer(modifier = Modifier.height(8.dp))

            section.exercises.forEachIndexed { exerciseIdx, exercise ->
                val itemKey = ExerciseData.makeKey(phase.id, activeDay, sectionIndex, exerciseIdx)
                val isDone = completionsMap[itemKey] == true

                ExerciseRowItem(
                    phase = phase,
                    exercise = exercise,
                    isDone = isDone,
                    sectionIndex = sectionIndex,
                    exerciseIndex = exerciseIdx,
                    onToggle = { onToggleExercise(sectionIndex, exerciseIdx) },
                    onOpenTimer = { onOpenTimer(exercise, sectionIndex, exerciseIdx) }
                )
            }
        }
    }
}

@Composable
fun ExerciseRowItem(
    phase: Phase,
    exercise: Exercise,
    isDone: Boolean,
    sectionIndex: Int,
    exerciseIndex: Int,
    onToggle: () -> Unit,
    onOpenTimer: () -> Unit
) {
    val bg = if (isDone) phase.lightColor else Color(0xFFFAFAFA)
    val borderColor = if (isDone) phase.borderColor else Color(0xFFEEEEEE)
    val isTimed = exercise.isTimedExercise()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bg)
            .border(1.dp, borderColor, RoundedCornerShape(10.dp))
            .clickable { onToggle() }
            .padding(10.dp)
            .testTag("exercise_item_${sectionIndex}_$exerciseIndex"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Checkbox box
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(if (isDone) phase.primaryColor else Color.White)
                .border(2.dp, if (isDone) phase.primaryColor else Color(0xFFCCCCCC), RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (isDone) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Completed",
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = exercise.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isDone) phase.primaryColor.copy(alpha = 0.8f) else Color(0xFF333333),
                textDecoration = if (isDone) TextDecoration.LineThrough else TextDecoration.None
            )
            Text(
                text = exercise.reps,
                fontSize = 12.sp,
                color = Color(0xFF888888),
                modifier = Modifier.padding(top = 1.dp)
            )
        }

        // Timer action button/chip
        if (isTimed) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isDone) Color.White.copy(alpha = 0.8f) else phase.lightColor)
                    .clickable { onOpenTimer() }
                    .padding(horizontal = 8.dp, vertical = 6.dp)
                    .testTag("exercise_timer_button_${sectionIndex}_$exerciseIndex"),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Timer,
                        contentDescription = "Start Timer",
                        tint = phase.primaryColor,
                        modifier = Modifier.size(14.dp)
                    )
                    val sec = exercise.getSuggestedTimerSeconds()
                    val label = if (sec >= 60) "${sec / 60}m" else "${sec}s"
                    Text(
                        text = "$label Timer",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = phase.primaryColor
                    )
                }
            }
        } else {
            IconButton(
                onClick = onOpenTimer,
                modifier = Modifier
                    .size(32.dp)
                    .testTag("exercise_timer_icon_${sectionIndex}_$exerciseIndex")
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = "Timer",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun WarningCard(warnings: List<String>) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("warning_card"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF0F0)),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFCDD2))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Warning",
                    tint = Color(0xFFC62828),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "⚠️ Hamesha Yaad Rakhein",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFC62828)
                )
            }

            warnings.forEach { warningText ->
                Text(
                    text = "• $warningText",
                    fontSize = 12.sp,
                    color = Color(0xFF555555),
                    modifier = Modifier.padding(vertical = 2.dp)
                )
            }
        }
    }
}
