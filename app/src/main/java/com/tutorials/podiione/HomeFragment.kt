package com.tutorials.podiione

import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tutorials.podiione.adapter.CategoryItemsAdapter
import com.tutorials.podiione.adapter.RecommendedAdapter
import com.tutorials.podiione.arch.HomeState
import com.tutorials.podiione.arch.SnacksViewModel
import com.tutorials.podiione.databinding.AddItemToCartDialogBinding
import com.tutorials.podiione.databinding.FragmentHomeBinding
import com.tutorials.podiione.model.CartItem
import com.tutorials.podiione.model.CurrentSelection
import com.tutorials.podiione.model.Response
import kotlinx.coroutines.launch


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val recommendedAdapter by lazy { RecommendedAdapter() }
    private val categoryItemsAdapter by lazy { CategoryItemsAdapter() }
    private val typedValue = TypedValue()
    private val theme by lazy { requireContext().theme }
    private val viewModel by activityViewModels<SnacksViewModel>()
    private lateinit var mContainer: ViewGroup

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.loadAllSnacks(this.requireContext())
        viewModel.setCurrentCategory(CurrentSelection(R.id.feature_category_text, "Featured"))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        mContainer = container!!
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.categoryItemsRecyclerView.adapter = categoryItemsAdapter
        binding.recommendationRecyclerView.adapter = recommendedAdapter
        categoryItemsAdapter.adapterClick {
            val action = HomeFragmentDirections.actionHomeFragmentToDetailsFragment(it)
            findNavController().navigate(action)
        }
        recommendedAdapter.adapterClick {
            val action = HomeFragmentDirections.actionHomeFragmentToDetailsFragment(it)
            findNavController().navigate(action)
        }
        categoryItemsAdapter.addToCartClick {
            showAddToCartDialog(it)
        }
        binding.searchPlate.setOnClickListener {
            val action = HomeFragmentDirections.actionHomeFragmentToSearchFragment()
            findNavController().navigate(action)
        }

        observeAllSnacks()
        binding.combosCategoryText.setOnClickListener {
            viewModel.setCurrentCategory(
                CurrentSelection(
                    it.id,
                    binding.combosCategoryText.text.toString()
                )
            )
            toggleCategoryColor(it.id)

        }
        binding.favoriteCategoryText.setOnClickListener {
            viewModel.setCurrentCategory(
                CurrentSelection(
                    it.id,
                    binding.favoriteCategoryText.text.toString()
                )
            )
            toggleCategoryColor(it.id)

        }
        binding.featureCategoryText.setOnClickListener {
            viewModel.setCurrentCategory(
                CurrentSelection(
                    it.id,
                    binding.featureCategoryText.text.toString()
                )
            )
            toggleCategoryColor(it.id)

        }
    }


    private fun observeAllSnacks() {

        lifecycleScope.launch {
            viewModel.allSnacks.collect { state ->
                when (state) {
                    is HomeState.Loading -> {
                        binding.categoryErrorText.isVisible = false
                        binding.emptyStateTv.isVisible = false
                        binding.recommendationRecyclerView.isVisible = false
                        binding.categoryItemsRecyclerView.isVisible = false
                        binding.progressBar.isVisible = true
                    }
                    is HomeState.Failure -> {
                        binding.categoryErrorText.isVisible = true
                        binding.emptyStateTv.isVisible = true
                        binding.recommendationRecyclerView.isVisible = false
                        binding.categoryItemsRecyclerView.isVisible = false
                        binding.progressBar.isVisible = false
                        binding.categoryErrorText.text = state.msg
                        binding.emptyStateTv.text = state.msg

                    }
                    is HomeState.Success -> {
                        binding.categoryErrorText.isVisible = false
                        binding.emptyStateTv.isVisible = false
                        binding.categoryItemsRecyclerView.isVisible = true
                        binding.recommendationRecyclerView.isVisible = true
                        binding.progressBar.isVisible = false
                        recommendedAdapter.submitList(state.allSnacks)

                        observeOnCategory({
                            categoryItemsAdapter.submitList(state.allSnacks)
                            toggleCategoryItemsState(state.allSnacks,"Features Unavailable...")
                            binding.categoryItemsRecyclerView.layoutManager?.smoothScrollToPosition(binding.categoryItemsRecyclerView,null,0)
                        }, {
                            lifecycleScope.launch {
                                viewModel.favoriteSnacks.collect {
                                    categoryItemsAdapter.submitList(it)
                                    toggleCategoryItemsState(it, "Favorites Unavailable...")
                                    binding.categoryItemsRecyclerView.layoutManager?.smoothScrollToPosition(binding.categoryItemsRecyclerView,null,0)
                                }
                            }
                        }, {
                            categoryItemsAdapter.submitList(state.allSnacks.shuffled().reversed())
                            toggleCategoryItemsState(state.allSnacks,"Combos Unavailable...")
                            binding.categoryItemsRecyclerView.layoutManager?.smoothScrollToPosition(binding.categoryItemsRecyclerView,null,0)
                        })
                    }
                }
            }
        }

    }

    private fun observeOnCategory(
        onFeature: () -> Unit,
        onFavorite: () -> Unit,
        onCombos: () -> Unit,
    ) {
        lifecycleScope.launch {
            viewModel.currentCategory.collect {
                toggleCategoryColor(it.id)
                when (it.text.lowercase()) {
                    "Featured".lowercase() -> {
                        onFeature()
                    }
                    "Favorites".lowercase() -> {
                        onFavorite()
                    }
                    "Combos".lowercase() -> {
                        onCombos()
                    }
                }
            }
        }
    }

    private fun toggleCategoryColor(id: Int) {
        theme.resolveAttribute(
            com.google.android.material.R.attr.colorOnSurface,
            typedValue,
            true
        )

        val colorOnSurface = typedValue.data
        if (id == R.id.feature_category_text) {
            binding.featureCategoryText.apply {
                textSize = 18f
                setTextColor(colorOnSurface)
                setTypeface(typeface, Typeface.BOLD)
            }
            binding.favoriteCategoryText.apply {
                textSize = 14f
                setTextColor(ContextCompat.getColor(requireContext(), R.color.some_white))
                setTypeface(typeface, Typeface.NORMAL)
            }
            binding.combosCategoryText.apply {
                textSize = 14f
                setTextColor(ContextCompat.getColor(requireContext(), R.color.some_white))
                setTypeface(typeface, Typeface.NORMAL)
            }

        }
        if (id == R.id.combos_category_text) {
            binding.combosCategoryText.apply {
                textSize = 18f
                setTextColor(colorOnSurface)
                setTypeface(typeface, Typeface.BOLD)
            }
            binding.favoriteCategoryText.apply {
                textSize = 14f
                setTextColor(ContextCompat.getColor(requireContext(), R.color.some_white))
                setTypeface(typeface, Typeface.NORMAL)
            }
            binding.featureCategoryText.apply {
                textSize = 14f
                setTextColor(ContextCompat.getColor(requireContext(), R.color.some_white))
                setTypeface(typeface, Typeface.NORMAL)
            }
        }
        if (id == R.id.favorite_category_text) {
            binding.favoriteCategoryText.apply {
                textSize = 18f
                setTextColor(colorOnSurface)
                setTypeface(typeface, Typeface.BOLD)
            }
            binding.combosCategoryText.apply {
                textSize = 14f
                setTextColor(ContextCompat.getColor(requireContext(), R.color.some_white))
                setTypeface(typeface, Typeface.NORMAL)
            }
            binding.featureCategoryText.apply {
                textSize = 14f
                setTextColor(ContextCompat.getColor(requireContext(), R.color.some_white))
                setTypeface(typeface, Typeface.NORMAL)
            }
        }
    }

    private fun showAddToCartDialog(response: Response) {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.add_item_to_cart_dialog,mContainer, false)
        val binding = AddItemToCartDialogBinding.bind(dialogView)
        val newDialog = MaterialAlertDialogBuilder(requireContext()).create()
        if (dialogView.parent != null) {
            (dialogView.parent as ViewGroup).removeView(binding.root)
        }
        newDialog.setView(binding.root)
        newDialog.show()

        binding.apply {
            mealTitleText.text = response.name
            addToCartBtn.setOnClickListener {
                if (amountEdtText.text.isNullOrEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        "You have to include the amount",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
                val cartItem = CartItem(response, binding.amountEdtText.text.toString().toInt())
                viewModel.addToCart(cartItem)
                newDialog.dismiss()
            }
            cancelBtn.setOnClickListener {
                newDialog.dismiss()
            }
        }
    }

    private fun toggleCategoryItemsState(list: List<Response>,text:String="") {
        if (list.isNotEmpty()) {
            binding.categoryErrorText.isVisible = false
        }else{
            binding.categoryErrorText.isVisible = true
            binding.categoryErrorText.text = text
        }
    }


}