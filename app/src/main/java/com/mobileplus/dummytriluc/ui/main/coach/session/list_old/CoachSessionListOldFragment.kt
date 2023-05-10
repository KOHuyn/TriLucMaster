package com.mobileplus.dummytriluc.ui.main.coach.session.list_old

import android.os.Bundle
import com.core.BaseFragmentZ
import com.core.OnItemClick
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.Page
import com.mobileplus.dummytriluc.databinding.FragmentCoachSessionListOldBinding
import com.mobileplus.dummytriluc.ui.dialog.YesNoButtonDialog
import com.mobileplus.dummytriluc.ui.main.coach.session.CoachSessionFragment
import com.mobileplus.dummytriluc.ui.main.coach.session.list_old.adapter.CoachSessionListOldAdapter
import com.mobileplus.dummytriluc.ui.utils.AppConstants
import com.mobileplus.dummytriluc.ui.utils.MarginItemDecoration
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventNextFragmentMain
import com.mobileplus.dummytriluc.ui.widget.CustomSwipeRcv
import com.utils.ext.clickWithDebounce
import com.utils.ext.postNormal
import com.utils.ext.setVisibility
import org.koin.android.viewmodel.ext.android.viewModel

class CoachSessionListOldFragment : BaseFragmentZ<FragmentCoachSessionListOldBinding>() {
    override fun getLayoutBinding(): FragmentCoachSessionListOldBinding =
        FragmentCoachSessionListOldBinding.inflate(layoutInflater)

    private val vm by viewModel<CoachSessionListOldViewModel>()

    private val adapter by lazy { CoachSessionListOldAdapter() }
    private val rcv by lazy { binding.rcvOldListSessionCoach }
    private val page by lazy { Page() }
    private var isReload = true
    private var lastIndex: Int = AppConstants.INTEGER_DEFAULT

    companion object {
        fun openFragment() {
            postNormal(EventNextFragmentMain(CoachSessionListOldFragment::class.java, true))
        }
    }

    override fun updateUI(savedInstanceState: Bundle?) {
        disposableViewModel()
        vm.getListOld()
        configRcv()
        binding.btnBackCoachSessionOld.clickWithDebounce { onBackPressed() }
    }

    private fun configRcv() {
        rcv.setUpRcv(adapter)
        rcv.getRcv().addItemDecoration(
            MarginItemDecoration(
                resources.getDimensionPixelOffset(
                    R.dimen.space_8
                )
            )
        )
        rcv.onCustomSwipeListener = CustomSwipeRcv.OnCustomSwipeListener {
            isReload = true
            vm.getListOld()
        }
        rcv.onLoadMoreListener = CustomSwipeRcv.OnLoadMoreListener {
            isReload = false
            page.run {
                if (currPage < totalPage && !isLoading) {
                    isLoading = true
                    currPage++
                    vm.getListOld(currPage)
                }
            }
        }
        adapter.onDeleteListener = CoachSessionListOldAdapter.OnDeleteItemListener { pos ->
            lastIndex = pos
            val item = adapter.items[pos]
            YesNoButtonDialog()
                .setTitle(getString(R.string.delete_list))
                .setMessage(String.format(getString(R.string.do_you_want_delete), item.title))
                .setTextAccept(getString(R.string.label_delete))
                .setTextCancel(getString(R.string.cancel))
                .setOnCallbackAcceptButtonListener {
                    item.id?.let { id -> vm.deleteSessionAtId(id) }
                }
                .showDialog(parentFragmentManager)
        }

        adapter.onItemClick = OnItemClick { item, _ ->
            item.id?.let { CoachSessionFragment.openFragmentFromSessionOld(it) }
        }
    }

    fun disposableViewModel() {
        addDispose(vm.rxItemsOld.subscribe {
            val items = it.first
            val pageResponse = it.second
            pageResponse?.run {
                page.currPage = currPage
                page.totalPage = totalPage
                page.isLoading = false
            }
            binding.noDataCoachSessionOld.root.setVisibility(items.isEmpty())
            if (adapter.items.isEmpty() || isReload)
                adapter.items = items
            else
                adapter.items.addAll(items)
        })
        addDispose(vm.isLoading.subscribe {
            if (page.isLoading) {
                binding.loadMoreOldListSessionCoach.root.setVisibility(it)
            } else {
                if (it) enableRefresh() else cancelRefresh()
            }
            rcv.getRcv().requestLayout()
        })
        addDispose(vm.rxMessage.subscribe { toast(it) })
        addDispose(vm.rxDeleteSuccess.subscribe { if (it) adapter.delete(lastIndex) })

    }

    private fun enableRefresh() = rcv.enableRefresh()
    private fun cancelRefresh() = rcv.cancelRefresh()

}