package com.scheduletracker.app.model

data class ScheduleItem(
    val id: Long = 0,
    val title: String,
    val dayOfWeek: String,
    val startTime: String,
    val endTime: String,
    val category: Category,
    val notes: String = "",
    val isCompleted: Boolean = false,
    val priority: String = "Medium"
) {
    fun getFormattedTimeRange(): String {
        return "$startTime - $endTime"
    }
}
