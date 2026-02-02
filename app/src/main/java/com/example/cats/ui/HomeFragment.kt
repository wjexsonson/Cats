package com.example.cats.ui

import android.app.DownloadManager
import android.content.Context.DOWNLOAD_SERVICE
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.cats.CatApplication
import com.example.cats.R
import com.example.cats.data.model.Cat
import com.example.cats.databinding.FragmentHomeBinding
import javax.inject.Inject

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var binding: FragmentHomeBinding

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel: MainViewModel by activityViewModels { viewModelFactory }
    private lateinit var catAdapter: CatAdapter

    private var pendingCatToDownload: Cat? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            pendingCatToDownload?.let { performDownload(it) }
        } else {
            Toast.makeText(context, R.string.error_no_permission, Toast.LENGTH_SHORT).show()
        }
        pendingCatToDownload = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity().application as CatApplication).appComponent.inject(this)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentHomeBinding.bind(view)

        setupRecyclerView()
        observeViewModel()

        if (viewModel.cats.value.isNullOrEmpty()) {
            viewModel.loadMoreCats()
        }
    }

    private fun setupRecyclerView() {
        catAdapter = CatAdapter(
            onFavoriteClick = { cat ->
                if (cat.isFavorite) {
                    viewModel.removeFromFavorites(cat.id)
                } else {
                    viewModel.addToFavorites(cat)
                }
            },
            onLoadMoreClick = {
                if (viewModel.isLoading.value == true) return@CatAdapter

                binding.catsRecyclerView.post {
                    viewModel.loadMoreCats()
                }
            },
            onLongClick = { cat -> showDownloadDialog(cat) }
        )

        catAdapter.isFooterVisible = false
        catAdapter.isFooterLoading = false

        val gridLayoutManager = GridLayoutManager(context, 2)

        gridLayoutManager.spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
            override fun getSpanSize(position: Int): Int {
                val viewType = catAdapter.getItemViewType(position)
                return if (viewType == 1) 2 else 1
            }
        }

        binding.catsRecyclerView.apply {
            layoutManager = gridLayoutManager
            adapter = catAdapter
            itemAnimator = null
        }
    }

    private fun observeViewModel() {
        viewModel.cats.observe(viewLifecycleOwner) { cats ->
            catAdapter.submitList(cats)
            val hasItems = !cats.isNullOrEmpty()
            catAdapter.isFooterVisible = hasItems
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            val isListEmpty = viewModel.cats.value.isNullOrEmpty()

            if (isLoading && isListEmpty) {
                binding.progressBar.visibility = View.VISIBLE
                binding.catsRecyclerView.visibility = View.GONE
            } else {
                binding.progressBar.visibility = View.GONE
                binding.catsRecyclerView.visibility = View.VISIBLE
            }

            catAdapter.isFooterLoading = isLoading
        }

        viewModel.errorText.observe(viewLifecycleOwner) { errorMsg ->
            if (errorMsg != null) {
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun showDownloadDialog(cat: Cat) {
        AlertDialog.Builder(requireContext())
            .setTitle(R.string.dialog_download_title)
            .setMessage(R.string.dialog_download_message)
            .setPositiveButton(R.string.yes) { _, _ -> downloadImage(cat) }
            .setNegativeButton(R.string.no, null)
            .show()
    }

    private fun downloadImage(cat: Cat) {
        if (android.os.Build.VERSION.SDK_INT <= android.os.Build.VERSION_CODES.P) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                pendingCatToDownload = cat
                requestPermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                return
            }
        }
        performDownload(cat)
    }

    private fun performDownload(cat: Cat) {
        try {
            val url = cat.url
            val fileName = "Cat_${cat.id}.jpg"

            val request = DownloadManager.Request(url.toUri())
                .setTitle(fileName)
                .setDescription("Downloading cat image...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            val downloadManager = requireContext().getSystemService(DOWNLOAD_SERVICE) as DownloadManager
            downloadManager.enqueue(request)

            Toast.makeText(context, R.string.downloading_start, Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Ошибка скачивания: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
