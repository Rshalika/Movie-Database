package com.strawhat.moviedatabase.ui.details

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.MenuItem
import com.bumptech.glide.Glide
import com.strawhat.moviedatabase.BuildConfig
import com.strawhat.moviedatabase.R
import com.strawhat.moviedatabase.services.bindings.Movie
import com.strawhat.moviedatabase.ui.ItemListActivity
import kotlinx.android.synthetic.main.activity_item_detail.*


class ItemDetailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_detail)
        setSupportActionBar(findViewById(R.id.detail_toolbar))

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (savedInstanceState == null) {

            val movie = intent.getSerializableExtra(ItemDetailFragment.ARG_ITEM) as Movie


            val fragment = ItemDetailFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ItemDetailFragment.ARG_ITEM, movie)
                }
            }

            supportFragmentManager.beginTransaction()
                .add(R.id.item_detail_container, fragment)
                .commit()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            android.R.id.home -> {
                navigateUpTo(Intent(this, ItemListActivity::class.java))

                true
            }
            else -> super.onOptionsItemSelected(item)
        }
}