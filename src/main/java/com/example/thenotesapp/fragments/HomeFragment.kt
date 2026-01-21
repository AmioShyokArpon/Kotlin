package com.example.thenotesapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.thenotesapp.MainActivity
import com.example.thenotesapp.R
import com.example.thenotesapp.adapter.NoteAdapter
import com.example.thenotesapp.databinding.FragmentHomeBinding
import com.example.thenotesapp.model.Note
import com.example.thenotesapp.viewmodel.NoteViewModel

class HomeFragment : Fragment(R.layout.fragment_home),
    SearchView.OnQueryTextListener,
    MenuProvider {

    private var homeBinding: FragmentHomeBinding? = null
    private val binding get() = homeBinding!!

    private lateinit var notesViewModel: NoteViewModel
    private lateinit var noteAdapter: NoteAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        homeBinding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(this, viewLifecycleOwner, Lifecycle.State.RESUMED)

        notesViewModel = (activity as MainActivity).noteViewModel
        setupRecyclerView()

        binding.addNoteFab.setOnClickListener {
            it.findNavController()
                .navigate(R.id.action_homeFragment_to_addNoteFragment)
        }
    }

    private fun setupRecyclerView() {
        noteAdapter = NoteAdapter()

        binding.homeRecyclerView.apply {
            layoutManager =
                StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            setHasFixedSize(true)
            adapter = noteAdapter
        }

        notesViewModel.getAllNotes()
            .observe(viewLifecycleOwner) { notes ->
                noteAdapter.differ.submitList(notes)
                updateUI(notes)
            }
    }

    private fun updateUI(notes: List<Note>) {
        if (notes.isNotEmpty()) {
            binding.homeRecyclerView.visibility = View.VISIBLE
            binding.emptyNotesImage.visibility = View.GONE
        } else {
            binding.homeRecyclerView.visibility = View.GONE
            binding.emptyNotesImage.visibility = View.VISIBLE
        }
    }

    private fun searchNote(query: String?) {
        val searchQuery = "%$query%"
        notesViewModel.searchNote(searchQuery)
            .observe(viewLifecycleOwner) { list ->
                noteAdapter.differ.submitList(list)
            }
    }

    override fun onQueryTextSubmit(query: String?): Boolean = false

    override fun onQueryTextChange(newText: String?): Boolean {
        searchNote(newText)
        return true
    }

    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menu.clear()
        menuInflater.inflate(R.menu.home_menu, menu)

        val searchView =
            menu.findItem(R.id.searchMenu).actionView as SearchView

        searchView.isSubmitButtonEnabled = false
        searchView.setOnQueryTextListener(this)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean = false

    override fun onDestroyView() {
        super.onDestroyView()
        homeBinding = null
    }
}
