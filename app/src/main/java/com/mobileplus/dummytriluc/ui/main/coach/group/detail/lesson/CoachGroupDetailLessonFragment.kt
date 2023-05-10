package com.mobileplus.dummytriluc.ui.main.coach.group.detail.lesson

import android.os.Bundle
import com.core.BaseFragment
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.BaseItemCoachGroup
import com.mobileplus.dummytriluc.data.model.Page
import com.mobileplus.dummytriluc.data.model.ItemCoachGroupLesson
import com.mobileplus.dummytriluc.ui.main.coach.group.detail.CoachGroupDetailFragment
import com.mobileplus.dummytriluc.ui.main.coach.group.detail.adapter.CoachGroupLessonAdapter
import com.mobileplus.dummytriluc.ui.main.coach.session.CoachSessionFragment
import com.mobileplus.dummytriluc.ui.main.practice.detail.PracticeDetailFragment
import com.mobileplus.dummytriluc.ui.main.practice.folder.PracticeFolderFragment
import com.mobileplus.dummytriluc.ui.utils.AppConstants
import com.mobileplus.dummytriluc.ui.utils.MarginItemDecoration
import com.mobileplus.dummytriluc.ui.widget.CustomSwipeRcv
import com.utils.ext.setVisibility
import kotlinx.android.synthetic.main.fragment_coach_group_detail_lesson.*
import org.koin.android.viewmodel.ext.android.viewModel

/**
 * Created by KOHuyn on 3/12/2021
 */
class CoachGroupDetailLessonFragment : BaseFragment() {
    override fun getLayoutId(): Int = R.layout.fragment_coach_group_detail_lesson
    private val vm by viewModel<CoachGroupDetailLessonViewModel>()
    private val adapterLesson by lazy { CoachGroupLessonAdapter() }
    private val rcv by lazy { rcvGroupLesson }
    private val page by lazy { Page() }
    private var isReload = true
    var classId: Int? = null
    private var initialized = false
    private var lastPosition = AppConstants.INTEGER_DEFAULT
    private val isGuest: Boolean by lazy { (requireParentFragment() as CoachGroupDetailFragment).isGuest }

    fun initView() {
        if (!initialized) {
            classId?.let { vm.getAllListAssign(it) }
            initialized = true
        }
    }

    override fun updateUI(savedInstanceState: Bundle?) {
        callbackViewModel()
        configRcv()
    }

    private fun configRcv() {
        rcv.setUpRcv(adapterLesson)
        rcv.getRcv().addItemDecoration(
            MarginItemDecoration(
                resources.getDimension(R.dimen.space_8).toInt()
            )
        )
        rcv.onCustomSwipeListener = CustomSwipeRcv.OnCustomSwipeListener {
            isReload = true
            classId?.let { vm.getAllListAssign(it) }
        }

        rcv.onLoadMoreListener = CustomSwipeRcv.OnLoadMoreListener {
            isReload = false
            page.run {
                if (currPage < totalPage && !isLoading) {
                    isLoading = true
                    currPage++
                    classId?.let { vm.getAllListAssign(it, page = currPage) }
                }
            }
        }
//        adapterLesson.isDelete = false
        adapterLesson.clickListener = object : CoachGroupLessonAdapter.OnClickLessonListener {
            override fun setOnClickLessonListener(position: Int, item: ItemCoachGroupLesson) {
                when (item.getType()) {
                    BaseItemCoachGroup.TYPE_PRACTICE -> {
                        item.id?.let { PracticeDetailFragment.openFragment(it) }
                    }
                    BaseItemCoachGroup.TYPE_FOLDER -> {
                        item.id?.let { PracticeFolderFragment.openFragment(it) }
                    }
                    BaseItemCoachGroup.TYPE_SESSION -> {
                        item.id?.let { CoachSessionFragment.openFragmentGuest(it) }
                    }
                }
            }

            override fun setOnDeleteLessonListener(position: Int, item: ItemCoachGroupLesson) {
                if (item.assignId != null) {
                    vm.deleteAssign(item.assignId, classId)
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
                noDataGroupLesson.setVisibility(items.isEmpty())
                if (adapterLesson.items.isEmpty() || isReload)
                    adapterLesson.items = items
                else
                    adapterLesson.items.addAll(items)
                rcv.getRcv().requestLayout()
            })
            addDispose(isLoading.subscribe {
                if (page.isLoading) {
                    loadMoreGroupLesson.setVisibility(it)
                } else {
                    if (it) enableRefresh() else cancelRefresh()
                }
            })
            addDispose(rxMessage.subscribe { toast(it) })
            addDispose(rxDeleteSuccess.subscribe { if (it) adapterLesson.delete(lastPosition) })
        }
    }

    private fun enableRefresh() = rcv.enableRefresh()
    private fun cancelRefresh() = rcv.cancelRefresh()
}