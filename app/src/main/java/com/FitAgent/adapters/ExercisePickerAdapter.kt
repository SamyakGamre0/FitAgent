package com.FitAgent.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.FitAgent.R
import com.FitAgent.data.MasterExercise
import com.google.android.material.card.MaterialCardView

class ExercisePickerAdapter(
    private var items: List<MasterExercise>,
    private val selectedIds: MutableSet<Int>,
    private val onSelectionChanged: () -> Unit
) : RecyclerView.Adapter<ExercisePickerAdapter.VH>() {

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivMuscleIcon: ImageView = itemView.findViewById(R.id.ivMuscleIcon)
        val tvExerciseName: TextView = itemView.findViewById(R.id.tvExerciseName)
        val tvMuscleGroup: TextView = itemView.findViewById(R.id.tvMuscleGroup)
        val cbSelect: CheckBox = itemView.findViewById(R.id.cbSelect)
        val cardRoot: MaterialCardView = itemView as MaterialCardView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_exercise_pick, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.ivMuscleIcon.setImageResource(getIconResForMuscle(item.muscleGroup))
        holder.tvExerciseName.text = item.name
        holder.tvMuscleGroup.text = item.muscleGroup
        holder.cbSelect.isChecked = selectedIds.contains(item.id)

        holder.cardRoot.setOnClickListener {
            if (selectedIds.contains(item.id)) {
                selectedIds.remove(item.id)
            } else {
                selectedIds.add(item.id)
            }
            notifyItemChanged(position)
            onSelectionChanged()
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateItems(newItems: List<MasterExercise>) {
        items = newItems
        notifyDataSetChanged()
    }

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
}
