package com.FitAgent

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.FitAgent.adapters.HistoryAdapter
import com.FitAgent.database.AppDatabase
import com.FitAgent.models.WorkoutSession
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class HistoryActivity : AppCompatActivity() {

    private lateinit var adapter: HistoryAdapter
    private lateinit var recycler: RecyclerView
    private lateinit var tvEmpty: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        recycler = findViewById(R.id.recycler_history)
        tvEmpty = findViewById(R.id.tv_empty)
        recycler.layoutManager = LinearLayoutManager(this)

        loadHistory()
    }

    private fun loadHistory() {
        val db = AppDatabase.getDatabase(this)
        lifecycleScope.launch {
            val sessions = db.workoutDao().getAllSessions().toMutableList()
            if (sessions.isEmpty()) {
                tvEmpty.visibility = View.VISIBLE
                recycler.visibility = View.GONE
            } else {
                tvEmpty.visibility = View.GONE
                recycler.visibility = View.VISIBLE
                adapter = HistoryAdapter(sessions) { session ->
                    val intent = Intent(this@HistoryActivity, SessionDetailActivity::class.java)
                    intent.putExtra("SESSION_ID", session.id)
                    intent.putExtra("SESSION_DATE", session.date)
                    intent.putExtra("SESSION_GOAL", session.goal)
                    intent.putExtra("SESSION_EQUIPMENT", session.equipment)
                    intent.putExtra("SESSION_DURATION", session.duration)
                    intent.putExtra("SESSION_VOLUME", session.totalVolume)
                    startActivity(intent)
                }
                recycler.adapter = adapter
                attachSwipeToDelete()
            }
        }
    }

    private fun attachSwipeToDelete() {
        val swipeCallback = object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val session = adapter.getSessionAt(position)
                showDeleteConfirmation(session, position)
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val background = ColorDrawable(Color.parseColor("#FF4444"))
                background.setBounds(
                    itemView.left, itemView.top,
                    itemView.right, itemView.bottom
                )
                background.draw(c)
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }
        ItemTouchHelper(swipeCallback).attachToRecyclerView(recycler)
    }

    private fun showDeleteConfirmation(session: WorkoutSession, position: Int) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Workout?")
            .setMessage("Delete workout from ${session.date}?")
            .setPositiveButton("Delete") { _, _ ->
                performDelete(session, position)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                adapter.notifyItemChanged(position)
                dialog.dismiss()
            }
            .setOnCancelListener {
                adapter.notifyItemChanged(position)
            }
            .show()
    }

    private fun performDelete(session: WorkoutSession, position: Int) {
        adapter.removeItem(position)

        val snackbar = Snackbar.make(
            recycler,
            "Workout deleted",
            5000
        ).setAction("UNDO") {
            adapter.restoreItem(session, position)
        }
        snackbar.setActionTextColor(Color.parseColor("#00FF94"))

        var wasUndone = false
        snackbar.addCallback(object : Snackbar.Callback() {
            override fun onDismissed(transientBottomBar: Snackbar?, event: Int) {
                if (event == DISMISS_EVENT_ACTION) {
                    wasUndone = true
                } else if (!wasUndone) {
                    val db = AppDatabase.getDatabase(this@HistoryActivity)
                    lifecycleScope.launch {
                        db.workoutDao().deleteLogsForSession(session.id)
                        db.workoutDao().deleteSession(session)

                        if (adapter.itemCount == 0) {
                            tvEmpty.visibility = View.VISIBLE
                            recycler.visibility = View.GONE
                        }
                    }
                }
            }
        })
        snackbar.show()
    }
}
