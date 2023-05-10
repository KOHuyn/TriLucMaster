package com.mobileplus.dummytriluc.ui.main.coach.assigned_exercise

import android.os.Bundle
import android.widget.TextView
import com.core.BaseFragment
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.Page
import com.mobileplus.dummytriluc.data.model.ItemCoachGroupLesson
import com.mobileplus.dummytriluc.ui.main.coach.group.detail.adapter.CoachGroupLessonAdapter
import com.mobileplus.dummytriluc.ui.main.practice.detail.PracticeDetailFragment
import com.mobileplus.dummytriluc.ui.main.practice.folder.PracticeFolderFragment
import com.mobileplus.dummytriluc.ui.utils.AppConstants
import com.mobileplus.dummytriluc.ui.utils.MarginItemDecoration
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventNextFragmentMain
import com.mobileplus.dummytriluc.ui.widget.CustomSwipeRcv
import com.utils.ext.clickWithDebounce
import com.utils.ext.postNormal
import com.utils.ext.setVisibility
import kotlinx.android.synthetic.main.fragment_assigned_exercise.*
import org.koin.android.viewmodel.ext.android.viewModel

/**
 * Created by KOHuyn on 3/16/2021
 */
class AssignedExerciseFragment : BaseFragment() {
    override fun getLayoutId(): Int = R.layout.fragment_assigned_exercise
    private val vm by viewModel<AssignedExerciseViewModel>()
    private val adapterAssign by lazy { CoachGroupLessonAdapter() }
    private val rcv by lazy { rcvAssigned }
    private val page by lazy { Page() }
    private var isReload = true
    private var lastPosition = AppConstants.INTEGER_DEFAULT

    companion object {
        fun openFragment() {
            postNormal(EventNextFragmentMain(AssignedExerciseFragment::class.java, true))
        }
    }

    override fun updateUI(savedInstanceState: Bundle?) {
        callbackViewModel()
        vm.getAssigned()
        btnBackAssigned.clickWithDebounce { onBackPressed() }
        configRcv()
    }

    private fun configRcv() {
        rcv.setUpRcv(adapterAssign)
        rcv.getRcv().addItemDecoration(
            MarginItemDecoration(
                resources.getDimension(R.dimen.space_8).toInt()
            )
        )
        rcv.onCustomSwipeListener = CustomSwipeRcv.OnCustomSwipeListener {
            isReload = true
            vm.getAssigned()
        }

        rcv.onLoadMoreListener = CustomSwipeRcv.OnLoadMoreListener {
            isReload = false
            page.run {
                if (currPage < totalPage && !isLoading) {
                    isLoading = true
                    currPage++
                    vm.getAssigned(currPage)
                }
            }
        }
        adapterAssign.clickListener = object : CoachGroupLessonAdapter.OnClickLessonListener {
            override fun setOnClickLessonListener(position: Int, item: ItemCoachGroupLesson) {
                when (item.type) {
                    ItemCoachGroupLesson.TYPE_PRACTICE -> {
                        item.practiceId?.let { PracticeDetailFragment.openFragment(it) }
                    }
                    ItemCoachGroupLesson.TYPE_FOLDER -> {
                        item.practiceId?.let { PracticeFolderFragment.openFragment(it) }
                    }
                }
            }

            override fun setOnDeleteLessonListener(position: Int, item: ItemCoachGroupLesson) {
                if (item.assignId != null) {
                    vm.deleteAssign(item.assignId)
                    lastPosition = position
                }
            }
        }
    }

    private fun callbackViewModel() {
        vm.apply {
            addDispose(rxItems.subscribe {
                val items = it.first.toMutableList()
                val pageResponse = it.second
                pageResponse?.run {
                    page.currPage = currPage
                    page.totalPage = totalPage
                    page.isLoading = false
                }
                (noDataAssigned as? TextView)?.text = getString(R.string.you_have_not_been_assigned_any_assignments)
                noDataAssigned.setVisibility(items.isEmpty())
                if (adapterAssign.items.isEmpty() || isReload)
                    adapterAssign.items = items
                else
                    adapterAssign.items.addAll(items)
                rcv.getRcv().requestLayout()
            })
            addDispose(isLoading.subscribe {
                if (page.isLoading) {
                    loadMoreAssigned.setVisibility(it)
                } else {
                    if (it) enableRefresh() else cancelRefresh()
                }
            })
            addDispose(rxMessage.subscribe { toast(it) })
            addDispose(rxDeleteSuccess.subscribe { if (it) adapterAssign.delete(lastPosition) })
        }
    }

    private fun enableRefresh() = rcv.enableRefresh()
    private fun cancelRefresh() = rcv.cancelRefresh()
}