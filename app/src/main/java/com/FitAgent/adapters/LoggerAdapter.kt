package com.FitAgent.adapters

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.FitAgent.R
import com.FitAgent.models.Exercise
import com.FitAgent.models.ExerciseType
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

data class LoggedExercise(
    val exercise: Exercise,
    var weight: Double = 0.0,
    var reps: Int = 0,
    var seconds: Int = 0,
    var sets: Int = 3,
    var unit: String = "kg"
)

class LoggerAdapter(
    val loggedList: MutableList<LoggedExercise>
) : RecyclerView.Adapter<LoggerAdapter.LoggerViewHolder>() {

    class LoggerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tv_log_exercise_name)
        val tvMuscle: TextView = view.findViewById(R.id.tv_log_muscle)
        val btnMinus: MaterialButton = view.findViewById(R.id.btn_minus_set)
        val btnPlus: MaterialButton = view.findViewById(R.id.btn_plus_set)
        val tvSets: TextView = view.findViewById(R.id.tv_sets_count)

        val layoutWeighted: LinearLayout = view.findViewById(R.id.layout_weighted)
        val layoutBodyweight: TextInputLayout = view.findViewById(R.id.layout_bodyweight)
        val layoutTimed: TextInputLayout = view.findViewById(R.id.layout_timed)

        val etWeight: TextInputEditText = view.findViewById(R.id.et_weight)
        val btnUnit: MaterialButton = view.findViewById(R.id.btn_unit_toggle)
        val etRepsWeighted: TextInputEditText = view.findViewById(R.id.et_reps_weighted)
        val etRepsBodyweight: TextInputEditText = view.findViewById(R.id.et_reps_bodyweight)
        val etSeconds: TextInputEditText = view.findViewById(R.id.et_seconds)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoggerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_logger, parent, false)
        return LoggerViewHolder(view)
    }

    override fun onBindViewHolder(holder: LoggerViewHolder, position: Int) {
        val logged = loggedList[position]

        holder.tvName.text = logged.exercise.name
        holder.tvMuscle.text = "Target: ${logged.exercise.muscleGroup}"
        holder.tvSets.text = logged.sets.toString()
        holder.btnUnit.text = logged.unit

        holder.layoutWeighted.visibility = View.GONE
        holder.layoutBodyweight.visibility = View.GONE
        holder.layoutTimed.visibility = View.GONE

        when (logged.exercise.type) {
            ExerciseType.WEIGHTED -> holder.layoutWeighted.visibility = View.VISIBLE
            ExerciseType.BODYWEIGHT -> holder.layoutBodyweight.visibility = View.VISIBLE
            ExerciseType.TIMED -> holder.layoutTimed.visibility = View.VISIBLE
        }

        // Restore typed values
        holder.etWeight.setText(if (logged.weight > 0) logged.weight.toString() else "")
        holder.etRepsWeighted.setText(if (logged.reps > 0) logged.reps.toString() else "")
        holder.etRepsBodyweight.setText(if (logged.reps > 0) logged.reps.toString() else "")
        holder.etSeconds.setText(if (logged.seconds > 0) logged.seconds.toString() else "")

        // Remove old watchers
        holder.etWeight.removeTextChangedListener(holder.etWeight.tag as? TextWatcher)
        holder.etRepsWeighted.removeTextChangedListener(holder.etRepsWeighted.tag as? TextWatcher)
        holder.etRepsBodyweight.removeTextChangedListener(holder.etRepsBodyweight.tag as? TextWatcher)
        holder.etSeconds.removeTextChangedListener(holder.etSeconds.tag as? TextWatcher)

        // Create new watchers
        val weightWatcher = simpleWatcher { logged.weight = it.toDoubleOrNull() ?: 0.0 }
        val repsWeightedWatcher = simpleWatcher { logged.reps = it.toIntOrNull() ?: 0 }
        val repsBodyweightWatcher = simpleWatcher { logged.reps = it.toIntOrNull() ?: 0 }
        val secondsWatcher = simpleWatcher { logged.seconds = it.toIntOrNull() ?: 0 }

        // Attach new watchers
        holder.etWeight.addTextChangedListener(weightWatcher)
        holder.etRepsWeighted.addTextChangedListener(repsWeightedWatcher)
        holder.etRepsBodyweight.addTextChangedListener(repsBodyweightWatcher)
        holder.etSeconds.addTextChangedListener(secondsWatcher)

        // Tag them so we can remove them later
        holder.etWeight.tag = weightWatcher
        holder.etRepsWeighted.tag = repsWeightedWatcher
        holder.etRepsBodyweight.tag = repsBodyweightWatcher
        holder.etSeconds.tag = secondsWatcher

        holder.btnMinus.setOnClickListener {
            if (logged.sets > 1) {
                logged.sets--
                holder.tvSets.text = logged.sets.toString()
            }
        }
        holder.btnPlus.setOnClickListener {
            if (logged.sets < 10) {
                logged.sets++
                holder.tvSets.text = logged.sets.toString()
            }
        }

        holder.btnUnit.setOnClickListener {
            logged.unit = if (logged.unit == "kg") "lbs" else "kg"
            holder.btnUnit.text = logged.unit
        }
    }

    private fun simpleWatcher(action: (String) -> Unit) = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) { action(s.toString()) }
    }

    // THIS IS THE MISSING PIECE! It tells the app how many items are in the list.
    override fun getItemCount() = loggedList.size
}