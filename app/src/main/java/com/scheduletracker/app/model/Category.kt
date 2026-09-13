package com.scheduletracker.app.model

enum class Category(val displayName: String, val colorHex: String) {
    WORK("Work", "#2563EB"),
    STUDY("Study", "#7C3AED"),
    FITNESS("Fitness", "#059669"),
    PERSONAL("Personal", "#D97706"),
    MEETING("Meeting", "#DC2626"),
    OTHER("Other", "#475569");

    companion object {
        fun fromString(name: String): Category {
            return entries.find { it.name.equals(name, ignoreCase = true) || it.displayName.equals(name, ignoreCase = true) } ?: OTHER
        }
    }
}
