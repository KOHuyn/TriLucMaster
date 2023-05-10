package com.mobileplus.dummytriluc.ui.main.coach.group.detail.member

import android.graphics.Color
import android.os.Bundle
import com.core.BaseFragment
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.ItemDisciple
import com.mobileplus.dummytriluc.data.model.ItemDiscipleGroup
import com.mobileplus.dummytriluc.data.model.Page
import com.mobileplus.dummytriluc.ui.main.coach.assign_exercise.CoachAssignExerciseFragment
import com.mobileplus.dummytriluc.ui.main.coach.group.detail.adapter.CoachGroupMemberAdapter
import com.mobileplus.dummytriluc.ui.main.coach.dialog.AddToGroupDialog
import com.mobileplus.dummytriluc.ui.main.coach.dialog.AddToGroupDialog.Companion.TYPE_MOVE
import com.mobileplus.dummytriluc.ui.main.coach.dialog.DeleteDiscipleDialog
import com.mobileplus.dummytriluc.ui.main.coach.group.detail.CoachGroupDetailFragment
import com.mobileplus.dummytriluc.ui.utils.AppConstants
import com.mobileplus.dummytriluc.ui.utils.MarginItemDecoration
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes
import com.mobileplus.dummytriluc.ui.widget.CustomSpinner
import com.mobileplus.dummytriluc.ui.widget.CustomSwipeRcv
import com.utils.ext.clickWithDebounce
import com.utils.ext.setVisibility
import kotlinx.android.synthetic.main.fragment_coach_group_detail_member.*

import org.koin.android.viewmodel.ext.android.viewModel

class CoachGroupDetailMemberFragment : BaseFragment() {
    override fun getLayoutId(): Int = R.layout.fragment_coach_group_detail_member

    private val memberViewModel by viewModel<CoachGroupDetailMemberViewModel>()
    private val memberAdapter by lazy { CoachGroupMemberAdapter() }
    private val rcv by lazy { rcvGroupMember }
    private val page by lazy { Page() }
    private var isReload: Boolean = true
    val groupInfo: ItemDiscipleGroup? by lazy { (requireParentFragment() as CoachGroupDetailFragment).groupInfo }
    private val isGuest: Boolean by lazy { (requireParentFragment() as CoachGroupDetailFragment).isGuest }
    private var lastPosition: Int = AppConstants.INTEGER_DEFAULT
    private var dayRange: Int = 1

    override fun updateUI(savedInstanceState: Bundle?) {
        callbackViewModel(memberViewModel)
        groupInfo?.id?.let { memberViewModel.getAllMemberInGroup(it, dayRange = dayRange) }
        configView()
        handleClick()
    }

    private fun configView() {
        rcv.setUpRcv(memberAdapter)
        rcv.getRcv().addItemDecoration(
            MarginItemDecoration(
                resources.getDimension(R.dimen.space_8).toInt()
            )
        )
        memberAdapter.isEnableMenu = !isGuest
    }

    private fun callbackViewModel(vm: CoachGroupDetailMemberViewModel) {
        addDispose(
            vm.rxMessage.subscribe {
                toast(it)
            },
            vm.isLoading.subscribe {
                if (page.isLoading) {
                    loadMoreGroupMember.setVisibility(it)
                } else {
                    if (it) rcv.enableRefresh() else rcv.cancelRefresh()
                }
            },
            vm.rxMembers.subscribe {
                val items = it.first.toMutableList()
                val pageResponse = it.second
                pageResponse?.run {
                    page.totalPage = totalPage
                    page.currPage = currPage
                    page.isLoading = false
                }
                noDataGroupMember.setVisibility(items.isEmpty())
                if (memberAdapter.items.isEmpty() || isReload) {
                    memberAdapter.items = items
                } else {
                    memberAdapter.items.addAll(items)
                }
            }, vm.rxDeleteMemberSuccess.subscribe {
                if (it) memberAdapter.delete(lastPosition)
            }
        )
    }

    private fun handleClick() {
        spStatisticalCoachGroupMember.clickWithDebounce {
            CustomSpinner(it, requireContext())
                .setBackGroundSpinner(Color.WHITE)
                .setTextColor(R.color.clr_tab)
                .setDataSource(
                    mutableListOf(
                        CustomSpinner.SpinnerItem(getString(R.string.statistics_day), 1.toString()),
                        CustomSpinner.SpinnerItem(getString(R.string.statistics_week), 7.toString()),
                        CustomSpinner.SpinnerItem(getString(R.string.statistics_month), 30.toString())
                    )
                ).build()
                .setOnSelectedItemCallback { item ->
                    if (dayRange != item.id.toInt()) {
                        dayRange = item.id.toInt()
                        groupInfo?.id?.let { id ->
                            memberViewModel.getAllMemberInGroup(
                                id,
                                dayRange = dayRange
                            )
                        }
                    }
                }
        }

        rcv.onCustomSwipeListener = CustomSwipeRcv.OnCustomSwipeListener {
            isReload = true
            groupInfo?.id?.let { memberViewModel.getAllMemberInGroup(it, dayRange = dayRange) }
        }
        rcv.onLoadMoreListener = CustomSwipeRcv.OnLoadMoreListener {
            isReload = false
            page.run {
                if (currPage < totalPage && !isLoading) {
                    isLoading = true
                    currPage++
                    groupInfo?.id?.let {
                        memberViewModel.getAllMemberInGroup(
                            it,
                            dayRange = dayRange,
                            page = currPage
                        )
                    }
                }
            }
        }
        memberAdapter.listener =
            object : CoachGroupMemberAdapter.DetailGroupListener {
                override fun onMoveClick(position: Int, item: ItemDisciple) {
                    val dialog =
                        AddToGroupDialog(TYPE_MOVE)
                    dialog.listener =
                        AddToGroupDialog.AddToGroupDialogListener { arrClass ->
                            item.studentId?.let { memberViewModel.moveMemberToGroups(arrClass, it) }
                        }
                    dialog.show(parentFragmentManager, dialog.tag)
                }

                override fun onAssignExercise(position: Int, item: ItemDisciple) {
                    item.studentId?.let { CoachAssignExerciseFragment.openFragmentStudent(it) }
                }

                override fun onRemoveClick(position: Int, item: ItemDisciple) {
                    lastPosition = position
                    val dialog =
                        DeleteDiscipleDialog()
                    dialog.listener = DeleteDiscipleDialog.OnDeleteListener {
                        if (groupInfo?.id != null && item.studentId != null) {
                            memberViewModel.deleteMemberInGroup(groupInfo?.id!!, item.studentId)
                        } else {
                            toast(loadStringRes(R.string.error_unknown_error))
                        }
                    }
                    dialog.setUp(
                        item.fullName,
                        loadStringRes(R.string.format_delete_disciple_from_group),
                        requireContext(),
                        loadStringRes(R.string.title_delete_from_group)
                    )
                    dialog.show(
                        requireActivity().supportFragmentManager,
                        DeleteDiscipleDialog::class.java.canonicalName
                    )
                }
            }
    }
}
