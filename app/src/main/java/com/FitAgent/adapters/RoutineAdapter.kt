package com.FitAgent.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.FitAgent.R
import com.FitAgent.models.RoutineWithExercises
import com.google.android.material.button.MaterialButton

class RoutineAdapter(
    private var items: List<RoutineWithExercises>,
    private val onStartClick: (RoutineWithExercises) -> Unit,
    private val onMoreClick: (RoutineWithExercises, View) -> Unit,
    private val onCardClick: (RoutineWithExercises) -> Unit
) : RecyclerView.Adapter<RoutineAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvRoutineName: TextView = view.findViewById(R.id.tvRoutineName)
        val tvExercisePreview: TextView = view.findViewById(R.id.tvExercisePreview)
        val btnMore: ImageButton = view.findViewById(R.id.btnMore)
        val btnStartRoutine: MaterialButton = view.findViewById(R.id.btnStartRoutine)
        val cardRoot: View = view
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_routine, parent, false)
        return VH(view)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val routineWithExercises = items[position]
        
        holder.tvRoutineName.text = routineWithExercises.routine.name
        
        val names = routineWithExercises.exercises.take(3).map { it.exerciseName }
        val extraCount = routineWithExercises.exercises.size - 3
        
        holder.tvExercisePreview.text = when {
            extraCount > 0 -> names.joinToString(", ") + "..."
            names.isEmpty() -> "No exercises yet"
            else -> names.joinToString(", ")
        }
        
        holder.btnStartRoutine.setOnClickListener { onStartClick(routineWithExercises) }
        holder.btnMore.setOnClickListener { onMoreClick(routineWithExercises, holder.btnMore) }
        holder.cardRoot.setOnClickListener { onCardClick(routineWithExercises) }
    }

    override fun getItemCount() = items.size

    fun updateItems(newItems: List<RoutineWithExercises>) {
        items = newItems
        notifyDataSetChanged()
    }
}
