package com.FitAgent.utils

import com.FitAgent.models.Exercise
import com.FitAgent.models.ExerciseType

object WorkoutGenerator {

    fun generate(goal: String, equipment: String): List<Exercise> {
        val goalKey = when {
            goal.contains("Muscle", true) -> "muscle"
            goal.contains("Weight Loss", true) -> "weightloss"
            goal.contains("Endurance", true) -> "endurance"
            else -> "general"
        }

        val equipKey = when {
            equipment.contains("No Equipment", true) -> "none"
            equipment.contains("Dumbbell", true) -> "dumbbell"
            equipment.contains("Full Gym", true) -> "gym"
            equipment.contains("Resistance", true) -> "bands"
            else -> "none"
        }

        return when ("$goalKey-$equipKey") {

            // MUSCLE GAIN
            "muscle-none" -> listOf(
                Exercise("Push-ups", 4, "12-15", "Chest", "Keep core tight", ExerciseType.BODYWEIGHT),
                Exercise("Diamond Push-ups", 3, "10", "Triceps", "Form diamond with hands", ExerciseType.BODYWEIGHT),
                Exercise("Bodyweight Squats", 4, "20", "Legs", "Go deep", ExerciseType.BODYWEIGHT),
                Exercise("Pike Push-ups", 3, "10", "Shoulders", "Elevate hips high", ExerciseType.BODYWEIGHT),
                Exercise("Plank", 3, "60", "Core", "Straight body line", ExerciseType.TIMED)
            )
            "muscle-dumbbell" -> listOf(
                Exercise("Dumbbell Bench Press", 4, "8-10", "Chest", "Full range of motion", ExerciseType.WEIGHTED),
                Exercise("Dumbbell Rows", 4, "10", "Back", "Squeeze shoulder blades", ExerciseType.WEIGHTED),
                Exercise("Dumbbell Shoulder Press", 3, "10", "Shoulders", "Elbows slightly forward", ExerciseType.WEIGHTED),
                Exercise("Dumbbell Bicep Curls", 3, "12", "Biceps", "Control the negative", ExerciseType.WEIGHTED),
                Exercise("Goblet Squats", 4, "12", "Legs", "Chest up", ExerciseType.WEIGHTED)
            )
            "muscle-gym" -> listOf(
                Exercise("Barbell Bench Press", 4, "6-8", "Chest", "Retract shoulder blades", ExerciseType.WEIGHTED),
                Exercise("Deadlifts", 4, "5", "Back/Legs", "Neutral spine", ExerciseType.WEIGHTED),
                Exercise("Barbell Squats", 4, "8", "Legs", "Break parallel", ExerciseType.WEIGHTED),
                Exercise("Pull-ups", 3, "8-10", "Back", "Full hang at bottom", ExerciseType.BODYWEIGHT),
                Exercise("Overhead Press", 3, "8", "Shoulders", "Tight core", ExerciseType.WEIGHTED)
            )
            "muscle-bands" -> listOf(
                Exercise("Band Chest Press", 4, "12", "Chest", "Slow eccentric", ExerciseType.WEIGHTED),
                Exercise("Band Rows", 4, "12", "Back", "Pull to hips", ExerciseType.WEIGHTED),
                Exercise("Band Squats", 4, "15", "Legs", "Bands under feet", ExerciseType.WEIGHTED),
                Exercise("Band Shoulder Press", 3, "12", "Shoulders", "Stand on band", ExerciseType.WEIGHTED),
                Exercise("Band Bicep Curls", 3, "15", "Biceps", "Full squeeze at top", ExerciseType.WEIGHTED)
            )

            // WEIGHT LOSS
            "weightloss-none" -> listOf(
                Exercise("Burpees", 4, "15", "Full Body", "Explode up", ExerciseType.BODYWEIGHT),
                Exercise("Mountain Climbers", 4, "60", "Core/Cardio", "Fast pace", ExerciseType.TIMED),
                Exercise("Jumping Jacks", 4, "45", "Cardio", "Keep tempo high", ExerciseType.TIMED),
                Exercise("High Knees", 3, "45", "Cardio", "Drive knees up", ExerciseType.TIMED),
                Exercise("Squat Jumps", 4, "12", "Legs/Cardio", "Land softly", ExerciseType.BODYWEIGHT)
            )
            "weightloss-dumbbell" -> listOf(
                Exercise("Dumbbell Thrusters", 4, "12", "Full Body", "Explode from squat", ExerciseType.WEIGHTED),
                Exercise("Renegade Rows", 3, "10", "Back/Core", "Stable plank", ExerciseType.WEIGHTED),
                Exercise("Dumbbell Swings", 4, "15", "Posterior/Cardio", "Hinge at hips", ExerciseType.WEIGHTED),
                Exercise("Dumbbell Lunges", 3, "12 each", "Legs", "Knee tracks toes", ExerciseType.WEIGHTED),
                Exercise("Burpees", 3, "10", "Full Body", "Full extension", ExerciseType.BODYWEIGHT)
            )
            "weightloss-gym" -> listOf(
                Exercise("Rowing Machine", 3, "300", "Full Body", "Steady pace", ExerciseType.TIMED),
                Exercise("Kettlebell Swings", 4, "20", "Posterior/Cardio", "Hips drive movement", ExerciseType.WEIGHTED),
                Exercise("Battle Ropes", 4, "30", "Full Body", "Alternating slams", ExerciseType.TIMED),
                Exercise("Box Jumps", 4, "10", "Legs", "Soft landing", ExerciseType.BODYWEIGHT),
                Exercise("Assault Bike Sprints", 5, "20", "Cardio", "All out effort", ExerciseType.TIMED)
            )
            "weightloss-bands" -> listOf(
                Exercise("Band Squats to Press", 4, "12", "Full Body", "Continuous motion", ExerciseType.WEIGHTED),
                Exercise("Band Woodchoppers", 3, "12 each", "Core", "Rotate through core", ExerciseType.WEIGHTED),
                Exercise("Jumping Jacks", 4, "45", "Cardio", "Add band pull-aparts", ExerciseType.TIMED),
                Exercise("Band Deadlifts", 4, "15", "Posterior", "Neutral spine", ExerciseType.WEIGHTED),
                Exercise("Mountain Climbers", 3, "45", "Cardio/Core", "Fast", ExerciseType.TIMED)
            )

            // ENDURANCE
            "endurance-none" -> listOf(
                Exercise("Running in Place", 4, "120", "Cardio", "Land midfoot", ExerciseType.TIMED),
                Exercise("Jumping Jacks", 4, "90", "Cardio", "Keep rhythm", ExerciseType.TIMED),
                Exercise("Plank", 3, "90", "Core", "Straight line", ExerciseType.TIMED),
                Exercise("Wall Sit", 3, "60", "Legs", "90 degrees", ExerciseType.TIMED),
                Exercise("Push-ups", 3, "15-20", "Chest", "Steady tempo", ExerciseType.BODYWEIGHT)
            )
            "endurance-dumbbell" -> listOf(
                Exercise("Dumbbell Farmer Walk", 4, "60", "Grip/Core", "Tall posture", ExerciseType.TIMED),
                Exercise("Dumbbell Step-ups", 3, "15 each", "Legs", "Drive through heel", ExerciseType.WEIGHTED),
                Exercise("Dumbbell Rows", 3, "15", "Back", "Higher reps", ExerciseType.WEIGHTED),
                Exercise("Dumbbell Squats", 3, "20", "Legs", "Full depth", ExerciseType.WEIGHTED),
                Exercise("Plank", 3, "90", "Core", "Rock solid", ExerciseType.TIMED)
            )
            "endurance-gym" -> listOf(
                Exercise("Treadmill Run", 1, "1200", "Cardio", "Zone 2 pace", ExerciseType.TIMED),
                Exercise("Cycling", 1, "900", "Cardio/Legs", "Steady cadence", ExerciseType.TIMED),
                Exercise("Rowing Machine", 3, "300", "Full Body", "Legs then arms", ExerciseType.TIMED),
                Exercise("Pull-ups", 3, "10", "Back", "Full range", ExerciseType.BODYWEIGHT),
                Exercise("Plank", 3, "120", "Core", "Long hold", ExerciseType.TIMED)
            )
            "endurance-bands" -> listOf(
                Exercise("Band Rows High Rep", 4, "20", "Back", "Slow and controlled", ExerciseType.WEIGHTED),
                Exercise("Band Squats", 4, "25", "Legs", "Continuous tension", ExerciseType.WEIGHTED),
                Exercise("Jumping Jacks", 4, "90", "Cardio", "Keep going", ExerciseType.TIMED),
                Exercise("Band Bicep Curls", 3, "20", "Biceps", "No rest", ExerciseType.WEIGHTED),
                Exercise("Plank", 3, "90", "Core", "Steady breathing", ExerciseType.TIMED)
            )

            // GENERAL FITNESS
            "general-none" -> listOf(
                Exercise("Push-ups", 3, "12", "Chest", "Elbows 45 degrees", ExerciseType.BODYWEIGHT),
                Exercise("Bodyweight Squats", 3, "15", "Legs", "Chest up", ExerciseType.BODYWEIGHT),
                Exercise("Plank", 3, "45", "Core", "Squeeze glutes", ExerciseType.TIMED),
                Exercise("Lunges", 3, "10 each", "Legs", "Vertical shin", ExerciseType.BODYWEIGHT),
                Exercise("Mountain Climbers", 3, "30", "Core/Cardio", "Controlled", ExerciseType.TIMED)
            )
            "general-dumbbell" -> listOf(
                Exercise("Dumbbell Bench Press", 3, "12", "Chest", "Full ROM", ExerciseType.WEIGHTED),
                Exercise("Dumbbell Rows", 3, "12", "Back", "Elbow to hip", ExerciseType.WEIGHTED),
                Exercise("Goblet Squats", 3, "15", "Legs", "Elbows inside knees", ExerciseType.WEIGHTED),
                Exercise("Dumbbell Curls", 3, "12", "Biceps", "No swinging", ExerciseType.WEIGHTED),
                Exercise("Plank", 3, "45", "Core", "Neutral spine", ExerciseType.TIMED)
            )
            "general-gym" -> listOf(
                Exercise("Barbell Squats", 3, "10", "Legs", "Break parallel", ExerciseType.WEIGHTED),
                Exercise("Bench Press", 3, "10", "Chest", "Retract scapula", ExerciseType.WEIGHTED),
                Exercise("Lat Pulldown", 3, "12", "Back", "Squeeze at bottom", ExerciseType.WEIGHTED),
                Exercise("Leg Press", 3, "12", "Legs", "Full depth", ExerciseType.WEIGHTED),
                Exercise("Plank", 3, "60", "Core", "Rigid line", ExerciseType.TIMED)
            )
            "general-bands" -> listOf(
                Exercise("Band Chest Press", 3, "15", "Chest", "Slow control", ExerciseType.WEIGHTED),
                Exercise("Band Rows", 3, "15", "Back", "Full squeeze", ExerciseType.WEIGHTED),
                Exercise("Band Squats", 3, "20", "Legs", "Stand on band", ExerciseType.WEIGHTED),
                Exercise("Band Curls", 3, "15", "Biceps", "Full range", ExerciseType.WEIGHTED),
                Exercise("Plank", 3, "45", "Core", "Straight body", ExerciseType.TIMED)
            )

            else -> listOf(
                Exercise("Push-ups", 3, "12", "Chest", "Basic form", ExerciseType.BODYWEIGHT),
                Exercise("Squats", 3, "15", "Legs", "Chest up", ExerciseType.BODYWEIGHT),
                Exercise("Plank", 3, "45", "Core", "Straight line", ExerciseType.TIMED),
                Exercise("Lunges", 3, "10 each", "Legs", "Deep lunge", ExerciseType.BODYWEIGHT),
                Exercise("Mountain Climbers", 3, "30", "Cardio", "Fast pace", ExerciseType.TIMED)
            )
        }
    }
}
