package com.tutorials.podiione.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.tutorials.podiione.R
import com.tutorials.podiione.databinding.ItemViewholderBinding
import com.tutorials.podiione.model.Response

class RecommendedAdapter : ListAdapter<Response, RecommendedAdapter.ViewHolder>(diffObject) {
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val binding = ItemViewholderBinding.bind(view)
        fun bind(response: Response) {
            binding.apply {
                mealTitleText.text = response.name
                mealPriceText.text = "$"+response.price
                mealImage.clipToOutline = true
                root.clipToOutline = true
                mealImage.load(response.images) {
                    crossfade(true)
                    error(R.drawable.ic_launcher_foreground)
                    placeholder(R.drawable.ic_launcher_foreground)
                }
                holder.setOnClickListener {
                    listener?.let { it(response) }
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.item_viewholder, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val pos = getItem(position)
        holder.bind(pos)
    }

    companion object {
        val diffObject = object : DiffUtil.ItemCallback<Response>() {
            override fun areItemsTheSame(oldItem: Response, newItem: Response): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Response, newItem: Response): Boolean {
                return oldItem.id == newItem.id && oldItem.name == newItem.name
            }
        }
    }

    private var listener: ((Response) -> Unit)? = null
    fun adapterClick(listener: (Response) -> Unit) {
        this.listener = listener
    }
}