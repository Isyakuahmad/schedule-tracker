package com.scheduletracker.app

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.scheduletracker.app.adapter.ScheduleAdapter
import com.scheduletracker.app.data.ScheduleDbHelper
import com.scheduletracker.app.model.Category
import com.scheduletracker.app.model.ScheduleItem
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var dbHelper: ScheduleDbHelper
    private lateinit var adapter: ScheduleAdapter
    private lateinit var rvSchedules: RecyclerView
    private lateinit var layoutEmptyState: LinearLayout
    private lateinit var layoutDayButtons: LinearLayout
    private lateinit var cardHappeningNow: MaterialCardView
    private lateinit var tvActiveTitle: TextView
    private lateinit var tvActiveTime: TextView
    private lateinit var tvActiveCategory: TextView
    private lateinit var tvProgressSummary: TextView
    private lateinit var pbDailyProgress: ProgressBar
    private lateinit var tvCurrentDate: TextView

    private val daysOfWeek = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    private var selectedDay = "Monday"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dbHelper = ScheduleDbHelper(this)

        initViews()
        setupCurrentDayAndDate()
        setupDayButtons()
        setupRecyclerView()
        refreshScheduleList()
    }

    private fun initViews() {
        rvSchedules = findViewById(R.id.rvSchedules)
        layoutEmptyState = findViewById(R.id.layoutEmptyState)
        layoutDayButtons = findViewById(R.id.layoutDayButtons)
        cardHappeningNow = findViewById(R.id.cardHappeningNow)
        tvActiveTitle = findViewById(R.id.tvActiveTitle)
        tvActiveTime = findViewById(R.id.tvActiveTime)
        tvActiveCategory = findViewById(R.id.tvActiveCategory)
        tvProgressSummary = findViewById(R.id.tvProgressSummary)
        pbDailyProgress = findViewById(R.id.pbDailyProgress)
        tvCurrentDate = findViewById(R.id.tvCurrentDate)

        findViewById<ExtendedFloatingActionButton>(R.id.fabAdd).setOnClickListener {
            showAddEditDialog(null)
        }
    }

    private fun setupCurrentDayAndDate() {
        val calendar = Calendar.getInstance()
        val dayFormat = SimpleDateFormat("EEEE", Locale.US)
        val dateFormat = SimpleDateFormat("EEEE, MMM d", Locale.US)

        val currentDayName = dayFormat.format(calendar.time)
        selectedDay = if (daysOfWeek.contains(currentDayName)) currentDayName else "Monday"

        tvCurrentDate.text = "Today • ${dateFormat.format(calendar.time)}"
    }

    private fun setupDayButtons() {
        layoutDayButtons.removeAllViews()

        for (day in daysOfWeek) {
            val button = Button(this).apply {
                text = day.substring(0, 3) // Mon, Tue, etc.
                isAllCaps = true
                textSize = 12f
                setPadding(28, 12, 28, 12)

                val isSelected = (day == selectedDay)
                if (isSelected) {
                    setBackgroundColor(Color.parseColor("#4F46E5"))
                    setTextColor(Color.WHITE)
                } else {
                    setBackgroundColor(Color.parseColor("#EEF2FF"))
                    setTextColor(Color.parseColor("#4F46E5"))
                }

                setOnClickListener {
                    selectedDay = day
                    setupDayButtons()
                    refreshScheduleList()
                }
            }

            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(6, 0, 6, 0)
            }
            layoutDayButtons.addView(button, params)
        }
    }

    private fun setupRecyclerView() {
        adapter = ScheduleAdapter(
            items = emptyList(),
            onToggleComplete = { item, isCompleted ->
                dbHelper.toggleComplete(item.id, isCompleted)
                updateDailyProgress()
            },
            onDelete = { item ->
                confirmDelete(item)
            },
            onItemClick = { item ->
                showAddEditDialog(item)
            }
        )

        rvSchedules.layoutManager = LinearLayoutManager(this)
        rvSchedules.adapter = adapter
    }

    private fun refreshScheduleList() {
        val items = dbHelper.getSchedulesByDay(selectedDay)
        adapter.updateList(items)

        if (items.isEmpty()) {
            layoutEmptyState.visibility = View.VISIBLE
            rvSchedules.visibility = View.GONE
        } else {
            layoutEmptyState.visibility = View.GONE
            rvSchedules.visibility = View.VISIBLE
        }

        updateHappeningNow(items)
        updateDailyProgress(items)
    }

    private fun updateHappeningNow(items: List<ScheduleItem>) {
        val currentTime = SimpleDateFormat("HH:mm", Locale.US).format(Calendar.getInstance().time)
        val activeItem = items.find { currentTime >= it.startTime && currentTime <= it.endTime }

        if (activeItem != null) {
            cardHappeningNow.visibility = View.VISIBLE
            tvActiveTitle.text = activeItem.title
            tvActiveTime.text = "${activeItem.startTime} - ${activeItem.endTime}"
            tvActiveCategory.text = activeItem.category.displayName
            try {
                tvActiveCategory.setTextColor(Color.parseColor(activeItem.category.colorHex))
            } catch (e: Exception) {}
        } else {
            // Find next upcoming
            val upcoming = items.find { it.startTime > currentTime }
            if (upcoming != null) {
                cardHappeningNow.visibility = View.VISIBLE
                tvActiveTitle.text = upcoming.title
                tvActiveTime.text = "Next at ${upcoming.startTime}"
                tvActiveCategory.text = upcoming.category.displayName
            } else {
                cardHappeningNow.visibility = View.GONE
            }
        }
    }

    private fun updateDailyProgress(providedItems: List<ScheduleItem>? = null) {
        val items = providedItems ?: dbHelper.getSchedulesByDay(selectedDay)
        if (items.isEmpty()) {
            tvProgressSummary.text = "No tasks for $selectedDay"
            pbDailyProgress.progress = 0
            return
        }

        val completedCount = items.count { it.isCompleted }
        val totalCount = items.size
        val percent = (completedCount * 100) / totalCount

        tvProgressSummary.text = "$completedCount of $totalCount completed ($percent%)"
        pbDailyProgress.progress = percent
    }

    private fun showAddEditDialog(existingItem: ScheduleItem?) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_schedule, null)
        val etTitle = dialogView.findViewById<EditText>(R.id.etTitle)
        val etStartTime = dialogView.findViewById<EditText>(R.id.etStartTime)
        val etEndTime = dialogView.findViewById<EditText>(R.id.etEndTime)
        val etNotes = dialogView.findViewById<EditText>(R.id.etNotes)
        val spinnerDay = dialogView.findViewById<Spinner>(R.id.spinnerDay)
        val spinnerCategory = dialogView.findViewById<Spinner>(R.id.spinnerCategory)
        val spinnerPriority = dialogView.findViewById<Spinner>(R.id.spinnerPriority)
        val tvDialogTitle = dialogView.findViewById<TextView>(R.id.tvDialogTitle)

        // Setup Spinners
        val dayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, daysOfWeek)
        spinnerDay.adapter = dayAdapter
        spinnerDay.setSelection(daysOfWeek.indexOf(existingItem?.dayOfWeek ?: selectedDay))

        val categoryNames = Category.entries.map { it.displayName }
        val catAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, categoryNames)
        spinnerCategory.adapter = catAdapter
        existingItem?.let {
            spinnerCategory.setSelection(Category.entries.indexOf(it.category))
        }

        val priorities = listOf("Low", "Medium", "High")
        val priorityAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, priorities)
        spinnerPriority.adapter = priorityAdapter
        existingItem?.let {
            spinnerPriority.setSelection(priorities.indexOf(it.priority))
        } ?: run {
            spinnerPriority.setSelection(1) // Medium
        }

        if (existingItem != null) {
            tvDialogTitle.text = "Edit Schedule Event"
            etTitle.setText(existingItem.title)
            etStartTime.setText(existingItem.startTime)
            etEndTime.setText(existingItem.endTime)
            etNotes.setText(existingItem.notes)
        }

        AlertDialog.Builder(this)
            .setView(dialogView)
            .setPositiveButton(if (existingItem != null) "Update" else "Add") { _, _ ->
                val title = etTitle.text.toString().trim()
                val startTime = etStartTime.text.toString().trim()
                val endTime = etEndTime.text.toString().trim()
                val notes = etNotes.text.toString().trim()
                val day = spinnerDay.selectedItem.toString()
                val category = Category.entries[spinnerCategory.selectedItemPosition]
                val priority = priorities[spinnerPriority.selectedItemPosition]

                if (title.isEmpty()) {
                    Toast.makeText(this, "Title cannot be empty", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                val item = ScheduleItem(
                    id = existingItem?.id ?: 0,
                    title = title,
                    dayOfWeek = day,
                    startTime = if (startTime.isEmpty()) "09:00" else startTime,
                    endTime = if (endTime.isEmpty()) "10:00" else endTime,
                    category = category,
                    notes = notes,
                    isCompleted = existingItem?.isCompleted ?: false,
                    priority = priority
                )

                if (existingItem != null) {
                    dbHelper.updateSchedule(item)
                    Toast.makeText(this, "Event updated", Toast.LENGTH_SHORT).show()
                } else {
                    dbHelper.insertSchedule(item)
                    Toast.makeText(this, "Event added", Toast.LENGTH_SHORT).show()
                }

                selectedDay = day
                setupDayButtons()
                refreshScheduleList()
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    private fun confirmDelete(item: ScheduleItem) {
        AlertDialog.Builder(this)
            .setTitle("Delete Event")
            .setMessage("Are you sure you want to delete \"${item.title}\"?")
            .setPositiveButton("Delete") { _, _ ->
                dbHelper.deleteSchedule(item.id)
                refreshScheduleList()
                Toast.makeText(this, "Event deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }
}
