package com.strawhat.moviedatabase.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.strawhat.moviedatabase.BuildConfig
import com.strawhat.moviedatabase.R
import com.strawhat.moviedatabase.services.bindings.Movie
import com.strawhat.moviedatabase.ui.details.ItemDetailActivity
import com.strawhat.moviedatabase.ui.details.ItemDetailFragment
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*


class MovieListAdapter(
    private val parentActivity: ItemListActivity,
    private val twoPane: Boolean
) :
    RecyclerView.Adapter<MovieListAdapter.ViewHolder>() {

    private val mDiffer: AsyncListDiffer<Movie> = AsyncListDiffer<Movie>(this, DIFF_CALLBACK)


    private val onClickListener: View.OnClickListener

    init {
        onClickListener = View.OnClickListener { v ->
            val item = v.tag as Movie
            if (twoPane) {
                val fragment = ItemDetailFragment()
                    .apply {
                        arguments = Bundle().apply {
                            putSerializable(ItemDetailFragment.ARG_ITEM, item)
                        }
                    }
                parentActivity.supportFragmentManager
                    .beginTransaction()
                    .replace(R.id.item_detail_container, fragment)
                    .commit()
            } else {
                val intent = Intent(v.context, ItemDetailActivity::class.java)
                intent.putExtra(ItemDetailFragment.ARG_ITEM, item)
                v.context.startActivity(intent)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_list_content, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mDiffer.currentList[position]
        holder.titleView.text = item.name
        holder.ratingView.text = item.voteAverage.toString()
        try {
            SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(item.firstAirDate)?.let {
                val cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"))
                cal.time = it
                val year = cal[Calendar.YEAR]
                holder.yarView.text = year.toString()
            }
        }catch (e:Exception){

        }


        Glide
            .with(parentActivity)
            .load("${BuildConfig.IMAGES_URL_PREFIX}${item.backdropPath}")
            .centerCrop()
//            .placeholder(R.drawable.loader_image)
            .into(holder.imageView)

        with(holder.itemView) {
            tag = item
            setOnClickListener(onClickListener)
        }
    }

    fun setMovies(newList: List<Movie>) {
        mDiffer.submitList(newList)
    }

    override fun getItemCount() = mDiffer.currentList.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleView: TextView = view.findViewById(R.id.title)
        val imageView: ImageView = view.findViewById(R.id.list_item_image)
        val ratingView: TextView = view.findViewById(R.id.rating)
        val yarView: TextView = view.findViewById(R.id.year)
    }

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Movie>() {
            override fun areItemsTheSame(oldItem: Movie, newItem: Movie): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: Movie, newItem: Movie): Boolean {
                return oldItem == newItem
            }

        }
    }

}