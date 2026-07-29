package com.FitAgent.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.FitAgent.R
import com.FitAgent.database.TopLiftResult
import com.FitAgent.utils.ProfileManager
import com.FitAgent.utils.UnitConverter

class TopLiftAdapter(
    private val lifts: List<TopLiftResult>
) : RecyclerView.Adapter<TopLiftAdapter.TopLiftViewHolder>() {

    class TopLiftViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivIcon: ImageView = view.findViewById(R.id.iv_lift_icon)
        val tvEmoji: TextView = view.findViewById(R.id.tv_lift_emoji)
        val tvName: TextView = view.findViewById(R.id.tv_lift_name)
        val tvWeight: TextView = view.findViewById(R.id.tv_lift_weight)
        val tvDetails: TextView = view.findViewById(R.id.tv_lift_details)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopLiftViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_top_lift, parent, false)
        return TopLiftViewHolder(view)
    }

    override fun onBindViewHolder(holder: TopLiftViewHolder, position: Int) {
        val lift = lifts[position]
        val context = holder.itemView.context
        val prefUnit = ProfileManager.getWeightUnit(context)

        holder.tvName.text = lift.cleanName

        // Pick icon: image for chest/back, emoji for others
        val muscleLower = lift.muscleGroup.lowercase()
        when {
            muscleLower.contains("chest") -> {
                holder.ivIcon.visibility = View.VISIBLE
                holder.tvEmoji.visibility = View.GONE
                holder.ivIcon.setImageResource(R.drawable.muscle_chest)
            }
            muscleLower.contains("back") -> {
                holder.ivIcon.visibility = View.VISIBLE
                holder.tvEmoji.visibility = View.GONE
                holder.ivIcon.setImageResource(R.drawable.muscle_back)
            }
            else -> {
                holder.ivIcon.visibility = View.GONE
                holder.tvEmoji.visibility = View.VISIBLE
                holder.tvEmoji.text = getEmojiForMuscle(muscleLower)
            }
        }

        // Format display based on exercise unit type
        when (lift.unit) {
            "sec" -> {
                val mins = lift.maxReps / 60
                val secs = lift.maxReps % 60
                holder.tvWeight.text = if (mins > 0) "${mins}m ${secs}s" else "${secs}s"
                holder.tvDetails.text = "Longest hold"
            }
            "reps" -> {
                holder.tvWeight.text = "${lift.maxReps} reps"
                holder.tvDetails.text = "Bodyweight PR"
            }
            else -> {
                // Weighted exercise - convert to user's preferred unit
                val displayWeight = if (prefUnit == "lbs") {
                    UnitConverter.kgToLbs(lift.maxWeight)
                } else {
                    lift.maxWeight
                }
                holder.tvWeight.text = "${String.format("%.1f", displayWeight)} $prefUnit"
                holder.tvDetails.text = "${String.format("%.1f", displayWeight)} $prefUnit × ${lift.maxReps} reps"
            }
        }
    }

    private fun getEmojiForMuscle(muscle: String): String {
        return when {
            muscle.contains("leg") -> "🦵"
            muscle.contains("glute") || muscle.contains("posterior") || muscle.contains("deadlift") -> "🍑"
            muscle.contains("shoulder") -> "💪"
            muscle.contains("bicep") -> "💪"
            muscle.contains("tricep") -> "💪"
            muscle.contains("core") || muscle.contains("abs") -> "🔥"
            muscle.contains("cardio") || muscle.contains("full body") -> "🏃"
            else -> "🏋️"
        }
    }

    override fun getItemCount() = lifts.size
}
