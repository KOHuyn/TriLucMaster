package com.mobileplus.dummytriluc.ui.main.coach.group

import android.os.Bundle
import com.core.BaseFragment
import com.google.gson.Gson
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.ItemDiscipleGroup
import com.mobileplus.dummytriluc.data.model.Page
import com.mobileplus.dummytriluc.ui.main.coach.dialog.DeleteDiscipleDialog
import com.mobileplus.dummytriluc.ui.dialog.RenameDialog
import com.mobileplus.dummytriluc.ui.main.coach.adapter.DiscipleGroupAdapter
import com.mobileplus.dummytriluc.ui.main.coach.assign_exercise.CoachAssignExerciseFragment
import com.mobileplus.dummytriluc.ui.main.coach.dialog.AddDiscipleDialog
import com.mobileplus.dummytriluc.ui.main.coach.group.detail.CoachGroupDetailFragment
import com.mobileplus.dummytriluc.ui.utils.AppConstants
import com.mobileplus.dummytriluc.ui.utils.MarginItemDecoration
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventNextFragmentMain
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes
import com.mobileplus.dummytriluc.ui.utils.extensions.logErr
import com.mobileplus.dummytriluc.ui.widget.CustomSwipeRcv
import com.utils.ext.argument
import com.utils.ext.clickWithDebounce
import com.utils.ext.postNormal
import com.utils.ext.setVisibility
import kotlinx.android.synthetic.main.fragment_coach_group.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

class CoachGroupFragment : BaseFragment() {
    private val groupViewModel by viewModel<CoachGroupViewModel>()
    private val adapterDataDisciple by lazy { DiscipleGroupAdapter() }
    private val rcv by lazy { rcvGroupDisciple }
    private val page by lazy { Page() }
    private val gson by inject<Gson>()
    private var lastPosition: Int = AppConstants.INTEGER_DEFAULT
    private val isGuest by argument(IS_GUEST_GROUP_ARG, true)
    override fun getLayoutId(): Int = R.layout.fragment_coach_group

    companion object {
        const val IS_GUEST_GROUP_ARG = "IS_GUEST_GROUP"
        fun openFragment(isGuest: Boolean) {
            val bundle = Bundle().apply {
                putBoolean(IS_GUEST_GROUP_ARG, isGuest)
            }
            postNormal(EventNextFragmentMain(CoachGroupFragment::class.java, bundle, true))
        }
    }

    override fun updateUI(savedInstanceState: Bundle?) {
        configView()
        initAction()
        groupViewModel.getGroupList(isGuest = isGuest)
        disposeViewModel()
    }

    private fun disposeViewModel() {
        addDispose(
            groupViewModel.rxMessage.subscribe {
                toast(it)
            },
            groupViewModel.isLoading.subscribe {
                if (page.isLoading) {
                    loadMoreDiscipleGroup.setVisibility(it)
                } else {
                    if (it) rcv.enableRefresh() else rcv.cancelRefresh()
                }
            },
            groupViewModel.rxGroups.subscribe {
                val items = it.first.toMutableList()
                val pageResponse = it.second
                pageResponse?.run {
                    page.totalPage = totalPage
                    page.currPage = currPage
                    page.isLoading = false
                }
                noDataDiscipleGroup.setVisibility(items.isEmpty())
                if (page.currPage == 1) {
                    adapterDataDisciple.items = items
                } else {
                    adapterDataDisciple.items.addAll(items)
                }
            },
            groupViewModel.createGroupSuccess.subscribe {
                if (it)
                    groupViewModel.getGroupList(isGuest = isGuest)
            },
            groupViewModel.deleteGroupSuccess.subscribe {
                if (it) adapterDataDisciple.delete(lastPosition)
            },
            groupViewModel.renameGroupSuccess.subscribe {
                val isSuccess = it.first
                val nameNew = it.second
                if (isSuccess) adapterDataDisciple.updateNewName(nameNew, lastPosition)
            }
        )
    }

    private fun configView() {
        rcv.setUpRcv(adapterDataDisciple)
        rcv.getRcv().addItemDecoration(
            MarginItemDecoration(
                resources.getDimension(R.dimen.space_8).toInt()
            )
        )
        adapterDataDisciple.isEnableMenu = !isGuest
        btnAddGroupDisciple.setVisibility(!isGuest)
        txtHeaderCoachGroup.text =
            if (isGuest) loadStringRes(R.string.my_group) else loadStringRes(R.string.label_disciple_group)
    }

    private fun initAction() {
        rcv.onCustomSwipeListener = CustomSwipeRcv.OnCustomSwipeListener {
            groupViewModel.getGroupList(isGuest)
        }

        rcv.onLoadMoreListener = CustomSwipeRcv.OnLoadMoreListener {
            page.run {
                if (currPage < totalPage && !isLoading) {
                    isLoading = true
                    currPage++
                    groupViewModel.getGroupList(isGuest, currPage)
                }
            }
        }
        btnBackDiscipleGroup.clickWithDebounce { onBackPressed() }
        btnAddGroupDisciple.clickWithDebounce {
            RenameDialog.builder()
                .setTitle(loadStringRes(R.string.label_create_group_name))
                .setDescription(loadStringRes(R.string.label_group_name))
                .build(parentFragmentManager)
                .setListenerCallback { newName ->
                    groupViewModel.createGroup(newName)
                }
        }
        adapterDataDisciple.listener = object : DiscipleGroupAdapter.GroupAdapterListener {
            override fun onDetailClick(position: Int, itemDiscipleGroup: ItemDiscipleGroup) {
                CoachGroupDetailFragment.openFragment(
                    isGuest = isGuest,
                    itemDiscipleGroup = itemDiscipleGroup,
                    gson = gson
                )
            }

            override fun onChangeNameClick(position: Int, itemDiscipleGroup: ItemDiscipleGroup) {
                lastPosition = position
                RenameDialog.builder()
                    .setTitle(loadStringRes(R.string.label_create_group_name))
                    .setDescription(loadStringRes(R.string.label_group_name))
                    .setRenameOld(itemDiscipleGroup.name ?: "")
                    .build(parentFragmentManager)
                    .setListenerCallback { newName ->
                        itemDiscipleGroup.id?.let { groupViewModel.renameGroup(it, newName) }
                    }
            }

            override fun onDeleteGroupClick(position: Int, itemDiscipleGroup: ItemDiscipleGroup) {
                lastPosition = position
                try {
                    val dialog =
                        DeleteDiscipleDialog()
                    dialog.listener = DeleteDiscipleDialog.OnDeleteListener {
                        groupViewModel.deleteGroup(itemDiscipleGroup.id)
                    }
                    dialog.setUp(
                        itemDiscipleGroup.name,
                        loadStringRes(R.string.format_delete_disciple_question),
                        requireContext(),
                        loadStringRes(R.string.title_delete_group)
                    )
                    dialog.show(parentFragmentManager, dialog.tag)
                } catch (e: Exception) {
                    e.logErr()
                }
            }

            override fun onAddMemberToGroup(position: Int, itemDiscipleGroup: ItemDiscipleGroup) {
                AddDiscipleDialog.Builder()
                    .build(parentFragmentManager)
                    .setIdsCallbackListener { ids ->
                        itemDiscipleGroup.id?.let { groupViewModel.addMemberIntoRoom(it, ids) }
                    }
            }

            override fun onAssignExercise(position: Int, itemDiscipleGroup: ItemDiscipleGroup) {
                itemDiscipleGroup.id?.let { CoachAssignExerciseFragment.openFragmentClass(it) }
            }
        }
    }
}