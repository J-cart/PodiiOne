package com.tutorials.podiione.arch

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tutorials.podiione.R
import com.tutorials.podiione.model.CartItem
import com.tutorials.podiione.model.CurrentSelection
import com.tutorials.podiione.model.Response
import com.tutorials.podiione.util.parseSnacksResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class SnacksViewModel : ViewModel() {
    var allSnacks = MutableStateFlow<HomeState>(HomeState.Loading)
        private set
    var snacksSearchFlow = MutableStateFlow<HomeState>(HomeState.Failure("Search Snacks..."))
        private set
    var favoriteSnacks = MutableStateFlow<List<Response>>(emptyList())
        private set

    var currentCategory =
        MutableStateFlow((CurrentSelection(R.id.feature_category_text, "FEATURE")))
        private set

    var allCartItem = MutableStateFlow<CartState>(CartState.Failure("Cart is empty..."))
        private set

    var currentSnackDetail = MutableStateFlow<HomeState>(HomeState.Loading)
        private set


    fun addToFavorite(favoriteSnack: Response) {
        val currentList = favoriteSnacks.value.toMutableList()
        currentList.add(favoriteSnack)
        favoriteSnacks.value = currentList.toList()
    }

    fun removeFromFavorite(favoriteSnack: Response) {
        val currentList = favoriteSnacks.value.toMutableList()
        currentList.remove(favoriteSnack)
        favoriteSnacks.value = currentList.toList()
    }

    fun loadAllSnacks(context: Context) {
        allSnacks.value = HomeState.Loading
        viewModelScope.launch(Dispatchers.IO) {
            delay(1000L)
            parseSnacksResponse(context)?.let {
                allSnacks.value = HomeState.Success(it)
            }
        }
    }

    fun setCurrentCategory(currentSelection: CurrentSelection) {
        currentCategory.value = currentSelection
    }

    fun addToCart(cartItem: CartItem) {
        if (allCartItem.value is CartState.Failure) {
            val newList = listOf(cartItem)
            allCartItem.value = CartState.Success(newList)
            return
        }
        if (allCartItem.value is CartState.Loading) {
            val newList = listOf(cartItem)
            allCartItem.value = CartState.Success(newList)
            return
        }

        val currentList = (allCartItem.value as CartState.Success).items.toMutableList()
        val oldItem = currentList.find { it.item == cartItem.item }
        if (oldItem != null){
            currentList.remove(oldItem)
            val oldItemCount = oldItem.count
            val newItem = cartItem.copy(count = cartItem.count + oldItemCount)
            currentList.add(newItem)
        }else{
            currentList.add(cartItem)
        }
        allCartItem.value = CartState.Success(currentList.toList())
    }

    fun searchSnack(query: String) {
        snacksSearchFlow.value = HomeState.Loading
        when (val allSnackState = allSnacks.value) {
            is HomeState.Success -> {
                val result = allSnackState.allSnacks.filter {
                    it.name.contains(query, true)
                }
                if (result.isEmpty()) {
                    snacksSearchFlow.value = HomeState.Failure("No results found...")
                    return
                }
                snacksSearchFlow.value = HomeState.Success(result)
            }
            is HomeState.Failure -> {
                snacksSearchFlow.value = HomeState.Failure("Search Snacks...")
            }
            else -> Unit

        }
    }

    fun toggleSearchSnackState(state: HomeState) {
        snacksSearchFlow.value = state
    }

    fun toggleCartState(state: CartState) {
        allCartItem.value = state
    }

    fun toggleCurrentSnackDetailState(state: HomeState){
        currentSnackDetail.value = state
    }

}

sealed class HomeState {
    object Loading : HomeState()
    data class Failure(val msg: String) : HomeState()
     class Success : HomeState {
        lateinit var response: Response
        lateinit var allSnacks: List<Response>

        constructor(data:List<Response>){
            allSnacks = data
        }
        constructor(data:Response){
            response = data
        }
    }
}

sealed class CartState {
    object Loading : CartState()
    data class Failure(val msg: String) : CartState()
    data class Success(val items: List<CartItem>) : CartState()
}