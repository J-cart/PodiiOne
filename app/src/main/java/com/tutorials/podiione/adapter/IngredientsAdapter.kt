package com.tutorials.podiione.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.tutorials.podiione.R
import com.tutorials.podiione.databinding.DetailsIngredientsViewholderBinding
import com.tutorials.podiione.databinding.ItemViewholderBinding
import com.tutorials.podiione.model.Ingredient
import com.tutorials.podiione.model.Response

class IngredientsAdapter : ListAdapter<Ingredient, IngredientsAdapter.ViewHolder>(diffObject) {
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding = DetailsIngredientsViewholderBinding.bind(view)
        fun bind(ingredient: Ingredient) {
            binding.apply {
                ingredientTitleText.text = ingredient.name
                ingredientImage.clipToOutline = true
                root.clipToOutline = true
                ingredientImage.load(ingredient.img) {
                    crossfade(true)
                    error(R.drawable.ic_launcher_foreground)
                    placeholder(R.drawable.ic_launcher_foreground)
                }

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.details_ingredients_viewholder, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pos = getItem(position)
        holder.bind(pos)
    }

    companion object {
        val diffObject = object : DiffUtil.ItemCallback<Ingredient>() {
            override fun areItemsTheSame(oldItem: Ingredient, newItem: Ingredient): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Ingredient, newItem: Ingredient): Boolean {
                return oldItem.id == newItem.id && oldItem.name == newItem.name
            }
        }
    }

}