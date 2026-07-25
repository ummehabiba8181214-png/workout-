package com.example.data

import androidx.compose.ui.graphics.Color

data class Exercise(
    val name: String,
    val reps: String
) {
    fun getSuggestedTimerSeconds(): Int {
        val text = "$name $reps".lowercase()
        val secMatch = Regex("(\\d+)\\s*sec").find(text)
        if (secMatch != null) {
            return secMatch.groupValues[1].toIntOrNull() ?: 30
        }
        val minMatch = Regex("(\\d+)\\s*min").find(text)
        if (minMatch != null) {
            val mins = minMatch.groupValues[1].toIntOrNull() ?: 1
            return mins * 60
        }
        return 30
    }

    fun isTimedExercise(): Boolean {
        val text = "$name $reps".lowercase()
        return text.contains("sec") || text.contains("min") || text.contains("hold") ||
                text.contains("plank") || text.contains("sit") || text.contains("bike") ||
                text.contains("walk") || text.contains("pose")
    }
}

data class Section(
    val title: String,
    val exercises: List<Exercise>
)

data class Phase(
    val id: Int,
    val label: String,
    val months: String,
    val level: String,
    val emoji: String,
    val primaryColorHex: String,
    val lightColorHex: String,
    val borderColorHex: String,
    val sections: List<Section>
) {
    val primaryColor: Color get() = Color(android.graphics.Color.parseColor(primaryColorHex))
    val lightColor: Color get() = Color(android.graphics.Color.parseColor(lightColorHex))
    val borderColor: Color get() = Color(android.graphics.Color.parseColor(borderColorHex))
}

object ExerciseData {
    val phases = listOf(
        Phase(
            id = 1,
            label = "Phase 1",
            months = "Month 1–2",
            level = "Foundation",
            emoji = "🟢",
            primaryColorHex = "#059669",
            lightColorHex = "#ECFDF5",
            borderColorHex = "#6EE7B7",
            sections = listOf(
                Section(
                    title = "🫁 Core & Recovery",
                    exercises = listOf(
                        Exercise("Diaphragmatic Breathing", "10 baar"),
                        Exercise("Pelvic Tilts", "15 baar"),
                        Exercise("Heel Slides", "10 baar"),
                        Exercise("Dead Bug", "10 baar"),
                        Exercise("Bird Dog", "10 baar each side"),
                        Exercise("Glute Bridges", "10 baar")
                    )
                ),
                Section(
                    title = "😊 Face & Double Chin",
                    exercises = listOf(
                        Exercise("Chin Tucks", "15 baar"),
                        Exercise("Sky Kisses", "10 baar"),
                        Exercise("Tongue Press", "10 baar"),
                        Exercise("Neck Rolls", "5 baar"),
                        Exercise("Jaw Release", "10 baar"),
                        Exercise("Cheek Puffs", "10 baar"),
                        Exercise("Fish Face", "10 baar")
                    )
                ),
                Section(
                    title = "🚴 Cardio",
                    exercises = listOf(
                        Exercise("Stationary Bike", "20 min forward + 10 min reverse"),
                        Exercise("Evening Walk", "20–30 min")
                    )
                )
            )
        ),
        Phase(
            id = 2,
            label = "Phase 2",
            months = "Month 3–4",
            level = "Intermediate",
            emoji = "🟡",
            primaryColorHex = "#D97706",
            lightColorHex = "#FFFBEB",
            borderColorHex = "#FCD34D",
            sections = listOf(
                Section(
                    title = "🏋️ Core (New Additions)",
                    exercises = listOf(
                        Exercise("Modified Plank", "20 sec hold — 5x"),
                        Exercise("Side Lying Leg Raises", "10 baar each side"),
                        Exercise("Clamshells", "15 baar each side"),
                        Exercise("Seated Marching", "20 baar")
                    )
                ),
                Section(
                    title = "💪 Upper Body",
                    exercises = listOf(
                        Exercise("Wall Push-ups", "10 baar — 3 sets"),
                        Exercise("Shoulder Rolls + Arm Circles", "10 baar"),
                        Exercise("Seated Dumbbell Shoulder Press", "Light weight — 10 baar"),
                        Exercise("Bicep Curls", "Light weight — 10 baar each")
                    )
                ),
                Section(
                    title = "🧘 Stretching",
                    exercises = listOf(
                        Exercise("Cat-Cow Stretch", "10 baar"),
                        Exercise("Child's Pose", "30 sec hold"),
                        Exercise("Hip Flexor Stretch", "20 sec each side"),
                        Exercise("Seated Spinal Twist", "10 baar each side")
                    )
                ),
                Section(
                    title = "🚴 Cardio (Upgraded)",
                    exercises = listOf(
                        Exercise("Stationary Bike", "25 min forward + 10 min reverse"),
                        Exercise("Brisk Walk", "30–40 min")
                    )
                )
            )
        ),
        Phase(
            id = 3,
            label = "Phase 3",
            months = "Month 5–6",
            level = "Progressive",
            emoji = "🔴",
            primaryColorHex = "#E11D48",
            lightColorHex = "#FFF1F2",
            borderColorHex = "#FDA4AF",
            sections = listOf(
                Section(
                    title = "🧱 Core (Advanced)",
                    exercises = listOf(
                        Exercise("Full Plank", "30 sec hold — 5x"),
                        Exercise("Modified Side Plank", "20 sec each side"),
                        Exercise("Superman Hold", "10 baar"),
                        Exercise("Standing Oblique Crunches", "10 baar each side")
                    )
                ),
                Section(
                    title = "🦵 Lower Body",
                    exercises = listOf(
                        Exercise("Bodyweight Squats", "15 baar — 3 sets"),
                        Exercise("Reverse Lunges", "10 baar each side"),
                        Exercise("Standing Leg Raises", "10 baar each side"),
                        Exercise("Wall Sit", "20–30 sec hold"),
                        Exercise("Calf Raises", "15 baar")
                    )
                ),
                Section(
                    title = "💪 Upper Body (Advanced)",
                    exercises = listOf(
                        Exercise("Knee Push-ups", "10 baar — 3 sets"),
                        Exercise("Resistance Band Rows", "15 baar"),
                        Exercise("Lateral Raises", "Light weight — 10 baar"),
                        Exercise("Tricep Dips (Chair)", "10 baar")
                    )
                ),
                Section(
                    title = "🚴 Cardio (Maximum)",
                    exercises = listOf(
                        Exercise("Stationary Bike", "30 min forward + 15 min reverse"),
                        Exercise("Brisk Walk", "40–45 min"),
                        Exercise("Stairs", "Jab ho sake — bonus!")
                    )
                )
            )
        )
    )

    val safetyWarnings = listOf(
        "Dard ho toh FORAN band karo",
        "Crunches, sit-ups — KABHI NAHI",
        "Pet pe direct pressure — KABHI NAHI",
        "Pehle warm up, baad mein cool down",
        "Paani exercise se pehle aur baad"
    )

    fun makeKey(phaseId: Int, day: Int, sectionIdx: Int, exerciseIdx: Int): String {
        return "p${phaseId}-d${day}-s${sectionIdx}-e${exerciseIdx}"
    }
}
