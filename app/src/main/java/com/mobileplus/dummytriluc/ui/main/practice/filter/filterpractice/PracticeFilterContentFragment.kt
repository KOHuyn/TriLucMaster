package com.mobileplus.dummytriluc.ui.main.practice.filter.filterpractice

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.core.BaseFragment
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.Page
import com.mobileplus.dummytriluc.ui.main.practice.adapter.PracticeMoreAdapter
import com.mobileplus.dummytriluc.ui.main.practice.detail.PracticeDetailFragment
import com.mobileplus.dummytriluc.ui.main.practice.filter.PracticeFilterFragment
import com.mobileplus.dummytriluc.ui.main.practice.filter.PracticeFilterViewModel
import com.mobileplus.dummytriluc.ui.main.practice.folder.PracticeFolderFragment
import com.mobileplus.dummytriluc.ui.utils.MarginItemDecoration
import com.mobileplus.dummytriluc.ui.widget.CustomSwipeRcv
import com.utils.ext.setVisibility
import kotlinx.android.synthetic.main.fragment_practice_filter_content.*
import org.koin.android.viewmodel.ext.android.getViewModel

/**
 * Created by KOHuyn on 3/11/2021
 */
class PracticeFilterContentFragment : BaseFragment() {
    override fun getLayoutId(): Int = R.layout.fragment_practice_filter_content
    private val practiceViewModel by lazy { requireParentFragment().getViewModel<PracticeFilterViewModel>() }
    private val adapterPractice by lazy { PracticeMoreAdapter() }
    private val page by lazy { Page() }
    private var isReload = true

    var onFilterContentCallback: OnFilterContentCallback? = null

    override fun updateUI(savedInstanceState: Bundle?) {
        disposableViewModel()
        configView()
        handleClick()
    }

    private fun disposableViewModel() {
        practiceViewModel.run {
            addDispose(rxLoadingPractice.subscribe {
                if (page.isLoading) {
                    loadMorePracticeFilterContent.setVisibility(it)
                } else {
                    if (it) enableRefresh() else cancelRefresh()
                }
            })
            addDispose(rxItemPractice.subscribe {
                val items = it.first
                val pageResponse = it.second
                page.run {
                    currPage = pageResponse?.currPage ?: -1
                    totalPage = pageResponse?.totalPage ?: -1
                    isLoading = false
                }
                noDataPracticeFilterContent.setVisibility(items.isEmpty())
//                rcvPracticeFilter.setVisibility(items.isNotEmpty())
                if (items.isEmpty()) {
                    adapterPractice.items.clear()
                    adapterPractice.notifyDataSetChanged()
                }
                if (adapterPractice.items.isEmpty() || isReload || page.currPage == 1)
                    adapterPractice.items = items
                else
                    adapterPractice.items.addAll(items)
                rcvPracticeFilterContent.getRcv().requestLayout()
            })
        }
    }

    private fun enableRefresh() = rcvPracticeFilterContent.enableRefresh()
    private fun cancelRefresh() = rcvPracticeFilterContent.cancelRefresh()

    private fun configView() {
        rcvPracticeFilterContent.getRcv().run {
            adapter = adapterPractice
            layoutManager = GridLayoutManager(requireContext(), 3)
            addItemDecoration(
                MarginItemDecoration(
                    resources.getDimension(R.dimen.space_4).toInt(),
                    isGridLayout = true,
                    spanCount = 3
                )
            )
        }
    }

    private fun handleClick() {
        adapterPractice.clickItem = PracticeMoreAdapter.OnClickPracticeFilter { item, type ->
            hideKeyboard()
            (parentFragment as PracticeFilterFragment).clearFocus()
            when (type) {
                PracticeMoreAdapter.TYPE_SINGLE -> {
                    PracticeDetailFragment.openFragment(item.id)
                }
                PracticeMoreAdapter.TYPE_MULTI -> {
                    PracticeFolderFragment.openFragment(item.id)
                }
            }
        }

        rcvPracticeFilterContent.onLoadMoreListener = CustomSwipeRcv.OnLoadMoreListener {
            isReload = false
            page.run {
                if (currPage < totalPage && !isLoading) {
                    isLoading = true
                    currPage++
                    onFilterContentCallback?.setOnLoadMoreListener(currPage)
                }
            }
        }

        rcvPracticeFilterContent.onCustomSwipeListener = CustomSwipeRcv.OnCustomSwipeListener {
            isReload = true
            onFilterContentCallback?.refresh()
        }
    }

    interface OnFilterContentCallback {
        fun setOnLoadMoreListener(currPage: Int)
        fun refresh()
    }
}