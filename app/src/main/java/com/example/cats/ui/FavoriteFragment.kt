package com.example.cats.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.cats.CatApplication
import com.example.cats.R
import com.example.cats.databinding.FragmentFavoriteBinding
import javax.inject.Inject

class FavoriteFragment : Fragment(R.layout.fragment_favorite) {

    private lateinit var binding: FragmentFavoriteBinding
    private lateinit var adapter: CatAdapter

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel: MainViewModel by activityViewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity().application as CatApplication).appComponent.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentFavoriteBinding.bind(view)

        setupList()
        setupClearButton()
        observeViewModel()
    }

    private fun setupList() {
        adapter = CatAdapter(
            onFavoriteClick = { cat ->
                viewModel.removeFromFavorites(cat.id)
            },
            onLoadMoreClick = {},
            onLongClick = {}
        )
        adapter.isFooterVisible = false

        binding.favoritesRecyclerView.layoutManager = GridLayoutManager(context, 2)
        binding.favoritesRecyclerView.adapter = adapter
    }

    private fun setupClearButton() {
        binding.btnClearAll.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Очистить избранное?")
                .setMessage("Вы точно хотите удалить всех?")
                .setPositiveButton("Да") { _, _ ->
                    viewModel.clearAllFavorites()
                }
                .setNegativeButton("Нет", null)
                .show()
        }
    }

    private fun observeViewModel() {
        viewModel.favoriteCats.observe(viewLifecycleOwner) { favoriteList ->
            adapter.submitList(favoriteList)

            val isEmpty = favoriteList.isEmpty()
            binding.emptyView.visibility = if (isEmpty) View.VISIBLE else View.GONE
            binding.btnClearAll.visibility = if (isEmpty) View.GONE else View.VISIBLE
        }
    }
}
