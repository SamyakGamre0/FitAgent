package com.FitAgent

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.FitAgent.adapters.ExerciseAdapter
import com.FitAgent.models.Exercise
import com.google.android.material.button.MaterialButton

class WorkoutPlanActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout_plan)

        val goal = intent.getStringExtra("GOAL") ?: "No Goal"
        val equipment = intent.getStringExtra("EQUIPMENT") ?: "No Equipment"

        val tvSubtitle = findViewById<TextView>(R.id.tv_plan_subtitle)
        tvSubtitle.text = "$goal | $equipment"

        val exercises = com.FitAgent.utils.WorkoutGenerator.generate(goal, equipment)

        val recyclerView = findViewById<RecyclerView>(R.id.recycler_exercises)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = ExerciseAdapter(exercises)

        val btnStart = findViewById<MaterialButton>(R.id.btn_start_logging)
        btnStart.setOnClickListener {
            val intent = Intent(this, LoggerActivity::class.java)
            intent.putExtra("GOAL", goal)
            intent.putExtra("EQUIPMENT", equipment)
            startActivity(intent)
        }
    }
}