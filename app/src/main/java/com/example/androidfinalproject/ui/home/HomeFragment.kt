package com.example.androidfinalproject.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.androidfinalproject.R

class HomeFragment : Fragment() {

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var reviewAdapter: ReviewAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView(view)
        setupSwipeRefresh(view)
        observeViewModel(view)
    }

    private fun setupRecyclerView(view: View) {
        reviewAdapter = ReviewAdapter()
        view.findViewById<RecyclerView>(R.id.reviewsRecyclerView).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = reviewAdapter
        }
    }

    private fun setupSwipeRefresh(view: View) {
        view.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout).setOnRefreshListener {
            viewModel.refreshReviews()
        }
    }

    private fun observeViewModel(view: View) {
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val emptyStateText = view.findViewById<TextView>(R.id.emptyStateText)
        val swipeRefreshLayout = view.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)

        viewModel.reviews.observe(viewLifecycleOwner) { reviews ->
            reviewAdapter.submitList(reviews)
            emptyStateText.visibility = if (reviews.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            swipeRefreshLayout.isRefreshing = isLoading
            progressBar.visibility = if (isLoading && reviewAdapter.currentList.isEmpty()) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }

        viewModel.seedingComplete.observe(viewLifecycleOwner) { success ->
            success?.let {
                val message = if (it) R.string.seeding_complete else R.string.seeding_failed
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                viewModel.clearSeedingStatus()
            }
        }
    }
}
