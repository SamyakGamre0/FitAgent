package com.FitAgent.adapters

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.FitAgent.R
import com.FitAgent.models.DraftExercise
import com.FitAgent.models.DraftSet
import com.google.android.material.button.MaterialButton

class RoutineExerciseAdapter(
    private val items: MutableList<DraftExercise>,
    private var unitLabel: String,
    private val onSetsChanged: () -> Unit,
    private val onRemoveExercise: (Int) -> Unit
) : RecyclerView.Adapter<RoutineExerciseAdapter.VH>() {

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivMuscleIcon: ImageView = itemView.findViewById(R.id.ivMuscleIcon)
        val tvExerciseName: TextView = itemView.findViewById(R.id.tvExerciseName)
        val btnExerciseMore: ImageButton = itemView.findViewById(R.id.btnExerciseMore)
        val etNotes: EditText = itemView.findViewById(R.id.etNotes)
        val tableSets: TableLayout = itemView.findViewById(R.id.tableSets)
        val tvUnitHeader: TextView = itemView.findViewById(R.id.tvUnitHeader)
        val btnAddSet: MaterialButton = itemView.findViewById(R.id.btnAddSet)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_routine_exercise, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.ivMuscleIcon.setImageResource(getIconResForMuscle(item.masterExercise.muscleGroup))
        holder.tvExerciseName.text = item.masterExercise.name
        holder.tvUnitHeader.text = unitLabel.uppercase()

        holder.etNotes.setTextIfDifferent(item.notes)

        val oldNotesWatcher = holder.etNotes.getTag(R.id.etNotes) as? TextWatcher
        if (oldNotesWatcher != null) holder.etNotes.removeTextChangedListener(oldNotesWatcher)
        val newNotesWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                item.notes = s?.toString() ?: ""
            }
        }
        holder.etNotes.addTextChangedListener(newNotesWatcher)
        holder.etNotes.setTag(R.id.etNotes, newNotesWatcher)

        while (holder.tableSets.childCount > 1) {
            holder.tableSets.removeViewAt(1)
        }

        item.sets.forEachIndexed { setIndex, draftSet ->
            val rowView = LayoutInflater.from(holder.itemView.context)
                .inflate(R.layout.row_set_input, holder.tableSets, false) as TableRow
            
            val tvSetNumber = rowView.findViewById<TextView>(R.id.tvSetNumber)
            val etWeight = rowView.findViewById<EditText>(R.id.etWeight)
            val etReps = rowView.findViewById<EditText>(R.id.etReps)
            val btnDeleteSet = rowView.findViewById<ImageButton>(R.id.btnDeleteSet)

            tvSetNumber.text = (setIndex + 1).toString()
            etWeight.setTextIfDifferent(if (draftSet.weight == 0f) "" else formatWeight(draftSet.weight))
            etReps.setTextIfDifferent(if (draftSet.reps == 0) "" else draftSet.reps.toString())

            etWeight.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    draftSet.weight = s?.toString()?.toFloatOrNull() ?: 0f
                }
            })

            etReps.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    draftSet.reps = s?.toString()?.toIntOrNull() ?: 0
                }
            })

            btnDeleteSet.setOnClickListener {
                if (item.sets.size > 1) {
                    item.sets.removeAt(setIndex)
                    notifyItemChanged(position)
                    onSetsChanged()
                }
            }

            holder.tableSets.addView(rowView)
        }

        holder.btnAddSet.setOnClickListener {
            val lastSet = item.sets.last()
            item.sets.add(DraftSet(weight = lastSet.weight, reps = lastSet.reps))
            notifyItemChanged(position)
            onSetsChanged()
        }

        holder.btnExerciseMore.setOnClickListener {
            val popup = PopupMenu(holder.itemView.context, holder.btnExerciseMore)
            popup.menu.add(0, 1, 0, "Remove Exercise")
            popup.setOnMenuItemClickListener { menuItem ->
                if (menuItem.itemId == 1) {
                    onRemoveExercise(position)
                    true
                } else false
            }
            popup.show()
        }
    }

    override fun getItemCount(): Int = items.size

    private fun getIconResForMuscle(muscleGroup: String): Int {
        return when (muscleGroup) {
            "Chest" -> R.drawable.muscle_chest
            "Back" -> R.drawable.muscle_back
            "Legs" -> R.drawable.muscle_legs
            "Shoulders" -> R.drawable.muscle_shoulders
            "Arms" -> R.drawable.muscle_arms
            "Core" -> R.drawable.muscle_core
            "Cardio" -> R.drawable.muscle_cardio
            else -> R.drawable.muscle_arms
        }
    }

    private fun EditText.setTextIfDifferent(newText: String) {
        if (text.toString() != newText) setText(newText)
    }

    private fun formatWeight(w: Float): String {
        return "%.1f".format(w).trimEnd('0').trimEnd('.')
    }
}
