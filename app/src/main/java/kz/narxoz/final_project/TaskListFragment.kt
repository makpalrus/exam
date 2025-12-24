package kz.narxoz.final_project



import kz.narxoz.final_project.EditTaskViewModelFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch
import kz.narxoz.final_project.TaskRepository
import kz.narxoz.final_project.databinding.FragmentTaskListBinding
import kz.narxoz.final_project.db.AppDatabase

class TaskListFragment : Fragment() {

    private var _binding: FragmentTaskListBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TaskListViewModel by viewModels {
        val database = AppDatabase.getDatabase(requireContext())
        val repository = TaskRepository(database.taskDao())
        TaskListViewModelFactory(repository)
    }

    private lateinit var adapter: TaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupObservers()
        setupClickListeners()
        setupSwipeToDelete()
    }

    private fun setupRecyclerView() {
        adapter = TaskAdapter(
            onCheckClick = { task -> viewModel.toggleTaskCompletion(task) },
            onItemClick = { task ->
                // Переход к редактированию задачи
                val args = Bundle().apply {
                    putLong("taskId", task.id)
                }
                findNavController().navigate(R.id.editTaskFragment, args)
            }
        )

        binding.recyclerViewTasks.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewTasks.adapter = adapter
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.uiState.collect { uiState ->
                when (uiState) {
                    is TaskListUiState.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                        binding.textViewEmpty.visibility = View.GONE
                    }
                    is TaskListUiState.Success -> {
                        binding.progressBar.visibility = View.GONE
                        adapter.submitList(uiState.tasks)

                        // Показываем сообщение, если список пустой
                        if (uiState.tasks.isEmpty()) {
                            binding.textViewEmpty.visibility = View.VISIBLE
                            binding.textViewEmpty.text = when (viewModel.currentFilter.value) {
                                FilterType.TODAY -> "Нет задач на сегодня"
                                FilterType.IMPORTANT -> "Нет важных задач"
                                else -> "Нет задач"
                            }
                        } else {
                            binding.textViewEmpty.visibility = View.GONE
                        }
                    }
                    is TaskListUiState.Error -> {
                        binding.progressBar.visibility = View.GONE
                        binding.textViewEmpty.visibility = View.VISIBLE
                        binding.textViewEmpty.text = "Ошибка: ${uiState.message}"
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.currentFilter.collect { filter ->
                binding.tabLayout.getTabAt(filter.ordinal)?.select()
            }
        }
    }

    private fun setupClickListeners() {
        binding.tabLayout.addOnTabSelectedListener(object : com.google.android.material.tabs.TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: com.google.android.material.tabs.TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> viewModel.loadTasks(FilterType.ALL)
                    1 -> viewModel.loadTasks(FilterType.TODAY)
                    2 -> viewModel.loadTasks(FilterType.IMPORTANT)
                }
            }
            override fun onTabUnselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
            override fun onTabReselected(tab: com.google.android.material.tabs.TabLayout.Tab?) {}
        })

        binding.fabAddTask.setOnClickListener {
            // Переход к созданию новой задачи
            val args = Bundle().apply {
                putLong("taskId", -1)
            }
            findNavController().navigate(R.id.editTaskFragment, args)
        }
    }

    private fun setupSwipeToDelete() {
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                val task = adapter.currentList[position]
                viewModel.deleteTask(task)
            }
        })

        itemTouchHelper.attachToRecyclerView(binding.recyclerViewTasks)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}