package com.FitAgent.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.FitAgent.R
import com.FitAgent.models.Exercise
import com.FitAgent.models.ExerciseType

class ExerciseAdapter(
    private val exercises: List<Exercise>
) : RecyclerView.Adapter<ExerciseAdapter.ExerciseViewHolder>() {

    class ExerciseViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tv_exercise_name)
        val tvMuscle: TextView = view.findViewById(R.id.tv_muscle_group)
        val tvSetsReps: TextView = view.findViewById(R.id.tv_sets_reps)
        val tvTip: TextView = view.findViewById(R.id.tv_tip)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExerciseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_exercise, parent, false)
        return ExerciseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExerciseViewHolder, position: Int) {
        val exercise = exercises[position]
        holder.tvName.text = exercise.name
        holder.tvMuscle.text = "Target: ${exercise.muscleGroup}"

        // Check if it is a TIMED exercise
        if (exercise.type == ExerciseType.TIMED) {
            // Show "Duration" instead of "Reps"
            holder.tvSetsReps.text = "Sets: ${exercise.sets} | Duration: ${exercise.reps}s"
        } else {
            // Normal weighted or bodyweight exercise
            holder.tvSetsReps.text = "Sets: ${exercise.sets} | Reps: ${exercise.reps}"
        }

        holder.tvTip.text = "💡 ${exercise.tip}"
    }

    override fun getItemCount() = exercises.size
}