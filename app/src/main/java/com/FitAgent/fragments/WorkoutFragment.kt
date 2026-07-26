package com.FitAgent.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.FitAgent.R
import com.FitAgent.WorkoutPlanActivity
import com.FitAgent.utils.ProfileManager
import com.google.android.material.button.MaterialButton

class WorkoutFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_workout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<MaterialButton>(R.id.btn_workout_quick_start).setOnClickListener {
            val profile = ProfileManager.loadProfile(requireContext())
            val intent = Intent(requireContext(), WorkoutPlanActivity::class.java)
            intent.putExtra("GOAL", profile.goal)
            intent.putExtra("EQUIPMENT", profile.equipment)
            intent.putExtra("DAYS", profile.daysPerWeek)
            startActivity(intent)
        }
    }
}
