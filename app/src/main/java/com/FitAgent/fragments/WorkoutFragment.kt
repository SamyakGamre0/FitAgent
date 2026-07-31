package com.FitAgent.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.FitAgent.LoggerActivity
import com.FitAgent.R
import com.FitAgent.adapters.RoutineAdapter
import com.FitAgent.database.AppDatabase
import com.FitAgent.database.RoutineDao
import com.FitAgent.models.RoutineWithExercises
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

class WorkoutFragment : Fragment() {

    private lateinit var routineDao: RoutineDao
    private lateinit var rvRoutines: RecyclerView
    private lateinit var emptyState: LinearLayout
    private lateinit var tvMyRoutinesHeader: TextView
    private lateinit var adapter: RoutineAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_workout, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        routineDao = AppDatabase.getDatabase(requireContext()).routineDao()

        rvRoutines = view.findViewById(R.id.rvRoutines)
        emptyState = view.findViewById(R.id.emptyState)
        tvMyRoutinesHeader = view.findViewById(R.id.tvMyRoutinesHeader)

        val btnStartEmptyWorkout = view.findViewById<MaterialButton>(R.id.btnStartEmptyWorkout)
        val btnNewRoutine = view.findViewById<MaterialButton>(R.id.btnNewRoutine)
        val btnExplore = view.findViewById<MaterialButton>(R.id.btnExplore)

        adapter = RoutineAdapter(
            items = emptyList(),
            onStartClick = { routine ->
                val intent = Intent(requireContext(), LoggerActivity::class.java)
                intent.putExtra("ROUTINE_ID", routine.routine.id)
                startActivity(intent)
            },
            onMoreClick = { routine, anchorView ->
                showPopupMenu(routine, anchorView)
            },
            onCardClick = { _ ->
                Toast.makeText(requireContext(), "Routine detail coming Day 9", Toast.LENGTH_SHORT).show()
            }
        )

        rvRoutines.layoutManager = LinearLayoutManager(requireContext())
        rvRoutines.adapter = adapter

        viewLifecycleOwner.lifecycleScope.launch {
            routineDao.getAllRoutines().collect { routines ->
                adapter.updateItems(routines)
                tvMyRoutinesHeader.text = "My Routines (${routines.size})"
                rvRoutines.visibility = if (routines.isEmpty()) View.GONE else View.VISIBLE
                emptyState.visibility = if (routines.isEmpty()) View.VISIBLE else View.GONE
            }
        }

        btnStartEmptyWorkout.setOnClickListener {
            val intent = Intent(requireContext(), LoggerActivity::class.java)
            intent.putExtra("ROUTINE_ID", -1L)
            startActivity(intent)
        }

        btnNewRoutine.setOnClickListener {
            val intent = Intent(requireContext(), com.FitAgent.CreateRoutineActivity::class.java)
            startActivity(intent)
        }

        btnExplore.setOnClickListener {
            Toast.makeText(requireContext(), "Explore coming Day 8", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showPopupMenu(routine: RoutineWithExercises, anchorView: View) {
        val popup = PopupMenu(requireContext(), anchorView)
        popup.menu.add(0, 1, 0, "Edit")
        popup.menu.add(0, 2, 1, "Duplicate")
        popup.menu.add(0, 3, 2, "Delete")

        popup.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                1 -> {
                    Toast.makeText(requireContext(), "Edit coming Day 7", Toast.LENGTH_SHORT).show()
                    true
                }
                2 -> {
                    Toast.makeText(requireContext(), "Duplicate coming Day 7", Toast.LENGTH_SHORT).show()
                    true
                }
                3 -> {
                    deleteRoutineWithUndo(routine)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun deleteRoutineWithUndo(routine: RoutineWithExercises) {
        viewLifecycleOwner.lifecycleScope.launch {
            routineDao.deleteRoutine(routine.routine.id)
            
            view?.let {
                Snackbar.make(it, "Routine deleted", Snackbar.LENGTH_LONG)
                    .setAction("Undo") {
                        viewLifecycleOwner.lifecycleScope.launch {
                            val newId = routineDao.insertRoutine(routine.routine.copy(id = 0))
                            val restoredExercises = routine.exercises.map { exercise ->
                                exercise.copy(id = 0, routineId = newId)
                            }
                            routineDao.insertRoutineExercises(restoredExercises)
                        }
                    }.show()
            }
        }
    }
}