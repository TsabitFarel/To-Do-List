package idn.tsabit.todolist.fragments.add

import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import idn.tsabit.todolist.R
import idn.tsabit.todolist.data.model.Priority
import idn.tsabit.todolist.data.model.ToDoData
import idn.tsabit.todolist.data.viewmodel.ToDoViewModel
import idn.tsabit.todolist.fragments.SharedViewModel
import kotlinx.android.synthetic.main.fragment_add.*
import kotlinx.android.synthetic.main.fragment_add.view.*

class AddFragment : Fragment() {

    private val mToDoViewModel: ToDoViewModel by viewModels()
    private val mSharedViewModel: SharedViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_add, container, false)
        // Set Menu
        setHasOptionsMenu(true)
        // Set Spinner
        view.spinner_priorities.onItemSelectedListener = mSharedViewModel.listener
        // Inflate the layout for this fragment
        return view
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.add_fragment_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.menu_add) {
            insertDataToDb()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun insertDataToDb() {
        val mTitle = edt_title.text.toString()
        val mPriority = spinner_priorities.selectedItem.toString()
        val mDescription = edt_description.text.toString()

        val validation = mSharedViewModel.verifyDataFromUser(mTitle, mDescription)
        if (validation) {
            // Insert data to Database
            val newData = ToDoData (0, mTitle, mSharedViewModel.parsePriority(mPriority), mDescription)
            mToDoViewModel.insertData(newData)
            Toast.makeText(requireContext(), "Successfully Added!", Toast.LENGTH_SHORT).show()
            // Navigate Back
            findNavController().navigate(R.id.action_addFragment_to_listFragment)
        } else {
            Toast.makeText(requireContext(), "Please Fill Out the Fields!", Toast.LENGTH_SHORT).show()
        }
    }
}