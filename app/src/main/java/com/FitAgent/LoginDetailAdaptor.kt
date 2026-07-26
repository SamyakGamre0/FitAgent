package com.FitAgent.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.FitAgent.R
import com.FitAgent.models.WorkoutLog

class LogDetailAdapter(
    private val logs: List<WorkoutLog>
) : RecyclerView.Adapter<LogDetailAdapter.LogDetailViewHolder>() {

    class LogDetailViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tv_detail_exercise_name)
        val tvMuscle: TextView = view.findViewById(R.id.tv_detail_muscle)
        val tvStats: TextView = view.findViewById(R.id.tv_detail_log_stats)
        val tvVolume: TextView = view.findViewById(R.id.tv_detail_log_volume)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogDetailViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_log_detail, parent, false)
        return LogDetailViewHolder(view)
    }

    override fun onBindViewHolder(holder: LogDetailViewHolder, position: Int) {
        val log = logs[position]
        holder.tvName.text = log.exerciseName
        holder.tvMuscle.text = "Target: ${log.muscleGroup}"

        val statsText = when (log.unit) {
            "sec" -> "${log.sets} sets × ${log.reps} sec"
            "reps" -> "${log.sets} sets × ${log.reps} reps"
            else -> "${log.sets} sets × ${log.reps} reps @ ${log.weight} ${log.unit}"
        }
        holder.tvStats.text = statsText
        holder.tvVolume.text = "Volume: ${String.format("%.1f", log.volume)}"
    }

    override fun getItemCount() = logs.size
}