package com.FitAgent.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.FitAgent.R
import com.FitAgent.models.WorkoutLog

class HistoryDetailAdapter(
    private val logs: List<WorkoutLog>
) : RecyclerView.Adapter<HistoryDetailAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvExercise: TextView = view.findViewById(R.id.tv_detail_exercise)
        val tvStats: TextView = view.findViewById(R.id.tv_detail_stats)
        val tvDurationNote: TextView = view.findViewById(R.id.tv_detail_duration_note)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_history_detail, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val log = logs[position]
        holder.tvExercise.text = log.exerciseName

        val statsText = buildString {
            append("Sets: ${log.sets}")
            if (log.unit == "sec" || log.unit == "min") {
                val totalSec = (if (log.reps > 60) log.reps else log.reps) // use reps field which holds seconds for timed
                // Actually for timed exercises: log.reps holds seconds, log.unit is "sec" or "min"
                // For weighted: log.weight is weight, log.reps is reps
                // Let's build based on unit
                if (log.unit.contains("sec") || log.unit.contains("min")) {
                    append(" | Duration: ")
                    val mins = totalSec / 60
                    val secs = totalSec % 60
                    if (mins > 0) append("${mins}m ") else append("")
                    if (secs > 0 || mins == 0) append("${secs}s")
                } else {
                    append(" | Weight: ${String.format("%.1f", log.weight)}${log.unit}")
                    append(" | Reps: ${log.reps}")
                }
            } else {
                append(" | Weight: ${String.format("%.1f", log.weight)}${log.unit}")
                append(" | Reps: ${log.reps}")
            }
            append(" • Volume: ${String.format("%.1f", log.volume)}kg")
        }
        holder.tvStats.text = statsText

        // Show duration note only for timed exercises
        if (log.unit.contains("sec") || log.unit.contains("min")) {
            holder.tvDurationNote.visibility = View.VISIBLE
            val totalSec = log.reps // reps field holds total seconds for timed
            val mins = totalSec / 60
            val secs = totalSec % 60
            val formatted = when {
                mins == 0 -> "${secs}s"
                secs == 0 -> "${mins}m"
                else -> "${mins}m ${secs}s"
            }
            holder.tvDurationNote.text = "⏱ Duration: $formatted"
        } else {
            holder.tvDurationNote.visibility = View.GONE
        }
    }

    override fun getItemCount() = logs.size
}
