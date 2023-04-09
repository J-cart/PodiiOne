package com.tutorials.podiione

import android.os.Bundle
import android.util.Log
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
import androidx.navigation.fragment.navArgs
import coil.load
import com.tutorials.podiione.adapter.CategoryItemsAdapter
import com.tutorials.podiione.adapter.DetailFeatureAdapter
import com.tutorials.podiione.adapter.RecommendedAdapter
import com.tutorials.podiione.databinding.FragmentDetailsBinding
import com.tutorials.podiione.databinding.FragmentHomeBinding
import com.tutorials.podiione.model.CartItem
import com.tutorials.podiione.model.Response
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.math.log


class DetailsFragment : Fragment() {
    private var _binding: FragmentDetailsBinding? = null
    private val binding get() = _binding!!
    private val detailFeatureAdapter by lazy { DetailFeatureAdapter() }
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
        val response = args.args
        binding.featureItemsRecyclerView.adapter = detailFeatureAdapter
        binding.apply {

            additionTextBtn.setOnClickListener {
                count++
                itemCountText.text = count.toString()
            }
            substractTextBtn.setOnClickListener {
                count--
                itemCountText.text = count.toString()
            }

            titleText.text = response.name
            mealPriceText.text = "$"+response.price
            mealImage.load(response.images) {
                crossfade(true)
                error(R.drawable.ic_launcher_foreground)
                placeholder(R.drawable.ic_launcher_foreground)
            }

            cartImg.setOnClickListener {
                val action = DetailsFragmentDirections.actionDetailsFragmentToCartFragment()
                findNavController().navigate(action)
            }

            addText.setOnClickListener {
                addToCart(response)
            }

            lifecycleScope.launch {
                viewModel.favoriteSnacks.collect{
                    isFavorite = it.contains(response)
                    if (isFavorite){
                        binding.favoriteImg.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.favorite_filled))
                    }else{
                        binding.favoriteImg.setImageDrawable(ContextCompat.getDrawable(requireContext(),R.drawable.favorite_colored))
                    }
                }
            }



            favoriteImg.setOnClickListener{
                isFavorite=!isFavorite
                toggleFavorite(response)
            }
        }
//        observeAllCartItem()
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

    private fun observeAllCartItem(){
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

    }

}