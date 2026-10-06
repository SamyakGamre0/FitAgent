package com.FitAgent

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.FitAgent.adapters.RoutineExerciseAdapter
import com.FitAgent.data.MasterExercise
import com.FitAgent.database.AppDatabase
import com.FitAgent.models.DraftExercise
import com.FitAgent.models.DraftSet
import com.FitAgent.models.Routine
import com.FitAgent.models.RoutineExercise
import com.FitAgent.utils.ProfileManager
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class CreateRoutineActivity : AppCompatActivity() {

    private val REQ_CODE_PICK_EXERCISE = 1001

    private var hasUnsavedChanges = false
    private var draftExercises: MutableList<DraftExercise> = mutableListOf()
    private var editRoutineId: Long = -1L

    private lateinit var btnCancel: TextView
    private lateinit var btnSave: MaterialButton
    private lateinit var tvToolbarTitle: TextView
    private lateinit var etRoutineTitle: EditText
    private lateinit var rvExercises: RecyclerView
    private lateinit var emptyState: LinearLayout
    private lateinit var btnAddExercise: MaterialButton

    private lateinit var exerciseAdapter: RoutineExerciseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_routine)

        btnCancel = findViewById(R.id.btnCancel)
        btnSave = findViewById(R.id.btnSave)
        tvToolbarTitle = findViewById(R.id.tvToolbarTitle)
        etRoutineTitle = findViewById(R.id.etRoutineTitle)
        rvExercises = findViewById(R.id.rvExercises)
        emptyState = findViewById(R.id.emptyState)
        btnAddExercise = findViewById(R.id.btnAddExercise)

        rvExercises.layoutManager = LinearLayoutManager(this)
        
        val unitLabel = ProfileManager.getWeightUnit(this)

        exerciseAdapter = RoutineExerciseAdapter(
            items = draftExercises,
            unitLabel = unitLabel,
            onSetsChanged = { updateSaveButtonState() },
            onRemoveExercise = { position ->
                draftExercises.removeAt(position)
                exerciseAdapter.notifyItemRemoved(position)
                exerciseAdapter.notifyItemRangeChanged(position, draftExercises.size - position)
                updateExercisesVisibility()
                updateSaveButtonState()
            }
        )
        rvExercises.adapter = exerciseAdapter

        updateExercisesVisibility()
        
        editRoutineId = intent.getLongExtra("EDIT_ROUTINE_ID", -1L)
        if (editRoutineId != -1L) {
            tvToolbarTitle.text = "Edit Routine"
            lifecycleScope.launch {
                val routineDao = AppDatabase.getDatabase(this@CreateRoutineActivity).routineDao()
                val routineWithExercises = routineDao.getRoutineWithExercises(editRoutineId)
                if (routineWithExercises != null) {
                    etRoutineTitle.setText(routineWithExercises.routine.name)
                    
                    draftExercises.clear()
                    routineWithExercises.exercises.forEach { re ->
                        val masterEx = com.FitAgent.data.ExerciseLibrary.MASTER_LIST.find { it.name == re.exerciseName } 
                            ?: MasterExercise(id = -1, name = re.exerciseName, muscleGroup = re.muscleGroup, emoji = getEmojiForGroup(re.muscleGroup))
                        
                        val draftSets = mutableListOf<DraftSet>()
                        if (re.defaultSets > 0) {
                            for (i in 0 until re.defaultSets) {
                                draftSets.add(DraftSet(weight = re.defaultWeight, reps = re.defaultReps))
                            }
                        }
                        
                        draftExercises.add(DraftExercise(masterExercise = masterEx, sets = draftSets, notes = re.notes))
                    }
                    exerciseAdapter.notifyDataSetChanged()
                    updateExercisesVisibility()
                    updateSaveButtonState()
                    hasUnsavedChanges = false
                }
            }
        }
        
        btnSave.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#444444"))
        btnSave.setTextColor(Color.parseColor("#888888"))
        btnSave.isEnabled = false

        etRoutineTitle.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                hasUnsavedChanges = true
                updateSaveButtonState()
            }
        })

        btnCancel.setOnClickListener {
            handleBackOrCancel()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                handleBackOrCancel()
            }
        })

        btnSave.setOnClickListener {
            if (!btnSave.isEnabled) return@setOnClickListener

            lifecycleScope.launch {
                val routineDao = AppDatabase.getDatabase(this@CreateRoutineActivity).routineDao()
                
                val routineIdToUse: Long
                if (editRoutineId == -1L) {
                    val routine = Routine(
                        name = etRoutineTitle.text.toString().trim(),
                        createdAt = System.currentTimeMillis(),
                        isPrebuilt = false
                    )
                    routineIdToUse = routineDao.insertRoutine(routine)
                } else {
                    val existing = routineDao.getRoutineWithExercises(editRoutineId)
                    if (existing != null) {
                        val updatedRoutine = existing.routine.copy(name = etRoutineTitle.text.toString().trim())
                        routineDao.updateRoutine(updatedRoutine)
                        routineDao.deleteRoutineExercises(editRoutineId)
                        routineIdToUse = editRoutineId
                    } else {
                        return@launch
                    }
                }

                val routineExercises = draftExercises.mapIndexed { index, draft ->
                    val firstSet = draft.sets.firstOrNull() ?: DraftSet()
                    RoutineExercise(
                        routineId = routineIdToUse,
                        exerciseName = draft.masterExercise.name,
                        muscleGroup = draft.masterExercise.muscleGroup,
                        orderIndex = index,
                        defaultSets = draft.sets.size,
                        defaultReps = firstSet.reps,
                        defaultWeight = firstSet.weight,
                        unit = unitLabel,
                        notes = draft.notes
                    )
                }
                routineDao.insertRoutineExercises(routineExercises)

                Toast.makeText(this@CreateRoutineActivity, "Routine saved!", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            }
        }

        btnAddExercise.setOnClickListener {
            val intent = Intent(this, ExercisePickerActivity::class.java)
            startActivityForResult(intent, REQ_CODE_PICK_EXERCISE)
        }
    }

    @Deprecated("Using startActivityForResult for MVP — will migrate to Activity Result API later")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_CODE_PICK_EXERCISE && resultCode == RESULT_OK && data != null) {
            val selected = data.getParcelableArrayListExtra<MasterExercise>(ExercisePickerActivity.RESULT_KEY_SELECTED)
            if (selected != null && selected.isNotEmpty()) {
                selected.forEach { masterEx ->
                    draftExercises.add(DraftExercise(masterExercise = masterEx))
                }
                exerciseAdapter.notifyItemRangeInserted(draftExercises.size - selected.size, selected.size)
                hasUnsavedChanges = true
                updateExercisesVisibility()
                updateSaveButtonState()
            }
        }
    }

    private fun updateExercisesVisibility() {
        if (draftExercises.isEmpty()) {
            emptyState.visibility = View.VISIBLE
            rvExercises.visibility = View.GONE
        } else {
            emptyState.visibility = View.GONE
            rvExercises.visibility = View.VISIBLE
        }
    }

    private fun updateSaveButtonState() {
        if (etRoutineTitle.text.isNotBlank() && draftExercises.isNotEmpty()) {
            btnSave.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#00FF94"))
            btnSave.setTextColor(Color.BLACK)
            btnSave.isEnabled = true
        } else {
            btnSave.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#444444"))
            btnSave.setTextColor(Color.parseColor("#888888"))
            btnSave.isEnabled = false
        }
    }

    private fun handleBackOrCancel() {
        if (hasUnsavedChanges) {
            AlertDialog.Builder(this)
                .setTitle("Discard changes?")
                .setMessage("Your routine will not be saved.")
                .setPositiveButton("Discard") { _, _ ->
                    finish()
                }
                .setNegativeButton("Keep editing") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        } else {
            finish()
        }
    }

    private fun getEmojiForGroup(group: String): String {
        return when (group) {
            "Chest" -> "🫁"
            "Back" -> "🔙"
            "Legs" -> "🦵"
            "Shoulders" -> "🤷"
            "Arms" -> "💪"
            "Core" -> "🧘"
            "Cardio" -> "🏃"
            else -> "🏋️"
        }
    }
}