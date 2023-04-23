package com.tutorials.podiione

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import coil.load
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.tutorials.podiione.adapter.DetailFeatureAdapter
import com.tutorials.podiione.adapter.IngredientsAdapter
import com.tutorials.podiione.arch.CartState
import com.tutorials.podiione.arch.HomeState
import com.tutorials.podiione.arch.SnacksViewModel
import com.tutorials.podiione.databinding.FragmentDetailsBinding
import com.tutorials.podiione.model.CartItem
import com.tutorials.podiione.model.Response
import com.tutorials.podiione.util.parseSnacksResponse
import kotlinx.coroutines.launch


class DetailsFragment : Fragment() {
    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!
    private val detailFeatureAdapter by lazy { DetailFeatureAdapter() }
    private val ingredientsAdapter by lazy { IngredientsAdapter() }
    private val args by navArgs<DetailsFragmentArgs>()
    private var count = 0
        set(value) {
            if (value <= 0){
                field = 0
                return
            }
            field = value
        }
    private var isFavorite = false
    private val viewModel by activityViewModels<SnacksViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        viewModel.toggleCurrentSnackDetailState(HomeState.Success(args.args))
        super.onCreate(savedInstanceState)

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentDetailsBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bottomSheet = BottomSheetBehavior.from(binding.bottomSheetFrame)
        bottomSheet.state = BottomSheetBehavior.STATE_COLLAPSED
        bottomSheet.peekHeight = 0
        binding.featureItemsRecyclerView.adapter = detailFeatureAdapter
        observeDetailState(bottomSheet)
        lifecycleScope.launch{
            viewModel.allCartItem.collect{state->
                when (state) {
                    is CartState.Success -> {
                        binding.badgeText.text = state.items.size.toString()
                    }
                    else->binding.badgeText.text = "0"
                }

            }

        }

        detailFeatureAdapter.adapterClick {
            viewModel.toggleCurrentSnackDetailState(HomeState.Loading)
            viewModel.toggleCurrentSnackDetailState(HomeState.Success(it))
        }

        binding.closeBtn.setOnClickListener {
            bottomSheet.state = BottomSheetBehavior.STATE_COLLAPSED
            bottomSheet.peekHeight = 0
        }
        parseSnacksResponse(requireContext())?.let {
            detailFeatureAdapter.submitList(it)
        }
    }

    private fun toggleFavorite(item:Response){
        if (isFavorite){
            binding.favoriteImg.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.favorite_filled))
            viewModel.addToFavorite(item)
        }else{
            binding.favoriteImg.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.favorite_colored))
            viewModel.removeFromFavorite(item)
        }
    }

    private fun addToCart(item: Response){
        if (count == 0){
            Toast.makeText(
                requireContext(),
                "You need to increase the snack count..",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        val cartItem = CartItem(item,count)
        viewModel.addToCart(cartItem)
        count = 0
        binding.itemCountText.text = count.toString()
    }

    private fun setUpBottomSheet(response: Response){
        binding.apply {
            ingredientsRecyclerView.adapter = ingredientsAdapter
            ingredientDesc.text = response.desc
            ingredientsAdapter.submitList(response.ingredients)
        }
    }

    private fun observeDetailState(bottomSheet: BottomSheetBehavior<FrameLayout>) {

        lifecycleScope.launch {
            viewModel.currentSnackDetail.collect { state ->
                when (state) {
                    is HomeState.Loading -> {
                        binding.apply {
                            progressBar.isVisible = true
                            titleText.text = ""
                            mealPriceText.text = "$   "
                            mealImage.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.ic_launcher_foreground))

                            cartImg.setOnClickListener {
                                val action = DetailsFragmentDirections.actionDetailsFragmentToCartFragment()
                                findNavController().navigate(action)
                            }

                            bottomSheetFrame.clipToOutline = true
                        }

                    }
                    is HomeState.Success -> {
                        binding.apply {
                            progressBar.isVisible = false
                            additionTextBtn.setOnClickListener {
                                count++
                                itemCountText.text = count.toString()
                            }
                            substractTextBtn.setOnClickListener {
                                count--
                                itemCountText.text = count.toString()
                            }

                            titleText.text = state.response.name
                            mealPriceText.text = "$"+state.response.price
                            mealImage.load(state.response.images) {
                                crossfade(true)
                                error(R.drawable.ic_launcher_foreground)
                                placeholder(R.drawable.ic_launcher_foreground)
                            }

                            cartImg.setOnClickListener {
                                val action = DetailsFragmentDirections.actionDetailsFragmentToCartFragment()
                                findNavController().navigate(action)
                            }

                            addText.setOnClickListener {
                                addToCart(state.response)
                            }

                            lifecycleScope.launch {
                                viewModel.favoriteSnacks.collect{
                                    isFavorite = it.contains(state.response)
                                    if (isFavorite){
                                        binding.favoriteImg.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.favorite_filled))
                                    }else{
                                        binding.favoriteImg.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.favorite_colored))
                                    }
                                }
                            }

                            bottomSheetFrame.clipToOutline = true
                            detailImg?.setOnClickListener {
                                setUpBottomSheet(state.response)
                                bottomSheet.state = BottomSheetBehavior.STATE_EXPANDED
                            }

                            favoriteImg.setOnClickListener{
                                isFavorite=!isFavorite
                                toggleFavorite(state.response)
                            }
                        }

                    }
                    else->Unit
                }
            }
        }

    }


}