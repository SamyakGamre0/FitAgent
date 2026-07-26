package com.FitAgent.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.FitAgent.EditProfileActivity
import com.FitAgent.R
import com.FitAgent.utils.ProfileManager
import com.FitAgent.utils.UnitConverter
import com.google.android.material.button.MaterialButton

class ProfileFragment : Fragment() {

    private lateinit var tvName: TextView
    private lateinit var tvDetails: TextView
    private lateinit var tvHeight: TextView
    private lateinit var tvWeight: TextView
    private lateinit var tvBmi: TextView
    private lateinit var tvGoal: TextView
    private lateinit var tvEquipment: TextView
    private lateinit var tvDays: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvName = view.findViewById(R.id.tv_profile_name)
        tvDetails = view.findViewById(R.id.tv_profile_details)
        tvHeight = view.findViewById(R.id.tv_profile_height)
        tvWeight = view.findViewById(R.id.tv_profile_weight)
        tvBmi = view.findViewById(R.id.tv_profile_bmi)
        tvGoal = view.findViewById(R.id.tv_profile_goal)
        tvEquipment = view.findViewById(R.id.tv_profile_equipment)
        tvDays = view.findViewById(R.id.tv_profile_days)

        view.findViewById<MaterialButton>(R.id.btn_edit_profile_from_tab).setOnClickListener {
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadProfileData()
    }

    private fun loadProfileData() {
        val profile = ProfileManager.loadProfile(requireContext())

        tvName.text = profile.name.ifEmpty { "User" }
        tvDetails.text = "${profile.age} years • ${profile.gender}"

        tvHeight.text = "Height: ${UnitConverter.formatHeight(profile.heightCm, profile.preferredHeightUnit)}"
        tvWeight.text = "Weight: ${UnitConverter.formatWeight(profile.weightKg, profile.preferredWeightUnit)}"

        val bmi = profile.calculateBMI()
        tvBmi.text = "BMI: ${String.format("%.1f", bmi)} (${profile.getBMICategory()})"

        tvGoal.text = "Goal: ${profile.goal}"
        tvEquipment.text = "Equipment: ${profile.equipment}"
        tvDays.text = "Days per Week: ${profile.daysPerWeek}"
    }
}