package com.tutorials.podiione

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.tutorials.podiione.adapter.DetailFeatureAdapter
import com.tutorials.podiione.arch.HomeState
import com.tutorials.podiione.arch.SnacksViewModel
import com.tutorials.podiione.databinding.FragmentSearchBinding
import kotlinx.coroutines.launch


class SearchFragment : Fragment() {
    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!
    private val searchAdapter by lazy { DetailFeatureAdapter() }

    private val viewModel by activityViewModels<SnacksViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.searchRecyclerView.adapter = searchAdapter
        changeSearchViewPlate()
        setUpQueryListener()
        getSearchedSnacks()
        searchAdapter.adapterClick {
            val action = SearchFragmentDirections.actionSearchFragmentToDetailsFragment(it)
            findNavController().navigate(action)
        }
    }

    private fun getSearchedSnacks() {
        lifecycleScope.launch {
            viewModel.snacksSearchFlow.collect { state ->
                when (state) {
                    is HomeState.Loading -> {
                        binding.progressBar.isVisible = true
                        binding.emptyStateTv.isVisible = false
                        binding.searchRecyclerView.isVisible = false
                    }
                    is HomeState.Success -> {
                        binding.progressBar.isVisible = false
                        binding.emptyStateTv.isVisible = false
                        binding.searchRecyclerView.isVisible = true
                        searchAdapter.submitList(state.allSnacks)
                    }
                    is HomeState.Failure -> {
                        binding.progressBar.isVisible = false
                        binding.emptyStateTv.isVisible = true
                        binding.searchRecyclerView.isVisible = false
                        binding.emptyStateTv.text = state.msg
                    }
                }

            }
        }
    }

    private fun setUpQueryListener() {
        binding.searchBar.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    viewModel.searchSnack(query.trim())
                } ?: viewModel.toggleSearchSnackState(HomeState.Failure("Search Snacks..."))
                return true

            }

            override fun onQueryTextChange(newText: String?): Boolean {

                newText?.let {
                    if (newText.trim().isEmpty()) {
                        viewModel.toggleSearchSnackState(HomeState.Failure("Search Snacks..."))
                        return@let
                    }
                    viewModel.searchSnack(newText.trim())

                } ?: viewModel.toggleSearchSnackState(HomeState.Failure("Search Snacks..."))
                return true
            }

        })
    }


    private fun changeSearchViewPlate(){
        val searchPlate = binding.searchBar.findViewById<View>(androidx.appcompat.R.id.search_plate)
        searchPlate.setBackgroundResource(R.drawable.transparent_background)
    }
}