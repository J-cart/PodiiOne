package com.tutorials.podiione.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.tutorials.podiione.R
import com.tutorials.podiione.databinding.CartItemViewholderBinding
import com.tutorials.podiione.databinding.DetailItemViewholderBinding
import com.tutorials.podiione.databinding.FullItemViewholderBinding
import com.tutorials.podiione.model.CartItem
import com.tutorials.podiione.model.Response

class CartAdapter : ListAdapter<CartItem, CartAdapter.ViewHolder>(diffObject) {
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding = CartItemViewholderBinding.bind(view)
        fun bind(cartItem: CartItem) {
            binding.apply {
                mealTitleText.text = cartItem.item.name
                mealPriceText.text = "$${cartItem.item.price} x ${cartItem.count}"
                totalAmountText.text ="$%.2f".format(cartItem.item.price.toFloat()*cartItem.count)
                mealImage.clipToOutline = true
                root.clipToOutline = true
                mealImage.load(cartItem.item.images) {
                    crossfade(true)
                    error(R.drawable.ic_launcher_foreground)
                    placeholder(R.drawable.ic_launcher_foreground)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.cart_item_viewholder, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pos = getItem(position)
        holder.bind(pos)
    }

    companion object {
        val diffObject = object : DiffUtil.ItemCallback<CartItem>() {
            override fun areItemsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
                return oldItem.item.id == newItem.item.id
            }

            override fun areContentsTheSame(oldItem: CartItem, newItem: CartItem): Boolean {
                return oldItem.item.id == newItem.item.id && oldItem.item.name == newItem.item.name
            }
        }
    }

    private var listener: ((CartItem) -> Unit)? = null
    fun adapterClick(listener: (CartItem) -> Unit) {
        this.listener = listener
    }
}