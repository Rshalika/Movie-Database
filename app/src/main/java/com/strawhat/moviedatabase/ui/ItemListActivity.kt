package com.strawhat.moviedatabase.ui

import android.os.Bundle
import androidx.core.widget.NestedScrollView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.widget.Toolbar
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.strawhat.moviedatabase.MyApplication
import com.strawhat.moviedatabase.R

import com.strawhat.moviedatabase.vm.MainViewModel
import com.strawhat.moviedatabase.vm.events.MainViewState
import io.reactivex.rxjava3.exceptions.OnErrorNotImplementedException
import io.reactivex.rxjava3.kotlin.subscribeBy
import kotlinx.android.synthetic.main.item_list.*

private const val VISIBLE_THRESHOLD = 2

class ItemListActivity : AppCompatActivity() {

    private var twoPane: Boolean = false

    private val viewModel by viewModels<MainViewModel>()

    private lateinit var layoutManager: LinearLayoutManager
    private lateinit var adapter: MovieListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_list)
        (application as MyApplication).appComponent.inject(viewModel)
        viewModel.afterInit()
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.title = title
        if (findViewById<NestedScrollView>(R.id.item_detail_container) != null) {
            twoPane = true
        }
        setupRecyclerView(findViewById(R.id.item_list))
        setUpLoadMoreListener()

        viewModel.viewStateRelay.subscribeBy(
            onNext = {
                updateState(it)
            },
            onError = {
                throw OnErrorNotImplementedException(it)
            }
        )
    }

    private fun updateState(state: MainViewState) {
        adapter.setMovies(state.items.toMutableList())
    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        adapter = MovieListAdapter(this , twoPane)
        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = adapter
    }


    private fun setUpLoadMoreListener() {
        item_list.setOnScrollChangeListener { _, _, _, _, _ ->
            val findLastVisibleItemPosition = layoutManager.findLastVisibleItemPosition()
            val totalItemCount = layoutManager.itemCount
            if (totalItemCount <= findLastVisibleItemPosition + VISIBLE_THRESHOLD) {
                viewModel.loadPageRequested()
            }
        }
    }

}