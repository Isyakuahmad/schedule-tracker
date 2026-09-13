package com.scheduletracker.app.adapter

import android.graphics.Color
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.checkbox.MaterialCheckBox
import com.scheduletracker.app.R
import com.scheduletracker.app.model.ScheduleItem

class ScheduleAdapter(
    private var items: List<ScheduleItem>,
    private val onToggleComplete: (ScheduleItem, Boolean) -> Unit,
    private val onDelete: (ScheduleItem) -> Unit,
    private val onItemClick: (ScheduleItem) -> Unit
) : RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder>() {

    class ScheduleViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val viewCategoryColor: View = view.findViewById(R.id.viewCategoryColor)
        val tvTimeRange: TextView = view.findViewById(R.id.tvTimeRange)
        val tvCategoryBadge: TextView = view.findViewById(R.id.tvCategoryBadge)
        val tvPriorityBadge: TextView = view.findViewById(R.id.tvPriorityBadge)
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvNotes: TextView = view.findViewById(R.id.tvNotes)
        val cbCompleted: MaterialCheckBox = view.findViewById(R.id.cbCompleted)
        val btnDelete: ImageView = view.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScheduleViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_schedule, parent, false)
        return ScheduleViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScheduleViewHolder, position: Int) {
        val item = items[position]

        holder.tvTimeRange.text = item.getFormattedTimeRange()
        holder.tvTitle.text = item.title
        holder.tvCategoryBadge.text = item.category.displayName

        try {
            val catColor = Color.parseColor(item.category.colorHex)
            holder.viewCategoryColor.setBackgroundColor(catColor)
            holder.tvCategoryBadge.setBackgroundColor(catColor)
        } catch (e: Exception) {
            // fallback
        }

        holder.tvPriorityBadge.text = item.priority

        if (item.notes.isNotBlank()) {
            holder.tvNotes.text = item.notes
            holder.tvNotes.visibility = View.VISIBLE
        } else {
            holder.tvNotes.visibility = View.GONE
        }

        // Handle completed state
        holder.cbCompleted.setOnCheckedChangeListener(null)
        holder.cbCompleted.isChecked = item.isCompleted
        updateStrikethrough(holder.tvTitle, item.isCompleted)

        holder.cbCompleted.setOnCheckedChangeListener { _, isChecked ->
            updateStrikethrough(holder.tvTitle, isChecked)
            onToggleComplete(item, isChecked)
        }

        holder.btnDelete.setOnClickListener {
            onDelete(item)
        }

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }
    }

    private fun updateStrikethrough(textView: TextView, completed: Boolean) {
        if (completed) {
            textView.paintFlags = textView.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            textView.alpha = 0.6f
        } else {
            textView.paintFlags = textView.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            textView.alpha = 1.0f
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateList(newItems: List<ScheduleItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
