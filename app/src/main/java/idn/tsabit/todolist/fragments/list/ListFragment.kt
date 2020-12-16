package idn.tsabit.todolist.fragments.list

import android.app.Activity
import android.os.Bundle
import android.view.*
import android.widget.SearchView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.google.android.material.snackbar.Snackbar
import idn.tsabit.todolist.R
import idn.tsabit.todolist.adapter.ListAdapter
import idn.tsabit.todolist.data.model.ToDoData
import idn.tsabit.todolist.data.viewmodel.ToDoViewModel
import idn.tsabit.todolist.databinding.FragmentListBinding
import idn.tsabit.todolist.fragments.SharedViewModel
import idn.tsabit.todolist.utils.hideKeyboard
import jp.wasabeef.recyclerview.animators.LandingAnimator
import kotlinx.android.synthetic.main.fragment_list.*
import kotlinx.android.synthetic.main.fragment_list.view.*
import kotlinx.android.synthetic.main.row_layout.view.*

class ListFragment : Fragment(), SearchView.OnQueryTextListener {

    private val mSharedViewModel: SharedViewModel by viewModels()
    private val mToDoViewModel: ToDoViewModel by viewModels()
    private val listAdapter: ListAdapter by lazy { ListAdapter() }

    private var bindingAdapters: FragmentListBinding? = null
    private val binding get() = bindingAdapters!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Data Binding
        bindingAdapters = FragmentListBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.mSharedViewModel = mSharedViewModel

        hideKeyboard(requireActivity())

        // Setup RecyclerView
        setUpRecyclerView()

        mToDoViewModel.getAllData.observe(viewLifecycleOwner, Observer { data ->
            mSharedViewModel.checkIfDatabaseEmpty(data)
            listAdapter.setData(data)
        })

        // Menu
        setHasOptionsMenu(true)
        return binding.root
    }

    private fun setUpRecyclerView() {
        val rvToDo = binding.rvTodo
        rvToDo.apply {
            layoutManager = StaggeredGridLayoutManager(2, GridLayoutManager.VERTICAL)
            adapter = listAdapter

            itemAnimator = LandingAnimator().apply {
                addDuration = 300
            }
        }
        swipeToDelete(rvToDo)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.menu_delete_all -> confirmDeleteAllData()
            R.id.menu_priority_high -> mToDoViewModel.sortByHighPriority.observe(this, Observer {
                listAdapter.setData(it)
            })
            R.id.menu_priority_low -> mToDoViewModel.sortByLowPriority.observe(this, Observer {
                listAdapter.setData(it)
            })
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menu.clear()
        inflater.inflate(R.menu.list_fragment_menu, menu)

        val search = menu.findItem(R.id.menu_search)
        val searchView = search.actionView as? SearchView
        searchView?.isSubmitButtonEnabled = true
        searchView?.setOnQueryTextListener(this)
    }

    private fun confirmDeleteAllData() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Delete Everything?")
                .setMessage("Are you sure want to Remove Everything?")
                .setPositiveButton("Yes") { _, _ ->
                    mToDoViewModel.deleteAllData()
                    Toast.makeText(
                            requireContext(),
                            "Successfully Removed Everything",
                            Toast.LENGTH_SHORT
                    ).show()
                }
                .setNegativeButton("No", null)
                .create()
                .show()
    }

    private fun swipeToDelete(recyclerView: RecyclerView) {
        val swipeToDeleteCallback = object : SwipeToDelete() {

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val deletedItem = listAdapter.dataList[viewHolder.adapterPosition]
                // Delete Item
                mToDoViewModel.deleteItem(deletedItem)
                listAdapter.notifyItemRemoved(viewHolder.adapterPosition)

                // Restore deleted Data
                restoreDeleteData(viewHolder.itemView, deletedItem)
            }
        }

        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }

    private fun restoreDeleteData(view: View, deletedItem: ToDoData) {
        val snackBar = Snackbar.make(view, "Deleted '${deletedItem.title}'", Snackbar.LENGTH_LONG)
        snackBar.setAction("Undo") {
            mToDoViewModel.insertData(deletedItem)
        }
        snackBar.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        bindingAdapters = null
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        if (query != null) {
            searchThroughDatabase(query)
        }
        return true
    }

    override fun onQueryTextChange(query: String?): Boolean {
        if (query != null) {
            searchThroughDatabase(query)
        }
        return true
    }

    private fun searchThroughDatabase(query: String) {
        val searchQuery = "%$query%"

        mToDoViewModel.searchDatabase(searchQuery).observe(this, Observer { list ->
            list?.let {
                listAdapter.setData(it)
            }
        })
    }
}