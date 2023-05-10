package com.mobileplus.dummytriluc.ui.widget

import android.content.Context
import android.util.AttributeSet
import com.core.BaseCustomLayout
import com.mobileplus.dummytriluc.R
import com.widget.AppScrollListener
import kotlinx.android.synthetic.main.item_swipe_rcv.view.*

class CustomSwipeRcv(context: Context, attributeSet: AttributeSet) :
    BaseCustomLayout(context, attributeSet) {

    var onCustomSwipeListener: OnCustomSwipeListener? = null
    var onLoadMoreListener: OnLoadMoreListener? = null

    override fun getLayoutId(): Int = R.layout.item_swipe_rcv

    override fun updateUI() {
        swipeToRefresh.setColorSchemeResources(
            R.color.clr_gradient_orange_start,
            R.color.clr_gradient_purple_start,
            R.color.clr_gradient_orange_end,
            R.color
                .clr_gradient_purple_end
        )
        swipeToRefresh.setOnRefreshListener { onCustomSwipeListener?.onRefresh() }

        rcv.addOnScrollListener(object : AppScrollListener() {
            override fun onLoadMore() {
                onLoadMoreListener?.onLoadMore()
            }
        })
    }

    fun getRcv() = rcv

    fun <VH : androidx.recyclerview.widget.RecyclerView.ViewHolder> setUpRcv(adapter: androidx.recyclerview.widget.RecyclerView.Adapter<VH>) {
        rcv.setHasFixedSize(true)
        rcv.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        rcv.adapter = adapter
    }

    fun <VH : androidx.recyclerview.widget.RecyclerView.ViewHolder> setUpGrid(
        adapter: androidx.recyclerview.widget.RecyclerView.Adapter<VH>,
        spanCount: Int
    ) {
        rcv.setHasFixedSize(true)
        rcv.layoutManager = androidx.recyclerview.widget.GridLayoutManager(context, spanCount)
        rcv.adapter = adapter
    }

    fun <VH : androidx.recyclerview.widget.RecyclerView.ViewHolder> setUpRcv(
        adapter: androidx.recyclerview.widget.RecyclerView.Adapter<VH>,
        isHasFixedSize: Boolean,
        isNestedScrollingEnabled: Boolean
    ) {
        rcv.setHasFixedSize(isHasFixedSize)
        rcv.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        rcv.adapter = adapter
        rcv.isNestedScrollingEnabled = isNestedScrollingEnabled
    }

    fun <VH : androidx.recyclerview.widget.RecyclerView.ViewHolder> setUpRcv(
        adapter: androidx.recyclerview.widget.RecyclerView.Adapter<VH>,
        isNestedScrollingEnabled: Boolean
    ) {
        rcv.setHasFixedSize(true)
        rcv.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        rcv.adapter = adapter
        rcv.isNestedScrollingEnabled = isNestedScrollingEnabled
    }

    fun enableRefresh() {
        swipeToRefresh.isRefreshing = true
    }

    fun cancelRefresh() {
        swipeToRefresh.isRefreshing = false
    }

    fun isRefresh() = swipeToRefresh.isRefreshing

    fun interface OnCustomSwipeListener {
        fun onRefresh()
    }

    fun interface OnLoadMoreListener {
        fun onLoadMore()
    }
}