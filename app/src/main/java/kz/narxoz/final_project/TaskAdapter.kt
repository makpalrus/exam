package kz.narxoz.final_project



import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
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

        private val dateFormat = SimpleDateFormat("dd.MM HH:mm", Locale.getDefault())

        fun bind(task: TaskEntity) {
            with(binding) {
                textViewTitle.text = task.title
                textViewDescription.text = task.description
                textViewDescription.visibility = if (task.description.isNullOrEmpty()) ViewGroup.GONE else ViewGroup.VISIBLE

                textViewTime.text = task.dateTime?.let { dateFormat.format(it) } ?: ""

                checkBoxCompleted.isChecked = task.isCompleted
                textViewTitle.paint.isStrikeThruText = task.isCompleted
                textViewDescription.paint.isStrikeThruText = task.isCompleted

                // TODO: Установить цвет viewCategoryColor из categoryId (через ViewModel)

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