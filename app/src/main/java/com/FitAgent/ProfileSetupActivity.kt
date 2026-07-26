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
import com.FitAgent.models.UserProfile
import com.FitAgent.utils.ProfileManager
import com.FitAgent.utils.UnitConverter
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class ProfileSetupActivity : AppCompatActivity() {

    private lateinit var etName: TextInputEditText
    private lateinit var etAge: TextInputEditText
    private lateinit var rgGender: RadioGroup

    // Height inputs
    private lateinit var layoutHeightCm: LinearLayout
    private lateinit var layoutHeightFt: LinearLayout
    private lateinit var etHeightCm: TextInputEditText
    private lateinit var etHeightFt: TextInputEditText
    private lateinit var etHeightIn: TextInputEditText
    private lateinit var btnToggleHeight: MaterialButton
    private lateinit var btnToggleHeightFt: MaterialButton

    // Weight input
    private lateinit var etWeight: TextInputEditText
    private lateinit var btnToggleWeight: MaterialButton

    // Preferences
    private lateinit var spinnerGoal: Spinner
    private lateinit var spinnerEquipment: Spinner
    private lateinit var spinnerDays: Spinner
    private lateinit var btnSave: MaterialButton

    private lateinit var layoutBmiPreview: LinearLayout
    private lateinit var tvBmiValue: TextView
    private lateinit var tvBmiCategory: TextView

    // Current unit modes
    private var currentHeightUnit = "cm"
    private var currentWeightUnit = "kg"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile_setup)

        bindViews()
        setupSpinners()
        applyAutoDetectedUnits()
        setupUnitToggles()
        setupLiveBMI()

        btnSave.setOnClickListener { saveProfile() }
    }

    private fun bindViews() {
        etName = findViewById(R.id.et_profile_name)
        etAge = findViewById(R.id.et_profile_age)
        rgGender = findViewById(R.id.rg_profile_gender)

        layoutHeightCm = findViewById(R.id.layout_height_cm)
        layoutHeightFt = findViewById(R.id.layout_height_ft)
        etHeightCm = findViewById(R.id.et_profile_height_cm)
        etHeightFt = findViewById(R.id.et_profile_height_ft)
        etHeightIn = findViewById(R.id.et_profile_height_in)
        btnToggleHeight = findViewById(R.id.btn_toggle_height)
        btnToggleHeightFt = findViewById(R.id.btn_toggle_height_ft)

        etWeight = findViewById(R.id.et_profile_weight)
        btnToggleWeight = findViewById(R.id.btn_toggle_weight)

        spinnerGoal = findViewById(R.id.spinner_profile_goal)
        spinnerEquipment = findViewById(R.id.spinner_profile_equipment)
        spinnerDays = findViewById(R.id.spinner_profile_days)
        btnSave = findViewById(R.id.btn_save_profile)

        layoutBmiPreview = findViewById(R.id.layout_bmi_preview)
        tvBmiValue = findViewById(R.id.tv_bmi_value)
        tvBmiCategory = findViewById(R.id.tv_bmi_category)
    }

    private fun applyAutoDetectedUnits() {
        currentHeightUnit = UnitConverter.detectDefaultHeightUnit()
        currentWeightUnit = UnitConverter.detectDefaultWeightUnit()

        updateHeightUI()
        updateWeightUI()
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
        // Height toggle from cm mode
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

        // Height toggle from ft mode
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

        // Weight toggle
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

    // Always calculate BMI using standard units (kg and cm)
    private fun updateBMIPreview() {
        val heightCm = getCurrentHeightInCm()
        val weightKg = getCurrentWeightInKg()

        if (heightCm > 0 && weightKg > 0) {
            val tempProfile = UserProfile(heightCm = heightCm, weightKg = weightKg)
            val bmi = tempProfile.calculateBMI()
            val category = tempProfile.getBMICategory()

            tvBmiValue.text = String.format("%.1f", bmi)
            tvBmiCategory.text = " ($category)"
            layoutBmiPreview.visibility = View.VISIBLE
        } else {
            layoutBmiPreview.visibility = View.GONE
        }
    }

    // Converts current input to standard cm
    private fun getCurrentHeightInCm(): Double {
        return if (currentHeightUnit == "cm") {
            etHeightCm.text.toString().toDoubleOrNull() ?: 0.0
        } else {
            val ft = etHeightFt.text.toString().toIntOrNull() ?: 0
            val inches = etHeightIn.text.toString().toIntOrNull() ?: 0
            if (ft == 0 && inches == 0) 0.0 else UnitConverter.feetInchesToCm(ft, inches)
        }
    }

    // Converts current input to standard kg
    private fun getCurrentWeightInKg(): Double {
        val value = etWeight.text.toString().toDoubleOrNull() ?: 0.0
        return if (currentWeightUnit == "kg") value else UnitConverter.lbsToKg(value)
    }

    private fun setupSpinners() {
        val goals = arrayOf(
            "Choose your goal...",
            "Muscle Gain 💪",
            "Weight Loss 🔥",
            "General Fitness ⚡",
            "Build Endurance 🏃"
        )
        spinnerGoal.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, goals
        )

        val equipment = arrayOf(
            "Choose equipment...",
            "No Equipment 🏠",
            "Dumbbells Only 🏋️",
            "Full Gym 🏟️",
            "Resistance Bands 🎽"
        )
        spinnerEquipment.adapter = ArrayAdapter(
            this, android.R.layout.simple_spinner_dropdown_item, equipment
        )

        val days = arrayOf(
            "Select days...",
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
            R.id.rb_male -> "Male"
            R.id.rb_female -> "Female"
            R.id.rb_other -> "Other"
            else -> ""
        }

        val heightCm = getCurrentHeightInCm()
        val weightKg = getCurrentWeightInKg()

        val goal = spinnerGoal.selectedItem.toString()
        val equipment = spinnerEquipment.selectedItem.toString()
        val days = spinnerDays.selectedItem.toString()

        // Validation
        if (name.isEmpty() || ageStr.isEmpty() || gender.isEmpty() ||
            goal.startsWith("Choose") || equipment.startsWith("Choose") ||
            days.startsWith("Select")) {
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

        val profile = UserProfile(
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

        ProfileManager.saveProfile(this, profile)

        Toast.makeText(this, "✅ Profile saved! Welcome, $name!", Toast.LENGTH_LONG).show()

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
