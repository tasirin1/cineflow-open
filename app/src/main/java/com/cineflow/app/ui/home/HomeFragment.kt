package com.cineflow.app.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cineflow.app.R
import com.cineflow.app.data.api.ApiClient
import com.cineflow.app.data.api.SessionManager
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var progressLoading: ProgressBar
    private lateinit var tvError: TextView
    private lateinit var rvModels: RecyclerView
    private lateinit var adapter: ModelAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressLoading = view.findViewById(R.id.progress_loading)
        tvError = view.findViewById(R.id.tv_error)
        rvModels = view.findViewById(R.id.rv_models)

        adapter = ModelAdapter()
        rvModels.layoutManager = LinearLayoutManager(requireContext())
        rvModels.adapter = adapter

        loadModels()
    }

    private fun loadModels() {
        progressLoading.visibility = View.VISIBLE
        tvError.visibility = View.GONE
        rvModels.visibility = View.GONE

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Check if we have a valid token
                if (!SessionManager.isTokenValid(requireContext())) {
                    progressLoading.visibility = View.GONE
                    tvError.text = "Silakan login terlebih dahulu."
                    tvError.visibility = View.VISIBLE
                    return@launch
                }

                val response = ApiClient.api.getModels()
                if (response.isSuccessful && response.body()?.isSuccess == true) {
                    val models = response.body()?.data.orEmpty()
                    adapter.submitList(models)
                    progressLoading.visibility = View.GONE
                    rvModels.visibility = View.VISIBLE
                } else {
                    progressLoading.visibility = View.GONE
                    tvError.text = "Gagal memuat model: ${response.code()}"
                    tvError.visibility = View.VISIBLE
                }
            } catch (e: Exception) {
                progressLoading.visibility = View.GONE
                tvError.text = "Error: ${e.message}"
                tvError.visibility = View.VISIBLE
            }
        }
    }
}
