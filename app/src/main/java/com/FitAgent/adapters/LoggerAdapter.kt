package com.FitAgent.adapters

import android.app.AlertDialog
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
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
        val btnRestTimer: MaterialButton = view.findViewById(R.id.btn_rest_timer)

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

        holder.etWeight.setText(if (logged.weight > 0) logged.weight.toString() else "")
        holder.etRepsWeighted.setText(if (logged.reps > 0) logged.reps.toString() else "")
        holder.etRepsBodyweight.setText(if (logged.reps > 0) logged.reps.toString() else "")
        holder.etSeconds.setText(if (logged.seconds > 0) logged.seconds.toString() else "")

        holder.etWeight.removeTextChangedListener(holder.etWeight.tag as? TextWatcher)
        holder.etRepsWeighted.removeTextChangedListener(holder.etRepsWeighted.tag as? TextWatcher)
        holder.etRepsBodyweight.removeTextChangedListener(holder.etRepsBodyweight.tag as? TextWatcher)
        holder.etSeconds.removeTextChangedListener(holder.etSeconds.tag as? TextWatcher)

        val weightWatcher = simpleWatcher { logged.weight = it.toDoubleOrNull() ?: 0.0 }
        val repsWeightedWatcher = simpleWatcher { logged.reps = it.toIntOrNull() ?: 0 }
        val repsBodyweightWatcher = simpleWatcher { logged.reps = it.toIntOrNull() ?: 0 }
        val secondsWatcher = simpleWatcher { logged.seconds = it.toIntOrNull() ?: 0 }

        holder.etWeight.addTextChangedListener(weightWatcher)
        holder.etRepsWeighted.addTextChangedListener(repsWeightedWatcher)
        holder.etRepsBodyweight.addTextChangedListener(repsBodyweightWatcher)
        holder.etSeconds.addTextChangedListener(secondsWatcher)

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

        holder.btnRestTimer.setOnClickListener {
            showRestTimerDialog(holder.itemView.context)
        }
    }

    private fun showRestTimerDialog(context: Context) {
        val dialogView = LayoutInflater.from(context)
            .inflate(R.layout.dialog_rest_timer, null)

        val tvCountdown = dialogView.findViewById<TextView>(R.id.tv_rest_countdown)
        val btnMinus15 = dialogView.findViewById<MaterialButton>(R.id.btn_minus_15)
        val btnPlus15 = dialogView.findViewById<MaterialButton>(R.id.btn_plus_15)
        val btnCancel = dialogView.findViewById<MaterialButton>(R.id.btn_cancel_rest)

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        var secondsLeft = 60
        tvCountdown.text = secondsLeft.toString()

        val handler = Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                secondsLeft--
                tvCountdown.text = secondsLeft.toString()
                if (secondsLeft <= 0) {
                    vibrate(context)
                    Toast.makeText(context, "Rest Over! 💪 Let's go!", Toast.LENGTH_LONG).show()
                    dialog.dismiss()
                } else {
                    handler.postDelayed(this, 1000)
                }
            }
        }
        handler.postDelayed(runnable, 1000)

        btnMinus15.setOnClickListener {
            secondsLeft = maxOf(1, secondsLeft - 15)
            tvCountdown.text = secondsLeft.toString()
        }
        btnPlus15.setOnClickListener {
            secondsLeft += 15
            tvCountdown.text = secondsLeft.toString()
        }
        btnCancel.setOnClickListener {
            handler.removeCallbacks(runnable)
            dialog.dismiss()
        }

        dialog.setOnDismissListener {
            handler.removeCallbacks(runnable)
        }

        dialog.show()
    }

    private fun vibrate(context: Context) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(500)
            }
        } catch (e: Exception) {
            // Vibrator not available, silently skip
        }
    }

    private fun simpleWatcher(action: (String) -> Unit) = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) { action(s.toString()) }
    }

    override fun getItemCount() = loggedList.size
}