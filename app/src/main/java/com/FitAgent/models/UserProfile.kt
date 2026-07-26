package com.FitAgent.models

data class UserProfile(
    var name: String = "",
    var age: Int = 0,
    var gender: String = "",
    var heightCm: Double = 0.0,        // ALWAYS stored in cm
    var weightKg: Double = 0.0,        // ALWAYS stored in kg
    var goal: String = "",
    var equipment: String = "",
    var daysPerWeek: String = "",
    var preferredWeightUnit: String = "kg",   // "kg" or "lbs"
    var preferredHeightUnit: String = "cm",   // "cm" or "ft"
    var isProfileComplete: Boolean = false
) {
    fun calculateBMI(): Double {
        if (heightCm <= 0 || weightKg <= 0) return 0.0
        val heightM = heightCm / 100
        return weightKg / (heightM * heightM)
    }

    fun getBMICategory(): String {
        val bmi = calculateBMI()
        return when {
            bmi <= 0 -> "Not calculated"
            bmi < 18.5 -> "Underweight"
            bmi < 25 -> "Normal ✅"
            bmi < 30 -> "Overweight"
            else -> "Obese"
        }
    }

    fun getBMIColor(): String {
        val bmi = calculateBMI()
        return when {
            bmi <= 0 -> "gray"
            bmi < 18.5 -> "orange"
            bmi < 25 -> "green"
            bmi < 30 -> "orange"
            else -> "red"
        }
    }
}
