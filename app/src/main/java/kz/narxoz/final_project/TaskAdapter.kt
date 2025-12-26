package kz.narxoz.final_project

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import kz.narxoz.final_project.databinding.ItemTaskBinding
import kz.narxoz.final_project.db.TaskEntity
import java.text.SimpleDateFormat
import java.util.Locale

class TaskAdapter(
    private val onCheckClick: (TaskEntity) -> Unit,
    private val onItemClick: (TaskEntity) -> Unit
) : ListAdapter<TaskEntity, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TaskViewHolder(private val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        private val dateOnlyFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

        fun bind(task: TaskEntity) {
            with(binding) {
                textViewTitle.text = task.title
                textViewDescription.text = task.description
                textViewDescription.visibility = if (task.description.isNullOrEmpty()) ViewGroup.GONE else ViewGroup.VISIBLE

                task.dateTime?.let {
                    textViewTime.text = dateFormat.format(it)
                    textViewDate.text = dateOnlyFormat.format(it)
                    textViewTime.visibility = View.VISIBLE
                    textViewDate.visibility = View.VISIBLE
                } ?: run {
                    textViewTime.visibility = View.GONE
                    textViewDate.visibility = View.GONE
                }

                checkBoxCompleted.isChecked = task.isCompleted

                if (task.isCompleted) {
                    textViewTitle.paintFlags = textViewTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    textViewDescription.paintFlags = textViewDescription.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    root.alpha = 0.6f
                } else {
                    textViewTitle.paintFlags = textViewTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    textViewDescription.paintFlags = textViewDescription.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                    root.alpha = 1f
                }

                // TODO: Установить цвет категории
                // viewCategoryColor.setBackgroundColor(Color.parseColor(categoryColor))

                // TODO: Установить название категории
                // textViewCategory.text = categoryName
                // textViewCategory.visibility = if (categoryName.isNotEmpty()) View.VISIBLE else View.GONE

                checkBoxCompleted.setOnClickListener { onCheckClick(task) }
                root.setOnClickListener { onItemClick(task) }
            }
        }
    }

    class TaskDiffCallback : DiffUtil.ItemCallback<TaskEntity>() {
        override fun areItemsTheSame(oldItem: TaskEntity, newItem: TaskEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: TaskEntity, newItem: TaskEntity): Boolean {
            return oldItem == newItem
        }
    }
}