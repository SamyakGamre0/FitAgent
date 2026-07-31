package com.FitAgent

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.FitAgent.adapters.ExercisePickerAdapter
import com.FitAgent.data.ExerciseLibrary
import com.FitAgent.data.MasterExercise
import com.google.android.material.button.MaterialButton

class ExercisePickerActivity : AppCompatActivity() {

    companion object {
        const val RESULT_KEY_SELECTED = "SELECTED_EXERCISES"
    }

    private val selectedIds = mutableSetOf<Int>()
    private var currentFilter: String = "All"
    private var currentSearch: String = ""
    
    private lateinit var adapter: ExercisePickerAdapter
    private lateinit var allChips: List<MaterialButton>

    private lateinit var btnBack: ImageButton
    private lateinit var tvTitle: TextView
    private lateinit var btnAddSelected: TextView
    private lateinit var etSearch: EditText
    private lateinit var rvExercises: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise_picker)

        btnBack = findViewById(R.id.btnBack)
        tvTitle = findViewById(R.id.tvTitle)
        btnAddSelected = findViewById(R.id.btnAddSelected)
        etSearch = findViewById(R.id.etSearch)
        rvExercises = findViewById(R.id.rvExercises)

        val btnChipAll: MaterialButton = findViewById(R.id.btnChipAll)
        val btnChipChest: MaterialButton = findViewById(R.id.btnChipChest)
        val btnChipBack: MaterialButton = findViewById(R.id.btnChipBack)
        val btnChipLegs: MaterialButton = findViewById(R.id.btnChipLegs)
        val btnChipShoulders: MaterialButton = findViewById(R.id.btnChipShoulders)
        val btnChipArms: MaterialButton = findViewById(R.id.btnChipArms)
        val btnChipCore: MaterialButton = findViewById(R.id.btnChipCore)
        val btnChipCardio: MaterialButton = findViewById(R.id.btnChipCardio)

        allChips = listOf(
            btnChipAll, btnChipChest, btnChipBack, btnChipLegs, 
            btnChipShoulders, btnChipArms, btnChipCore, btnChipCardio
        )

        adapter = ExercisePickerAdapter(ExerciseLibrary.MASTER_LIST, selectedIds) {
            updateAddButton()
        }
        rvExercises.layoutManager = LinearLayoutManager(this)
        rvExercises.adapter = adapter

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                currentSearch = s?.toString() ?: ""
                applyFilters()
            }
        })

        allChips.forEach { chip ->
            chip.setOnClickListener {
                updateChipSelection(chip)
                currentFilter = chip.text.toString()
                applyFilters()
            }
        }

        updateChipSelection(btnChipAll)

        btnBack.setOnClickListener {
            finish()
        }

        btnAddSelected.setOnClickListener {
            if (selectedIds.isEmpty()) return@setOnClickListener
            
            val selected = ExerciseLibrary.MASTER_LIST.filter { selectedIds.contains(it.id) }
            val intent = Intent()
            intent.putParcelableArrayListExtra(RESULT_KEY_SELECTED, ArrayList(selected))
            setResult(RESULT_OK, intent)
            finish()
        }
    }

    private fun applyFilters() {
        val filtered = ExerciseLibrary.MASTER_LIST.filter { ex ->
            (currentFilter == "All" || ex.muscleGroup == currentFilter) &&
            (currentSearch.isBlank() || ex.name.contains(currentSearch, ignoreCase = true))
        }
        adapter.updateItems(filtered)
    }

    private fun updateChipSelection(selectedChip: MaterialButton) {
        allChips.forEach { chip ->
            if (chip == selectedChip) {
                chip.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#00FF94"))
                chip.setTextColor(Color.parseColor("#000000"))
                chip.strokeWidth = 0
            } else {
                chip.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#1A1A1A"))
                chip.setTextColor(Color.parseColor("#FFFFFF"))
                chip.strokeWidth = (1 * resources.displayMetrics.density).toInt()
            }
        }
    }

    private fun updateAddButton() {
        val count = selectedIds.size
        btnAddSelected.text = "Add ($count)"
        if (count > 0) {
            btnAddSelected.setTextColor(Color.parseColor("#00FF94"))
            btnAddSelected.isClickable = true
        } else {
            btnAddSelected.setTextColor(Color.parseColor("#666666"))
            btnAddSelected.isClickable = false
        }
    }
}
