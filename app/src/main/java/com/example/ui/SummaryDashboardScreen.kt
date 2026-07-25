package com.example.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ExerciseData

private const val PHASE_DAYS = 60

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SummaryDashboardScreen(
    completionsMap: Map<String, Boolean>,
    modifier: Modifier = Modifier
) {
    // Selected day for detailed inspection in Heatmap
    var inspectedDay by remember { mutableIntStateOf(1) }
    var selectedPhaseId by remember { mutableIntStateOf(1) }

    val phase = ExerciseData.phases.find { it.id == selectedPhaseId } ?: ExerciseData.phases[0]

    // Calculate Stats
    val totalPossibleExercises = remember(selectedPhaseId) {
        val p = ExerciseData.phases.find { it.id == selectedPhaseId } ?: ExerciseData.phases[0]
        val singleDayCount = p.sections.sumOf { it.exercises.size }
        singleDayCount * PHASE_DAYS
    }

    val completedInCurrentPhase = remember(completionsMap, selectedPhaseId) {
        val p = ExerciseData.phases.find { it.id == selectedPhaseId } ?: ExerciseData.phases[0]
        var count = 0
        for (d in 1..PHASE_DAYS) {
            p.sections.forEachIndexed { sIdx, sec ->
                sec.exercises.indices.forEach { eIdx ->
                    if (completionsMap[ExerciseData.makeKey(p.id, d, sIdx, eIdx)] == true) {
                        count++
                    }
                }
            }
        }
        count
    }

    // Compute Daily Exercise Counts for current phase
    val dailyCounts = remember(completionsMap, selectedPhaseId) {
        val p = ExerciseData.phases.find { it.id == selectedPhaseId } ?: ExerciseData.phases[0]
        val map = mutableMapOf<Int, Int>()
        for (d in 1..PHASE_DAYS) {
            var c = 0
            p.sections.forEachIndexed { sIdx, sec ->
                sec.exercises.indices.forEach { eIdx ->
                    if (completionsMap[ExerciseData.makeKey(p.id, d, sIdx, eIdx)] == true) {
                        c++
                    }
                }
            }
            map[d] = c
        }
        map
    }

    val exercisesPerDayTarget = remember(selectedPhaseId) {
        val p = ExerciseData.phases.find { it.id == selectedPhaseId } ?: ExerciseData.phases[0]
        p.sections.sumOf { it.exercises.size }
    }

    // Streaks
    val (currentStreak, bestStreak, activeDaysCount) = remember(dailyCounts) {
        var curr = 0
        var best = 0
        var active = 0
        for (d in 1..PHASE_DAYS) {
            val count = dailyCounts[d] ?: 0
            if (count > 0) {
                active++
                curr++
                if (curr > best) best = curr
            } else {
                curr = 0
            }
        }
        Triple(curr, best, active)
    }

    val consistencyRate = ((activeDaysCount.toFloat() / PHASE_DAYS) * 100).toInt()

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("summary_dashboard_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(12.dp)) }

        // Title Header
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "RECOVERY ANALYTICS",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A7A4A),
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = "Consistency & Summary",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F172A)
                )
                Text(
                    text = "Track workout frequency, streaks, and completion trends.",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B)
                )
            }
        }

        // Phase Filter Tabs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExerciseData.phases.forEach { p ->
                    val isSelected = p.id == selectedPhaseId
                    val bg = if (isSelected) p.primaryColor else p.lightColor
                    val textColor = if (isSelected) Color.White else p.primaryColor

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .background(bg)
                            .clickable { selectedPhaseId = p.id }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = p.label,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }
                }
            }
        }

        // Key KPI Metric Cards
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiMetricCard(
                        title = "Completed",
                        value = "$completedInCurrentPhase",
                        subtitle = "of $totalPossibleExercises total reps",
                        icon = Icons.Default.CheckCircle,
                        color = Color(0xFF1A7A4A),
                        modifier = Modifier.weight(1f)
                    )
                    KpiMetricCard(
                        title = "Consistency Rate",
                        value = "$consistencyRate%",
                        subtitle = "$activeDaysCount / $PHASE_DAYS active days",
                        icon = Icons.AutoMirrored.Filled.TrendingUp,
                        color = Color(0xFF0284C7),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KpiMetricCard(
                        title = "Current Streak",
                        value = "$currentStreak Days",
                        subtitle = "Keep going!",
                        icon = Icons.Default.LocalFireDepartment,
                        color = Color(0xFFE11D48),
                        modifier = Modifier.weight(1f)
                    )
                    KpiMetricCard(
                        title = "Best Streak",
                        value = "$bestStreak Days",
                        subtitle = "Personal record",
                        icon = Icons.Default.EmojiEvents,
                        color = Color(0xFFD97706),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Daily Exercise Frequency Bar Chart
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("frequency_bar_chart_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = "Bar Chart",
                                tint = phase.primaryColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Daily Exercise Frequency",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                        }
                        Text(
                            text = "Target: $exercisesPerDayTarget/day",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = phase.primaryColor
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Scrollable Bar Chart Canvas
                    val scrollState = rememberScrollState()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .horizontalScroll(scrollState)
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        for (d in 1..PHASE_DAYS) {
                            val count = dailyCounts[d] ?: 0
                            val pct = if (exercisesPerDayTarget > 0) (count.toFloat() / exercisesPerDayTarget).coerceIn(0f, 1f) else 0f
                            val barHeightRatio by animateFloatAsState(
                                targetValue = pct,
                                animationSpec = tween(durationMillis = 500),
                                label = "barHeight_$d"
                            )

                            val barColor = when {
                                count == 0 -> Color(0xFFE2E8F0)
                                count >= exercisesPerDayTarget -> phase.primaryColor
                                else -> phase.primaryColor.copy(alpha = 0.6f)
                            }

                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier
                                    .width(28.dp)
                                    .fillMaxHeight()
                                    .clickable { inspectedDay = d }
                            ) {
                                if (count > 0) {
                                    Text(
                                        text = "$count",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (inspectedDay == d) phase.primaryColor else Color(0xFF64748B)
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                }

                                Box(
                                    modifier = Modifier
                                        .width(18.dp)
                                        .weight(1f, fill = false)
                                        .height((110 * barHeightRatio).coerceAtLeast(4f).dp)
                                        .clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp))
                                        .background(if (inspectedDay == d) Color(0xFF0F172A) else barColor)
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = "D$d",
                                    fontSize = 9.sp,
                                    fontWeight = if (inspectedDay == d) FontWeight.Bold else FontWeight.Normal,
                                    color = if (inspectedDay == d) Color(0xFF0F172A) else Color(0xFF94A3B8)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 60-Day Consistency Heatmap Grid
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("heatmap_grid_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Calendar",
                                tint = phase.primaryColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Consistency Heatmap Matrix",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1E293B)
                            )
                        }
                    }

                    Text(
                        text = "Tap any day block to inspect exercise completions.",
                        fontSize = 11.sp,
                        color = Color(0xFF64748B),
                        modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                    )

                    // Heatmap Legend
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Legend:", fontSize = 10.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Bold)
                        HeatmapLegendItem(color = Color(0xFFF1F5F9), label = "0%")
                        HeatmapLegendItem(color = Color(0xFFA8D5B5), label = "<50%")
                        HeatmapLegendItem(color = Color(0xFF4CAF50), label = "50%+")
                        HeatmapLegendItem(color = Color(0xFF1A7A4A), label = "100%")
                    }

                    // Heatmap Matrix Grid (10 Columns x N Rows)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        for (d in 1..PHASE_DAYS) {
                            val count = dailyCounts[d] ?: 0
                            val target = exercisesPerDayTarget
                            val ratio = if (target > 0) count.toFloat() / target else 0f

                            val cellBg = when {
                                count == 0 -> Color(0xFFF1F5F9)
                                ratio >= 1f -> Color(0xFF1A7A4A)
                                ratio >= 0.5f -> Color(0xFF4CAF50)
                                else -> Color(0xFFA8D5B5)
                            }

                            val isInspected = inspectedDay == d

                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(cellBg)
                                    .then(
                                        if (isInspected) Modifier.border(2.dp, Color(0xFF0F172A), RoundedCornerShape(8.dp))
                                        else Modifier
                                    )
                                    .clickable { inspectedDay = d }
                                    .testTag("heatmap_day_$d"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$d",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (ratio >= 0.5f) Color.White else Color(0xFF334155)
                                )
                            }
                        }
                    }

                    // Inspected Day Breakdown Banner
                    Spacer(modifier = Modifier.height(14.dp))
                    val inspCount = dailyCounts[inspectedDay] ?: 0
                    val inspPct = if (exercisesPerDayTarget > 0) ((inspCount.toFloat() / exercisesPerDayTarget) * 100).toInt() else 0

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = phase.lightColor
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "${phase.label} • Day $inspectedDay",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = phase.primaryColor
                                )
                                Text(
                                    text = "$inspCount of $exercisesPerDayTarget exercises done ($inspPct%)",
                                    fontSize = 11.sp,
                                    color = Color(0xFF475569)
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(phase.primaryColor)
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (inspCount >= exercisesPerDayTarget) "Complete ✓" else "In Progress",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section & Category Frequency Breakdown
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("category_breakdown_card"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PieChart,
                            contentDescription = "Category",
                            tint = phase.primaryColor,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Category Exercise Distribution",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1E293B)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    phase.sections.forEachIndexed { sIdx, sec ->
                        var secDoneCount = 0
                        val secTotalCount = sec.exercises.size * PHASE_DAYS

                        for (d in 1..PHASE_DAYS) {
                            sec.exercises.indices.forEach { eIdx ->
                                if (completionsMap[ExerciseData.makeKey(phase.id, d, sIdx, eIdx)] == true) {
                                    secDoneCount++
                                }
                            }
                        }

                        val secRatio = if (secTotalCount > 0) secDoneCount.toFloat() / secTotalCount else 0f
                        val secPct = (secRatio * 100).toInt()

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = sec.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF334155)
                                )
                                Text(
                                    text = "$secDoneCount / $secTotalCount ($secPct%)",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = phase.primaryColor
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            LinearProgressIndicator(
                                progress = { secRatio },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = phase.primaryColor,
                                trackColor = Color(0xFFE2E8F0)
                            )
                        }
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(24.dp)) }
    }
}

@Composable
private fun KpiMetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.testTag("kpi_card_$title"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0))
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
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF64748B)
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = color,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0F172A)
            )

            Text(
                text = subtitle,
                fontSize = 10.sp,
                color = Color(0xFF94A3B8),
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun HeatmapLegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
        )
        Text(text = label, fontSize = 9.sp, color = Color(0xFF64748B))
    }
}
