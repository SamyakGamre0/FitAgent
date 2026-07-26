package com.FitAgent.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.FitAgent.R
import com.FitAgent.models.WorkoutSession

class HistoryAdapter(
    private val sessions: MutableList<WorkoutSession>,
    private val onSessionClick: (WorkoutSession) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDate: TextView = view.findViewById(R.id.tv_hist_date)
        val tvGoal: TextView = view.findViewById(R.id.tv_hist_goal)
        val tvStats: TextView = view.findViewById(R.id.tv_hist_stats)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val s = sessions[position]
        holder.tvDate.text = s.date
        holder.tvGoal.text = "${s.goal} • ${s.equipment}"
        val cleanVolume = String.format("%.1f", s.totalVolume)
        holder.tvStats.text = "${s.exerciseCount} exercises • $cleanVolume vol • ⏱ ${s.duration}"

        holder.itemView.setOnClickListener { onSessionClick(s) }
    }

    override fun getItemCount() = sessions.size

    fun removeItem(position: Int): WorkoutSession {
        val removed = sessions[position]
        sessions.removeAt(position)
        notifyItemRemoved(position)
        return removed
    }

    fun restoreItem(session: WorkoutSession, position: Int) {
        sessions.add(position, session)
        notifyItemInserted(position)
    }

    fun getSessionAt(position: Int): WorkoutSession = sessions[position]
}
