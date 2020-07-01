package com.strawhat.moviedatabase.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.CollapsingToolbarLayout
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.strawhat.moviedatabase.R
import com.strawhat.moviedatabase.services.bindings.Movie


class ItemDetailFragment : Fragment() {

    private var item: Movie? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            if (it.containsKey(ARG_ITEM_ID)) {
//                item = DummyContent.ITEM_MAP[it.getString(ARG_ITEM_ID)]
                activity?.findViewById<CollapsingToolbarLayout>(R.id.toolbar_layout)?.title =
                    item?.name
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.item_detail, container, false)
        item?.let {
            rootView.findViewById<TextView>(R.id.item_detail).text = it.overview
        }

        return rootView
    }

    companion object {
        const val ARG_ITEM_ID = "item_id"
    }
}