package com.mobileplus.dummytriluc.ui.main.coach.session.saved_list

import android.os.Bundle
import com.core.BaseFragmentZ
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.Page
import com.mobileplus.dummytriluc.databinding.FragmentCoachSessionSavedListBinding
import com.mobileplus.dummytriluc.ui.dialog.YesNoButtonDialog
import com.mobileplus.dummytriluc.ui.main.coach.session.saved_list.adapter.CoachSessionSavedListAdapter
import com.mobileplus.dummytriluc.ui.utils.AppConstants
import com.mobileplus.dummytriluc.ui.utils.MarginItemDecoration
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventNextFragmentMain
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventPostCoachSessionItemSavedList
import com.mobileplus.dummytriluc.ui.utils.extensions.OnClickItemAdapter
import com.mobileplus.dummytriluc.ui.widget.CustomSwipeRcv
import com.utils.ext.argument
import com.utils.ext.clickWithDebounce
import com.utils.ext.postNormal
import com.utils.ext.setVisibility
import org.koin.android.viewmodel.ext.android.viewModel

/**
 * Created by KOHuyn on 4/27/2021
 */
class CoachSessionSavedListFragment : BaseFragmentZ<FragmentCoachSessionSavedListBinding>() {
    override fun getLayoutBinding(): FragmentCoachSessionSavedListBinding =
        FragmentCoachSessionSavedListBinding.inflate(layoutInflater)

    private val vm by viewModel<CoachSessionSavedListViewModel>()

    private val adapter by lazy { CoachSessionSavedListAdapter() }
    private val rcv by lazy { binding.rcvSavedListSessionCoach }
    private val page by lazy { Page() }
    private var isReload = true
    private var lastIndex: Int = AppConstants.INTEGER_DEFAULT

    private val isSelectFromSavedList: Boolean by argument(ARG_CHOICE_PRACTICE, false)

    companion object {
        private const val ARG_CHOICE_PRACTICE = "ARG_CHOICE_PRACTICE"

        fun openFragment() {
            val bundle = Bundle().apply {
                putBoolean(ARG_CHOICE_PRACTICE, false)
            }
            postNormal(
                EventNextFragmentMain(
                    CoachSessionSavedListFragment::class.java,
                    bundle,
                    true
                )
            )
        }

        fun openFragmentChoicePractice() {
            val bundle = Bundle().apply {
                putBoolean(ARG_CHOICE_PRACTICE, true)
            }
            postNormal(
                EventNextFragmentMain(
                    CoachSessionSavedListFragment::class.java,
                    bundle,
                    true
                )
            )
        }
    }

    override fun updateUI(savedInstanceState: Bundle?) {
        disposableViewModel()
        vm.getListSaved()
        configRcv()
        binding.btnBackCoachSessionSaved.clickWithDebounce { onBackPressed() }
    }

    private fun configRcv() {
        adapter.isDelete = !isSelectFromSavedList
        adapter.isClickableItem = isSelectFromSavedList
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
            vm.getListSaved()
        }
        rcv.onLoadMoreListener = CustomSwipeRcv.OnLoadMoreListener {
            isReload = false
            page.run {
                if (currPage < totalPage && !isLoading) {
                    isLoading = true
                    currPage++
                    vm.getListSaved(currPage)
                }
            }
        }
        adapter.onDeleteListener = CoachSessionSavedListAdapter.OnDeleteItemListener { pos ->
            lastIndex = pos
            val item = adapter.items[pos]
            YesNoButtonDialog()
                .setTitle(getString(R.string.delete_list))
                .setMessage(String.format(getString(R.string.do_you_want_delete), item.title))
                .setTextAccept(getString(R.string.label_delete))
                .setTextCancel(getString(R.string.cancel))
                .setOnCallbackAcceptButtonListener {
                    item.id?.let { id -> vm.deleteAtId(id) }
                }
                .showDialog(parentFragmentManager, YesNoButtonDialog.TAG)
        }

        adapter.onClickItem = OnClickItemAdapter { _, pos ->
            adapter.items[pos].id?.let { vm.getDetailListSaved(it) }
        }
    }

    fun disposableViewModel() {
        addDispose(vm.rxItemsSaved.subscribe {
            val items = it.first
            val pageResponse = it.second
            pageResponse?.run {
                page.currPage = currPage
                page.totalPage = totalPage
                page.isLoading = false
            }
            binding.noDataCoachSessionSaved.root.setVisibility(items.isEmpty())
            if (adapter.items.isEmpty() || isReload)
                adapter.items = items
            else
                adapter.items.addAll(items)
        })
        addDispose(vm.isLoading.subscribe {
            if (page.isLoading) {
                binding.loadMoreSavedListSessionCoach.root.setVisibility(it)
            } else {
                if (it) enableRefresh() else cancelRefresh()
            }
            rcv.getRcv().requestLayout()
        })
        addDispose(vm.rxMessage.subscribe { toast(it) })
        addDispose(vm.rxDeleteSuccess.subscribe { if (it) adapter.delete(lastIndex) })
        addDispose(vm.rxItemPracticeSaved.subscribe {
            postNormal(
                EventPostCoachSessionItemSavedList(
                    it
                )
            )
            onBackPressed()
        })
    }

    private fun enableRefresh() = rcv.enableRefresh()
    private fun cancelRefresh() = rcv.cancelRefresh()
}