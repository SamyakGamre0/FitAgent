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
import android.text.InputType
import android.text.TextWatcher
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.FitAgent.R
import com.FitAgent.models.Exercise
import com.FitAgent.models.ExerciseType
import com.FitAgent.models.WorkoutLog
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.button.MaterialButton
import kotlin.math.roundToInt

data class SetData(
    var weight: Double = 0.0,
    var reps: Int = 0,
    var seconds: Int = 0
)

data class LoggedExercise(
    val exercise: Exercise,
    var sets: Int = 3,
    var unit: String = "kg",
    val setDataList: MutableList<SetData> = mutableListOf()
)

class LoggerAdapter(
    val loggedList: MutableList<LoggedExercise>
) : RecyclerView.Adapter<LoggerAdapter.LoggerViewHolder>() {

    private val previousLogsMap = mutableMapOf<String, List<WorkoutLog>>()

    fun setPreviousLogs(exerciseName: String, logs: List<WorkoutLog>) {
        previousLogsMap[exerciseName] = logs
        notifyDataSetChanged()
    }

    class LoggerViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tv_log_exercise_name)
        val tvMuscle: TextView = view.findViewById(R.id.tv_log_muscle)
        val btnMinus: MaterialButton = view.findViewById(R.id.btn_minus_set)
        val btnPlus: MaterialButton = view.findViewById(R.id.btn_plus_set)
        val tvSets: TextView = view.findViewById(R.id.tv_sets_count)
        val btnRestTimer: MaterialButton = view.findViewById(R.id.btn_rest_timer)
        val btnUnit: MaterialButton = view.findViewById(R.id.btn_unit_toggle)
        val setsContainer: LinearLayout = view.findViewById(R.id.sets_container)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoggerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_logger, parent, false)
        return LoggerViewHolder(view)
    }

    override fun onBindViewHolder(holder: LoggerViewHolder, position: Int) {
        val logged = loggedList[position]
        val context = holder.itemView.context

        holder.tvName.text = logged.exercise.name
        holder.tvMuscle.text = "Target: ${logged.exercise.muscleGroup}"
        holder.tvSets.text = logged.sets.toString()
        holder.btnUnit.text = logged.unit

        while (logged.setDataList.size < logged.sets) {
            logged.setDataList.add(SetData())
        }
        while (logged.setDataList.size > logged.sets) {
            logged.setDataList.removeAt(logged.setDataList.size - 1)
        }

        buildSetsUI(context, holder, logged)

        holder.btnMinus.setOnClickListener {
            if (logged.sets > 1) {
                logged.sets--
                logged.setDataList.removeAt(logged.setDataList.size - 1)
                holder.tvSets.text = logged.sets.toString()
                buildSetsUI(context, holder, logged)
            }
        }
        holder.btnPlus.setOnClickListener {
            if (logged.sets < 10) {
                logged.sets++
                logged.setDataList.add(SetData())
                holder.tvSets.text = logged.sets.toString()
                buildSetsUI(context, holder, logged)
            }
        }

        holder.btnUnit.setOnClickListener {
            showUnitBottomSheet(context, logged, holder)
        }
    }

    private fun showUnitBottomSheet(context: Context, logged: LoggedExercise, holder: LoggerViewHolder) {
        val dialog = BottomSheetDialog(context)
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_weight_units, null)
        dialog.setContentView(view)
        
        val tvTitle = view.findViewById<TextView>(R.id.tvExerciseSubTitle)
        tvTitle.text = logged.exercise.name

        val rgUnits = view.findViewById<RadioGroup>(R.id.rgUnits)
        when (logged.unit) {
            "kg" -> rgUnits.check(R.id.rbKg)
            "lbs" -> rgUnits.check(R.id.rbLbs)
            else -> rgUnits.check(R.id.rbDefault)
        }

        rgUnits.setOnCheckedChangeListener { _, checkedId ->
            val oldUnit = logged.unit
            val newUnit = when (checkedId) {
                R.id.rbKg -> "kg"
                R.id.rbLbs -> "lbs"
                else -> "kg"
            }
            if (oldUnit != newUnit) {
                logged.unit = newUnit
                holder.btnUnit.text = logged.unit
                convertWeights(logged, oldUnit, newUnit)
                buildSetsUI(context, holder, logged)
            }
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun convertWeights(logged: LoggedExercise, oldUnit: String, newUnit: String) {
        val factor = 2.20462
        for (setData in logged.setDataList) {
            if (setData.weight > 0) {
                val newW = if (oldUnit == "kg" && newUnit == "lbs") {
                    setData.weight * factor
                } else if (oldUnit == "lbs" && newUnit == "kg") {
                    setData.weight / factor
                } else {
                    setData.weight
                }
                setData.weight = (newW * 10.0).roundToInt() / 10.0
            }
        }
    }

    private fun buildSetsUI(context: Context, holder: LoggerViewHolder, logged: LoggedExercise) {
        holder.setsContainer.removeAllViews()

        val greenColor = ContextCompat.getColor(context, R.color.aura_green)
        val secondaryColor = ContextCompat.getColor(context, R.color.text_secondary)
        val primaryColor = ContextCompat.getColor(context, R.color.text_primary)
        val previousLogs = previousLogsMap[logged.exercise.name] ?: emptyList()

        val header = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, 0, dp(context, 8)) }
        }
        
        header.addView(makeHeader(context, "SET", 1f, secondaryColor))
        when (logged.exercise.type) {
            ExerciseType.WEIGHTED -> {
                header.addView(makeHeader(context, "PREVIOUS", 2f, secondaryColor))
                val unitHeader = makeHeader(context, "🏋️ ${logged.unit.uppercase()}", 2f, secondaryColor)
                unitHeader.setOnClickListener { showUnitBottomSheet(context, logged, holder) }
                header.addView(unitHeader)
                header.addView(makeHeader(context, "REPS", 2f, secondaryColor))
            }
            ExerciseType.BODYWEIGHT -> {
                header.addView(makeHeader(context, "PREVIOUS", 2f, secondaryColor))
                header.addView(makeHeader(context, "REPS", 4f, secondaryColor))
            }
            ExerciseType.TIMED -> {
                header.addView(makeHeader(context, "PREVIOUS", 2f, secondaryColor))
                header.addView(makeHeader(context, "SECONDS", 4f, secondaryColor))
            }
        }
        holder.setsContainer.addView(header)

        for (i in 0 until logged.sets) {
            val setData = logged.setDataList[i]
            val row = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, dp(context, 4), 0, dp(context, 4)) }
            }

            val setNumber = TextView(context).apply {
                text = (i + 1).toString()
                setTextColor(greenColor)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            }
            row.addView(setNumber)

            val previousLog = previousLogs.find { it.exerciseName.contains("Set ${i + 1}") }
            val prevText = if (previousLog != null) {
                when (logged.exercise.type) {
                    ExerciseType.WEIGHTED -> "${formatWeight(previousLog.weight)}${previousLog.unit} × ${previousLog.reps}"
                    ExerciseType.BODYWEIGHT -> "${previousLog.reps} reps"
                    ExerciseType.TIMED -> "${previousLog.reps} sec"
                }
            } else "-"

            val tvPrevious = TextView(context).apply {
                text = prevText
                setTextColor(secondaryColor)
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f)
            }

            when (logged.exercise.type) {
                ExerciseType.WEIGHTED -> {
                    row.addView(tvPrevious)
                    
                    val weightEt = makeInput(context, primaryColor, InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL)
                    weightEt.setText(if (setData.weight > 0) formatWeight(setData.weight) else "")
                    weightEt.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f).apply {
                        setMargins(dp(context, 4), 0, dp(context, 4), 0)
                    }
                    weightEt.addTextChangedListener(simpleWatcher {
                        setData.weight = it.toDoubleOrNull() ?: 0.0
                    })
                    row.addView(weightEt)

                    val repsEt = makeInput(context, primaryColor, InputType.TYPE_CLASS_NUMBER)
                    repsEt.setText(if (setData.reps > 0) setData.reps.toString() else "")
                    repsEt.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 2f).apply {
                        setMargins(dp(context, 4), 0, dp(context, 4), 0)
                    }
                    repsEt.addTextChangedListener(simpleWatcher {
                        setData.reps = it.toIntOrNull() ?: 0
                    })
                    row.addView(repsEt)
                }
                ExerciseType.BODYWEIGHT -> {
                    row.addView(tvPrevious)
                    
                    val repsEt = makeInput(context, primaryColor, InputType.TYPE_CLASS_NUMBER)
                    repsEt.setText(if (setData.reps > 0) setData.reps.toString() else "")
                    repsEt.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 4f).apply {
                        setMargins(dp(context, 4), 0, dp(context, 4), 0)
                    }
                    repsEt.addTextChangedListener(simpleWatcher {
                        setData.reps = it.toIntOrNull() ?: 0
                    })
                    row.addView(repsEt)
                }
                ExerciseType.TIMED -> {
                    row.addView(tvPrevious)
                    
                    val secEt = makeInput(context, primaryColor, InputType.TYPE_CLASS_NUMBER)
                    secEt.setText(if (setData.seconds > 0) setData.seconds.toString() else "")
                    secEt.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 4f).apply {
                        setMargins(dp(context, 4), 0, dp(context, 4), 0)
                    }
                    secEt.addTextChangedListener(simpleWatcher {
                        setData.seconds = it.toIntOrNull() ?: 0
                    })
                    row.addView(secEt)
                }
            }

            holder.setsContainer.addView(row)
        }
    }

    private fun formatWeight(w: Double): String {
        return "%.1f".format(w).trimEnd('0').trimEnd('.')
    }

    private fun makeHeader(context: Context, text: String, weight: Float, color: Int): TextView {
        return TextView(context).apply {
            this.text = text
            setTextColor(color)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, weight)
        }
    }

    private fun makeInput(context: Context, textColor: Int, inputTypeFlag: Int): EditText {
        return EditText(context).apply {
            setTextColor(textColor)
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            gravity = Gravity.CENTER
            inputType = inputTypeFlag
            setBackgroundResource(android.R.drawable.editbox_background_normal)
            setPadding(dp(context, 8), dp(context, 6), dp(context, 8), dp(context, 6))
        }
    }

    private fun dp(context: Context, value: Int): Int {
        return (value * context.resources.displayMetrics.density).toInt()
    }

    private fun simpleWatcher(action: (String) -> Unit) = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        override fun afterTextChanged(s: Editable?) { action(s.toString()) }
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
                    Toast.makeText(context, "Rest Over! 💪", Toast.LENGTH_LONG).show()
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
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vm.defaultVibrator
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
        } catch (e: Exception) {}
    }

    override fun getItemCount() = loggedList.size
}