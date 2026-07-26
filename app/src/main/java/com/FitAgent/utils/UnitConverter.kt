package com.FitAgent.utils

import kotlin.math.roundToInt

object UnitConverter {

    // ═══ WEIGHT CONVERSIONS ═══
    fun kgToLbs(kg: Double): Double = kg * 2.20462
    fun lbsToKg(lbs: Double): Double = lbs / 2.20462

    // ═══ HEIGHT CONVERSIONS ═══
    fun cmToInches(cm: Double): Double = cm / 2.54
    fun inchesToCm(inches: Double): Double = inches * 2.54

    // Convert cm to feet + remaining inches
    // Example: 175 cm → Pair(5, 9)  meaning 5 feet 9 inches
    fun cmToFeetInches(cm: Double): Pair<Int, Int> {
        if (cm <= 0) return Pair(0, 0)
        val totalInches = cmToInches(cm)
        val feet = (totalInches / 12).toInt()
        val inches = (totalInches % 12).roundToInt()
        // Handle edge case: if inches rounds to 12, bump feet up
        return if (inches == 12) Pair(feet + 1, 0) else Pair(feet, inches)
    }

    // Convert feet + inches to cm
    // Example: 5 feet 9 inches → 175.26 cm
    fun feetInchesToCm(feet: Int, inches: Int): Double {
        val totalInches = (feet * 12) + inches
        return inchesToCm(totalInches.toDouble())
    }

    // ═══ DISPLAY FORMATTERS ═══
    // Returns nicely formatted weight string based on unit preference
    fun formatWeight(kg: Double, unit: String): String {
        return if (unit == "lbs") {
            "${String.format("%.1f", kgToLbs(kg))} lbs"
        } else {
            "${String.format("%.1f", kg)} kg"
        }
    }

    // Returns nicely formatted height string
    // Example (cm mode): "175 cm"
    // Example (ft mode): "5'9\""
    fun formatHeight(cm: Double, unit: String): String {
        return if (unit == "ft") {
            val (feet, inches) = cmToFeetInches(cm)
            "${feet}'${inches}\""
        } else {
            "${cm.toInt()} cm"
        }
    }

    // ═══ AUTO-DETECT DEFAULT UNIT FROM PHONE LOCALE ═══
    // Countries that primarily use imperial system
    private val imperialCountries = setOf("US", "LR", "MM")

    fun detectDefaultWeightUnit(): String {
        val country = java.util.Locale.getDefault().country
        return if (country in imperialCountries) "lbs" else "kg"
    }

    fun detectDefaultHeightUnit(): String {
        val country = java.util.Locale.getDefault().country
        return if (country in imperialCountries) "ft" else "cm"
    }
}
