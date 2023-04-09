package com.tutorials.podiione

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.tutorials.podiione.adapter.CartAdapter
import com.tutorials.podiione.databinding.AddItemToCartDialogBinding
import com.tutorials.podiione.databinding.FragmentCartBinding
import com.tutorials.podiione.model.CartItem
import com.tutorials.podiione.model.Response
import kotlinx.coroutines.launch


class CartFragment : Fragment() {
    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!
    private val cartAdapter by lazy { CartAdapter() }
    private val viewModel by activityViewModels<SnacksViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.cartItemsRecyclerView.adapter = cartAdapter
        lifecycleScope.launch {
            viewModel.allCartItem.collect { state ->
                binding.clearText.setOnClickListener { showClearDialog(state) }
                when (state) {
                    is CartState.Loading -> {
                        Log.d("JOE", "observe cart items: $state")
                        binding.errorText.isVisible = false
                        binding.cartItemsRecyclerView.isVisible = false
                        binding.progressBar.isVisible = true
                        getTotalCost()
                    }
                    is CartState.Failure -> {
                        Log.d("JOE", "observe cart items: $state")
                        binding.errorText.isVisible = true
                        binding.cartItemsRecyclerView.isVisible = false
                        binding.progressBar.isVisible = false
                        binding.errorText.text = state.msg
                        getTotalCost()

                    }
                    is CartState.Success -> {
                        Log.d("JOE", "observe cart items: $state")
                        binding.errorText.isVisible = false
                        binding.cartItemsRecyclerView.isVisible = true
                        binding.progressBar.isVisible = false
                        cartAdapter.submitList(state.items)
                        getTotalCost(state.items)

                    }
                }

            }

        }
    }

    private fun getTotalCost(list: List<CartItem> = emptyList()) {
        var sum = 0f
        if (list.isNotEmpty()){
            list.forEach {
                val total = it.item.price.toFloat() * it.count
                sum += total
            }
            binding.totalText.text = "$%.02f".format(sum)
            return
        }

        binding.totalText.text = "$%.02f".format(sum)
    }

    private fun showClearDialog(state: CartState) {
       val dialogBuilder = MaterialAlertDialogBuilder(requireContext()).apply {
            setMessage("Are you sure you want to clear cart ?")
            setTitle("CLEAR CART")
            setPositiveButton("OK") { dialogInterface, int ->
                viewModel.toggleCartState(CartState.Failure("Cart is empty..."))
                dialogInterface.dismiss()
            }
            setNegativeButton("CANCEL") { dialogInterface, int ->
                dialogInterface.dismiss()
            }

            create()
        }
        if (state is CartState.Success){
            dialogBuilder.show()
        }

    }

}