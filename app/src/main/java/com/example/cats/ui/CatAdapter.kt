package com.example.cats.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.cats.R
import com.example.cats.data.model.Cat
import com.example.cats.databinding.ItemCatBinding
import com.example.cats.databinding.ItemFooterBinding

private const val TYPE_CAT = 0
private const val TYPE_FOOTER = 1

class CatAdapter(
    private val onFavoriteClick: (Cat) -> Unit,
    private val onLoadMoreClick: () -> Unit,
    private val onLongClick: (Cat) -> Unit
) : ListAdapter<Cat, RecyclerView.ViewHolder>(CatDiffCallback()) {

    var isFooterVisible: Boolean = true
        set(value) {
            if (field == value) return
            val hadFooter = field
            field = value
            val footerPosition = super.getItemCount()
            val canHaveFooter = super.getItemCount() > 0

            if (!canHaveFooter) return

            if (hadFooter && !value) {
                notifyItemRemoved(footerPosition)
            } else if (!hadFooter && value) {
                notifyItemInserted(footerPosition)
            }
        }

    var isFooterLoading: Boolean = false
        set(value) {
            if (field == value) return
            field = value
            val canHaveFooter = isFooterVisible && super.getItemCount() > 0
            if (canHaveFooter) {
                notifyItemChanged(super.getItemCount())
            }
        }

    class CatDiffCallback : DiffUtil.ItemCallback<Cat>() {
        override fun areItemsTheSame(oldItem: Cat, newItem: Cat): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Cat, newItem: Cat): Boolean = oldItem == newItem
    }

    override fun getItemCount(): Int {
        val cats = super.getItemCount()
        val hasFooter = isFooterVisible && cats > 0
        return cats + if (hasFooter) 1 else 0
    }

    override fun getItemViewType(position: Int): Int {
        val catsCount = super.getItemCount()
        val hasFooter = isFooterVisible && catsCount > 0
        return if (hasFooter && position == catsCount) TYPE_FOOTER else TYPE_CAT
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_CAT) {
            CatViewHolder(ItemCatBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        } else {
            FooterViewHolder(ItemFooterBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is CatViewHolder -> holder.bind(getItem(position))
            is FooterViewHolder -> holder.bind(isFooterLoading)
        }
    }

    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        if (holder is CatViewHolder) {
            holder.clear()
        }
    }

    inner class CatViewHolder(private val binding: ItemCatBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(cat: Cat) {
            Glide.with(itemView.context)
                .load(cat.url)
                .override(300, 300)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.stat_notify_error)
                .into(binding.catImageView)

            val iconRes = if (cat.isFavorite)
                android.R.drawable.btn_star_big_on
            else
                android.R.drawable.btn_star_big_off

            binding.favoriteButton.setImageResource(iconRes)
            binding.favoriteButton.setOnClickListener { onFavoriteClick(cat) }

            itemView.setOnLongClickListener {
                onLongClick(cat)
                true
            }
        }

        fun clear() {
            Glide.with(itemView.context).clear(binding.catImageView)
        }
    }

    inner class FooterViewHolder(private val binding: ItemFooterBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(loading: Boolean) {
            binding.footerProgressBar.visibility = if (loading) View.VISIBLE else View.GONE
            binding.loadMoreButton.isEnabled = !loading
            binding.loadMoreButton.text = binding.root.context.getString(
                if (loading) R.string.loading else R.string.load_more
            )
            binding.loadMoreButton.setOnClickListener {
                if (!loading) onLoadMoreClick()
            }
        }
    }
}
