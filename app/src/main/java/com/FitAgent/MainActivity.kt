package com.FitAgent

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {

    private lateinit var spinnerGoal: Spinner
    private lateinit var spinnerEquipment: Spinner
    private lateinit var spinnerDays: Spinner
    private lateinit var btnGenerate: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        spinnerGoal = findViewById(R.id.spinner_goal)
        spinnerEquipment = findViewById(R.id.spinner_equipment)
        spinnerDays = findViewById(R.id.spinner_days)
        btnGenerate = findViewById(R.id.btn_generate)

        setupSpinners()

        btnGenerate.setOnClickListener {
            handleGenerateClick()
        }
    }

    private fun setupSpinners() {
        val goals = arrayOf(
            "Choose your goal...",
            "Muscle Gain 💪",
            "Weight Loss 🔥",
            "General Fitness ⚡",
            "Build Endurance 🏃"
        )
        val goalsAdapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_item, goals)
        goalsAdapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item)
        spinnerGoal.adapter = goalsAdapter

        val equipment = arrayOf(
            "Choose equipment...",
            "No Equipment 🏠",
            "Dumbbells Only 🏋️",
            "Full Gym 🏟️",
            "Resistance Bands 🎽"
        )
        val equipAdapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_item, equipment)
        equipAdapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item)
        spinnerEquipment.adapter = equipAdapter

        val days = arrayOf(
            "Select days...",
            "3 days",
            "4 days",
            "5 days",
            "6 days"
        )
        val daysAdapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_item, days)
        daysAdapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item)
        spinnerDays.adapter = daysAdapter
    }

    private fun handleGenerateClick() {
        val selectedGoal = spinnerGoal.selectedItem.toString()
        val selectedEquipment = spinnerEquipment.selectedItem.toString()
        val selectedDays = spinnerDays.selectedItem.toString()

        if (selectedGoal.startsWith("Choose") ||
            selectedEquipment.startsWith("Choose") ||
            selectedDays.startsWith("Select")) {
            Toast.makeText(this,
                "Please select all options",
                Toast.LENGTH_SHORT).show()
            return
        }

        Toast.makeText(this,
            "Generating workout for $selectedGoal",
            Toast.LENGTH_SHORT).show()
    }
}