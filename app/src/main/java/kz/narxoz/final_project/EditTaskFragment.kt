package kz.narxoz.final_project






import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import kz.narxoz.final_project.CategoryRepository
import kz.narxoz.final_project.TaskRepository
import kz.narxoz.final_project.databinding.FragmentEditTaskBinding
import kz.narxoz.final_project.db.AppDatabase
import kz.narxoz.final_project.db.CategoryEntity
import kz.narxoz.final_project.db.TaskEntity
import kz.narxoz.final_project.EditTaskViewModel
import kz.narxoz.final_project.EditTaskViewModelFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EditTaskFragment : Fragment() {

    private var _binding: FragmentEditTaskBinding? = null
    private val binding get() = _binding!!

    private val viewModel: EditTaskViewModel by viewModels {
        val database = AppDatabase.getDatabase(requireContext())
        val taskRepository = TaskRepository(database.taskDao())
        val categoryRepository = CategoryRepository(database.categoryDao())
        EditTaskViewModelFactory(taskRepository, categoryRepository)
    }

    private val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Получаем taskId из аргументов
        val taskId = arguments?.getLong("taskId", -1) ?: -1

        viewModel.loadTask(taskId)
        viewModel.loadCategories()

        setupObservers()
        setupClickListeners()
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.task.collect { task ->
                task?.let { updateUI(it) }
            }
        }

        lifecycleScope.launch {
            viewModel.categories.collect { categories ->
                updateCategorySpinner(categories)
            }
        }

        lifecycleScope.launch {
            viewModel.saved.collect { saved ->
                if (saved) {
                    findNavController().popBackStack()
                }
            }
        }

        lifecycleScope.launch {
            viewModel.errorMessage.collect { error ->
                error?.let {
                    // Показать ошибку пользователю
                    android.widget.Toast.makeText(requireContext(), error, android.widget.Toast.LENGTH_SHORT).show()
                    viewModel.clearError()
                }
            }
        }
    }

    private fun updateUI(task: TaskEntity) {
        with(binding) {
            if (editTextTitle.text.toString() != task.title) {
                editTextTitle.setText(task.title)
            }

            editTextDescription.setText(task.description ?: "")

            task.dateTime?.let {
                textViewDateTime.text = dateTimeFormat.format(it)
                buttonTime.isEnabled = true
            } ?: run {
                textViewDateTime.text = "Не установлено"
                buttonTime.isEnabled = false
            }

            switchImportant.isChecked = task.isImportant
            switchReminder.isChecked = task.reminderTime != null

            // Показываем кнопку удаления только для существующих задач
            buttonDelete.visibility = if (task.id != 0L) View.VISIBLE else View.GONE
        }
    }

    private fun updateCategorySpinner(categories: List<CategoryEntity>) {
        val categoryNames = listOf("Без категории") + categories.map { it.name }
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            categoryNames
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        binding.spinnerCategory.adapter = adapter

        // Выбираем текущую категорию задачи
        viewModel.task.value?.categoryId?.let { categoryId ->
            val position = categories.indexOfFirst { it.id == categoryId } + 1
            if (position > 0 && position < adapter.count) {
                binding.spinnerCategory.setSelection(position)
            }
        }
    }

    private fun setupClickListeners() {
        with(binding) {
            editTextTitle.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    viewModel.updateTitle(editTextTitle.text.toString())
                }
            }

            editTextDescription.setOnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    viewModel.updateDescription(editTextDescription.text.toString())
                }
            }

            buttonDate.setOnClickListener { showDatePicker() }
            buttonTime.setOnClickListener { showTimePicker() }

            spinnerCategory.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                    if (position == 0) {
                        // "Без категории"
                        viewModel.updateCategory(null)
                    } else {
                        // Найти категорию по имени
                        val categories = viewModel.categories.value
                        if (position - 1 < categories.size) {
                            viewModel.updateCategory(categories[position - 1].id)
                        }
                    }
                }
                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
                    viewModel.updateCategory(null)
                }
            }

            switchImportant.setOnCheckedChangeListener { _, isChecked ->
                viewModel.updateImportant(isChecked)
            }

            switchReminder.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    // Установить напоминание за 15 минут до времени задачи
                    val currentTask = viewModel.task.value
                    if (currentTask?.dateTime != null) {
                        val reminderTime = currentTask.dateTime!! - (15 * 60 * 1000)
                        viewModel.updateReminder(reminderTime)
                    }
                } else {
                    viewModel.updateReminder(null)
                }
            }

            buttonSave.setOnClickListener {
                viewModel.updateTitle(editTextTitle.text.toString())
                viewModel.updateDescription(editTextDescription.text.toString())
                viewModel.saveTask()
            }

            buttonDelete.setOnClickListener {
                viewModel.deleteTask()
            }
        }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedCalendar = Calendar.getInstance().apply {
                    set(selectedYear, selectedMonth, selectedDay)
                    // Сохраняем текущее время, если оно уже было установлено
                    viewModel.task.value?.dateTime?.let {
                        val oldCalendar = Calendar.getInstance().apply { timeInMillis = it }
                        set(Calendar.HOUR_OF_DAY, oldCalendar.get(Calendar.HOUR_OF_DAY))
                        set(Calendar.MINUTE, oldCalendar.get(Calendar.MINUTE))
                    }
                }
                viewModel.updateDateTime(selectedCalendar.timeInMillis)
                binding.buttonTime.isEnabled = true
            },
            year, month, day
        ).show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        viewModel.task.value?.dateTime?.let {
            calendar.timeInMillis = it
        }
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(
            requireContext(),
            { _, selectedHour, selectedMinute ->
                val currentDateTime = viewModel.task.value?.dateTime
                    ?: Calendar.getInstance().apply {
                        // Если дата не установлена, используем сегодня
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                    }.timeInMillis

                val updatedCalendar = Calendar.getInstance().apply {
                    timeInMillis = currentDateTime
                    set(Calendar.HOUR_OF_DAY, selectedHour)
                    set(Calendar.MINUTE, selectedMinute)
                    set(Calendar.SECOND, 0)
                }
                viewModel.updateDateTime(updatedCalendar.timeInMillis)
            },
            hour, minute, true
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}