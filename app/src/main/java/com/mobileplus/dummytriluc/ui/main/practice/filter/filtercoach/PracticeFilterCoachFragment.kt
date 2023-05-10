package com.mobileplus.dummytriluc.ui.main.practice.filter.filtercoach

import android.os.Bundle
import com.core.BaseFragmentZ
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.Page
import com.mobileplus.dummytriluc.databinding.FragmentPracticeFilterCoachBinding
import com.mobileplus.dummytriluc.ui.main.practice.adapter.PracticeMasterMoreAdapter
import com.mobileplus.dummytriluc.ui.main.practice.filter.PracticeFilterFragment
import com.mobileplus.dummytriluc.ui.main.practice.filter.PracticeFilterViewModel
import com.mobileplus.dummytriluc.ui.main.user.UserInfoFragment
import com.mobileplus.dummytriluc.ui.utils.MarginItemDecoration
import com.mobileplus.dummytriluc.ui.utils.extensions.OnClickItemAdapter
import com.mobileplus.dummytriluc.ui.widget.CustomSwipeRcv
import com.utils.ext.setVisibility
import org.koin.android.viewmodel.ext.android.getViewModel

/**
 * Created by KOHuyn on 3/11/2021
 */
class PracticeFilterCoachFragment : BaseFragmentZ<FragmentPracticeFilterCoachBinding>() {
    override fun getLayoutBinding(): FragmentPracticeFilterCoachBinding =
        FragmentPracticeFilterCoachBinding.inflate(layoutInflater)

    private val practiceViewModel by lazy { requireParentFragment().getViewModel<PracticeFilterViewModel>() }
    private val adapterMaster by lazy { PracticeMasterMoreAdapter() }
    private val page by lazy { Page() }
    private var isReload = true

    var onFilterMasterCallback: OnFilterMasterCallback? = null

    override fun updateUI(savedInstanceState: Bundle?) {
        disposableViewModel()
        configView()
        handleClick()
    }

    private fun disposableViewModel() {
        practiceViewModel.run {
            addDispose(rxLoadingMaster.subscribe {
                if (page.isLoading) {
                    binding.loadMorePracticeFilterCoach.root.setVisibility(it)
                } else {
                    if (it) enableRefresh() else cancelRefresh()
                }
            })
            addDispose(rxItemMaster.subscribe {
                val items = it.first
                val pageResponse = it.second
                pageResponse?.run {
                    page.currPage = currPage
                    page.totalPage = totalPage
                    page.isLoading = false
                }
                binding.noDataPracticeFilterCoach.root.setVisibility(items.isEmpty())
                if (items.isEmpty()) {
                    adapterMaster.items.clear()
                    adapterMaster.notifyDataSetChanged()
                }
                if (adapterMaster.items.isEmpty() || isReload || page.currPage == 1)
                    adapterMaster.items = items
                else
                    adapterMaster.items.addAll(items)
                binding.rcvPracticeFilterCoach.getRcv().requestLayout()
            })
        }
    }

    private fun enableRefresh() = binding.rcvPracticeFilterCoach.enableRefresh()
    private fun cancelRefresh() = binding.rcvPracticeFilterCoach.cancelRefresh()

    private fun configView() {
       binding.rcvPracticeFilterCoach.setUpRcv(adapterMaster)
       binding.rcvPracticeFilterCoach.getRcv().run {
            addItemDecoration(
                MarginItemDecoration(
                    resources.getDimension(R.dimen.space_8).toInt()
                )
            )
        }
    }

    private fun handleClick() {
        adapterMaster.onItemClick = OnClickItemAdapter { _, position ->
            hideKeyboard()
            (parentFragment as PracticeFilterFragment).clearFocus()
            adapterMaster.items[position].id?.let { UserInfoFragment.openInfoGuest(it) }
        }

        binding. rcvPracticeFilterCoach.onLoadMoreListener = CustomSwipeRcv.OnLoadMoreListener {
            isReload = false
            page.run {
                if (currPage < totalPage && !isLoading) {
                    isLoading = true
                    currPage++
                    onFilterMasterCallback?.setOnLoadMoreListener(currPage)
                }
            }
        }

        binding. rcvPracticeFilterCoach.onCustomSwipeListener = CustomSwipeRcv.OnCustomSwipeListener {
            isReload = true
            onFilterMasterCallback?.refresh()
        }
    }


    interface OnFilterMasterCallback {
        fun setOnLoadMoreListener(currPage: Int)
        fun refresh()
    }
}