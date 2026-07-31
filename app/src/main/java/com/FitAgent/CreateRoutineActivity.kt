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
import com.FitAgent.data.MasterExercise
import com.FitAgent.database.AppDatabase
import com.FitAgent.models.Routine
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.launch

class CreateRoutineActivity : AppCompatActivity() {

    private val REQ_CODE_PICK_EXERCISE = 1001

    private var hasUnsavedChanges = false
    private var draftExercises: MutableList<Any> = mutableListOf()

    private lateinit var btnCancel: TextView
    private lateinit var btnSave: MaterialButton
    private lateinit var tvToolbarTitle: TextView
    private lateinit var etRoutineTitle: EditText
    private lateinit var rvExercises: RecyclerView
    private lateinit var emptyState: LinearLayout
    private lateinit var btnAddExercise: MaterialButton

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

        emptyState.visibility = View.VISIBLE
        rvExercises.visibility = View.GONE

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
                val routine = Routine(
                    name = etRoutineTitle.text.toString().trim(),
                    createdAt = System.currentTimeMillis(),
                    isPrebuilt = false
                )
                val routineDao = AppDatabase.getDatabase(this@CreateRoutineActivity).routineDao()
                routineDao.insertRoutine(routine)
                // TODO Day 5: insert routine exercises
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
                Toast.makeText(this, "${selected.size} exercises picked (config coming Day 5)", Toast.LENGTH_SHORT).show()
                hasUnsavedChanges = true
                android.util.Log.d("CreateRoutine", "Picked exercises: ${selected.map { it.name }}")
            }
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
}