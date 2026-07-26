package com.FitAgent.utils

import android.content.Context
import android.content.SharedPreferences
import com.FitAgent.models.UserProfile

object ProfileManager {

    private const val PREFS_NAME = "fitagent_profile"
    private const val KEY_NAME = "name"
    private const val KEY_AGE = "age"
    private const val KEY_GENDER = "gender"
    private const val KEY_HEIGHT = "height"
    private const val KEY_WEIGHT = "weight"
    private const val KEY_GOAL = "goal"
    private const val KEY_EQUIPMENT = "equipment"
    private const val KEY_DAYS = "days"
    private const val KEY_WEIGHT_UNIT = "pref_weight_unit"
    private const val KEY_HEIGHT_UNIT = "pref_height_unit"
    private const val KEY_COMPLETE = "is_complete"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveProfile(context: Context, profile: UserProfile) {
        val editor = getPrefs(context).edit()
        editor.putString(KEY_NAME, profile.name)
        editor.putInt(KEY_AGE, profile.age)
        editor.putString(KEY_GENDER, profile.gender)
        editor.putFloat(KEY_HEIGHT, profile.heightCm.toFloat())
        editor.putFloat(KEY_WEIGHT, profile.weightKg.toFloat())
        editor.putString(KEY_GOAL, profile.goal)
        editor.putString(KEY_EQUIPMENT, profile.equipment)
        editor.putString(KEY_DAYS, profile.daysPerWeek)
        editor.putString(KEY_WEIGHT_UNIT, profile.preferredWeightUnit)
        editor.putString(KEY_HEIGHT_UNIT, profile.preferredHeightUnit)
        editor.putBoolean(KEY_COMPLETE, profile.isProfileComplete)
        editor.apply()
    }

    fun loadProfile(context: Context): UserProfile {
        val prefs = getPrefs(context)
        return UserProfile(
            name = prefs.getString(KEY_NAME, "") ?: "",
            age = prefs.getInt(KEY_AGE, 0),
            gender = prefs.getString(KEY_GENDER, "") ?: "",
            heightCm = prefs.getFloat(KEY_HEIGHT, 0f).toDouble(),
            weightKg = prefs.getFloat(KEY_WEIGHT, 0f).toDouble(),
            goal = prefs.getString(KEY_GOAL, "") ?: "",
            equipment = prefs.getString(KEY_EQUIPMENT, "") ?: "",
            daysPerWeek = prefs.getString(KEY_DAYS, "") ?: "",
            preferredWeightUnit = prefs.getString(KEY_WEIGHT_UNIT, UnitConverter.detectDefaultWeightUnit()) ?: "kg",
            preferredHeightUnit = prefs.getString(KEY_HEIGHT_UNIT, UnitConverter.detectDefaultHeightUnit()) ?: "cm",
            isProfileComplete = prefs.getBoolean(KEY_COMPLETE, false)
        )
    }

    fun isProfileComplete(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_COMPLETE, false)
    }

    // Quick getters for unit preferences (used across app)
    fun getWeightUnit(context: Context): String {
        return getPrefs(context).getString(KEY_WEIGHT_UNIT, UnitConverter.detectDefaultWeightUnit()) ?: "kg"
    }

    fun getHeightUnit(context: Context): String {
        return getPrefs(context).getString(KEY_HEIGHT_UNIT, UnitConverter.detectDefaultHeightUnit()) ?: "cm"
    }

    fun clearProfile(context: Context) {
        getPrefs(context).edit().clear().apply()
    }
}
