package com.scheduletracker.app.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.scheduletracker.app.model.Category
import com.scheduletracker.app.model.ScheduleItem

class ScheduleDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "schedule_tracker.db"
        const val DATABASE_VERSION = 1

        const val TABLE_SCHEDULE = "schedules"
        const val COLUMN_ID = "id"
        const val COLUMN_TITLE = "title"
        const val COLUMN_DAY = "day_of_week"
        const val COLUMN_START_TIME = "start_time"
        const val COLUMN_END_TIME = "end_time"
        const val COLUMN_CATEGORY = "category"
        const val COLUMN_NOTES = "notes"
        const val COLUMN_IS_COMPLETED = "is_completed"
        const val COLUMN_PRIORITY = "priority"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_SCHEDULE (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TITLE TEXT NOT NULL,
                $COLUMN_DAY TEXT NOT NULL,
                $COLUMN_START_TIME TEXT NOT NULL,
                $COLUMN_END_TIME TEXT NOT NULL,
                $COLUMN_CATEGORY TEXT NOT NULL,
                $COLUMN_NOTES TEXT,
                $COLUMN_IS_COMPLETED INTEGER DEFAULT 0,
                $COLUMN_PRIORITY TEXT DEFAULT 'Medium'
            )
        """.trimIndent()
        db.execSQL(createTable)
        seedInitialData(db)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SCHEDULE")
        onCreate(db)
    }

    private fun seedInitialData(db: SQLiteDatabase) {
        val defaultItems = listOf(
            ScheduleItem(title = "Morning Workout", dayOfWeek = "Monday", startTime = "07:00", endTime = "08:00", category = Category.FITNESS, notes = "Cardio & Stretching", priority = "High"),
            ScheduleItem(title = "Team Standup", dayOfWeek = "Monday", startTime = "09:30", endTime = "10:00", category = Category.WORK, notes = "Weekly sync & sprint planning", priority = "High"),
            ScheduleItem(title = "Deep Study Block", dayOfWeek = "Monday", startTime = "11:00", endTime = "13:00", category = Category.STUDY, notes = "Focus on core modules", priority = "Medium"),
            ScheduleItem(title = "Project Work", dayOfWeek = "Monday", startTime = "14:30", endTime = "17:00", category = Category.WORK, notes = "Feature development", priority = "High"),
            ScheduleItem(title = "Evening Reading", dayOfWeek = "Monday", startTime = "20:00", endTime = "21:00", category = Category.PERSONAL, notes = "Read 20 pages", priority = "Low"),

            ScheduleItem(title = "Algorithms & Data Structures", dayOfWeek = "Tuesday", startTime = "09:00", endTime = "11:00", category = Category.STUDY, notes = "Practice problems", priority = "High"),
            ScheduleItem(title = "Client Discussion", dayOfWeek = "Tuesday", startTime = "14:00", endTime = "15:00", category = Category.MEETING, notes = "Review deliverable milestones", priority = "Medium"),
            ScheduleItem(title = "Gym Session", dayOfWeek = "Tuesday", startTime = "18:00", endTime = "19:30", category = Category.FITNESS, notes = "Strength training", priority = "Medium"),

            ScheduleItem(title = "Product Architecture Review", dayOfWeek = "Wednesday", startTime = "10:00", endTime = "12:00", category = Category.WORK, notes = "System design", priority = "High"),
            ScheduleItem(title = "Review & Practice", dayOfWeek = "Wednesday", startTime = "15:00", endTime = "17:00", category = Category.STUDY, notes = "Review assignments", priority = "Medium"),

            ScheduleItem(title = "Weekly Retrospective", dayOfWeek = "Friday", startTime = "16:00", endTime = "17:00", category = Category.WORK, notes = "Team wrap-up", priority = "Medium"),
            ScheduleItem(title = "Weekend Outdoor Run", dayOfWeek = "Saturday", startTime = "08:00", endTime = "09:15", category = Category.FITNESS, notes = "5km run", priority = "Medium")
        )

        for (item in defaultItems) {
            val values = ContentValues().apply {
                put(COLUMN_TITLE, item.title)
                put(COLUMN_DAY, item.dayOfWeek)
                put(COLUMN_START_TIME, item.startTime)
                put(COLUMN_END_TIME, item.endTime)
                put(COLUMN_CATEGORY, item.category.name)
                put(COLUMN_NOTES, item.notes)
                put(COLUMN_IS_COMPLETED, if (item.isCompleted) 1 else 0)
                put(COLUMN_PRIORITY, item.priority)
            }
            db.insert(TABLE_SCHEDULE, null, values)
        }
    }

    fun insertSchedule(item: ScheduleItem): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, item.title)
            put(COLUMN_DAY, item.dayOfWeek)
            put(COLUMN_START_TIME, item.startTime)
            put(COLUMN_END_TIME, item.endTime)
            put(COLUMN_CATEGORY, item.category.name)
            put(COLUMN_NOTES, item.notes)
            put(COLUMN_IS_COMPLETED, if (item.isCompleted) 1 else 0)
            put(COLUMN_PRIORITY, item.priority)
        }
        return db.insert(TABLE_SCHEDULE, null, values)
    }

    fun updateSchedule(item: ScheduleItem): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, item.title)
            put(COLUMN_DAY, item.dayOfWeek)
            put(COLUMN_START_TIME, item.startTime)
            put(COLUMN_END_TIME, item.endTime)
            put(COLUMN_CATEGORY, item.category.name)
            put(COLUMN_NOTES, item.notes)
            put(COLUMN_IS_COMPLETED, if (item.isCompleted) 1 else 0)
            put(COLUMN_PRIORITY, item.priority)
        }
        return db.update(TABLE_SCHEDULE, values, "$COLUMN_ID = ?", arrayOf(item.id.toString()))
    }

    fun toggleComplete(id: Long, isCompleted: Boolean): Int {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_IS_COMPLETED, if (isCompleted) 1 else 0)
        }
        return db.update(TABLE_SCHEDULE, values, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }

    fun deleteSchedule(id: Long): Int {
        val db = writableDatabase
        return db.delete(TABLE_SCHEDULE, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }

    fun getSchedulesByDay(day: String): List<ScheduleItem> {
        val list = mutableListOf<ScheduleItem>()
        val db = readableDatabase
        val cursor = db.query(
            TABLE_SCHEDULE,
            null,
            "$COLUMN_DAY = ?",
            arrayOf(day),
            null,
            null,
            "$COLUMN_START_TIME ASC"
        )
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    ScheduleItem(
                        id = it.getLong(it.getColumnIndexOrThrow(COLUMN_ID)),
                        title = it.getString(it.getColumnIndexOrThrow(COLUMN_TITLE)),
                        dayOfWeek = it.getString(it.getColumnIndexOrThrow(COLUMN_DAY)),
                        startTime = it.getString(it.getColumnIndexOrThrow(COLUMN_START_TIME)),
                        endTime = it.getString(it.getColumnIndexOrThrow(COLUMN_END_TIME)),
                        category = Category.fromString(it.getString(it.getColumnIndexOrThrow(COLUMN_CATEGORY))),
                        notes = it.getString(it.getColumnIndexOrThrow(COLUMN_NOTES)) ?: "",
                        isCompleted = it.getInt(it.getColumnIndexOrThrow(COLUMN_IS_COMPLETED)) == 1,
                        priority = it.getString(it.getColumnIndexOrThrow(COLUMN_PRIORITY)) ?: "Medium"
                    )
                )
            }
        }
        return list
    }

    fun getAllSchedules(): List<ScheduleItem> {
        val list = mutableListOf<ScheduleItem>()
        val db = readableDatabase
        val cursor = db.query(TABLE_SCHEDULE, null, null, null, null, null, "$COLUMN_DAY ASC, $COLUMN_START_TIME ASC")
        cursor.use {
            while (it.moveToNext()) {
                list.add(
                    ScheduleItem(
                        id = it.getLong(it.getColumnIndexOrThrow(COLUMN_ID)),
                        title = it.getString(it.getColumnIndexOrThrow(COLUMN_TITLE)),
                        dayOfWeek = it.getString(it.getColumnIndexOrThrow(COLUMN_DAY)),
                        startTime = it.getString(it.getColumnIndexOrThrow(COLUMN_START_TIME)),
                        endTime = it.getString(it.getColumnIndexOrThrow(COLUMN_END_TIME)),
                        category = Category.fromString(it.getString(it.getColumnIndexOrThrow(COLUMN_CATEGORY))),
                        notes = it.getString(it.getColumnIndexOrThrow(COLUMN_NOTES)) ?: "",
                        isCompleted = it.getInt(it.getColumnIndexOrThrow(COLUMN_IS_COMPLETED)) == 1,
                        priority = it.getString(it.getColumnIndexOrThrow(COLUMN_PRIORITY)) ?: "Medium"
                    )
                )
            }
        }
        return list
    }
}
