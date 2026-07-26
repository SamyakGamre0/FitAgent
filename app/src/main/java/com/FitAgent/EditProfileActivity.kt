package com.FitAgent

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.FitAgent.database.AppDatabase
import com.FitAgent.models.UserProfile
import com.FitAgent.utils.ProfileManager
import com.FitAgent.utils.UnitConverter
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class EditProfileActivity : AppCompatActivity() {

    private lateinit var etName: TextInputEditText
    private lateinit var etAge: TextInputEditText
    private lateinit var rgGender: RadioGroup

    private lateinit var layoutHeightCm: LinearLayout
    private lateinit var layoutHeightFt: LinearLayout
    private lateinit var etHeightCm: TextInputEditText
    private lateinit var etHeightFt: TextInputEditText
    private lateinit var etHeightIn: TextInputEditText
    private lateinit var btnToggleHeight: MaterialButton
    private lateinit var btnToggleHeightFt: MaterialButton

    private lateinit var etWeight: TextInputEditText
    private lateinit var btnToggleWeight: MaterialButton

    private lateinit var spinnerGoal: Spinner
    private lateinit var spinnerEquipment: Spinner
    private lateinit var spinnerDays: Spinner
    private lateinit var btnSave: MaterialButton

    private lateinit var layoutBmiPreview: LinearLayout
    private lateinit var tvBmiValue: TextView
    private lateinit var tvBmiCategory: TextView

    private var currentHeightUnit = "cm"
    private var currentWeightUnit = "kg"

    private lateinit var existingProfile: UserProfile

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        bindViews()
        setupSpinners()
        loadExistingProfile()
        setupUnitToggles()
        setupLiveBMI()

        btnSave.setOnClickListener { saveProfile() }
    }

    private fun bindViews() {
        etName = findViewById(R.id.et_edit_name)
        etAge = findViewById(R.id.et_edit_age)
        rgGender = findViewById(R.id.rg_edit_gender)

        layoutHeightCm = findViewById(R.id.layout_edit_height_cm)
        layoutHeightFt = findViewById(R.id.layout_edit_height_ft)
        etHeightCm = findViewById(R.id.et_edit_height_cm)
        etHeightFt = findViewById(R.id.et_edit_height_ft)
        etHeightIn = findViewById(R.id.et_edit_height_in)
        btnToggleHeight = findViewById(R.id.btn_edit_toggle_height)
        btnToggleHeightFt = findViewById(R.id.btn_edit_toggle_height_ft)

        etWeight = findViewById(R.id.et_edit_weight)
        btnToggleWeight = findViewById(R.id.btn_edit_toggle_weight)

        spinnerGoal = findViewById(R.id.spinner_edit_goal)
        spinnerEquipment = findViewById(R.id.spinner_edit_equipment)
        spinnerDays = findViewById(R.id.spinner_edit_days)
        btnSave = findViewById(R.id.btn_save_edit_profile)

        layoutBmiPreview = findViewById(R.id.layout_edit_bmi_preview)
        tvBmiValue = findViewById(R.id.tv_edit_bmi_value)
        tvBmiCategory = findViewById(R.id.tv_edit_bmi_category)
    }

    private fun loadExistingProfile() {
        existingProfile = ProfileManager.loadProfile(this)

        etName.setText(existingProfile.name)
        etAge.setText(existingProfile.age.toString())

        when (existingProfile.gender) {
            "Male" -> rgGender.check(R.id.rb_edit_male)
            "Female" -> rgGender.check(R.id.rb_edit_female)
            "Other" -> rgGender.check(R.id.rb_edit_other)
        }

        currentHeightUnit = existingProfile.preferredHeightUnit
        currentWeightUnit = existingProfile.preferredWeightUnit

        if (currentHeightUnit == "cm") {
            etHeightCm.setText(String.format("%.0f", existingProfile.heightCm))
        } else {
            val (ft, inches) = UnitConverter.cmToFeetInches(existingProfile.heightCm)
            etHeightFt.setText(ft.toString())
            etHeightIn.setText(inches.toString())
        }
        updateHeightUI()

        val displayWeight = if (currentWeightUnit == "lbs") {
            UnitConverter.kgToLbs(existingProfile.weightKg)
        } else {
            existingProfile.weightKg
        }
        etWeight.setText(String.format("%.1f", displayWeight))
        updateWeightUI()

        setSpinnerSelection(spinnerGoal, existingProfile.goal)
        setSpinnerSelection(spinnerEquipment, existingProfile.equipment)
        setSpinnerSelection(spinnerDays, existingProfile.daysPerWeek)

        updateBMIPreview()
    }

    private fun setSpinnerSelection(spinner: Spinner, value: String) {
        val adapter = spinner.adapter
        for (i in 0 until adapter.count) {
            if (adapter.getItem(i).toString() == value) {
                spinner.setSelection(i)
                return
            }
        }
    }

    private fun updateHeightUI() {
        if (currentHeightUnit == "cm") {
            layoutHeightCm.visibility = View.VISIBLE
            layoutHeightFt.visibility = View.GONE
            btnToggleHeight.text = "cm"
        } else {
            layoutHeightCm.visibility = View.GONE
            layoutHeightFt.visibility = View.VISIBLE
            btnToggleHeightFt.text = "ft/in"
        }
    }

    private fun updateWeightUI() {
        btnToggleWeight.text = currentWeightUnit
    }

    private fun setupUnitToggles() {
        btnToggleHeight.setOnClickListener {
            val currentCm = etHeightCm.text.toString().toDoubleOrNull() ?: 0.0
            currentHeightUnit = "ft"
            if (currentCm > 0) {
                val (ft, inches) = UnitConverter.cmToFeetInches(currentCm)
                etHeightFt.setText(ft.toString())
                etHeightIn.setText(inches.toString())
            }
            updateHeightUI()
            updateBMIPreview()
        }

        btnToggleHeightFt.setOnClickListener {
            val ft = etHeightFt.text.toString().toIntOrNull() ?: 0
            val inches = etHeightIn.text.toString().toIntOrNull() ?: 0
            currentHeightUnit = "cm"
            if (ft > 0 || inches > 0) {
                val cm = UnitConverter.feetInchesToCm(ft, inches)
                etHeightCm.setText(String.format("%.1f", cm))
            }
            updateHeightUI()
            updateBMIPreview()
        }

        btnToggleWeight.setOnClickListener {
            val currentValue = etWeight.text.toString().toDoubleOrNull() ?: 0.0
            currentWeightUnit = if (currentWeightUnit == "kg") "lbs" else "kg"
            if (currentValue > 0) {
                val convertedValue = if (currentWeightUnit == "lbs") {
                    UnitConverter.kgToLbs(currentValue)
                } else {
                    UnitConverter.lbsToKg(currentValue)
                }
                etWeight.setText(String.format("%.1f", convertedValue))
            }
            updateWeightUI()
            updateBMIPreview()
        }
    }

    private fun setupLiveBMI() {
        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                updateBMIPreview()
            }
        }
        etHeightCm.addTextChangedListener(watcher)
        etHeightFt.addTextChangedListener(watcher)
        etHeightIn.addTextChangedListener(watcher)
        etWeight.addTextChangedListener(watcher)
    }

    private fun updateBMIPreview() {
        val heightCm = getCurrentHeightInCm()
        val weightKg = getCurrentWeightInKg()

        if (heightCm > 0 && weightKg > 0) {
            val tempProfile = UserProfile(heightCm = heightCm, weightKg = weightKg)
            tvBmiValue.text = String.format("%.1f", tempProfile.calculateBMI())
            tvBmiCategory.text = " (${tempProfile.getBMICategory()})"
            layoutBmiPreview.visibility = View.VISIBLE
        } else {
            layoutBmiPreview.visibility = View.GONE
        }
    }

    private fun getCurrentHeightInCm(): Double {
        return if (currentHeightUnit == "cm") {
            etHeightCm.text.toString().toDoubleOrNull() ?: 0.0
        } else {
            val ft = etHeightFt.text.toString().toIntOrNull() ?: 0
            val inches = etHeightIn.text.toString().toIntOrNull() ?: 0
            if (ft == 0 && inches == 0) 0.0 else UnitConverter.feetInchesToCm(ft, inches)
        }
    }

    private fun getCurrentWeightInKg(): Double {
        val value = etWeight.text.toString().toDoubleOrNull() ?: 0.0
        return if (currentWeightUnit == "kg") value else UnitConverter.lbsToKg(value)
    }

    private fun setupSpinners() {
        val goals = arrayOf(
            "Muscle Gain 💪",
            "Weight Loss 🔥",
            "General Fitness ⚡",
            "Build Endurance 🏃"
        )
        spinnerGoal.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, goals
        )

        val equipment = arrayOf(
            "No Equipment 🏠",
            "Dumbbells Only 🏋️",
            "Full Gym 🏟️",
            "Resistance Bands 🎽"
        )
        spinnerEquipment.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, equipment
        )

        val days = arrayOf(
            "3 days",
            "4 days",
            "5 days",
            "6 days"
        )
        spinnerDays.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, days
        )
    }

    private fun saveProfile() {
        val name = etName.text?.toString()?.trim() ?: ""
        val ageStr = etAge.text?.toString()?.trim() ?: ""

        val gender = when (rgGender.checkedRadioButtonId) {
            R.id.rb_edit_male -> "Male"
            R.id.rb_edit_female -> "Female"
            R.id.rb_edit_other -> "Other"
            else -> ""
        }

        val heightCm = getCurrentHeightInCm()
        val weightKg = getCurrentWeightInKg()

        val goal = spinnerGoal.selectedItem.toString()
        val equipment = spinnerEquipment.selectedItem.toString()
        val days = spinnerDays.selectedItem.toString()

        if (name.isEmpty() || ageStr.isEmpty() || gender.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            return
        }

        val age = ageStr.toIntOrNull() ?: 0
        if (age <= 0 || age > 120) {
            Toast.makeText(this, "Please enter a valid age", Toast.LENGTH_SHORT).show()
            return
        }
        if (heightCm < 50 || heightCm > 250) {
            Toast.makeText(this, "Please enter valid height", Toast.LENGTH_SHORT).show()
            return
        }
        if (weightKg < 20 || weightKg > 300) {
            Toast.makeText(this, "Please enter valid weight", Toast.LENGTH_SHORT).show()
            return
        }

        val updatedProfile = UserProfile(
            name = name,
            age = age,
            gender = gender,
            heightCm = heightCm,
            weightKg = weightKg,
            goal = goal,
            equipment = equipment,
            daysPerWeek = days,
            preferredWeightUnit = currentWeightUnit,
            preferredHeightUnit = currentHeightUnit,
            isProfileComplete = true
        )

        ProfileManager.saveProfile(this, updatedProfile)
        Toast.makeText(this, "✅ Profile updated!", Toast.LENGTH_SHORT).show()
        finish()
    }

    // Kept for future Settings screen (Phase 4)
    private fun confirmResetAllData() {
        MaterialAlertDialogBuilder(this)
            .setTitle("⚠️ Reset Everything?")
            .setMessage("This will permanently delete:\n\n• Your profile\n• All workout history\n• All progress data\n\nThis CANNOT be undone. Are you absolutely sure?")
            .setPositiveButton("Yes, Delete Everything") { _, _ ->
                performReset()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performReset() {
        val db = AppDatabase.getDatabase(this)
        lifecycleScope.launch {
            val allSessions = db.workoutDao().getAllSessions()
            allSessions.forEach { session ->
                db.workoutDao().deleteLogsForSession(session.id)
                db.workoutDao().deleteSession(session)
            }

            ProfileManager.clearProfile(this@EditProfileActivity)

            Toast.makeText(this@EditProfileActivity, "All data cleared", Toast.LENGTH_LONG).show()

            val intent = Intent(this@EditProfileActivity, ProfileSetupActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}