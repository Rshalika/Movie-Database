package com.strawhat.moviedatabase.ui

import android.app.SearchManager
import android.app.SearchManager.EXTRA_DATA_KEY
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.NestedScrollView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.strawhat.moviedatabase.MyApplication
import com.strawhat.moviedatabase.R
import com.strawhat.moviedatabase.services.bindings.Movie
import com.strawhat.moviedatabase.ui.details.ItemDetailActivity
import com.strawhat.moviedatabase.ui.details.ItemDetailFragment
import com.strawhat.moviedatabase.util.toVisibility
import com.strawhat.moviedatabase.vm.MainViewModel
import com.strawhat.moviedatabase.vm.events.MainViewState
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.exceptions.OnErrorNotImplementedException
import io.reactivex.rxjava3.kotlin.subscribeBy
import kotlinx.android.synthetic.main.item_list.*


private const val VISIBLE_THRESHOLD = 2

class ItemListActivity : AppCompatActivity(), SearchView.OnQueryTextListener {

    private var twoPane: Boolean = false

    private val viewModel by viewModels<MainViewModel>()

    private lateinit var layoutManager: LinearLayoutManager

    private lateinit var adapter: MovieListAdapter

    private var searchView: SearchView? = null


    private val disposable = CompositeDisposable()

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

        disposable.add(
            viewModel.viewStateRelay.subscribeBy(
                onNext = {
                    updateState(it)
                },
                onError = {
                    throw OnErrorNotImplementedException(it)
                }
            )
        )
        val intent = intent
        if (Intent.ACTION_SEARCH == intent.action) {
            openMovie(intent)
        } else if (Intent.ACTION_VIEW == intent.action) {
            println("as")
        }
        close_search_button?.let {
            it.setOnClickListener {
                findViewById<Toolbar>(R.id.toolbar).collapseActionView()
                viewModel.searchDeActivated()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (Intent.ACTION_SEARCH == intent.action) {
            openMovie(intent)
        } else if (Intent.ACTION_VIEW == intent.action) {
            val uri = intent.dataString

        }
    }

    private fun openMovie(intent: Intent) {
        val movie = Gson().fromJson<Movie?>(intent.getStringExtra(EXTRA_DATA_KEY), Movie::class.java)
        if (movie != null) {
            if (twoPane) {
                val fragment = ItemDetailFragment()
                    .apply {
                        arguments = Bundle().apply {
                            putSerializable(ItemDetailFragment.ARG_ITEM, movie)
                        }
                    }
                supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.item_detail_container, fragment)
                    .commit()
            } else {
                val intent = Intent(this, ItemDetailActivity::class.java)
                intent.putExtra(ItemDetailFragment.ARG_ITEM, movie)
                this.startActivity(intent)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)

        val searchItem: MenuItem = menu.findItem(R.id.search)
        val searchView = searchItem.actionView as SearchView
        this.searchView = searchView
        searchView.setOnQueryTextListener(this)
        searchView.setOnCloseListener {
            viewModel.searchDeActivated()
            return@setOnCloseListener false
        }
        val searchManager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        searchView.setSearchableInfo(searchManager.getSearchableInfo(ComponentName(this, ItemListActivity::class.java)))
        searchView.isIconifiedByDefault = false
        return true
    }

    private fun updateState(state: MainViewState) {
        adapter.setMovies(state.items.toMutableList())
        search_indicator?.visibility = state.searchEnabled.toVisibility()
        search_query_string?.text = state.searchQuery
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        adapter = MovieListAdapter(this, twoPane)
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

    override fun onQueryTextSubmit(query: String?): Boolean {
        query?.let {
            viewModel.searchActivated()
            viewModel.search(it)
        }
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        return false
    }

}